package com.example.sample.exception_mapper

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response.{Status, StatusType}
import javax.ws.rs.core.{Context, MediaType, Response, UriInfo}
import javax.ws.rs.ext.{ExceptionMapper, Provider}

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/*
 */
@Provider
class JsonResponseBodyExceptionMapper(@Context private val uriInfo: UriInfo) extends ExceptionMapper[Throwable] {

  override def toResponse(exception: Throwable): Response = {
    def converter(status: StatusType = Status.INTERNAL_SERVER_ERROR, e: Throwable): Response = {
      Response.status(status)
        .entity(ErrorBody(status, uriInfo, e))
        .`type`(MediaType.APPLICATION_JSON)
        .build()
    }

    exception match {
      case e: WebApplicationException => converter(e.getResponse.getStatusInfo, e)
      case e: JsonProcessingException => converter(Status.BAD_REQUEST, e)
      case e => converter(e = e)
    }
  }
}

@JsonNaming(classOf[PropertyNamingStrategy.SnakeCaseStrategy])
case class ErrorBody(statusCode: Int,
                     statusType: String,
                     message: String,
                     forDeveloper: ErrorBodyForDeveloper)

@JsonNaming(classOf[PropertyNamingStrategy.SnakeCaseStrategy])
case class ErrorBodyForDeveloper(
                                  throwClassName: String,
                                  message: String,
                                  path: String)

object ErrorBody {
  def apply(status: StatusType,
            uriInfo: UriInfo,
            e: Throwable): ErrorBody = {
    ErrorBody(status.getStatusCode, status.getFamily.name(), status.getReasonPhrase,
      ErrorBodyForDeveloper(e.getClass.getName, e.getMessage, uriInfo.getAbsolutePath.toASCIIString))
  }
}

