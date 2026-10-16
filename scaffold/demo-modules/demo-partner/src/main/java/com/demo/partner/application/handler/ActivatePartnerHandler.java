package com.demo.partner.application.handler;

import com.demo.framework.exception.BusinessException;
import com.demo.framework.result.ResultCode;
import com.demo.partner.application.assembler.PartnerAssembler;
import com.demo.partner.domain.entity.PartnerEntity;
import com.demo.partner.domain.event.PartnerStatusChangedEvent;
import com.demo.partner.domain.port.PartnerCreditPort;
import com.demo.partner.domain.repository.PartnerRepository;
import com.demo.partner.interfaces.response.PartnerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 激活伙伴 Handler：加载 → 跨模块查询（Port）→ 调用 Entity 业务方法 → 持久化 → 发布事件。
 */
@Component
@RequiredArgsConstructor
public class ActivatePartnerHandler {

    private final PartnerRepository partnerRepository;
    private final PartnerCreditPort partnerCreditPort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public PartnerResponse execute(Long partnerId) {
        PartnerEntity partner = partnerRepository.requireById(partnerId);

        // 跨模块同步查询：走 Port（契约在 domain，实现在 infrastructure）
        if (!partnerCreditPort.hasAvailableCredit(partnerId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "伙伴授信额度不足");
        }

        Integer oldStatus = partner.getStatus();

        partner.activate();

        partnerRepository.save(partner);

        eventPublisher.publishEvent(
                new PartnerStatusChangedEvent(this, partnerId, oldStatus, partner.getStatus()));

        return PartnerAssembler.toResponse(partner);
    }
}
