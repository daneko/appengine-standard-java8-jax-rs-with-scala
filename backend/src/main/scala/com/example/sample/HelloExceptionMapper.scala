package com.example.sample

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response.{Status, StatusType}
import javax.ws.rs.core.{Context, MediaType, Response, UriInfo}
import javax.ws.rs.ext.{ExceptionMapper, Provider}

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/*
 * Error発生時にResponseをJsonで返したいみたいな場合
 * これなぜか UnrecognizedPropertyException（かな？）をキャッチしない
 * おそらくReaderInterceptor（エンティティの加工時）でThrowされたやつがだめ？？
 */
@Provider
class HelloExceptionMapper(@Context private val uriInfo: UriInfo) extends ExceptionMapper[Throwable] {


  override def toResponse(exception: Throwable): Response = {
    def converter(status: StatusType = Status.INTERNAL_SERVER_ERROR, e: Throwable): Response = {
      Response.status(status)
        .entity(ErrorBody(status, uriInfo, e))
        .`type`(MediaType.APPLICATION_JSON)
        .build()
    }

    exception match {
      case e: WebApplicationException => converter(e.getResponse.getStatusInfo, e)
      case e => converter(e = e)
    }
  }
}

@JsonNaming(classOf[PropertyNamingStrategy.SnakeCaseStrategy])
case class ErrorBody(statusCode: Int,
                     statusType: String,
                     message: String,
                     forDeveloper: ErrorBodyForDeveloper)

case class ErrorBodyForDeveloper(
                                  message: String,
                                  path: String)

object ErrorBody {
  def apply(status: StatusType,
            uriInfo: UriInfo,
            e: Throwable): ErrorBody = {
    ErrorBody(status.getStatusCode, status.getFamily.name(), status.getReasonPhrase,
      ErrorBodyForDeveloper(e.getMessage, uriInfo.getAbsolutePath.toASCIIString))
  }
}
