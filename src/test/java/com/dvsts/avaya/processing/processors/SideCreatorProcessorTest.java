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

import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;
import static com.dvsts.avaya.processing.AppConfig.initialAvayaSourceTopic;
import static com.dvsts.avaya.processing.AppConfig.sessionEventTopic;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SideCreatorProcessorTest extends BaseKafkaStreamTest {

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
    public void checkIfWhenGetSenderReportCorrect() throws IOException, URISyntaxException, InterruptedException {

        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventWindSenderReportSide1()));

        AvayaSideEvent result = (AvayaSideEvent)  testDriver.readOutput(detailsEventTopic, stringDeserializer, specificAvroSerde.deserializer()).value();

        assertEquals(new Integer(55),result.getJitter());

    }

    @Test
    public void checkIfWhenGetReceiverReportCorrect() throws IOException, URISyntaxException, InterruptedException {

        testDriver.pipeInput(recordFactory.create(getInitialAvayaEventWithReceiverReportSide1()));

        AvayaSideEvent result = (AvayaSideEvent)  testDriver.readOutput(detailsEventTopic, stringDeserializer, specificAvroSerde.deserializer()).value();

        assertEquals(new Integer(85),result.getJitter());

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
