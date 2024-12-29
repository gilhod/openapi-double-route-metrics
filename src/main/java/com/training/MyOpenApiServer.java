package com.training;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.Operation;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;

public class MyOpenApiServer {

    private static final String METER_REGISTRY_NAME="reproducer";
    private Vertx vertx;
    private HttpServer httpServer;


    private void createVertxWithMetrics() {

        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        var metricOptions = new MicrometerMetricsOptions()
                .setPrometheusOptions(
                        new VertxPrometheusOptions()
                                .setEnabled(true)
                )
                .setRegistryName(METER_REGISTRY_NAME)
                .setMicrometerRegistry(meterRegistry)
                .setEnabled(true);
        var vertxOptions = new VertxOptions()
                .setMetricsOptions(
                        metricOptions
                );
        vertx = Vertx.vertx(vertxOptions);
    }

    public void run() {
        createVertxWithMetrics();
        RouterBuilder.create(vertx, "openapi.yaml")
                .flatMap(routerBuilder -> {
                    // Set handlers
                    Operation getUserOperation = routerBuilder.operation("getUser");

                    getUserOperation.handler(GetUserApi::handleRequest);

                    // Set error handler on the Operation
                    // This causes the route in metrics to be "path>path"
                    getUserOperation.failureHandler(GetUserApi::handleError);

                    Router router = routerBuilder.createRouter();

                    // set error handler on the router
                    // When this is set, instead on the Operation, the reported route is as expected: just the path
//                    router.errorHandler(400, GetUserApi::handleError);

                    // Set metrics endpoint
                    router.get("/metrics")
                            .handler(PrometheusScrapingHandler.create(METER_REGISTRY_NAME));

                    // Set and run the server
                    httpServer = vertx
                            .createHttpServer()
                            .requestHandler(router);
                    // Run the server
                    return httpServer.listen(9999);
                })
                .onSuccess(event -> {
                    System.out.println("server started on port 9999");
                })
                .onFailure(error -> {
                    System.out.println("Got failure:");
                    error.printStackTrace();
                });
    }

    public void stop() {
        httpServer.close();
    }
}
