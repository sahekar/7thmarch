package com.dvsts.avaya.processing.processors;


import com.dvsts.avaya.core.domain.event.AvayaEvent;
import com.dvsts.avaya.core.domain.session.AvayaSideEvent;
import com.dvsts.avaya.core.domain.session.Session;
import com.dvsts.avaya.processing.BaseKafkaStreamTest;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.dvsts.avaya.processing.AppConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TODO: think how to refresh rockdb after finishing test
 */
public class SessionCreatorProcessorTest extends BaseKafkaStreamTest {


    @BeforeEach
    public void setUp() throws IOException, RestClientException, URISyntaxException {
        MockitoAnnotations.initMocks(this);

        init();

        String inputSchema = AvayaEvent.getClassSchema().toString(true);
        String sideSchema = AvayaSideEvent.getClassSchema().toString(true);
        String sessionSchema = Session.getClassSchema().toString(true);


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

        String ssrc1 = "78846";
        String ssrc2 = "88979";
        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventWindSenderReportSide1()));
        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventWindSenderReportSide1()));

        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide2(ssrc1, ssrc2)));
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
        assertEquals("payloadtype2", result3.get("payloadtype2"));
        assertEquals("phone1", result.get("type1"));
        assertEquals("phone2", result3.get("type2"));

        assertEquals("25", result3.get("maxloss"));
        assertEquals(true, result.get("active"));

        System.out.println(result.get("duration"));


    }

    @Test
    public void OneSessionCreateWithBiggestSsrc2() throws IOException, URISyntaxException, InterruptedException {
        String ssrc1 = "78847";
        String ssrc2 = "88979";

        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide2(ssrc1, ssrc2)));
        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide2(ssrc1, ssrc2)));
        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventSide2(ssrc1, ssrc2)));
        GenericRecord result = testDriver.readOutput(sessionEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();
        GenericRecord result2 = testDriver.readOutput(sessionEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();
        GenericRecord result3 = testDriver.readOutput(sessionEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();

        assertEquals(result3.get("sessionindex"), "88979788471");


    }

    @Test
    public void avgMetrics() {


    }




    @AfterEach
    public void tearDown() throws IOException {

        try{
            testDriver.close();// Close processors after finish the tests
        } catch (Exception e) {
            FileUtils.cleanDirectory(new File("\\tmp\\kafka-streams\\ks-stock-analysis-appid\\"));

        }



    }
}
