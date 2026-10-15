package com.demo.partner.application.handler

import com.demo.framework.exception.BusinessException
import com.demo.partner.domain.entity.PartnerEntity
import com.demo.partner.domain.repository.PartnerRepository
import com.demo.partner.interfaces.request.CreatePartnerRequest
import com.demo.partner.interfaces.response.PartnerResponse
import spock.lang.Specification

/**
 * 创建伙伴 Handler 单元测试（Spock BDD）。
 */
class CreatePartnerHandlerSpec extends Specification {

    PartnerRepository repository = Mock()
    CreatePartnerHandler handler = new CreatePartnerHandler(repository)

    def "创建伙伴成功"() {
        given:
        def request = new CreatePartnerRequest(code: "P001", name: "测试伙伴")
        repository.existsByCode("P001") >> false

        when:
        PartnerResponse response = handler.execute(request)

        then:
        response.code == "P001"
        response.name == "测试伙伴"
        response.status == 0
        1 * repository.save(_ as PartnerEntity)
    }

    def "编码重复时抛业务异常"() {
        given:
        def request = new CreatePartnerRequest(code: "P001", name: "测试伙伴")
        repository.existsByCode("P001") >> true

        when:
        handler.execute(request)

        then:
        thrown(BusinessException)
        0 * repository.save(_)
    }
}
