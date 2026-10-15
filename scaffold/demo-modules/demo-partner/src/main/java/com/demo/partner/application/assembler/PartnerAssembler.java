package com.demo.partner.application.assembler;

import com.demo.partner.domain.entity.PartnerEntity;
import com.demo.partner.interfaces.response.PartnerResponse;

/**
 * 伙伴对象转换器（Entity → Response）。
 */
public final class PartnerAssembler {

    private PartnerAssembler() {
    }

    public static PartnerResponse toResponse(PartnerEntity partner) {
        if (partner == null) {
            return null;
        }
        PartnerResponse response = new PartnerResponse();
        response.setId(partner.getId());
        response.setCode(partner.getCode());
        response.setName(partner.getName());
        response.setStatus(partner.getStatus());
        response.setStatusDesc(partner.status().getDesc());
        return response;
    }
}
