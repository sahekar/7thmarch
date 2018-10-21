package com.dvsts.avaya.processing.processors;

import com.dvsts.avaya.core.domain.event.*;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.LocalDateTime;

import static com.dvsts.avaya.processing.AppConfig.db;
import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;


/**
 *
 * Main purpose is to calculate all needed metric for this avaya event
 * this processor get the initial pcrf packet from kafka topic  create {@link AvayaPacket (String)} then calculates all metrics
 * save AvayaPacket in embedded RockDB
 */
public class SideCreatorProcessor implements Processor<String, AvayaEvent> {


    private ProcessorContext context;
    private KeyValueStore<String, AvayaPacket> kvStore;
    private final MainComputationModel mainComputationModel;
    private final AvroTransformer transformer;

    public SideCreatorProcessor(AvroTransformer transformer, MainComputationModel mainComputationModel) {
        this.transformer = transformer;
        this.mainComputationModel = mainComputationModel;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(db);
    }

    @Override
    public void process(String key, AvayaEvent value) {

        System.out.println("initial data: "+ value);

        String ssrc1 =  value.get("ssrc1").toString();
        String ssrc2 = value.get("ssrc2").toString();
        String aggrKey = ssrc1+ssrc2; // TODO: add here cliendId

        AvayaPacket existPacket = this.kvStore.get(aggrKey);

        AvayaPacket result = null;

        final AvayaPacket initialData = create(value,"create");

        if (existPacket == null) {
            initialData.setStartCall(System.currentTimeMillis());
            result = mainComputationModel.calculatesCallMetric(initialData,new AvayaPacket());
        } else {
            initialData.setLastTime(existPacket.getStartCall());
            initialData.setStartCall(existPacket.getStartCall());
            result = mainComputationModel.calculatesCallMetric(initialData, existPacket);
        }

        this.kvStore.put(aggrKey,result);


        GenericRecord avroResult = transformer.toEventAvroRecord(result,detailsEventTopic);

         context.forward(key, avroResult );
    }


    private AvayaPacket create(AvayaEvent entry, String status){

        AvayaPacket packet = new AvayaPacket();
        packet.setStatus("active");

        packet.setIp1(entry.getIp());
        packet.setSsrc1(entry.getSsrc1());
        packet.setSsrc2(entry.getSsrc2());
        packet.setClientId(entry.getClientid()+"");

        SenderReport senderReport = entry.getSenderReport();
        AppSpecificReport appSpecificReport = entry.getAppSpecificReport();
        SourceDescription sourceDescription = entry.getSourceDescription();
        ReceiverReport receiverReport = new ReceiverReport();

        if (senderReport == null) {
            packet.setJitter(Integer.parseInt(senderReport.getJitter()));
            packet.setLoss(Integer.parseInt(senderReport.getLoss()));
        } else {
            packet.setJitter(Integer.parseInt(senderReport.get("jitter").toString()));
            packet.setLoss(Integer.parseInt(senderReport.get("loss").toString()));
        }


        if(appSpecificReport.getRtd() != null) {
            packet.setRtd(Integer.parseInt(appSpecificReport.getRtd()));
            packet.setPayloadType(appSpecificReport.getPayloadtype());
        }

        packet.setType1(sourceDescription.getType());
        packet.setName1(sourceDescription.getName());
        packet.setInsertTime(LocalDateTime.now());

        return packet;

    }



    @Override
    public void close() {

    }
}
