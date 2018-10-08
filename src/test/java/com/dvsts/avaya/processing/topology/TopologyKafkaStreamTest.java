package com.dvsts.avaya.processing.topology;

import com.dvsts.avaya.processing.BaseKafkaStreamTest;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.generic.GenericRecord;
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

        registerSchema(schemaRegistryClient, inputSchema,initialAvayaSourceTopic);
        registerSchema(schemaRegistryClient, outputSchema,detailsEventTopic);



    }


    @Test
    public void simpleInsertAndOutputEventPrint() throws IOException, URISyntaxException {

        GenericRecord record = getInitialAvayaEventSide1();

        testDriver.pipeInput(recordFactory.create(record));
        GenericRecord result =  testDriver.readOutput(detailsEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();

        assertEquals(1,result.get("alarm"));

    }

    @Test
    public void stateStoreSimpleInsertOutputPrint() throws IOException, URISyntaxException {
        GenericRecord record = getInitialAvayaEventSide1();
       testDriver.pipeInput(recordFactory.create(record));
       final   KeyValueStore store =  testDriver.getKeyValueStore(db);
       AvayaPacket packet1 = (AvayaPacket)  store.get("dddfdfdf");

        assertEquals("ddd",packet1.getSsrc1());


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


    @AfterEach
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
