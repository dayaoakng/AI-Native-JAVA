package com.demo.partner.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.framework.exception.BusinessException;
import com.demo.framework.result.ResultCode;
import com.demo.partner.domain.entity.PartnerEntity;
import com.demo.partner.domain.repository.PartnerRepository;
import com.demo.partner.infrastructure.mapper.PartnerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 伙伴仓储实现（基础设施层）。
 */
@Repository
@RequiredArgsConstructor
public class PartnerRepositoryImpl implements PartnerRepository {

    private final PartnerMapper partnerMapper;

    @Override
    public PartnerEntity findById(Long partnerId) {
        if (partnerId == null) {
            return null;
        }
        return partnerMapper.selectById(partnerId);
    }

    @Override
    public PartnerEntity requireById(Long partnerId) {
        PartnerEntity partner = findById(partnerId);
        if (partner == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "伙伴不存在: " + partnerId);
        }
        return partner;
    }

    @Override
    public boolean existsByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        Long count = partnerMapper.selectCount(
                new LambdaQueryWrapper<PartnerEntity>()
                        .eq(PartnerEntity::getCode, code.trim()));
        return count != null && count > 0;
    }

    @Override
    public void save(PartnerEntity partner) {
        if (partner == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "伙伴实体不能为空");
        }
        if (partner.getId() == null) {
            partnerMapper.insert(partner);
        } else {
            partnerMapper.updateById(partner);
        }
    }
}
