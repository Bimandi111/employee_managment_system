package com.EMS.auth;

import com.EMS.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        String authHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(containerRequestContext, "Missing or invalid Authorization header.");
            return;
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        try {
            Claims claims = JwtUtil.validateToken(token);
            String username = claims.getSubject();
            String role = (String) claims.get("role");
            containerRequestContext.setSecurityContext(new SecurityContext() {
                public Principal getUserPrincipal() {
                    return () -> username;
                }

                public boolean isUserInRole(String r) {
                    return r.equalsIgnoreCase(role);
                }

                public boolean isSecure() {
                    return containerRequestContext.getSecurityContext().isSecure();
                }

                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });
        } catch (JwtException e) {
            abort(containerRequestContext, "Token is invalid or has expired.");
        }
    }

    private void abort(ContainerRequestContext containerRequestContext, String message) {
        containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"success\":false,\"message\":\"" + message + "\"}")
                .type("application/json").build());
    }
}