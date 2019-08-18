package com.banking.api;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class apiApplication extends Application<apiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new apiApplication().run(args);
    }

    @Override
    public String getName() {
        return "api";
    }

    @Override
    public void initialize(final Bootstrap<apiConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final apiConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
