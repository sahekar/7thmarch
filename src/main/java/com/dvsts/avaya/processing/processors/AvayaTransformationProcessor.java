package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.dvsts.avaya.processing.AppConfig.db;
import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;


/**
 *
 * Main purpose is to calculate all needed metric for this avaya event
 * this processor get the initial pcrf packet from kafka topic  create {@link AvayaPacket(String)} then calculates all metrics
 * save AvayaPacket in embedded RockDB
 */
public class AvayaTransformationProcessor implements Processor<String, GenericRecord> {


    private ProcessorContext context;
    private KeyValueStore<String,AvayaPacket> kvStore;
    private final MainComputationModel mainComputationModel;
    private final AvroTransformer transformer;

    public AvayaTransformationProcessor(AvroTransformer transformer, MainComputationModel mainComputationModel) {
        this.transformer = transformer;
        this.mainComputationModel = mainComputationModel;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(db);
    }

    @Override
    public void process(String key, GenericRecord value) {

        //  System.out.println("data: "+ value);

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String aggrKey = ssrc1+ssrc2;

        AvayaPacket existPacket = this.kvStore.get(aggrKey);

        AvayaPacket result = null;

        if (existPacket == null) {
            result = mainComputationModel.calculatesCallMetric(value,new AvayaPacket());
        } else {
            result = mainComputationModel.calculatesCallMetric(value, existPacket);
        }

        this.kvStore.put(aggrKey,result);

        //  System.out.println("data from store: "+ this.kvStore.get(aggrKey));

        GenericRecord avroResult = transformer.toEventAvroRecord(result,detailsEventTopic);

         context.forward(key, avroResult );


    }

    @Override
    public void close() {

    }
}
