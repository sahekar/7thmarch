package com.dvsts.avaya.processing;

import com.dvsts.avaya.processing.processors.TestProcessor;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.StoreSupplier;
import org.apache.kafka.streams.state.Stores;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class TopologySchema {

    private Properties properties;
    private String kafkaRegistry;
    private String bootstrapServers;

    public TopologySchema( Properties properties) {
        this.kafkaRegistry = properties.getProperty("kafka.schema.registry.url");
        this.bootstrapServers = properties.getProperty("camel.component.kafka.brokers");
    }

    public void createSimpleStorage() {
        SchemaRegistryClient schemaRegistryClient =    new CachedSchemaRegistryClient(kafkaRegistry, 2);
        GenericAvroDeserializer avroDeserializer = new GenericAvroDeserializer();
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",kafkaRegistry);
        StringDeserializer stringDeserializer = new StringDeserializer();

        avroDeserializer.configure(serdeConfig,false);

        Topology builder = new Topology();

        builder

                .addSource("Source",stringDeserializer,avroDeserializer,"test4_avaya")

                .addProcessor("NewCallProcessor", TestProcessor::new,"Source")

                .addStateStore(initStore(),"NewCallProcessor")

                .addSink("ChangeState","change_state_test","NewCallProcessor")

                // link store to first processor
                .connectProcessorAndStateStores("NewCallProcessor","test");

        KafkaStreams streams = new KafkaStreams(builder,createProps());

        streams.start();

    }

    private Properties createProps(){
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test4-avayadee");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  GenericAvroSerde.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaRegistry);

        return props;
    }

    private StoreBuilder initStore(){

        final Serde<String> stringSerde = Serdes.String();
        final Serde<GenericRecord> genericAvroSerde = new GenericAvroSerde();
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",kafkaRegistry);
        genericAvroSerde.configure(serdeConfig,false);

        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore("test"),stringSerde,genericAvroSerde);
    }


}
