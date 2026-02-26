package com.EMS.auth;

import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;

@Provider
@Secured
@Priority(Priorities.AUTHORIZATION)
public class RoleAuthFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        Method method = resourceInfo.getResourceMethod();
        RolesAllowed methodRoles = method.getAnnotation(RolesAllowed.class);
        RolesAllowed classRoles = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        RolesAllowed roles = (methodRoles != null) ? methodRoles : classRoles;
        if (roles == null) return;
        boolean permitted = false;
        for (String role : roles.value()) {
            if (containerRequestContext.getSecurityContext().isUserInRole(role)) {
                permitted = true;
                break;
            }
        }
        if (!permitted) {
            containerRequestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"success\":false,\"message\":\"You do not have permission.\"}")
                    .type("application/json").build());
        }
    }
}