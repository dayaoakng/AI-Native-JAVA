package com.demo.partner.interfaces.controller;

import com.demo.framework.result.Result;
import com.demo.partner.application.handler.ActivatePartnerHandler;
import com.demo.partner.application.handler.CreatePartnerHandler;
import com.demo.partner.interfaces.request.CreatePartnerRequest;
import com.demo.partner.interfaces.response.PartnerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 伙伴管理接口：只接收参数 → 调用 Handler → 返回结果。
 */
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/partners")
public class PartnerManagementController {

    private final CreatePartnerHandler createPartnerHandler;
    private final ActivatePartnerHandler activatePartnerHandler;

    @PostMapping
    public Result<PartnerResponse> create(@Valid @RequestBody CreatePartnerRequest request) {
        return Result.ok(createPartnerHandler.execute(request));
    }

    @PostMapping("/{partnerId}/activate")
    public Result<PartnerResponse> activate(@PathVariable Long partnerId) {
        return Result.ok(activatePartnerHandler.execute(partnerId));
    }
}
