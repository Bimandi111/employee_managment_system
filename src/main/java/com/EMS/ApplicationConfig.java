package com.EMS;

import com.EMS.auth.JwtAuthFilter;
import com.EMS.auth.RoleAuthFilter;
import com.EMS.resource.AuthResource;
import com.EMS.resource.EmployeeResource;
import com.EMS.resource.LookupResource;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        register(AuthResource.class);
        register(EmployeeResource.class);
        register(LookupResource.class);
        register(JwtAuthFilter.class);
        register(RoleAuthFilter.class);
        register(JacksonFeature.class);
        register(JacksonConfig.class);
    }
}