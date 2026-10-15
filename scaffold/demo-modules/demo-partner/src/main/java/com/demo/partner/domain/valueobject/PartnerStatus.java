package com.demo.partner.domain.valueobject;

import com.demo.framework.exception.BusinessException;
import com.demo.framework.result.ResultCode;
import lombok.Getter;

import java.util.Arrays;

/**
 * 伙伴状态值对象：把散落的魔法数字（0/1/2）收敛为语义化枚举。
 */
@Getter
public enum PartnerStatus {

    INACTIVE(0, "未激活"),
    ACTIVE(1, "已激活"),
    FROZEN(2, "已冻结");

    private final int code;
    private final String desc;

    PartnerStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PartnerStatus of(Integer code) {
        if (code == null) {
            throw new BusinessException(ResultCode.PARTNER_STATUS_INVALID, "伙伴状态不能为空");
        }
        return Arrays.stream(values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.PARTNER_STATUS_INVALID, "非法伙伴状态: " + code));
    }
}
