package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.dvsts.avaya.processing.AppConfig.db;

public class SessionCreatorProcessor implements Processor<String, GenericRecord> {

    private ProcessorContext context;
    private KeyValueStore<String,AvayaPacket> kvStore;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(db);
    }

    @Override
    public void process(String key, GenericRecord value) {

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String clientId  = value.get("clientid").toString();
        String aggrKey = ssrc1+ssrc2;
        String sessionId = generateSessionId(ssrc1,ssrc2,clientId);

        AvayaPacket side1 = this.kvStore.get(aggrKey);
        AvayaPacket side2 = this.kvStore.get(aggrKey);


        GenericRecord session = createSession(side1,side2);

        context.forward(sessionId, session );

    }

    @Override
    public void close() {

    }

    private GenericRecord createSession(AvayaPacket side1,AvayaPacket side2){
        return null;
    }

    private String generateSessionId(String ssrc1,String ssrc2,String clientId){
        final long ssrc1L = Long.parseLong( ssrc1);
        final long ssrc2L = Long.parseLong( ssrc2);

        if(ssrc1L>ssrc2L)  return ssrc1+ssrc2+clientId;
        else return ssrc2+ssrc1+clientId;
    }
}
