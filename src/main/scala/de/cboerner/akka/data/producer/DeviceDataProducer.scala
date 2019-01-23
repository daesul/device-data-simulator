package de.cboerner.akka.data.producer

import java.time.Instant

import akka.actor.{Actor, ActorLogging, Props}
import io.circe.generic.auto._
import io.circe.syntax._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._

object DeviceDataProducer {

  def props(): Props = Props(new DeviceDataProducer)

  val Name = "device-data-producer"

  final case class DataRecord(deviceId: Int, temperature: Double, rpm: Int, timestamp: Long = Instant.now().toEpochMilli)


}

final class DeviceDataProducer extends Actor with ActorLogging {

  import context.dispatcher
  import de.cboerner.akka.data.producer.DeviceDataProducer._
  import scala.util.Random

  implicit val mat = ActorMaterializer()


  val config = context.system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")


  val done = Source(1 to 100000)
    .throttle(1, 200.millis)
    .map { i =>
      DataRecord(
        i % 5,
        Random.nextDouble() * 100,
        Random.nextInt(10000)
      )
    }
    .map(dataRecord => dataRecord.asJson)
    .map{value =>
      log.info(s"Value is: $value")
      new ProducerRecord[String, String]("device-data", value.noSpaces)
    }
    .runWith(Producer.plainSink(producerSettings))

  done.onComplete(_ => context.system.terminate())

  override def receive: Receive = Actor.emptyBehavior
}


