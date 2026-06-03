package com.example.demo.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;  // 状态码
    private String msg;    // 提示信息
    private T data;        // 数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        return result;
    }
}