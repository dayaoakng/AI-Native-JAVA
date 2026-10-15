package com.demo.partner.domain.repository;

import com.demo.partner.domain.entity.PartnerEntity;

/**
 * 伙伴仓储接口（领域层定义，基础设施层实现）。
 */
public interface PartnerRepository {

    PartnerEntity findById(Long partnerId);

    PartnerEntity requireById(Long partnerId);

    boolean existsByCode(String code);

    void save(PartnerEntity partner);
}
