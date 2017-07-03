package com.example.sample

import javax.ws.rs.{GET, Path}

@Path("hello")
class HelloResource {

  @GET
  def hello(): String = {
    "Hello "
  }

}