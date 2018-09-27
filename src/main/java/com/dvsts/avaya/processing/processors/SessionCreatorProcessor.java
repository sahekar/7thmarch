package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.processing.domain.Session;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.SessionComputationModel;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.zookeeper.server.SessionTracker;
import sun.plugin2.message.Serializer;

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
        String aggrKey = ssrc1+ssrc2;

        AvayaPacket side1 = this.kvStore.get(aggrKey);
        AvayaPacket side2 = this.kvStore.get(aggrKey);


        GenericRecord session = sessionComputationModel.createSession(side1,side2);

        context.forward(clientId, session );

    }

    @Override
    public void close() {

    }


}
