package com.demo.partner.application.handler;

import com.demo.partner.application.assembler.PartnerAssembler;
import com.demo.partner.domain.entity.PartnerEntity;
import com.demo.partner.domain.event.PartnerStatusChangedEvent;
import com.demo.partner.domain.repository.PartnerRepository;
import com.demo.partner.interfaces.response.PartnerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 激活伙伴 Handler：加载 → 调用 Entity 业务方法 → 持久化 → 发布事件。
 */
@Component
@RequiredArgsConstructor
public class ActivatePartnerHandler {

    private final PartnerRepository partnerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public PartnerResponse execute(Long partnerId) {
        PartnerEntity partner = partnerRepository.requireById(partnerId);
        Integer oldStatus = partner.getStatus();

        partner.activate();

        partnerRepository.save(partner);

        eventPublisher.publishEvent(
                new PartnerStatusChangedEvent(this, partnerId, oldStatus, partner.getStatus()));

        return PartnerAssembler.toResponse(partner);
    }
}
