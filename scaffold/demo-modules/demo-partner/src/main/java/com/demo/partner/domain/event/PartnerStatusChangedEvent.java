package com.demo.partner.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 伙伴状态变更领域事件。
 */
@Getter
public class PartnerStatusChangedEvent extends ApplicationEvent {

    private final Long partnerId;
    private final Integer oldStatus;
    private final Integer newStatus;

    public PartnerStatusChangedEvent(Object source, Long partnerId, Integer oldStatus, Integer newStatus) {
        super(source);
        this.partnerId = partnerId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
