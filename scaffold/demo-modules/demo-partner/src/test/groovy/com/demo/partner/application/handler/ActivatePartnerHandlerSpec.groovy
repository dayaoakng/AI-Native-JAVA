package com.demo.partner.application.handler

import com.demo.framework.exception.BusinessException
import com.demo.partner.domain.entity.PartnerEntity
import com.demo.partner.domain.port.PartnerCreditPort
import com.demo.partner.domain.repository.PartnerRepository
import com.demo.partner.interfaces.response.PartnerResponse
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification

/**
 * 激活伙伴 Handler 单元测试（Spock BDD）。
 */
class ActivatePartnerHandlerSpec extends Specification {

    PartnerRepository repository = Mock()
    PartnerCreditPort creditPort = Mock()
    ApplicationEventPublisher publisher = Mock()
    ActivatePartnerHandler handler = new ActivatePartnerHandler(repository, creditPort, publisher)

    def "激活伙伴成功"() {
        given:
        def partner = PartnerEntity.create("P001", "测试伙伴")
        repository.requireById(1L) >> partner
        creditPort.hasAvailableCredit(1L) >> true

        when:
        PartnerResponse response = handler.execute(1L)

        then:
        response.status == 1
        partner.isActive()
        1 * repository.save(_ as PartnerEntity)
    }

    def "授信不足时抛业务异常"() {
        given:
        def partner = PartnerEntity.create("P001", "测试伙伴")
        repository.requireById(1L) >> partner
        creditPort.hasAvailableCredit(1L) >> false

        when:
        handler.execute(1L)

        then:
        thrown(BusinessException)
        0 * repository.save(_)
    }
}
