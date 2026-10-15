package com.demo.partner.application.listener;

import com.demo.partner.domain.event.PartnerStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 伙伴状态变更监听（支柱 4：跨模块走事件）。
 *
 * 这里仅示意事件被消费的位置与写法；真实项目中把「发通知/记审计/同步下游」放在此类，
 * 且可替换为 @TransactionalEventListener 以在事务提交后触发。
 */
@Slf4j
@Component
public class PartnerStatusListener {

    @EventListener
    public void onStatusChanged(PartnerStatusChangedEvent event) {
        log.info("伙伴状态变更: partnerId={}, {} -> {}",
                event.getPartnerId(), event.getOldStatus(), event.getNewStatus());
        // 实际业务：发 MQ、记审计日志、通知下游模块……
    }
}
