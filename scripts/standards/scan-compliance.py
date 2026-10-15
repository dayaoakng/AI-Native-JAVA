#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""DDD Lite 合规扫描：把「硬规则」变成可执行的机器检查。

纯标准库实现（Python 3.6+），无第三方依赖。CI 中作为硬门禁使用。

规则分三级：
  fail —— 违反即拒绝（退出码 1）
  warn —— 提示，不阻断
  info —— 统计报告，不判定

用法：
  python scripts/standards/scan-compliance.py --root scaffold [--json]
"""

import argparse
import json
import os
import re
import sys

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

# ---------------------------------------------------------------- 规则常量

MAX_FILE_LINES = 300
MAX_HANDLER_LINES = 100
MAX_METHOD_LINES = 30

DOMAIN_SUBPACKAGES = {"entity", "valueobject", "repository", "event", "port"}

# 方法签名近似匹配（以修饰符开头、含参数表、行尾非分号）
_SIG_RE = re.compile(
    r"^\s*(?:public|private|protected|static|final|synchronized|abstract|default|native)\b"
    r".*\w+\s*\([^;]*\)\s*(?:\{|$)\s*$"
)
_CONTROL_RE = re.compile(r"^\s*(if|for|while|switch|catch|else|try|finally|do)\b")
_IMPORT_SERVICE_RE = re.compile(r"^import\s+[\w.]*\.service\.")


def read_java_files(root):
    """遍历 root 下所有 main/java 的 .java 文件，返回 [(abspath, relpath)]。"""
    found = []
    for dirpath, _dirnames, filenames in os.walk(root):
        for fn in filenames:
            if fn.endswith(".java"):
                abs_path = os.path.join(dirpath, fn)
                rel = os.path.relpath(abs_path, root).replace(os.sep, "/")
                if "/src/main/java/" in "/" + rel:
                    found.append((abs_path, rel))
    return sorted(found)


def package_path(rel):
    """从 rel 中截取 src/main/java 之后的包路径（不含文件名）。"""
    m = re.search(r"src/main/java/(.+)/[^/]+\.java$", rel)
    return m.group(1) if m else ""


def has_bom(path):
    with open(path, "rb") as f:
        return f.read(3) == b"\xef\xbb\xbf"


def iter_method_blocks(lines):
    """近似定位方法体，yield (start, end)（0-based 闭区间）。

    仅做启发式括号匹配，不构建 AST：对模板规模足够，宁漏报不误报。
    """
    n = len(lines)
    i = 0
    while i < n:
        s = lines[i].strip()
        if not s or s.startswith(("//", "/*", "*", "@")):
            i += 1
            continue
        if _CONTROL_RE.match(s):
            i += 1
            continue
        if "=" in s and "(" not in s.split("=", 1)[0]:
            i += 1
            continue
        if not _SIG_RE.match(s):
            i += 1
            continue
        # 定位方法体的开括号 {（本行或后续行）
        brace = s.count("{") - s.count("}")
        j = i
        while brace <= 0 and j + 1 < n:
            j += 1
            brace += lines[j].count("{") - lines[j].count("}")
        if brace <= 0:
            i += 1
            continue
        start = j
        while j < n and brace > 0:
            j += 1
            brace += lines[j].count("{") - lines[j].count("}")
        yield (start, j - 1)
        i = j


def scan(root):
    results = {"fail": [], "warn": [], "info": []}
    files = read_java_files(root)
    handler_count = 0

    for abs_path, rel in files:
        pkg = package_path(rel)
        basename = os.path.basename(rel)
        with open(abs_path, "r", encoding="utf-8") as f:
            content = f.read()
        lines = content.splitlines()

        # 规则 5：UTF-8 无 BOM
        if has_bom(abs_path):
            results["fail"].append(f"{rel}: 文件含 UTF-8 BOM")

        # 规则 1：禁止 service/ 包、XxxService 命名
        if "/service/" in "/" + pkg:
            results["fail"].append(f"{rel}: 存在 service/ 包（禁止）")
        if re.match(r".*(Service|ApplicationService)\.java$", basename):
            results["fail"].append(f"{rel}: 禁止 XxxService / XxxApplicationService 命名")

        # 规则 3：行数上限
        if len(lines) > MAX_FILE_LINES:
            results["fail"].append(f"{rel}: {len(lines)} 行 > {MAX_FILE_LINES} 行")
        # 用例 Handler：仅在 application/handler/ 包下计数与限行
        if basename.endswith("Handler.java") and "/application/handler/" in "/" + pkg + "/":
            handler_count += 1
            if len(lines) > MAX_HANDLER_LINES:
                results["fail"].append(f"{rel}: Handler {len(lines)} 行 > {MAX_HANDLER_LINES} 行")

        # 规则 3：方法长度上限
        for start, end in iter_method_blocks(lines):
            method_lines = end - start + 1
            if method_lines > MAX_METHOD_LINES:
                sig = lines[start].strip()[:60]
                results["fail"].append(
                    f"{rel}:{start + 1}: 方法 {method_lines} 行 > {MAX_METHOD_LINES} 行（{sig}…）"
                )

        # 规则 4：domain 层子包白名单
        dm = re.search(r"domain/([^/]+)/", "/" + pkg + "/")
        if dm:
            sub = dm.group(1)
            if sub not in DOMAIN_SUBPACKAGES:
                results["fail"].append(
                    f"{rel}: domain 下禁止子包 '{sub}'（仅允许 {sorted(DOMAIN_SUBPACKAGES)}）"
                )

        # 规则 4：实体必须位于 domain/entity/（shared 基础包除外）
        if basename.endswith("Entity.java"):
            segs = pkg.split("/")
            if "entity" in segs:
                idx = segs.index("entity")
                if idx > 0 and segs[idx - 1] != "domain" and "shared" not in segs[:idx]:
                    results["fail"].append(f"{rel}: 实体必须位于 domain/entity/ 下")

        # 规则（warn）：Handler import service 包类 = 假 Handler 信号
        if basename.endswith("Handler.java") and "/application/handler/" in "/" + pkg + "/":
            for ln in lines:
                if _IMPORT_SERVICE_RE.match(ln.strip()):
                    results["warn"].append(
                        f"{rel}: Handler import 了 service 包类（假 Handler 信号）: {ln.strip()}"
                    )

    results["info"].append(f"扫描 Java 文件: {len(files)} 个")
    results["info"].append(f"Handler 数量: {handler_count} 个")
    return results


def main(argv=None):
    ap = argparse.ArgumentParser(description="DDD Lite 合规扫描")
    ap.add_argument("--root", default="scaffold", help="扫描根目录（默认 scaffold）")
    ap.add_argument("--json", action="store_true", help="以 JSON 输出")
    args = ap.parse_args(argv)

    root = os.path.abspath(args.root)
    if not os.path.isdir(root):
        print(f"目录不存在: {root}", file=sys.stderr)
        return 2

    r = scan(root)
    n_fail = len(r["fail"])

    if args.json:
        print(json.dumps(r, ensure_ascii=False, indent=2))
    else:
        for line in r["info"]:
            print(f"[info] {line}")
        for line in r["warn"]:
            print(f"[warn] {line}")
        for line in r["fail"]:
            print(f"[FAIL] {line}")
        print()
        print("合规：未发现 fail 级违规。" if n_fail == 0 else f"不合规：{n_fail} 条 fail 级违规。")

    return 1 if n_fail else 0


if __name__ == "__main__":
    sys.exit(main())
