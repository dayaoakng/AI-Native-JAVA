package com.demo.partner.interfaces.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建伙伴请求体。
 */
@Data
public class CreatePartnerRequest {

    @NotBlank(message = "伙伴编码不能为空")
    private String code;

    @NotBlank(message = "伙伴名称不能为空")
    private String name;
}
