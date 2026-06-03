package com.example.demo.controller;

import com.example.demo.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "用于测试 Swagger 是否正常")
public class TestController {

    @Operation(summary = "测试接口", description = "这是一个测试接口，返回 Hello World")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello World! Swagger 已正常启动！");
    }
}