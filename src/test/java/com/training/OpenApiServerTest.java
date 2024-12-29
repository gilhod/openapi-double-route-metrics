package com.training;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
class OpenApiServerTest {

    // Just simple test to make sure server is working
    @Test
    @Disabled
    void getUserTest(VertxTestContext testContext) {
        Vertx  vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);
        MyOpenApiServer server = new MyOpenApiServer();
        server.run();

        webClient.getAbs("http://127.0.0.1:9999/v1/users?id=1")
                .send()
                .map(HttpResponse::bodyAsString)
                .onComplete(testContext.succeeding(user -> testContext.verify(()->{
                    assertEquals("\"dummy user\"", user);
                    testContext.completeNow();
                })));
    }

    // The test that demonstrate the issue
    @Test
    void BadRequestTest(VertxTestContext testContext) {
        Vertx  vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);
        MyOpenApiServer server = new MyOpenApiServer();
        server.run();
        webClient.getAbs("http://127.0.0.1:9999/v1/users") // Send request without required query param "id" so it will fail on validation
                .send()
                .map(HttpResponse::bodyAsString)
                .andThen(testContext.succeeding(user -> testContext.verify(()->{
                    assertEquals("\"dummy error\"", user); // Expect to get custom error message from error handler
                })))
                .compose(result-> webClient
                        .getAbs("http://127.0.0.1:9999/metrics") // call metrics report
                        .send()
                )
                .map(HttpResponse::bodyAsString)
                .map(this::getRouteFromMetricReport) // Extract path in report from one of the metrics
                .onComplete(testContext.succeeding(route -> testContext.verify(()->{
                    assertEquals("/v1/users", route); // Expect the path in report but this assert will fail since it will be /v1/users>/v1/users
                    testContext.completeNow();
                })));
    }

    //  Helper function
    private String getRouteFromMetricReport(String metricReport) {
        String totalRequestsMetric = Arrays.asList(metricReport.split("\n"))
                .stream()
                .filter(metric-> metric.startsWith("vertx_http_server_requests_total"))
                .findFirst()
                .get();

        String route = Arrays.asList(totalRequestsMetric.split("\""))
                .stream()
                .filter(substring-> substring.trim().startsWith("/v1"))
                .findFirst()
                .get()
                .trim();

        return route;
    }


}
