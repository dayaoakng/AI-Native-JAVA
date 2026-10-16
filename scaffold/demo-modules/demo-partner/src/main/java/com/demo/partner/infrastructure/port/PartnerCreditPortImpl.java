package com.demo.partner.infrastructure.port;

import com.demo.partner.domain.port.PartnerCreditPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Port 实现（基础设施层）。
 *
 * 示例为本地桩：真实项目里改为调用下游模块的接口 / 外部 HTTP / RPC / MQ。
 * 契约定义在 domain/port/，实现在 infrastructure/，符合「依赖方向单向」。
 */
@Slf4j
@Component
public class PartnerCreditPortImpl implements PartnerCreditPort {

    @Override
    public boolean hasAvailableCredit(Long partnerId) {
        log.info("查询伙伴授信额度: partnerId={}（示例桩，真实项目调用下游模块）", partnerId);
        return true;
    }
}
