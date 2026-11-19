package com.example.demo.auth;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 读取 token：jwt 模式读 Authorization 头，session 模式读 sessionId 头
        String token = extractToken(request);

        if (token == null) {
            return reject(response, "未登录");
        }

        // 2. 校验 token，失败会抛 BusinessException（401）
        Long userId = tokenService.parseToken(token);

        // 3. 校验通过，把 userId 塞进 request，供 Controller 用
        request.setAttribute("userId", userId);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        // 优先取 Authorization: Bearer <token>
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        // 其次取 sessionId 头
        return request.getHeader("sessionId");
    }

    private boolean reject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(message)));
        return false;
    }
}
