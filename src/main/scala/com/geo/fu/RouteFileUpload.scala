package com.geo.fu

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.Config

import scala.util.Failure
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-6:49 AM
 */
class RouteFileUpload(config: Config)(implicit val system: ActorSystem[_]){
  implicit val ec = system.executionContext
  case class responseMessage(code: String, message: String)
  implicit val responseFormat = jsonFormat2(responseMessage)
  implicit val responseGetObjectList = jsonFormat3(ResponseObjectList)

  val uploadFile =
    concat(path("api" / "cdn-upload") {
      parameters("folderType", "id") {
        (folderType, id) =>
          entity(as[Multipart.FormData]) { formData =>

            val allPartsF: Future[Map[String, Any]] = formData.parts.mapAsync[(String, Any)](1) {

              case b: BodyPart if b.name == "cdnFiles" =>
                b.filename match {
                  case Some(filename) => {
                    val minioSink = new MinioSink(folderType,filename, config, id)
                    b.entity.dataBytes.runWith(minioSink)
                    Future {"00"->"SUCCESS"}
                  }
                  case _ =>
                    system.log.info("Cannot find file name")
                    Future {"10"->"Cannot find file name"}
                }

              case b: BodyPart =>
                b.toStrict(2.seconds).map(strict =>
                  (b.name -> strict.entity.data.utf8String))

            }.runFold(Map.empty[String, Any])((map, tuple) => map + tuple)

            onSuccess(allPartsF) { allParts =>

              val result = allParts.keySet.find(p=> p != "10")
              result match {
                case Some(_) => complete(responseMessage("00","SUCCESS"))
                case _ => complete(responseMessage("10","One Of data is failed to upload"))
              }

            }
          }
      }
    },
      path("api" / "cdn-get-datalist") {
        get{
          parameters("folderType", "id") {
            (folderType, id) => {
              val minioGetObjectList = new MinioGetObjectList(config)
              minioGetObjectList.getObjectList(id.toLong, folderType) match {
                case Success(data) => complete(data)
                case Failure(_) => complete(ResponseObjectList("10","Failed",None))
              }
            }
          }
        }
    })
}
