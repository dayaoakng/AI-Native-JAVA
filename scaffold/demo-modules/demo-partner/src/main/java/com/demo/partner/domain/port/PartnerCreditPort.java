package com.demo.partner.domain.port;

/**
 * 跨模块 / 外部系统能力契约（Port）。
 *
 * 调用方在 domain 层定义接口，提供方在 infrastructure 层实现——依赖方向保持不变。
 * 真实项目中，该能力通常来自另一个模块或外部系统（如下游计费、风控、库存服务）。
 */
public interface PartnerCreditPort {

    /** 查询伙伴是否有可用授信额度（示例：真实项目改为调用下游模块 / 外部系统）。 */
    boolean hasAvailableCredit(Long partnerId);
}
