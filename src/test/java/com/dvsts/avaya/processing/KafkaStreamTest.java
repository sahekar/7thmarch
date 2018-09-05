package com.dvsts.avaya.processing;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.streams.TopologySchema;
import com.dvsts.avaya.processing.transformers.*;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
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
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.KafkaStreamConfigTest.inputSchema;
import static com.dvsts.avaya.processing.KafkaStreamConfigTest.outputSchema;
import static com.dvsts.avaya.processing.streams.TopologySchema.db;

public class KafkaStreamTest {

    private TopologyTestDriver testDriver;
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private LongDeserializer longDeserializer = new LongDeserializer();
    private SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
    private final Serde<GenericRecord> genericAvroSerde = createConfiguredSerdeForRecordValues();
    private AvroTransformer transformer = new AvroTransformer(schemaRegistryClient());
    private MainComputationModel mainComputationModel = new MainComputationModel();
    private ConsumerRecordFactory<String, GenericRecord> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), genericAvroSerde.serializer());


    private final String INPUT = "test";
    private final String OUTPUT = "test-out";

    @BeforeEach
    public void setUp() throws IOException, RestClientException {

        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "ks-papi-stock-analysis-client");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ks-papi-stock-analysis-group");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ks-stock-analysis-appid");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,"fake");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  GenericAvroSerde.class);


        final Serde<String> stringSerde = Serdes.String();
        registerSchema(schemaRegistryClient, inputSchema,INPUT);
        registerSchema(schemaRegistryClient, outputSchema,OUTPUT);



        StreamsBuilder builder = new StreamsBuilder();
        builder.addStateStore(initStore());
    // TODO: add late    builder.addStateStore(initStore());

        KStream<String,GenericRecord> stream = builder.stream(INPUT);

        stream.transform(() -> new AvayaPacketTransformer(transformer, mainComputationModel), db)
                .to(OUTPUT, Produced.with(stringSerde,genericAvroSerde));

        Topology topology = builder.build();


       testDriver = new TopologyTestDriver(topology, props);
    }

    private  GenericAvroSerde createConfiguredSerdeForRecordValues() {

        GenericAvroSerde serde = new GenericAvroSerde(schemaRegistryClient);
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url","fake");
        serde.configure(serdeConfig, false);
        return serde;
    }

    public SchemaProvider schemaRegistryClient() {
        SchemaProvider provider = new SchemaProvider(schemaRegistryClient,1);
        return provider;
    }



    private void registerSchema(SchemaRegistryClient schemaRegistryClient,String schema,String topic) throws IOException, RestClientException {
        org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
        org.apache.avro.Schema avroSchema = parser.parse(schema);
        schemaRegistryClient.register(topic+"-value",
                avroSchema);

    }

   /* @After
    public void tearDown() {
        testDriver.close();
    }*/
   private StoreBuilder initStore(){

       final Serde<String> stringSerde = Serdes.String();
       Map<String, Object> serdeProps = new HashMap<>();
       final Serializer<AvayaPacket> jsonPOJOSerializer = new JsonPOJOSerializer<>();
       serdeProps.put("JsonPOJOClass", AvayaPacket.class);
       jsonPOJOSerializer.configure(serdeProps, false);
       final Deserializer<AvayaPacket> jsonDeserializer = new JsonPOJODeserializer<>();
       jsonDeserializer.configure(serdeProps,false);
       final Serde<AvayaPacket> serde = Serdes.serdeFrom(jsonPOJOSerializer, jsonDeserializer);

       return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(TopologySchema.db),stringSerde,serde);
   }

    @Test
    public void shouldFlushStoreForFirstInput() {

        Map<String,Object> map = new HashMap<>();

        map.put("ssrc1","ddd");
        map.put("ssrc2","fdfdf");
        map.put("jitter","0");

        GenericRecord record = transformer.toAvroRecord(map,INPUT);
        //  testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, new GenericRecord()));
        // GenericRecord record = AvroUtils.createGenericRecord();

        OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);

        Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
    }


}
