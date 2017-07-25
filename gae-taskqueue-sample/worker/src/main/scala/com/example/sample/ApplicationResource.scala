package com.example.sample

import java.util.logging.Logger
import javax.ws.rs.core.MediaType
import javax.ws.rs.ext.{ContextResolver, Provider}
import javax.ws.rs._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.glassfish.jersey.server.ResourceConfig

/**
  */
@ApplicationPath("/")
class ApplicationResource extends ResourceConfig {
  packages(this.getClass.getPackage.getName, "com.fasterxml.jackson.jaxrs")
}

@Path("/do_work")
class Work {

  val logger = Logger.getLogger(classOf[Work].getName)

  @GET
  def get() = "ok"

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def post(workParam: TaskBody) = {
    logger.info(workParam.toString)
  }
}

@Provider
@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
class JsonContextResolver extends ContextResolver[ObjectMapper] {

  private val mapper = {
    val _mapper = new ObjectMapper()
    _mapper.registerModule(DefaultScalaModule)
    _mapper
  }

  override def getContext(`type`: Class[_]): ObjectMapper = {
    mapper
  }

}

case class TaskBody(a: String, b: Int)
