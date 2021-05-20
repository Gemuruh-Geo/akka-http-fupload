package com.geo.fu

import akka.stream.stage.{AbstractInHandler, GraphStage, GraphStageLogic}
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.util.ByteString
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-10:36 AM
 */
class MinioSink(folderName: String, fileName: String, config: Config, id: String) extends GraphStage[SinkShape[ByteString]]{

  val log = LoggerFactory.getLogger(getClass)
  val in: Inlet[ByteString] = Inlet.create("MinioSink.in")

  val minioFileUpload = new MinioFileUpload(folderName,fileName, config, id)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      override def preStart(): Unit = pull(in)

      setHandler(in, new AbstractInHandler {
        override def onPush(): Unit = {

          log.info("Handle Sink")

          val byteString: ByteString = grab(in);
          //logic to save data to minio place here
          minioFileUpload.doUploadToMinio(byteString)
          pull(in)
        }
      })
    }
  }

  override def shape: SinkShape[ByteString] = SinkShape.of(in)
}
