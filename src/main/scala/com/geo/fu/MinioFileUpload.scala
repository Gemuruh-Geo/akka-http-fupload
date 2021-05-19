package com.geo.fu

import java.io.ByteArrayInputStream

import akka.util.ByteString
import com.typesafe.config.Config
import io.minio.{BucketExistsArgs, MinioClient}
import org.slf4j.LoggerFactory

import scala.util.Try

/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-11:21 AM
 */
class MinioFileUpload( folderName: Option[String], fileName: String, config: Config) {
  val log = LoggerFactory.getLogger(getClass)
  def doUploadToMinio(byteString: ByteString): Try[String] = Try{
    val bucketName = config.getString("minio.bucket-name")
    val minioClient = MinioClient.builder()
      .endpoint(config.getString("minio.host"))
      .credentials(
        config.getString("minio.access-key"),
        config.getString("minio.secret-key")
      ).build()

    val found: Boolean = minioClient.bucketExists(BucketExistsArgs.builder.bucket(bucketName).build)
    if(!found) {
      log.info("Create New Bucket, with bucket Name = {}", bucketName)
      import io.minio.MakeBucketArgs
      minioClient.makeBucket(MakeBucketArgs.builder.bucket(bucketName).build)
    }
    val objectName = folderName match {
      case Some(fName) => s"$fName/$fileName"
      case None => fileName
    }
    import io.minio.PutObjectArgs
    val inputStream = new ByteArrayInputStream(byteString.toArray)
    minioClient.putObject(PutObjectArgs.builder.bucket(bucketName).`object`(objectName)
      .stream(inputStream, -1, 10485760).build)
    "SUCCESS"
  }
}
