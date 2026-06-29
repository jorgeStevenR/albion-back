package com.albion.guildbalance.web.security;

import com.albion.guildbalance.application.port.PlayerRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final PlayerRepositoryPort playerRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null
                || !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof PlayerPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAllowedWhilePasswordChangePending(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean mustChange = playerRepository.findById(principal.getPlayerId())
                .map(player -> player.isMustChangePassword())
                .orElse(false);

        if (!mustChange) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "success", false,
                "message", "Debes cambiar tu contraseña antes de continuar"
        ));
    }

    private boolean isAllowedWhilePasswordChangePending(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/auth/")) {
            return false;
        }
        if (HttpMethod.GET.matches(request.getMethod()) && uri.equals("/api/auth/me")) {
            return true;
        }
        return HttpMethod.POST.matches(request.getMethod()) && uri.equals("/api/auth/change-password");
    }
}
