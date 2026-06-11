package com.agent.service;

import cn.dev33.satoken.stp.StpUtil;
import com.agent.common.ResultCode;
import com.agent.common.exception.BizException;
import com.agent.dto.LoginRequest;
import com.agent.entity.SysUser;
import com.agent.mapper.SysUserMapper;
import com.agent.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证服务。
 * 演示用：密码明文比对；生产应使用 BCrypt。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

    public LoginVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        log.info("用户登录成功 userId={} username={}", user.getId(), user.getUsername());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRole());
    }

    public void logout() {
        StpUtil.logout();
    }

    public Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
