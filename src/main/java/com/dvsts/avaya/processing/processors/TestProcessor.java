package com.dvsts.avaya.processing.processors;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class TestProcessor implements Processor<String, GenericRecord> {

    private ProcessorContext context;
    private KeyValueStore<String,GenericRecord> kvStore;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;

        kvStore = (KeyValueStore) context.getStateStore("test");

    }

    @Override
    public void process(String key, GenericRecord value) {

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String aggrKey = ssrc1+ssrc2;

        System.out.println("aggregated key: "+aggrKey);

        GenericRecord existKey = this.kvStore.get(aggrKey);


        if(existKey == null) this.context.forward(key,value);
    }

    @Override
    public void close() {

    }
}
