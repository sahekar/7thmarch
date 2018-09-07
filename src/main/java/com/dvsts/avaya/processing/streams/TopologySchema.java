package com.dvsts.avaya.processing.streams;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.processors.TestProcessor;
import com.dvsts.avaya.processing.processors.TransfomationProcessor;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.StoreSupplier;
import org.apache.kafka.streams.state.Stores;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.db;

public class TopologySchema {

    private Properties properties;
    private String kafkaRegistry;
    private String bootstrapServers;
  final private String transformationProcessor = "transformationProcessor";


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

                .addProcessor(transformationProcessor, TransfomationProcessor::new,"Source")

                .addStateStore(initStore(),transformationProcessor)

                .addSink("ChangeState","trasformation_state_test1",transformationProcessor)

                // link store to first processor
                .connectProcessorAndStateStores(transformationProcessor,db);

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
        Serde serde = Serdes.serdeFrom(new KafkaAvroSerializer(schemaRegistryClient()),new KafkaAvroDeserializer(schemaRegistryClient()));

        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(db),stringSerde,serde);
    }

        public SchemaRegistryClient schemaRegistryClient() {
            return new CachedSchemaRegistryClient(kafkaRegistry, 2);
        }


    private Serializer createSerializer(){
        Serializer serializer = new KafkaAvroSerializer();
        Map<String, Object> serProps = new HashMap<>();
        serProps.put("schema.registry.url",kafkaRegistry);
        serializer.configure(serProps,false);

        return serializer;
    }


}
