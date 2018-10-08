package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.SessionComputationModel;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.dvsts.avaya.processing.AppConfig.db;

public class

SessionCreatorProcessor implements Processor<String, GenericRecord> {

    private ProcessorContext context;
    private KeyValueStore<String,AvayaPacket> kvStore;
    private SessionComputationModel sessionComputationModel = new SessionComputationModel();

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(db);
    }

    @Override
    public void process(String key, GenericRecord value) {

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String clientId  = value.get("client_id").toString();
        String aggrKeySide1 = ssrc1 + ssrc2;
        String aggrkeySide2 = ssrc2 + ssrc1;

        AvayaPacket side1 = this.kvStore.get(aggrKeySide1);
        AvayaPacket side2 = this.kvStore.get(aggrkeySide2);


        GenericRecord session = sessionComputationModel.createSession(side1,side2);

        context.forward(clientId, session );

    }

    @Override
    public void close() {

    }


}
