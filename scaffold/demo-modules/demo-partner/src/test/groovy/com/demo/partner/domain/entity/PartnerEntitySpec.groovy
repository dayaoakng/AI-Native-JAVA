package com.demo.partner.domain.entity

import com.demo.framework.exception.BusinessException
import spock.lang.Specification

/**
 * 伙伴实体业务方法单元测试。
 */
class PartnerEntitySpec extends Specification {

    def "create 校验编码与名称非空"() {
        when:
        PartnerEntity.create("", "name")

        then:
        thrown(BusinessException)

        when:
        PartnerEntity.create("code", " ")

        then:
        thrown(BusinessException)
    }

    def "activate 状态转换"() {
        given:
        PartnerEntity partner = PartnerEntity.create("P001", "测试")

        when:
        partner.activate()

        then:
        partner.isActive()
        partner.status == 1
    }

    def "重复 activate 抛异常"() {
        given:
        PartnerEntity partner = PartnerEntity.create("P001", "测试")
        partner.activate()

        when:
        partner.activate()

        then:
        thrown(BusinessException)
    }
}
