package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.processing.streams.TopologySchema;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class TransfomationProcessor implements Processor<String, GenericRecord> {


    private ProcessorContext context;
    private KeyValueStore<String,AvayaPacket> kvStore;
    private final MainComputationModel mainComputationModel = new MainComputationModel();

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(TopologySchema.db);
    }

    @Override
    public void process(String key, GenericRecord value) {

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String aggrKey = ssrc1+ssrc2;

        final AvayaPacket result = mainComputationModel.calculatesCallMetric(value,null);
        System.out.println("result: "+ result);

        AvayaPacket existKey = this.kvStore.get(aggrKey);

        if(existKey == null) { this.context.forward(key,value); }
        else { this.context.forward(key,result); }

    }

    @Override
    public void close() {

    }
}
