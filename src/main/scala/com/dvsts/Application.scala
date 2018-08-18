package com.dvsts

import java.util.{Collections, Properties}

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}

import scala.util.Properties

object Application {

  def main(args: Array[String]): Unit ={

    val inTopic = "test4_avaya"

     val  builder = new StreamsBuilder
     val streamsConfig: Properties = {
         val p = new Properties()
       p.put(StreamsConfig.APPLICATION_ID_CONFIG, "test4-avayaeesfs")
       p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "94.130.90.122:9092")
       p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[GenericAvroSerde])
       p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[GenericAvroSerde])

       p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
       p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://94.130.90.122:8081")

       p
     }

    implicit val genericAvroSerde: Serde[GenericRecord] = {
       val  gas  = new GenericAvroSerde
       val isKeySerde: Boolean = false
       gas.configure(Collections.singletonMap("schema.registry.url", "http://94.130.90.122:8081"),false)
       gas
    }

    builder.stream[String,GenericRecord](inTopic)
      .print(_)
          // .flatMapValues(v => v)
           //.filter((a,b) => false)
          // .to("ff")

    val streams: KafkaStreams = new KafkaStreams(builder.build(),streamsConfig)
    streams.start()
    
  }
  
}
