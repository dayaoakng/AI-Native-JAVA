# AI 开发手册

> 一套通用的 Java 后端 AI 开发规范：分层、命名、职责边界、质量门禁、事件驱动、开发流程、审查清单。
> 本手册是「AI 开发规范方法论」的**实操版**——不讲为什么，只讲怎么做。配套可运行骨架见 [`scaffold/`](../scaffold/)。

---

## 一、核心原则

### 1.1 四大边界

```
Controller   不写业务
Application  不写规则
Domain       不碰技术
Infrastructure 不懂业务
```

### 1.2 模块优先

按**业务域**划分模块，模块内部再按分层组织；**禁止**按技术分层作为一级目录。

```
✅ modules/order（一个业务域，内部 interfaces/application/domain/infrastructure）
❌ modules/controller + modules/service + modules/mapper（按技术分层）
❌ modules/common + modules/utils（技术模块）
```

---

## 二、硬规则（P0，违反即拒绝生成代码）

| # | 规则 | 目的 |
|---|------|------|
| 1 | 禁止新增 `service/`、`service/impl/`、`XxxService`、`XxxApplicationService` | 消灭上帝类 |
| 2 | 1 用例 = 1 Handler，Handler ≤ 100 行 | 单一职责、AI 生成准确率 |
| 3 | 单文件 ≤ 300 行（Port/PortImpl/RepositoryImpl/Mapper ≤ 500 行豁免）；方法 ≤ 30 行 | 强制拆分 |
| 4 | 领域实体必须放 `domain/entity/`；domain 层仅 `entity/valueobject/repository/event/port` | 对象各归其位 |
| 5 | 源码 UTF-8 无 BOM | 消灭编码事故 |

> 这五条**都能被机器自动检查**：`scripts/standards/scan-compliance.py` 一键扫描，pre-commit hook + CI 双门禁拦截。规则不能自动检查 = 只是口号。

---

## 三、代码放哪（决策树）

写代码前直接查表，**不要猜**：

### 3.1 按职责定位

```
这段代码是什么？
├── 领域规则（状态校验/变更、业务计算、不变量维护）
│   └── domain/entity/、domain/valueobject/
├── 用例编排（加载实体、调仓储、跨实体协作、事务、发事件）
│   └── application/handler/（1 用例 1 Handler）
├── 事件监听（消费领域事件，事件驱动的用例入口）
│   └── application/listener/
├── 持久化抽象
│   └── 接口 domain/repository/；实现 infrastructure/
├── 跨模块 / 外部系统能力
│   └── 契约 domain/port/；实现 infrastructure/
├── 跨模块通知
│   └── domain/event/（发布）+ 对方 @EventListener（消费）
├── 外部适配（MyBatis/Redis/HTTP/MQ）
│   └── infrastructure/
└── 接口层参数/返回值
    └── interfaces/request|response|vo/
```

### 3.2 对象落位

```
这个对象有没有独立身份、状态、业务行为？
├── 有 → 领域实体 domain/entity/XxxEntity
└── 没有
    ├── 不可变业务概念（金额/编号/时间区间）？→ domain/valueobject/（无 VO 后缀）
    ├── 只是数据库映射 / 复杂 SQL 查询结果？→ infrastructure/（XxxPO / XxxRow）
    └── 只是 HTTP / 消息契约？→ interfaces/（Request / Response / VO）
```

---

## 四、分层职责与命名

### 4.1 interfaces（接口层）

**Controller 只做 4 件事**：接收参数 → 校验参数 → 调用 Handler → 返回结果。

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/partners")
public class PartnerManagementController {
    private final CreatePartnerHandler createPartnerHandler;

