package com.proyecto2.app.backend;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

@ApplicationPath("api/v1")
public class JakartaRestConfiguration extends ResourceConfig {

   public JakartaRestConfiguration() {
        packages(
                "com.proyecto2.app.backend.Backend.controller",
                "com.proyecto2.app.backend.Backend.seguridad"
        );

        register(RolesAllowedDynamicFeature.class);
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    }
}
