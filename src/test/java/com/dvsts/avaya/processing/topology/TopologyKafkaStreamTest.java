package com.dvsts.avaya.processing.topology;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.streams.TopologySchema;
import com.dvsts.avaya.processing.transformers.*;
import com.dvsts.avaya.processing.utils.JsonUtils;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.test.ConsumerRecordFactory;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.db;
import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;
import static com.dvsts.avaya.processing.AppConfig.initialAvayaSourceTopic;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.inputSchema;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.outputSchema;

public class TopologyKafkaStreamTest {
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
    private  KeyValueStore store;

    @Before
    public void setUp() throws IOException, RestClientException, URISyntaxException {

        final  Properties props = createKafkaProperties();
        final Serde<String> stringSerde = Serdes.String();

        String inputSchema =  JsonUtils.getJsonString("/avro-shema/initial_avaya_event_avro_schema.json");
        registerSchema(schemaRegistryClient, inputSchema,initialAvayaSourceTopic);
        registerSchema(schemaRegistryClient, outputSchema,detailsEventTopic);

        final Map<String, String> serdeConfig1 = Collections.singletonMap("schema.registry.url","http://fake");
        genericAvroSerializer.configure(serdeConfig1,false);

        Properties properties =new Properties();
        properties.put("kafka.schema.registry.url","fake");
        properties.put("camel.component.kafka.brokers","dat");

        TopologySchema topologySchema = new TopologySchema(properties);
        Topology topology = topologySchema.createTopology(schemaRegistryClient,genericAvroSerde.deserializer(),genericAvroSerde.serializer());


        recordFactory = new ConsumerRecordFactory<>(initialAvayaSourceTopic,new StringSerializer(),  genericAvroSerde.serializer());
        testDriver = new TopologyTestDriver(topology, props);
        this.store = testDriver.getKeyValueStore(db);

    }

    private Properties createKafkaProperties() {
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

        return props;
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
    public void simpleInsertAndOutputEventPrint() throws IOException, URISyntaxException {

        GenericRecord record = getInitialAvayaEvent();

        testDriver.pipeInput(recordFactory.create(record));
        GenericRecord result =  testDriver.readOutput(detailsEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();

        Assert.assertEquals(1,result.get("alarm"));

    }

    @Test
    public void stateStoreSimpleInsertOutputPrint() throws IOException, URISyntaxException {
       GenericRecord record = getInitialAvayaEvent();
       testDriver.pipeInput(recordFactory.create(record));
      AvayaPacket packet1 = (AvayaPacket)  store.get("dddfdfdf");
      Assert.assertEquals("ddd",packet1.getSsrc1());


    }

   /* @Test
    public void sessionCreatorSimpleFlowTest(){

    }*/


    private GenericRecord getInitialAvayaEvent() throws IOException, URISyntaxException {
        String schemaString = JsonUtils.getJsonString("/avro-shema/initial_avaya_event_avro_schema.json");

        Schema schema = new Schema.Parser().parse(schemaString);
        GenericRecord record = new GenericData.Record(schema);
        record.put("clientId",1L);
        record.put("cumulativeloss","4");
        record.put("dlsr","7");
        record.put("hopnamelookup",true);
        record.put("ip","10.10.10");
        record.put("jitter","1L");
        record.put("loss","3");
        record.put("lsr","6");
        record.put("pcktlosspct","fdf");
        record.put("remoteport",5050);
        record.put("requestType",2002);
        record.put("sr","fdf");
        record.put("ssrc1","ddd");
        record.put("ssrc2","fdfdf");
        record.put("subtype",5);
        record.put("time",5L);

        Schema childSchema = record.getSchema().getField("sourceDescription").schema().getTypes().get(1);
        System.out.println(childSchema);
        GenericRecord sourceDescription = new GenericData.Record(childSchema);
        sourceDescription.put("type1","test");

        record.put("sourceDescription",sourceDescription);


        System.out.println("result: "+record);
        return record;
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


    @After
    public void tearDown() throws IOException {

        try{
            testDriver.close();// Close processors after finish the tests
        } catch (Exception e) {
            FileUtils.cleanDirectory(new File("\\tmp\\kafka-streams\\ks-stock-analysis-appid\\"));

        }



    }

  /*  @AfterEach
    public void tearDown() {
        testDriver.getStateStore(db).flush();
        testDriver.close(); // Close processors after finish the tests
    }*/
}
