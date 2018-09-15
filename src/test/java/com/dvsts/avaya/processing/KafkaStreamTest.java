package com.dvsts.avaya.processing;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.transformers.*;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;

import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.db;
import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;
import static com.dvsts.avaya.processing.KafkaStreamConfigTest.inputSchema;
import static com.dvsts.avaya.processing.KafkaStreamConfigTest.outputSchema;


public class KafkaStreamTest {

    private TopologyTestDriver testDriver;
    private StringDeserializer stringDeserializer = new StringDeserializer();
    private LongDeserializer longDeserializer = new LongDeserializer();
    private SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
    private final Serde<GenericRecord> genericAvroSerde = createConfiguredSerdeForRecordValues();
    private AvroTransformer transformer = new AvroTransformer(schemaRegistryClient());
    private MainComputationModel mainComputationModel = new MainComputationModel();
    public GenericAvroSerializer genericAvroSerializer = new GenericAvroSerializer();
    private ConsumerRecordFactory<String, GenericRecord> recordFactory;
    private final String INPUT = "test";

    @BeforeEach
    public void setUp() throws IOException, RestClientException {

        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "ks-papi-stock-analysis-client");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ks-papi-stock-analysis-group");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ks-stock-analysis-appid");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,"http://fake");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  GenericAvroSerde.class);


        final Serde<String> stringSerde = Serdes.String();

        registerSchema(schemaRegistryClient, inputSchema,INPUT);
        registerSchema(schemaRegistryClient, outputSchema,detailsEventTopic);


        final Map<String, String> serdeConfig1 = Collections.singletonMap("schema.registry.url","http://fake");
         genericAvroSerializer.configure(serdeConfig1,false);



        StreamsBuilder builder = new StreamsBuilder();
        builder.addStateStore(initStore());
    // TODO: add late    builder.addStateStore(initStore());

        KStream<String,GenericRecord> stream = builder.stream(INPUT, Consumed.with(stringSerde,genericAvroSerde));


        stream.transform(() -> new AvayaPacketTransformer(transformer, mainComputationModel), db)
                .to(detailsEventTopic, Produced.with(stringSerde,genericAvroSerde));

        Topology topology = builder.build();
       recordFactory = new ConsumerRecordFactory<>(INPUT,new StringSerializer(),  genericAvroSerde.serializer());
       testDriver = new TopologyTestDriver(topology, props);

    }

    private  GenericAvroSerde createConfiguredSerdeForRecordValues() {

        GenericAvroSerde serde = new GenericAvroSerde(schemaRegistryClient);
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url","http://fake");
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

       return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(db),stringSerde,serde);
   }

    @Test
    public void shouldFlushStoreForFirstInput() {


        Map<String,Object> packet = createPcrfPacket();

         GenericRecord record = transformer.toEventAvroRecord(packet,INPUT);

        //System.out.println((String) record.get("gaploss"));
           testDriver.pipeInput(recordFactory.create(record));
           GenericRecord result =  testDriver.readOutput(detailsEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();


          Assert.assertEquals(1,result.get("alarm"));

        KeyValueStore store = testDriver.getKeyValueStore(db);

      AvayaPacket packet1 = (AvayaPacket)  store.get("dddfdfdf");

        System.out.println("packet: "+packet1);


     //   OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);

      //  Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
    }


    private  Map<String,Object> createPcrfPacket(){
        Map<String,Object> map = new HashMap<>();

        map.put("ssrc1","ddd");
        map.put("ssrc2","fdfdf");
        map.put("jitter","1L");
        map.put("rtt","2");
        map.put("loss","3");
        map.put("cumulativeloss","4");
        map.put("time",5L);
        map.put("lsr","6");
        map.put("dlsr","7");
        map.put("codec","fdf");
        map.put("sr","fdf");
        map.put("name1","fdf");
        map.put("name2","fdf");
        map.put("transpondername","fdf");
        map.put("type1","fdf");
        map.put("type2","fdf");
        map.put("reportedip","fdf");
        map.put("reportedport","fdf");
        map.put("owd","fdf");
        map.put("burstloss","fdf");
        map.put("burstdensity","fdf");
        map.put("gaploss","fdf");
        map.put("gapdensity","8");
        return map;
    }

}
