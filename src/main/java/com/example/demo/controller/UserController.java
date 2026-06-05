package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户信息 CRUD 接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<IPage<User>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        IPage<User> result = userService.page(new Page<>(page, size));
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @Operation(summary = "根据 ID 查询用户")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        userService.register(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }
}