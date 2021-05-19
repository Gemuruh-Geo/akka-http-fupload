package com.geo.fu

import java.io.File

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{FileIO, Sink}

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers
import com.typesafe.config.Config

/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-6:49 AM
 */
class RouteFileUpload(config: Config)(implicit val system: ActorSystem[_]){
  implicit val ec = system.executionContext
  case class responseMessage(code: String, message: String)
  implicit val responseFormat = jsonFormat2(responseMessage)



  val uploadFile =
    path("api" / "cdn-upload") {
      parameters("folderName".optional) {
        (folderName) =>
          entity(as[Multipart.FormData]) { formData =>

            val allPartsF: Future[Map[String, Any]] = formData.parts.mapAsync[(String, Any)](1) {

              case b: BodyPart if b.name == "cdnFiles" =>
                b.filename match {
                  case Some(filename) => {
                    val file = new File("/Users/gemuruhgeopratama/Desktop/testfile/"+filename)
                    system.log.info(file.toPath.toString)
                    val minioSink = new MinioSink(folderName,filename, config)
                    /*b.entity.dataBytes.runWith(FileIO.toPath(file.toPath)).map(_ =>
                      (b.name -> file))*/

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
    }
}
