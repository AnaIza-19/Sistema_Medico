package com.example.sistemamedico.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminSessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("usuarioAdmin") == null) {

            response.sendRedirect(
                    "/admin/login"
            );

            return false;
        }

        return true;
    }
}