package com.banking.api;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import java.io.IOException;
import java.net.URI;

public class ApiApplication{

    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws IOException {

        final HttpServer server = startServer();

        System.out.println("Jersey app started at: "+ BASE_URI);
    }

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig().packages("com.banking.api.controllers");
        resourceConfig.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig);
    }

}
