package com.example;

import io.quarkus.redis.client.RedisClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;

@Path("/cache")
public class CacheResource {

    @Inject
    RedisClient redis;

    @POST
    @Path("/{key}/{value}")
    public Response put(@PathParam("key") String key,
                        @PathParam("value") String value) {
        redis.set(Arrays.asList(key, value));
        return Response.ok("Stored").build();
    }

    @GET
    @Path("/{key}")
    public String get(@PathParam("key") String key) {
        return redis.get(key).toString();
    }
}