    @PostMapping
    public Result<PartnerResponse> create(@Valid @RequestBody CreatePartnerRequest request) {
        return Result.ok(createPartnerHandler.execute(request));
    }
}
```

禁止在 Controller 中：业务判断、直接调 Mapper、发布事件、操作数据库。

### 4.2 application（应用层）

**Handler = 用例编排**，允许前置校验（exists/null/permission），禁止复杂业务 if-else 与业务计算。

```java
@Component
@RequiredArgsConstructor
public class ActivatePartnerHandler {
    private final PartnerRepository partnerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public PartnerResponse execute(Long partnerId) {
        PartnerEntity partner = partnerRepository.requireById(partnerId); // 前置校验
        partner.activate();                                              // 规则在 Entity
        partnerRepository.save(partner);
        eventPublisher.publishEvent(...);                                // 跨模块通知
        return PartnerAssembler.toResponse(partner);
    }
}
```

### 4.3 domain（领域层）

**仅含** `entity/`、`valueobject/`、`repository/`、`event/`（+ 跨模块 `port/`），**无 `service/`**。

- 单实体规则 → `XxxEntity` 方法（工厂方法 / 状态转换 / 业务判断）
- 同模块跨实体编排 → Handler
- 跨模块协作 → Port 或领域事件

Entity 必须充血（≥3 类业务方法），**不依赖 Spring**（不 `@Autowired`）。

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner")
public class PartnerEntity extends BaseEntity {
    private String code;
    private Integer status;

    public static PartnerEntity create(String code, String name) {  // 工厂方法
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "编码不能为空");
        }
        PartnerEntity p = new PartnerEntity();
        p.code = code.trim();
        p.status = PartnerStatus.INACTIVE.getCode();
        return p;
    }

    public void activate() {                                        // 状态转换
        if (status() == PartnerStatus.ACTIVE) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "已激活");
        }
        this.status = PartnerStatus.ACTIVE.getCode();
    }

    public boolean isActive() {                                     // 业务判断
        return status() == PartnerStatus.ACTIVE;
    }
}
```

### 4.4 infrastructure（基础设施层）

`RepositoryImpl` 实现 domain 的 Repository 接口，负责 Entity ↔ PO 转换；**禁止 Repository 返回 PO**。

```java
@Repository
@RequiredArgsConstructor
public class PartnerRepositoryImpl implements PartnerRepository {
    private final PartnerMapper partnerMapper;

    @Override public PartnerEntity findById(Long id) { return partnerMapper.selectById(id); }
    @Override public void save(PartnerEntity p) {
        if (p.getId() == null) partnerMapper.insert(p);
        else partnerMapper.updateById(p);
    }
}
```

### 4.5 命名对照表

| 层级 | 类型 | 命名 | 示例 |
|------|------|------|------|
| interfaces | Controller | `{Business}Controller` | `PartnerController` |
| | Request | `{Action}{Business}Request` | `CreatePartnerRequest` |
| | Response | `{Business}Response` | `PartnerResponse` |
| | 视图对象 | `{Business}VO` | `PartnerListVO` |
| application | Handler | `{Action}{Business}Handler` | `CreatePartnerHandler` |
| | Assembler | `{Business}Assembler` | `PartnerAssembler` |
| | Listener | `{Business}{Action}Listener` | `PartnerStatusListener` |
| domain | Entity | `{Business}Entity` | `PartnerEntity` |
| | 值对象 | `{Concept}`（无 VO 后缀） | `PartnerStatus`、`Money` |
| | Repository | `{Business}Repository` | `PartnerRepository` |
| | Event | `{Business}{Action}Event` | `PartnerStatusChangedEvent` |
| | Port | `{Business}{Action}Port` | `PartnerQueryPort` |
| infrastructure | Mapper | `{Business}Mapper` | `PartnerMapper` |
| | PO | `{Business}PO` | `PartnerPO` |
| | RepositoryImpl | `{Business}RepositoryImpl` | `PartnerRepositoryImpl` |

**禁止后缀**：`BO`、`DO`、`Model`、`Bean`、`Manager`、`Util`、`Info`、`Data`。

---

## 五、代码质量硬约束

| 约束项 | 标准 | 超过则 |
|--------|------|--------|
| Handler 类 | ≤ 100 行 | 拆多个 Handler，或抽 `XxxSupport` 到 `application/support/` |
| Entity 类 | ≤ 300 行 | 拆子领域或提取值对象 |
| 单文件 | ≤ 300 行 | 按文件类型拆分（Port 等适配器 ≤ 500 行豁免） |
| 方法 | ≤ 30 行 | 提取子方法 |
| 方法参数 | ≤ 5 个 | 用对象封装 |
| if-else 嵌套 | ≤ 3 层 | 策略模式 / 卫语句 |
| 循环嵌套 | ≤ 2 层 | 提取方法 |

---

## 六、跨模块协作

