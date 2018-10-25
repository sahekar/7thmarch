package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.core.domain.session.Session;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.SessionComputationModel;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.zookeeper.server.SessionTracker;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.dvsts.avaya.processing.AppConfig.db;

public class SessionFinisherProcessor implements Processor<String, GenericRecord> {

    private ProcessorContext context;
    private KeyValueStore<String, AvayaPacket> kvStore;
    private final AvroTransformer transformer;
    private SessionComputationModel sessionComputationModel = new SessionComputationModel();

    public SessionFinisherProcessor(AvroTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void init(ProcessorContext context) {
        // keep the processor context locally because we need it in punctuate() and commit()
        this.context = context;

        // retrieve the key-value store named "Counts"
        kvStore = (KeyValueStore) context.getStateStore(db);

        // schedule a punctuate() method every 1000 milliseconds based on event-time
        this.context.schedule(1000, PunctuationType.STREAM_TIME, (timestamp) -> {

            System.out.println("start work the schedular");
            KeyValueIterator<String, AvayaPacket> iter = this.kvStore.all();
            while (iter.hasNext()) {

                KeyValue<String, AvayaPacket> entry = iter.next();
                System.out.println("get entry for calculation d_uration: "+entry.value);
                //  if(entry.value.getInsertTime() == null) kvStore.delete(entry.key); // TODO: remove it only for testing
                   final AvayaPacket side1 = entry.value;
                   final String side2Key = side1.getSsrc2()+side1.getSsrc1();
                   final AvayaPacket side2 = this.kvStore.get(side2Key);

                 long secondsSide1 = Duration.between(side1.getInsertTime(), LocalDateTime.now()).getSeconds();
                 long secondsSide2 = 0;

                 if(side2 != null) {
                      secondsSide2 = Duration.between(side2.getInsertTime(), LocalDateTime.now()).getSeconds();
                 }


                 if(secondsSide1 >= 20 && secondsSide2 >= 20 ){
                     //TODO: create and send a session here

                    GenericRecord session =  sessionComputationModel.createSession(side1,side2);
                    session.put("active",false);
                    kvStore.delete(entry.key);

                    if(((Session) session).getSsrc1() == null) {
                        System.out.println();
                    }

                     context.forward("gg",session);
                     System.out.println(secondsSide1);
                 }


            }
            iter.close();

            // commit the current processing progress
            context.commit();
        });
    }

    @Override
    public void process(String key, GenericRecord value) {
        System.out.println("get value: "+ value);
    }

    @Override
    public void close() {

    }
}
