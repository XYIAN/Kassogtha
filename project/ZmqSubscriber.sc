#!/usr/bin/env amm

import scala.util.control.NonFatal
import $ivy.`org.zeromq:jeromq:0.5.2`
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMQ.Socket
import org.zeromq.ZContext

/**
  * This is a sanity check script. It runs using Ammonite REPL.
  * It will listen to our convention of port 5562 (outgoing port
  * in Cthulhu) to topic "localization". When Cthulhu publishes 
  * a new localization this app will print it to the console.
  *
  * @param port
  * @param host
  * @param topic
  */
@arg(doc = "Listens to Cthulhu for localization messages")
@main
def main(
    @arg(doc = "The ZMQ publisher port") port: Int = 5562,
    @arg(doc = "The ZMQ publisher host") host: String = "localhost",
    @arg(doc = "The ZMQ topic to listen to") topic: String = "localization") {
  try {
    val context = new ZContext
    val socket = context.createSocket(SocketType.SUB)
    socket.connect(s"tcp://$host:$port")
    socket.subscribe(topic.getBytes(ZMQ.CHARSET))
    println(s"Listening to tcp://$host:$port")
    while (!Thread.currentThread().isInterrupted()) {
      val address = socket.recvStr()
      val contents = socket.recvStr()
      println(s"$address : $contents")
    }

  } catch {
    case NonFatal(e) => println("Boom! An exception occurred :-(")
  }

}