package com.demo.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动类（示例模块 demo-partner 为可启动模块）。
 *
 * scanBasePackages 覆盖 com.demo：既要扫描本模块的 Controller/Handler/Listener，
 * 也要扫描 demo-framework 的全局异常处理（@RestControllerAdvice）。
 */
@SpringBootApplication(scanBasePackages = "com.demo")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
