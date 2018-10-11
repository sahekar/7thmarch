package com.dvsts.avaya.processing.processors;


import com.dvsts.avaya.processing.BaseKafkaStreamTest;
import com.dvsts.avaya.processing.utils.JsonUtils;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static com.dvsts.avaya.processing.AppConfig.*;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.outputSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class SessionCreatorProcessorTest extends BaseKafkaStreamTest {

 /*   @Mock
    private KeyValueStore<String,AvayaPacket> kvStore;
    @InjectMocks
    private SessionCreatorProcessor sessionCreatorProcessor;*/

    @BeforeEach
    public void setUp() throws IOException, RestClientException, URISyntaxException {
        MockitoAnnotations.initMocks(this);

        init();

        String inputSchema = JsonUtils.getJsonString("/avro-shema/initial_avaya_event_avro_schema.json");
        String sideSchema = JsonUtils.getJsonString("/avro-shema/side_schema.json");
        String sessionSchema = JsonUtils.getJsonString("/avro-shema/session_schema.json");


        registerSchema(schemaRegistryClient, inputSchema,initialAvayaSourceTopic);
        registerSchema(schemaRegistryClient, sideSchema,detailsEventTopic);
        registerSchema(schemaRegistryClient, sessionSchema,sessionEventTopic);

    }

    @Test
    public void test() {
        long now = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        System.out.println(now);
    }


    @Test
    public void simpleSessionCreate() throws IOException, URISyntaxException, InterruptedException {


        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide1()));
        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide1()));

        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide2()));
        GenericRecord result =  testDriver.readOutput(sessionEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();
        GenericRecord result2 = testDriver.readOutput(sessionEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();
        GenericRecord result3 = testDriver.readOutput(sessionEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();

        assertEquals(result.get("sessionindex"), "88979788461");
        assertNotNull(result.get("insertdata"));
        assertNotNull(result.get("avgloss"));
        System.out.println(result.get("avgjitter"));
        System.out.println(result.get("avgmos"));
        System.out.println(result.get("avgrtd"));
        assertEquals("11.11.21", result.get("ip1"));
        assertEquals("12.12.21", result3.get("ip2"));
        assertEquals("test1", result.get("name1"));
        assertEquals("test2", result3.get("name2"));
        assertEquals("payloadtype1", result.get("payloadtype1"));
        assertEquals("payloadtype2", result.get("payloadtype2"));
        assertEquals("phone1", result.get("type1"));
        assertEquals("phone2", result.get("type2"));

        assertEquals("25", result.get("maxloss"));
        assertEquals(true, result.get("active"));

        assertEquals(true, result.get("active"));

        System.out.println(result.get("duration"));


    }

    @Test
    public void simpleWithOneSession() {

    }


    private GenericRecord createSide1GenericRecord(){
        Schema schema = new Schema.Parser().parse(outputSchema);
        GenericRecord record = new GenericData.Record(schema);
         record.put("id","1234");
         record.put("ssrc1","564789");
         record.put("ssrc2","987456");
        record.put("jitter",1234);
        record.put("rtd",5);
        record.put("rtp", 5);
        record.put("loss",510);
        record.put("mos",2F);
        record.put("alarm",5);
        //Schema childSchema = record.getSchema().getField("friends").schema().getElementType();

        return  record;
    }

    private Map<String,Object> createPcrfPacket(){
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
