package com.agent.config;

import cn.dev33.satoken.stp.StpInterface;
import com.agent.entity.SysUser;
import com.agent.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限 / 角色数据源实现。
 * 角色直接取自 sys_user.role；权限演示性地由角色派生。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String role = resolveRole(loginId);
        List<String> perms = new ArrayList<>();
        switch (role) {
            case "admin" -> {
                perms.add("chat:*");
                perms.add("knowledge:*");
                perms.add("user:*");
            }
            case "agent" -> {
                perms.add("chat:read");
                perms.add("escalation:handle");
            }
            default -> perms.add("chat:read");
        }
        return perms;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.singletonList(resolveRole(loginId));
    }

    private String resolveRole(Object loginId) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            SysUser user = sysUserMapper.selectById(userId);
            return user != null && user.getRole() != null ? user.getRole() : "user";
        } catch (Exception e) {
            return "user";
        }
    }
}
