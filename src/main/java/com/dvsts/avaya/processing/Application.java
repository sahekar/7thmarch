package com.dvsts.avaya.processing;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;

public class Application {




    public static void main(String[] args) throws IOException {

        Properties properties =new Properties();
        properties.load(Application.class.getClassLoader().getResourceAsStream("application.properties"));

     //TopologySchema topologySchema = new TopologySchema(properties);

    // topologySchema.createSimpleStorage();

         StreamCreator creator = new StreamCreator(properties);
         creator.streamWithTransformer("test4_avaya",detailsEventTopic);



    }

}
