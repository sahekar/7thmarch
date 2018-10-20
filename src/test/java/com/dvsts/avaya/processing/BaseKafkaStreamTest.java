package com.dvsts.avaya.processing;

import com.dvsts.avaya.core.domain.event.AppSpecificReport;
import com.dvsts.avaya.core.domain.event.AvayaEvent;
import com.dvsts.avaya.core.domain.event.SenderReport;
import com.dvsts.avaya.core.domain.event.SourceDescription;
import com.dvsts.avaya.processing.streams.TopologySchema;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import com.dvsts.avaya.processing.transformers.SchemaProvider;
import com.dvsts.avaya.processing.utils.JsonUtils;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.initialAvayaSourceTopic;


/**
 * this is the base class where configured mocks for main components in kafka streams
 * - mock for kafka registry
 * - configured topologytestdriver
 *  - configured serdes
 *  -
 *
 */
public abstract class BaseKafkaStreamTest {

    public final SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
    public  AvroTransformer transformer = new AvroTransformer(schemaRegistryClient());
    public GenericAvroSerializer genericAvroSerializer = new GenericAvroSerializer();
    public ConsumerRecordFactory<String, SpecificRecord> recordFactory;
    public TopologyTestDriver testDriver;

    public final Serde<GenericRecord> genericAvroSerde = createConfiguredSerdeForRecordValues();
    public final Serde<SpecificRecord> specificAvroSerde = createConfiguredSpecificAvroSerdeForRecordValues();
    public StringDeserializer stringDeserializer = new StringDeserializer();

    public SchemaProvider schemaRegistryClient() {
        SchemaProvider provider = new SchemaProvider(schemaRegistryClient,1);
        return provider;
    }

    public void init(){
        final Properties props = createKafkaProperties();
        final Serde<String> stringSerde = Serdes.String();

        final Map<String, String> serdeConfig1 = Collections.singletonMap("schema.registry.url","http://fake");
        genericAvroSerializer.configure(serdeConfig1,false);



        Properties properties =new Properties();
        properties.put("kafka.schema.registry.url","fake");
        properties.put("camel.component.kafka.brokers","dat");

        TopologySchema topologySchema = new TopologySchema(properties);
        Topology topology = topologySchema.createTopology(schemaRegistryClient,specificAvroSerde.deserializer(),specificAvroSerde.serializer());

        recordFactory = new ConsumerRecordFactory<>(initialAvayaSourceTopic,new StringSerializer(),  specificAvroSerde.serializer());
        testDriver = new TopologyTestDriver(topology, props);

    }

    private  GenericAvroSerde createConfiguredSerdeForRecordValues() {

        GenericAvroSerde serde = new GenericAvroSerde(schemaRegistryClient);
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url","http://fake");
        serde.configure(serdeConfig, false);
        return serde;
    }

    private  SpecificAvroSerde<SpecificRecord> createConfiguredSpecificAvroSerdeForRecordValues() {

        SpecificAvroSerde<SpecificRecord> serde = new SpecificAvroSerde(schemaRegistryClient);
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url","http://fake");
        serde.configure(serdeConfig, false);
        return serde;
    }


    public  void registerSchema(SchemaRegistryClient schemaRegistryClient,String schema,String topic) throws IOException, RestClientException {
        org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
        org.apache.avro.Schema avroSchema = parser.parse(schema);
        schemaRegistryClient.register(topic+"-value",
                avroSchema);

    }

    public SpecificRecord getInitialAvayaEventSide1() throws IOException, URISyntaxException {

        AvayaEvent event = new AvayaEvent();
        event.setClientid(1L);
        event.setSsrc1("88979");
        event.setSsrc2("78846");
        event.setIp("11.11.21");
        event.setSubtype(5);
        event.setRemoteport(5020);
        event.setTime(5L);
        event.setHopnamelookup(true);
        event.setGetcalltime(LocalDateTime.now().minusSeconds(25).toEpochSecond(ZoneOffset.UTC));

        SenderReport senderReport = new SenderReport();

        senderReport.setJitter("55");
        senderReport.setLoss("5");
        senderReport.setCumulativepktloss(554);
        senderReport.setEhsnr(75);

        AppSpecificReport appSpecificReport = new AppSpecificReport();

        appSpecificReport.setRtd("45");
        appSpecificReport.setPayloadtype("payloadtype1");

        SourceDescription sourceDescription = new SourceDescription();
        sourceDescription.setType("phone1");
        sourceDescription.setName("test1");


        event.setSenderReport(senderReport);
        event.setSourceDescription(sourceDescription);
        event.setAppSpecificReport(appSpecificReport);


        System.out.println("result: "+event);

        return event;
    }

    public SpecificRecord getInitialAvayaEventSide2(String ssrc1, String ssrc2) throws IOException, URISyntaxException {

        AvayaEvent event = new AvayaEvent();
        event.setClientid(1L);
        event.setSsrc1(ssrc1);
        event.setSsrc2(ssrc2);
        event.setIp("12.12.21");
        event.setSubtype(5);
        event.setRemoteport(5020);
        event.setTime(5L);
        event.setHopnamelookup(true);
        event.setGetcalltime(LocalDateTime.now().minusSeconds(25).toEpochSecond(ZoneOffset.UTC));

        SenderReport senderReport = new SenderReport();

        senderReport.setJitter("75");
        senderReport.setLoss("25");
        senderReport.setCumulativepktloss(554);
        senderReport.setEhsnr(75);

        AppSpecificReport appSpecificReport = new AppSpecificReport();

        appSpecificReport.setRtd("78");
        appSpecificReport.setPayloadtype("payloadtype2");

        SourceDescription sourceDescription = new SourceDescription();
        sourceDescription.setType("phone2");
        sourceDescription.setName("test2");

        event.setSenderReport(senderReport);
        event.setSourceDescription(sourceDescription);
        event.setAppSpecificReport(appSpecificReport);

        return event;
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
}
