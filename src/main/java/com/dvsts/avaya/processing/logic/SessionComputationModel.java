package com.dvsts.avaya.processing.logic;

import com.dvsts.avaya.processing.domain.Session;
import org.apache.avro.generic.GenericRecord;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

public class SessionComputationModel {


    public GenericRecord createSession(AvayaPacket side1,AvayaPacket side2){

        Session session = new Session();
        String sessionIndex = generateSessionId(side1.getSsrc1(),side1.getSsrc2(),side1.getClientId());
        Long duration = calculatesDuration(side1.getStartCall());

        String avgLoss = ((side1.getAvgLoss() + side2.getAvgLoss()) / 2) + "";
        String avgJitter = ((side1.getAvgJitter() + side2.getAvgJitter()) / 2) + "";
        String avgMos = ((side1.getMosAverage() + side2.getMosAverage()) / 2) + "";
        String avgRtd = (double) ((side1.getRtd() + side2.getRtd()) / 2) + "";
        session.setAvgloss(avgLoss);
        session.setAvgjitter(avgJitter);
        session.setAvgmos(avgMos);
        session.setAvgrtd(avgRtd);
        session.setIp1(side1.getIp1());
        session.setIp2(side2.getIp1());
        session.setName1(side1.getName1());
        session.setName2(side2.getName1());
        session.setPayloadtype1(side1.getPayloadType());
        session.setPayloadtype2(side2.getPayloadType());
        session.setType1(side1.getType1());
        session.setType2(side2.getType1());
        session.setMinmos(Math.min(side1.getMos1(), side2.getMos1()) + "");
        session.setMaxjitter(Math.max(side1.getMaxJitter(), side2.getMaxJitter()) + "");
        session.setMaxloss(Math.max(side1.getMaxLoss(), side2.getMaxLoss()) + "");
        session.setSessionindex(sessionIndex);

        session.setDuration(duration);
        session.setInsertdata(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());

        updateBaseSideData(session,side1,side2) ;

        return session;
    }

    private void updateBaseSideData(Session session,AvayaPacket side1,AvayaPacket side2){

        session.setAlert(session.getAlert1());
        session.setAlert(session.getAlert());
        session.setSsrc1(side1.getSsrc1());
        session.setSsrc2(side1.getSsrc2());
        session.setActive(true);
    }

    String generateSessionId(String ssrc1,String ssrc2,String clientId){
        final long ssrc1L = Long.parseLong( ssrc1);
        final long ssrc2L = Long.parseLong( ssrc2);

        if(ssrc1L>ssrc2L)  return ssrc1+ssrc2+clientId;
        else return ssrc2+ssrc1+clientId;
    }

    String generate(String ssrc1,String ssrc2,String clientId){
        final long ssrc1L = Long.parseLong( ssrc1);
        final long ssrc2L = Long.parseLong( ssrc2);

        if(ssrc1L>ssrc2L)  return ssrc1+ssrc2+clientId;
        else return ssrc2+ssrc1+clientId;
    }

    Long calculatesDuration(long startCall) {

        return (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startCall) / 1000L;
    }

    private SimpleEntry<String, Object>[]  createGenericRegord(AvayaPacket side1, AvayaPacket side2){
        SimpleEntry<String, Object>[] array = new AbstractMap.SimpleEntry[42];



        return array;
    }

}
