package com.example.sample.json

import javax.ws.rs._
import javax.ws.rs.core.MediaType

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
  * https://www.mkyong.com/webservices/jax-rs/jax-rs-queryparam-example/
  */
@Path("hello_json")
class JsonResource {

  /*
   * curl http://localhost:8080/hello_json/hoge?q=hoge
   */
  @GET
  @Path("{path}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def json(@PathParam("path") path: String,
           @DefaultValue("default") @QueryParam("q") query: String): JsonResponseBody = {
    JsonResponseBody(path, query)
  }

  /*
   * curl -X POST -d '{"a":1, "b":"hoge"}' -H 'Content-Type: application/json' http://localhost:8080/hello_json
   *
   * { "a" : 1 } だと bはnull扱い
   * { "a" : 1, "c" :"hoge"} だと
   * Unrecognized field "c" (class com.example.sample.RequestBody), not marked as ignorable (2 known properties: "a", "b"])
   *  at [Source: org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream@155a1984; line: 1, column: 20] (through reference chain: com.example.sample.RequestBody["c"])%
   *
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def echo(requestBody: RequestBody): RequestBody = requestBody

}

/*
 * Can not construct instance of com.example.sample.HelloJsonResource$RequestBody: can only instantiate non-static inner class by using default, no-argument constructor
 *  at [Source: org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream@ee62f0a; line: 1, column: 2]
 *  とでるのでここにおいている
 */
case class RequestBody(a: Int, b: String)

/*
 * JsonIgnorePropertiesで未知のPropertiesが無視できない…
 */
@JsonIgnoreProperties(ignoreUnknown = true)
case class JsonResponseBody(path: String, query: String)
