package com.dvsts.avaya.processing;

import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.transformers.AvayaPacketTransformer;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import com.dvsts.avaya.processing.transformers.SchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaStreamTest {

    private TopologyTestDriver testDriver;
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private LongDeserializer longDeserializer = new LongDeserializer();
    private SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
    private final Serde<GenericRecord> genericAvroSerde = createConfiguredSerdeForRecordValues();
    private AvroTransformer transformer = new AvroTransformer(schemaRegistryClient());
    private MainComputationModel mainComputationModel = new MainComputationModel();
    private ConsumerRecordFactory<String, GenericRecord> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), genericAvroSerde);

    private final String INPUT = "test";

    @BeforeEach
    public void setUp() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "ks-papi-stock-analysis-client");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ks-papi-stock-analysis-group");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ks-stock-analysis-appid");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  GenericAvroSerde.class);

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url","dummy:8081");
        final Serde<String> stringSerde = Serdes.String();



        StreamsBuilder builder = new StreamsBuilder();
    // TODO: add late    builder.addStateStore(initStore());

        KStream<String,GenericRecord> stream = builder.stream("inpue");

        stream.transform(() -> new AvayaPacketTransformer(transformer, mainComputationModel),TopologySchema.db)
                .to("", Produced.with(stringSerde,genericAvroSerde));

        Topology topology = builder.build();


       testDriver = new TopologyTestDriver(topology, props);
    }

    private  GenericAvroSerde createConfiguredSerdeForRecordValues() {

        GenericAvroSerde serde = new GenericAvroSerde(schemaRegistryClient);
        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "fake");
        serde.configure(serdeConfig, false);
        return serde;
    }

    public SchemaProvider schemaRegistryClient() {
       ;
        SchemaProvider provider = new SchemaProvider(schemaRegistryClient,1);
        return provider;
    }

    @Test
    public void shouldFlushStoreForFirstInput() {

      //  testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, new GenericRecord()));



        OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);

        Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
    }


   /* @After
    public void tearDown() {
        testDriver.close();
    }*/

}
