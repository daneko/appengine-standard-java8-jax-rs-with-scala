package com.example.sample

import java.util.Collections
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import javax.ws.rs.core.{Context, MediaType}
import javax.ws.rs.ext.{ContextResolver, Provider, Providers}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.appengine.api.blobstore.{BlobstoreService, BlobstoreServiceFactory, UploadOptions}
import com.google.appengine.api.images.{ImagesServiceFactory, ServingUrlOptions}
import com.google.appengine.api.taskqueue.{QueueFactory, TaskOptions}
import com.google.appengine.api.utils.SystemProperty
import com.google.appengine.tools.cloudstorage.{GcsFileOptions, GcsFilename, GcsServiceFactory, RetryParams}
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig

import scala.collection.JavaConverters._

/**
  */
@ApplicationPath("/")
class ApplicationResource extends ResourceConfig {
  packages(this.getClass.getPackage.getName)
  register(classOf[JacksonFeature])
}

@Path("sample")
class CallWorkerApi {

  val logger = Logger.getLogger("sample")

  @Path("pin")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def pin(): ResponseBody = {
    ResponseBody("pon")
  }

  @Path("pin")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def pinPost(taskBody: TaskBody): ResponseBody = {
    ResponseBody("pon")
  }

  val bucket = SystemProperty.applicationId.get() + ".appspot.com"

  private val blobstoreService: BlobstoreService = BlobstoreServiceFactory.getBlobstoreService

  @GET
  @Path("upload_url")
  def getUploadUrl() = {
    logger.info(s"get upload url bucket : ${SystemProperty.applicationId.get()}")
    val megaByte: Long = 1024 * 1024
    val uploadOptions = UploadOptions.Builder
      .withGoogleStorageBucketName(bucket + "/test")
      .maxUploadSizeBytes(100 * megaByte)

    blobstoreService.createUploadUrl("/sample/upload_callback", uploadOptions)
  }

  @POST
  @Path("upload_callback")
  def getUploadCallback(@Context request: HttpServletRequest) = {

    logger.info("upload_callback")

    logger.info("check header")

    Collections.list(request.getHeaderNames).asScala
      .map(h => h.asInstanceOf[String])
      .foreach(name => logger.info(s"$name : ${request.getHeader(name)}"))


    val fileInfoMap = blobstoreService.getFileInfos(request)

    logger.info("check file info")

    fileInfoMap.asScala.foreach {
      case (s, l) => logger.info(s"key : $s")
        l.forEach { finfo =>
          logger.info(s"fInfo: ${finfo}")
        }
    }
    val fileinfo = fileInfoMap.get("image").get(0)

    val gcsFilename = new GcsFilename(bucket, fileinfo.getGsObjectName.substring(s"/gs/${bucket}/".length))
    logger.info(s"current gcs file name $gcsFilename")
    val gcsNewFilename = new GcsFilename(bucket, s"sample/${System.currentTimeMillis()}")
    logger.info(s"new gcs file name $gcsNewFilename")
    val service = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance)
    service.copy(gcsFilename, gcsNewFilename)
    logger.info(s"copy done")
    service.update(gcsNewFilename, new GcsFileOptions.Builder().acl("public-read").build())
    logger.info(s"update done")
    // serving url は不要になったら削除しないとならないので一旦動作確認したしコメントアウト
    // 問題はdeleteServingUrlの引数が blob key しかないことなんだけど…
//    val gcsFileNameStr = s"/gs/${gcsNewFilename.getBucketName}/${gcsNewFilename.getObjectName}"
//    val servingUrl = ImagesServiceFactory.getImagesService.getServingUrl(ServingUrlOptions.Builder.withGoogleStorageFileName(gcsFileNameStr))
//    logger.info(s"servingUrl : $servingUrl")

    logger.info("check blob info")
    blobstoreService.getBlobInfos(request).asScala.foreach {
      case (s, l) => logger.info(s"key: $s")
        l.forEach {
          bInfo =>
            logger.info(s"blob info : $bInfo")
            logger.info(s"blob delete")
            blobstoreService.delete(bInfo.getBlobKey)
        }
    }
//    servingUrl
    "ok"
  }

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
