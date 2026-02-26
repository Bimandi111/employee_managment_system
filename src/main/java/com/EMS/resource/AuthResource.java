package com.EMS.resource;

import com.EMS.service.AuthService;
import com.EMS.util.ApiResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);
    private final AuthService authService = new AuthService();

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        log.info("Login attempt received");

        if (credentials == null) {
            log.error("Credentials are NULL");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Credentials are null")).build();
        }

        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Username received: {}", username);
        log.info("Password received: {}", password != null ? "****" : "NULL");

        if (username == null || password == null) {
            log.error("Username or password is null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Username and password are required.")).build();
        }

        try {
            Map<String, String> tokenData = authService.login(username, password);
            log.info("Login successful for: {}", username);
            return Response.ok(ApiResponse.success("Login successful.", tokenData)).build();
        } catch (SecurityException e) {
            log.error("Login failed for {}: {}", username, e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Server error: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/test")
    public Response test() {
        log.info("Test endpoint hit");
        return Response.ok("{\"status\":\"Jersey is working\"}").build();
    }
}
