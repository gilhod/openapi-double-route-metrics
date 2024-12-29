package com.training;

import io.vertx.ext.web.RoutingContext;

public class GetUserApi {

    public static void handleRequest(RoutingContext routingContext) {
        System.out.println("handleRequest");
        routingContext.json("dummy user");
    }

    public static void handleError(RoutingContext routingContext) {
        System.out.println("handleError");
        routingContext.response().setStatusCode(500);
        routingContext.json("dummy error");
    }


}
