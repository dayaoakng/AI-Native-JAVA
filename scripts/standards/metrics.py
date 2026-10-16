#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""DDD Lite 指标统计：把 README「三个关键数字」变成可复算的机器口径。

口径说明（与文档一致，均为静态启发式扫描）：
- handler_total   ：application/handler/ 下 *Handler.java 数量
- handler_spec    ：test 下 *HandlerSpec.{groovy,java} 数量
- fake_handler    ：Handler 类 import 了 *service* 包类（转调 Service 的信号）
- service_count   ：*Service.java / *ApplicationService.java / *DomainService.java 数量
- aggregate_count ：*Aggregate.java 数量
- domain_service_pkg：是否存在 domain/service/ 目录
- controller_direct_rate：Controller 类中引用 Handler 类型的占比（直调 Handler 口径）

纯标准库实现，Python 3.6+。CI 中与 scan-compliance 并列使用。

用法：
  python scripts/standards/metrics.py --root scaffold [--json]
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

_SERVICE_FILE_RE = re.compile(r".*(Service|ApplicationService|DomainService)\.java$")
_AGGREGATE_FILE_RE = re.compile(r".*Aggregate\.java$")
_HANDLER_IMPORT_RE = re.compile(r"^import\s+[\w.]*\.service\.")
_HANDLER_FIELD_RE = re.compile(r"\b\w*Handler\b")
_HANDLER_IMPORT_TYPE_RE = re.compile(r"import\s+[\w.]*\.application\.handler\.\w+Handler;")


def walk_files(root, sub="main/java"):
    """遍历 root 下指定子路径的源码文件，返回 [(abspath, relpath)]。"""
    found = []
    for dirpath, _dirnames, filenames in os.walk(root):
        for fn in filenames:
            if fn.endswith((".java", ".groovy")):
                abs_path = os.path.join(dirpath, fn)
                rel = os.path.relpath(abs_path, root).replace(os.sep, "/")
                if ("/" + sub + "/") in ("/" + rel + "/"):
                    found.append((abs_path, rel))
    return sorted(found)


def pkg_path(rel, sub="main/java"):
    m = re.search(sub + r"/(.+)/[^/]+\.(java|groovy)$", rel)
    return m.group(1) if m else ""


def metrics(root):
    m = {
        "handler_total": 0,
        "handler_spec": 0,
        "fake_handler": 0,
        "service_count": 0,
        "aggregate_count": 0,
        "domain_service_pkg": False,
        "controller_total": 0,
        "controller_direct": 0,
        "controller_direct_rate": None,
    }

    main_files = walk_files(root, "main/java")
    test_files = walk_files(root, "test/groovy") + walk_files(root, "test/java")

    for abs_path, rel in main_files:
        pkg = pkg_path(rel)
        base = os.path.basename(rel)

        # Handler 总数
        if base.endswith("Handler.java") and "/application/handler/" in "/" + pkg + "/":
            m["handler_total"] += 1
            with open(abs_path, "r", encoding="utf-8") as f:
                content = f.read()
            # 假 Handler：import service 包类
            if any(_HANDLER_IMPORT_RE.match(ln.strip()) for ln in content.splitlines()):
                m["fake_handler"] += 1

        # 遗留 Service / ApplicationService / DomainService
        if _SERVICE_FILE_RE.match(base):
            m["service_count"] += 1

        # Aggregate
        if _AGGREGATE_FILE_RE.match(base):
            m["aggregate_count"] += 1

        # Controller 直调 Handler
        if base.endswith("Controller.java"):
            m["controller_total"] += 1
            with open(abs_path, "r", encoding="utf-8") as f:
                content = f.read()
            if _HANDLER_IMPORT_TYPE_RE.search(content) or _HANDLER_FIELD_RE.search(content):
                m["controller_direct"] += 1

    # domain/service 包
    for abs_path, rel in main_files:
        if "/domain/service/" in "/" + pkg_path(rel) + "/":
            m["domain_service_pkg"] = True
            break

    # HandlerSpec 数
    for abs_path, rel in test_files:
        base = os.path.basename(rel)
        if re.match(r".*HandlerSpec\.(groovy|java)$", base):
            m["handler_spec"] += 1

    if m["controller_total"] > 0:
        m["controller_direct_rate"] = round(m["controller_direct"] * 100.0 / m["controller_total"], 1)

    return m


def main(argv=None):
    ap = argparse.ArgumentParser(description="DDD Lite 指标统计")
    ap.add_argument("--root", default="scaffold", help="统计根目录（默认 scaffold）")
    ap.add_argument("--json", action="store_true", help="以 JSON 输出")
    args = ap.parse_args(argv)

    root = os.path.abspath(args.root)
    if not os.path.isdir(root):
        print(f"目录不存在: {root}", file=sys.stderr)
        return 2

    m = metrics(root)
    if args.json:
        print(json.dumps(m, ensure_ascii=False, indent=2))
    else:
        rate = m["controller_direct_rate"]
        rate_s = f"{rate}%" if rate is not None else "N/A"
        print("指标（口径见脚本注释）")
        print(f"  业务模块 Handler 总数    : {m['handler_total']}")
        print(f"  HandlerSpec 单元测试     : {m['handler_spec']}")
        print(f"  假 Handler（转调 Service）: {m['fake_handler']}")
        print(f"  遗留 Service 类          : {m['service_count']}")
        print(f"  Aggregate 类            : {m['aggregate_count']}")
        print(f"  domain/service 包        : {'存在（违规）' if m['domain_service_pkg'] else '不存在'}")
        print(f"  Controller 直调 Handler  : {m['controller_direct']}/{m['controller_total']} = {rate_s}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
