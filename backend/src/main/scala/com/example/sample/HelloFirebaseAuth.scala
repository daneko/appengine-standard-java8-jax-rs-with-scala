package com.example.sample

import java.util.logging.{Level, Logger}
import javax.ws.rs.core.{Context, SecurityContext}
import javax.ws.rs.{GET, Path}

@Path("firebase")
class HelloFirebaseAuth {

  val logger = Logger.getLogger(classOf[HelloFirebaseAuth].getName)

  @WithFirebaseAuth
  @GET
  def withAuth(@Context context: SecurityContext): String = {
    logger.log(Level.INFO, "call with auth")
    context.getUserPrincipal.getName
  }
}