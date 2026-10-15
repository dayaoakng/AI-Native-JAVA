package com.demo.partner.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.framework.exception.BusinessException;
import com.demo.framework.result.ResultCode;
import com.demo.partner.domain.valueobject.PartnerStatus;
import com.demo.shared.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 伙伴实体（DDD Lite 充血模型）：既是数据模型也是业务模型，业务方法写在 Entity 上。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("demo_partner")
public class PartnerEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("status")
    private Integer status;

    // ==================== 工厂方法 ====================

    public static PartnerEntity create(String code, String name) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "伙伴编码不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "伙伴名称不能为空");
        }
        PartnerEntity partner = new PartnerEntity();
        partner.code = code.trim();
        partner.name = name.trim();
        partner.status = PartnerStatus.INACTIVE.getCode();
        return partner;
    }

    // ==================== 状态转换方法 ====================

    public void activate() {
        if (status() == PartnerStatus.ACTIVE) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "伙伴已激活，无法重复激活");
        }
        this.status = PartnerStatus.ACTIVE.getCode();
    }

    public void freeze() {
        if (status() == PartnerStatus.FROZEN) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "伙伴已冻结");
        }
        this.status = PartnerStatus.FROZEN.getCode();
    }

    // ==================== 业务判断方法 ====================

    public PartnerStatus status() {
        return PartnerStatus.of(this.status);
    }

    public boolean isActive() {
        return status() == PartnerStatus.ACTIVE;
    }

    public boolean isFrozen() {
        return status() == PartnerStatus.FROZEN;
    }
}
