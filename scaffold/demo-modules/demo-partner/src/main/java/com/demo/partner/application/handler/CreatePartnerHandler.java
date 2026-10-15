package com.demo.partner.application.handler;

import com.demo.framework.exception.BusinessException;
import com.demo.framework.result.ResultCode;
import com.demo.partner.application.assembler.PartnerAssembler;
import com.demo.partner.domain.entity.PartnerEntity;
import com.demo.partner.domain.repository.PartnerRepository;
import com.demo.partner.interfaces.request.CreatePartnerRequest;
import com.demo.partner.interfaces.response.PartnerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 创建伙伴 Handler：1 用例 = 1 Handler，只做编排，业务规则在 Entity。
 */
@Component
@RequiredArgsConstructor
public class CreatePartnerHandler {

    private final PartnerRepository partnerRepository;

    @Transactional(rollbackFor = Exception.class)
    public PartnerResponse execute(CreatePartnerRequest request) {
        // 1. 前置校验（查询）
        if (partnerRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "伙伴编码已存在");
        }

        // 2. 创建实体（业务规则在 Entity 工厂方法）
        PartnerEntity partner = PartnerEntity.create(request.getCode(), request.getName());

        // 3. 保存
        partnerRepository.save(partner);

        // 4. 返回
        return PartnerAssembler.toResponse(partner);
    }
}
