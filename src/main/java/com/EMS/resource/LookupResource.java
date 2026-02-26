package com.EMS.resource;

import com.EMS.auth.Secured;
import com.EMS.service.LookupService;
import com.EMS.util.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/lookups")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class LookupResource {

    private final LookupService lookupService = new LookupService();

    @GET
    @Path("/departments")
    public Response getDepartments() {
        return Response.ok(ApiResponse.success(lookupService.getAllDepartments())).build();
    }

    @GET
    @Path("/positions")
    public Response getPositions() {
        return Response.ok(ApiResponse.success(lookupService.getAllPositions())).build();
    }
}