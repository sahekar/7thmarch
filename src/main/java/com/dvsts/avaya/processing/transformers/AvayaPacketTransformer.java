package com.dvsts.avaya.processing.transformers;

import com.dvsts.avaya.processing.TopologySchema;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.Transformation;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;


public class AvayaPacketTransformer implements Transformer<String, GenericRecord, KeyValue<String, GenericRecord>> {
    private ProcessorContext context;
    private KeyValueStore<String,AvayaPacket> kvStore;
    private final Transformation transformation = new Transformation();
    AvroTransformer transformer = new AvroTransformer(schemaRegistryClient());

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

         AvayaPacket result = transformation.logicForCurrentSession(value,null);

        AvayaPacket existKey = this.kvStore.get(aggrKey);
        this.kvStore.put(aggrKey,result);
        System.out.println("data from store: "+ this.kvStore.get(aggrKey));

           GenericRecord avroResult = transformer.toAvroRecord(result,"avaya_output_test4");
        return new KeyValue<>(key,avroResult);
    }


    public SchemaProvider schemaRegistryClient() {
        SchemaRegistryClient client =  new CachedSchemaRegistryClient("http://94.130.90.122:8081", 2);
        SchemaProvider provider = new SchemaProvider(client,1);
        return provider;
    }

    @Override
    public void close() {

    }
}
