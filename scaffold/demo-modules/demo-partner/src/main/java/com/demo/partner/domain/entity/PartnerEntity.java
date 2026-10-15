package com.demo.partner.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.framework.exception.BusinessException;
import com.demo.framework.result.ResultCode;
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
        partner.status = 0;
        return partner;
    }

    // ==================== 状态转换方法 ====================

    public void activate() {
        if (this.status != null && this.status == 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "伙伴已激活，无法重复激活");
        }
        this.status = 1;
    }

    public void freeze() {
        if (this.status != null && this.status == 2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "伙伴已冻结");
        }
        this.status = 2;
    }

    // ==================== 业务判断方法 ====================

    public boolean isActive() {
        return this.status != null && this.status == 1;
    }

    public boolean isFrozen() {
        return this.status != null && this.status == 2;
    }
}
