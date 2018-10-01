package com.dvsts.avaya.processing.logic;

import com.dvsts.avaya.processing.domain.Session;
import org.apache.avro.generic.GenericRecord;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

public class SessionComputationModel {


    public GenericRecord createSession(AvayaPacket side1,AvayaPacket side2){

        Session session = new Session();
        String sessionIndex = generateSessionId(side1.getSsrc1(),side1.getSsrc2(),side1.getClientId());
        session.setSessionindex(sessionIndex);


        updateBaseSideData(session,side1,side2) ;

        return session;
    }

    

    private Session updateBaseSideData(Session session,AvayaPacket side1,AvayaPacket side2){


        session.setAlert(session.getAlert1());
        session.setAlert(session.getAlert());
        session.setSsrc1(side1.getSsrc1());
        session.setSsrc2(side1.getSsrc2());
        session.setActive(true);

        return session;
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

    private SimpleEntry<String, Object>[]  createGenericRegord(AvayaPacket side1, AvayaPacket side2){
        SimpleEntry<String, Object>[] array = new AbstractMap.SimpleEntry[42];



        return array;
    }

}
