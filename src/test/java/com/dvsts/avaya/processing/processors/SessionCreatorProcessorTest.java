package com.dvsts.avaya.processing.processors;


import com.dvsts.avaya.processing.AbstractKafkaStreamTest;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;
import static com.dvsts.avaya.processing.AppConfig.initialAvayaSourceTopic;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.inputSchema;
import static com.dvsts.avaya.processing.config.KafkaStreamConfigTest.outputSchema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class SessionCreatorProcessorTest extends AbstractKafkaStreamTest {

    @Mock
    private KeyValueStore<String,AvayaPacket> kvStore;
    @InjectMocks
    private SessionCreatorProcessor sessionCreatorProcessor;

    @BeforeEach
    public void setUp() throws IOException, RestClientException {
        MockitoAnnotations.initMocks(this);

        init();

        registerSchema(schemaRegistryClient, inputSchema,initialAvayaSourceTopic);
        registerSchema(schemaRegistryClient, outputSchema,detailsEventTopic);

        when(kvStore.get(any())).thenReturn(new AvayaPacket());
        when(kvStore.get(any())).thenReturn(new AvayaPacket());

    }

    //@Test
    @Ignore
    public void simpleSessionCreate() throws IOException, RestClientException {

        /*Map<String,Object> packet = createPcrfPacket();
        GenericRecord record = transformer.toEventAvroRecord(packet,detailsEventTopic);

        testDriver.pipeInput(recordFactory.create(record));
        GenericRecord result =  testDriver.readOutput(detailsEventTopic, stringDeserializer, genericAvroSerde.deserializer()).value();


        Assert.assertEquals(1,result.get("alarm"));*/


        GenericRecord input = createSide1GenericRecord();
        sessionCreatorProcessor.process("ttt",createSide1GenericRecord());

    }


    private GenericRecord createSide1GenericRecord(){
        Schema schema = new Schema.Parser().parse(outputSchema);
        GenericRecord record = new GenericData.Record(schema);
         record.put("id","1234");
         record.put("ssrc1","564789");
         record.put("ssrc2","987456");
        record.put("jitter",1234);
        record.put("rtd",5);
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
