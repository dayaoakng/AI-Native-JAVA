package com.demo.partner.domain.valueobject

import com.demo.framework.exception.BusinessException
import spock.lang.Specification

/**
 * 伙伴状态值对象单元测试。
 */
class PartnerStatusSpec extends Specification {

    def "of 已知 code 返回对应枚举"() {
        expect:
        PartnerStatus.of(0) == PartnerStatus.INACTIVE
        PartnerStatus.of(1) == PartnerStatus.ACTIVE
        PartnerStatus.of(2) == PartnerStatus.FROZEN
    }

    def "of null 或非法 code 抛业务异常"() {
        when:
        PartnerStatus.of(null)

        then:
        thrown(BusinessException)

        when:
        PartnerStatus.of(99)

        then:
        thrown(BusinessException)
    }
}
