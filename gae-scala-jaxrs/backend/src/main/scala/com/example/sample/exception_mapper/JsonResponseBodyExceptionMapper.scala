package com.example.sample.exception_mapper

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response.{Status, StatusType}
import javax.ws.rs.core.{Context, MediaType, Response, UriInfo}
import javax.ws.rs.ext.{ExceptionMapper, Provider}

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/*
 * Error発生時にResponseをJsonで返したいみたいな場合
 * これなぜか UnrecognizedPropertyException（かな？）をキャッチしない
 * おそらくReaderInterceptor（エンティティの加工時）でThrowされたやつがだめ？？
 *
 * とか思っていたいけど原因は
 * https://github.com/FasterXML/jackson-jaxrs-providers/search?utf8=%E2%9C%93&q=ExceptionMapper&type=
 * 既にソレに対してMapperが用意されていることが原因のようだ
 *
 * その為、同一のmapperを用意する or ResourceConfig内で対象となるパッケージを追加(mapperがあるのはcom.fasterxml.jackson.jaxrs以下なのでそれを)する
 * いっぱいMapper作るのメンドイので後者選択
 * どうしても特定のExceptionをHandlingしたいときだけMapper上書きすれば良いんじゃないかなー
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
