package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class AdminLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 어드민 API 경로인지 확인
        if (requestURI.startsWith("/admin")) {
            // 사용자 권한 확인 (JwtFilter에서 설정된 값)
            String userRole = (String) request.getAttribute("userRole");
            Long userId = (Long) request.getAttribute("userId");

            // 어드맨 권한 체크
            if (!UserRole.ADMIN.name().equals(userRole)) {
                log.warn("비인가 어드민 API 접근 시도 - 사용자 ID: {}, 권한: {}, URL: {}",
                        userId, userRole, requestURI);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
                return false;
            }

            // 어드민 API 접근 로깅
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("어드민 API 접근 - 사용자 ID: {}, 접근 시간: {}, URL: {}, 메서드: {}",
                    userId, currentTime, requestURI, request.getMethod());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        String requestURI = request.getRequestURI();

        // /admin 요청이 완료된 후
        if (requestURI.startsWith("/admin")) {
            if (ex !=null) {
                log.error("비인가 어드민 API 실행 중 오류 발생: {}", ex.getMessage());
            } else {
                log.info("어드민 API 실행 완료: {}", requestURI);
            }
        }
    }
}
