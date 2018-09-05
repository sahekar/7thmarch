package com.dvsts.avaya.processing.transformers;

import com.dvsts.avaya.processing.TopologySchema;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;


public class AvayaPacketTransformer implements Transformer<String, GenericRecord, KeyValue<String, GenericRecord>> {
    private ProcessorContext context;
    private KeyValueStore<String,AvayaPacket> kvStore;
    private final AvroTransformer transformer;
    private MainComputationModel mainComputationModel;

    public AvayaPacketTransformer(AvroTransformer transformer, MainComputationModel mainComputationModel) {
        this.transformer = transformer;
        this.mainComputationModel = mainComputationModel;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(TopologySchema.db);
    }

    @Override
    public KeyValue<String, GenericRecord> transform(String key, GenericRecord value) {

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String aggrKey = ssrc1+ssrc2;
        AvayaPacket existKey = this.kvStore.get(aggrKey);

        AvayaPacket result = null;

        if(existKey == null) {
             result = mainComputationModel.calculatesCallMetric(value,null);
        } else {
             result = mainComputationModel.calculatesCallMetric(value,existKey);
        }

        this.kvStore.put(aggrKey,result);

        System.out.println("data from store: "+ this.kvStore.get(aggrKey));

        GenericRecord avroResult = transformer.toAvroRecord(result,detailsEventTopic);

        return new KeyValue<>(key,avroResult);
    }




    @Override
    public void close() {

    }
}
