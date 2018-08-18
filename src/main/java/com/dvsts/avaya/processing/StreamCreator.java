package com.dvsts.avaya.processing;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class StreamCreator {

    private Properties properties;
    private String schemaRegistry;
    private String bootstrapServers;

    public StreamCreator( Properties properties) {
       this.schemaRegistry = properties.getProperty("kafka.schema.registry.url");
       this.bootstrapServers = properties.getProperty("camel.component.kafka.brokers");

    }


    public void streamtoTable(String topicIn,String topicOut){


        final String tableName = "";
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",schemaRegistry);
        final Serde<String> stringSerde = Serdes.String();
        final Serde<GenericRecord> genericAvroSerde = new GenericAvroSerde();

        genericAvroSerde.configure(serdeConfig,false);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String,GenericRecord> stream = builder.stream(topicIn);
        final KTable<String,GenericRecord> table = builder.table(tableName);

        stream.map((k,v) -> KeyValue.pair(v.get("ssrc1") +(String) v.get("ssrc1"),v))
               .to(topicOut, Produced.with(stringSerde,genericAvroSerde));

        KafkaStreams streams = new KafkaStreams(builder.build(),createProps());
        streams.start();
    }




    private  Properties createProps(){
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test4-avayaee");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  GenericAvroSerde.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,schemaRegistry);

        return props;
    }
}
