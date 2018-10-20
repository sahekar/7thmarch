package com.dvsts.avaya.processing.topology;

import com.dvsts.avaya.core.domain.session.AvayaSideEvent;
import com.dvsts.avaya.processing.BaseKafkaStreamTest;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.utils.JsonUtils;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.dvsts.avaya.processing.AppConfig.*;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.inputSchema;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.outputSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TopologyKafkaStreamTest extends BaseKafkaStreamTest {

    @BeforeEach
    public void setUp() throws IOException, RestClientException, URISyntaxException {

        init();

        String inputSchema = JsonUtils.getJsonString("/avro-shema/initial_avaya_event_avro_schema.json");

        registerSchema(schemaRegistryClient, inputSchema,initialAvayaSourceTopic);
        registerSchema(schemaRegistryClient, outputSchema,detailsEventTopic);
    }

    @Test
    public void simpleInsertAndOutputEventPrint() throws IOException, URISyntaxException {

        SpecificRecord record = getInitialAvayaEventSide1();

        testDriver.pipeInput(recordFactory.create(record));
        AvayaSideEvent result = (AvayaSideEvent) testDriver.readOutput(detailsEventTopic, stringDeserializer, specificAvroSerde.deserializer()).value();

       assertEquals("1",result.getClientId());

    }

    @Test
    public void stateStoreSimpleInsertOutputPrint() throws IOException, URISyntaxException {
        SpecificRecord record = getInitialAvayaEventSide1();
       testDriver.pipeInput(recordFactory.create(record));
       final   KeyValueStore store =  testDriver.getKeyValueStore(db);
       AvayaPacket packet1 = (AvayaPacket)  store.get("8897978846");

        assertEquals("88979",packet1.getSsrc1());
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
