package com.demo.partner.interfaces.response;

import lombok.Data;

/**
 * 伙伴响应体。
 */
@Data
public class PartnerResponse {

    private Long id;
    private String code;
    private String name;
    private Integer status;
}
