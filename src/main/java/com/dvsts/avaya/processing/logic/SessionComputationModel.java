package com.dvsts.avaya.processing.logic;


import com.dvsts.avaya.core.domain.session.*;
import org.apache.avro.generic.GenericRecord;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

public class SessionComputationModel {

    public Session createSession(AvayaPacket packet1, AvayaPacket packet2) {

        Session session = new Session();
        final String ssrc1 = packet1.getSsrc1();
        final String ssrc2 = packet1.getSsrc2();

        String side1Ssrc = determineSide(ssrc1, ssrc2);
        String sessionIndex = generateSessionId(ssrc1, ssrc2, packet1.getClientId());


        if (packet2 == null) {
           return oneSession(packet1, session, sessionIndex);
        } else {

            AvayaPacket side1 = null;
            AvayaPacket side2 = null;

            if (side1Ssrc.equals(packet1.getSsrc1())) {
                side1 = packet1;
                side2 = packet2;
            } else {
                side1 = packet2;
                side2 = packet1;

            }



         return    bothSession(side1, side2, session, sessionIndex);
        }






    }

    private Session bothSession(AvayaPacket side1, AvayaPacket side2, Session session, String sessionIndex) {


        Long durationSide1 = calculatesDuration(side1.getStartCall());
        Long durationSide2 = 0L;
        durationSide2 = calculatesDuration(side2.getStartCall());

        session.setSsrc1(side1.getSsrc1());
        session.setSsrc2(side1.getSsrc2());
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
        session.setMaxrtd(Math.max(side1.getMaxrtd(), side2.getMaxrtd()) + "");
        session.setSessionindex(sessionIndex);
        session.setActive(true);
        session.setDuration(Math.max(durationSide1, durationSide2));
        session.setInsertdata(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());

        return session;
    }

    private Session oneSession(AvayaPacket side1, Session session, String sessionIndex) {
        Long durationSide1 = calculatesDuration(side1.getStartCall());

        session.setSsrc1(side1.getSsrc1());
        session.setSsrc2(side1.getSsrc2());
        String avgLoss = ((side1.getAvgLoss())) + "";
        String avgJitter = ((side1.getAvgJitter())) + "";
        String avgMos = ((side1.getMosAverage())) + "";
        String avgRtd = (double) ((side1.getRtd())) + "";
        session.setAvgloss(avgLoss);
        session.setAvgjitter(avgJitter);
        session.setAvgmos(avgMos);
        session.setAvgrtd(avgRtd);
        session.setIp1(side1.getIp1());
        session.setActive(true);

        session.setName1(side1.getName1());

        session.setPayloadtype1(side1.getPayloadType());

        session.setType1(side1.getType1());

        session.setMinmos(side1.getMos1() + "");
        session.setMaxjitter((side1.getMaxJitter()) + "");
        session.setMaxloss((side1.getMaxLoss()) + "");
        session.setMaxrtd((side1.getMaxrtd()) + "");
        session.setSessionindex(sessionIndex);

        session.setDuration(durationSide1);
        session.setInsertdata(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());

        return session;
    }



    String determineSide(String ssrc1, String ssrc2) {
        if (ssrc1.compareTo(ssrc2) > 0) {
            return ssrc1;
        } else {
            return ssrc2;
        }
    }


    String generateSessionId(String ssrc1, String ssrc2, String clientId) {

        if (ssrc1.compareTo(ssrc2) > 0) return ssrc1 + ssrc2 + clientId;
        else return ssrc2+ssrc1+clientId;
    }

    String generate(String ssrc1,String ssrc2,String clientId){
        final long ssrc1L = Long.parseLong( ssrc1);
        final long ssrc2L = Long.parseLong( ssrc2);

        if(ssrc1L>ssrc2L)  return ssrc1+ssrc2+clientId;
        else return ssrc2+ssrc1+clientId;
    }

    Long calculatesDuration(long startCall) {

        return (LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli() - startCall) / 1000L;
    }

    private SimpleEntry<String, Object>[]  createGenericRegord(AvayaPacket side1, AvayaPacket side2){
        SimpleEntry<String, Object>[] array = new AbstractMap.SimpleEntry[42];



        return array;
    }

}
