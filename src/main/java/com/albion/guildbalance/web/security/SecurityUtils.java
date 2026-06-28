package com.albion.guildbalance.web.security;



import com.albion.guildbalance.domain.enums.PlayerRole;
import com.albion.guildbalance.application.exception.BusinessException;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;



public final class SecurityUtils {



    private SecurityUtils() {

    }



    public static PlayerPrincipal getCurrentPlayer() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof PlayerPrincipal principal) {

            return principal;

        }

        throw new IllegalStateException("No authenticated player found");

    }



    public static boolean hasRole(PlayerRole role) {

        return getCurrentPlayer().getRole() == role;

    }



    public static boolean isAdmin() {

        return getCurrentPlayer().getRole() == PlayerRole.ADMIN;

    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException("Solo administradores pueden realizar esta acción");
        }
    }



    public static boolean isCallerOrAdmin() {

        PlayerRole role = getCurrentPlayer().getRole();

        return role == PlayerRole.ADMIN || role == PlayerRole.CALLER || role == PlayerRole.OFFICER;

    }



    /** @deprecated use {@link #isCallerOrAdmin()} */

    @Deprecated

    public static boolean isAdminOrOfficer() {

        return isCallerOrAdmin();

    }

}


