package de.cboerner.akka.data.producer

import akka.actor.ActorSystem

object Main {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("device-simulator")
    system.actorOf(DeviceDataProducer.props(), DeviceDataProducer.Name)
  }

}
