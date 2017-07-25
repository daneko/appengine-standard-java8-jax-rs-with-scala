package com.example.sample

import java.util.logging.Logger
import javax.ws.rs._
import javax.ws.rs.core.{Context, MediaType}
import javax.ws.rs.ext.{ContextResolver, Provider, Providers}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.appengine.api.taskqueue.{QueueFactory, TaskOptions}
import org.glassfish.jersey.server.ResourceConfig

/**
  */
@ApplicationPath("/")
class ApplicationResource extends ResourceConfig {
  packages(this.getClass.getPackage.getName, "com.fasterxml.jackson.jaxrs")
}

@Path("sample")
class CallWorkerApi {

  val logger = Logger.getLogger("sample")


  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def get(@Context providers: Providers): ResponseBody = {
    val requestBody = TaskBody("self", 1)
    val objectMapper = providers.getContextResolver(classOf[ObjectMapper], MediaType.APPLICATION_JSON_TYPE).
      getContext(classOf[ObjectMapper])

    val task = TaskOptions.Builder.withUrl("/sample/local").
      method(TaskOptions.Method.POST).
      payload(objectMapper.writeValueAsBytes(requestBody), "application/json")

    QueueFactory.getQueue("local-queue").add(task)

    ResponseBody("ok")
  }

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def post(@Context providers: Providers, requestBody: TaskBody): ResponseBody = {
    val objectMapper = providers.getContextResolver(classOf[ObjectMapper], MediaType.APPLICATION_JSON_TYPE).
      getContext(classOf[ObjectMapper])

    val task = TaskOptions.Builder.withUrl("/do_work").
      method(TaskOptions.Method.POST).
      payload(objectMapper.writeValueAsBytes(requestBody), "application/json")

    QueueFactory.getQueue("worker-queue").add(task)

    ResponseBody("accept")
  }

  @Path("local")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def work(requestBody: TaskBody) = {
    logger.info(requestBody.toString)
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

case class ResponseBody(message: String)

case class TaskBody(a: String, b: Int)
