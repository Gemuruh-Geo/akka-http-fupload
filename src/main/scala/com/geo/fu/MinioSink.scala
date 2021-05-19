package com.geo.fu

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.stage.{AbstractInHandler, GraphStage, GraphStageLogic}
import akka.util.ByteString

/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-10:36 AM
 */
class MinioSink(objectName: String) extends GraphStage[SinkShape[ByteString]]{

  val in: Inlet[ByteString] = Inlet.create("MinioSink.in")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new AbstractInHandler {
        override def onPush(): Unit = {
          val byteString: ByteString = grab(in);

          //logic to save data to minio place here

          pull(in)
        }
      })
    }
  }

  override def shape: SinkShape[ByteString] = SinkShape.of(in)
}
