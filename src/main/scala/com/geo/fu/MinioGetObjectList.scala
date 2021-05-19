package com.geo.fu

import com.typesafe.config.Config
import io.minio.messages.Item
import io.minio.{BucketExistsArgs, ListObjectsArgs, MinioClient, Result}
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters._

import scala.util.Try

/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-4:55 PM
 */
class MinioGetObjectList(config: Config) {
  val log = LoggerFactory.getLogger(getClass)
  case class ResponseObjectList(code: String, message: String)

  def getObjectList(id: Long, folderType: String): Try[ResponseObjectList] = Try{
    val bucketName = config.getString("minio.bucket-name")
    val minioClient = MinioClient.builder()
      .endpoint(config.getString("minio.host"))
      .credentials(
        config.getString("minio.access-key"),
        config.getString("minio.secret-key")
      ).build()

    val found: Boolean = minioClient.bucketExists(BucketExistsArgs.builder.bucket(bucketName).build)
    found match {
      case false => {
        log.info("Object not found")
        ResponseObjectList("10","Object Not Found")
      }
      case _ => {
        log.info("Start To Fetch Object List")
        val iterableObjectList = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(folderType).recursive(true).build()).asScala
      }
    }
  }
}