| 场景 | 手段 |
|------|------|
| 异步通知 | 领域事件：`@Transactional` 内 `publishEvent` → 对方 `@EventListener` |
| 同步查询 | Port 接口：调用方在 `domain/port/` 定义契约，提供方模块实现 |

```java
// 发布（Handler 内）
eventPublisher.publishEvent(new OrderCreatedEvent(this, order.getId()));

// 消费（其他模块）
@Component
public class CommissionListener {
    @EventListener
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) { /* ... */ }
}
```

**禁止**：模块间直接 `@Autowired` 对方的 Service/Mapper；Handler 调用 Handler。

---

## 七、开发流程

### 7.1 新增功能

```
需求分析 → 确定模块 → 设计领域模型（Entity/ValueObject/Handler）
→ TDD 先写测试（Spock）→ 实现 Handler → 实现 Entity → 实现 Repository
→ 实现 Controller → 跑测试 → Code Review → 提交
```

### 7.2 修改现有功能

```
定位 Handler → 阅读 Entity 理解规则 → 先改测试 → 改业务逻辑（优先改 Entity）
→ 跑测试 → Code Review → 提交
```

---

## 八、Code Review 检查清单

- [ ] 代码放在正确的模块与层级
- [ ] 命名符合后缀规范，无 BO/DO/Model/Bean 等禁止后缀
- [ ] Controller 只做 4 件事
- [ ] Handler 只编排，不含复杂业务规则，≤ 100 行
- [ ] Entity 充血，业务规则在 Entity，≤ 300 行
- [ ] domain 无 `service/`，仅 entity/valueobject/repository/event/port
- [ ] Handler 不直调 Mapper（经 Repository）
- [ ] 无跨模块直接依赖（走 Port/事件）
- [ ] 方法 ≤ 30 行，if-else ≤ 3 层
- [ ] Java 8：Request/Response/值对象用 class + Lombok，不用 record
- [ ] Handler 有单元测试、Entity 业务方法有测试（Spock）
- [ ] 源码 UTF-8 无 BOM

---

## 九、FAQ

**Q1：现有代码不符合规范怎么办？**  
三策略：新增功能严格按新规范；小改（<10 行）保持原样；大改（>50 行）重构为 Handler + Entity。

**Q2：Handler 适合所有场景吗？**  
适合：创建/查询/导入导出等独立用例。**状态机**（订单 创建→支付→发货→完成）不适合每步一个 Handler，应集中在充血 Entity 的 `pay()/ship()/complete()` 方法，由 Handler 编排调用。

**Q3：跨实体业务规则放哪？**  
单实体规则 → Entity 方法；跨实体用例流程 → Handler 编排；跨模块 → Port / 事件。

**Q4：值对象和 DTO 区别？**  
值对象（domain 层）：领域概念（Money、PhoneNumber），不可变，含业务校验；DTO（interfaces 层）：数据传输（Request/Response），可变，不含业务逻辑。

**Q5：Repository 一定要用吗？**  
新模块必须用；老模块小改保持原样，大改时引入。

---

## 十、给 AI 的提示词模板

**新增功能**：

```
请按 DDD Lite 规范开发：
模块: {module}
功能: {功能描述}

要求:
1. 创建 {Action}{Business}Handler（≤100 行）
2. 业务规则写在 {Business}Entity 内（充血）
3. Request/Response 用 class + Lombok（Java 8 禁止 record）
4. Controller 不写业务逻辑
5. 方法 ≤ 30 行，if-else ≤ 3 层
6. 编写 Spock 单元测试

请先设计领域模型，再实现代码。
```

**修改功能**：

```
修改 {module} 的 {功能}：
需求变更: {描述}

步骤:
1. 定位相关 Handler
2. 阅读 Entity 理解现有规则
3. 先改测试
4. 改业务逻辑
5. 跑测试并确保符合规范
```

---

## 十一、渐进式改造策略

| 级别 | 内容 | 节奏 |
|------|------|------|
| P0（必须） | 模块按业务划分、命名统一、Controller 不写业务 | 新功能立即执行 |
| P1（推荐） | Handler 模式、Repository 封装、事件驱动 | 大改时应用 |
| P2（高级） | 充血模型、CQRS | 逐步引入 |

---

**配套**：[方法论（为什么）](AI开发规范方法论.md) · [架构优势](架构优势.md) · [可运行骨架](../scaffold/) · [AI 规则入口](../AGENTS.md)
