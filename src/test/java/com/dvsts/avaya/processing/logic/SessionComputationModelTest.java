package com.dvsts.avaya.processing.logic;

import com.dvsts.avaya.core.domain.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SessionComputationModelTest {

    private SessionComputationModel sessionComputationModel = new SessionComputationModel();

    @Test
    public void generateSessionId(){

        String ssrc1 = "564789";
        String ssrc2 = "987456";
        String clientId = "5";
        assertEquals(sessionComputationModel.generateSessionId(ssrc2,ssrc1,clientId) ,sessionComputationModel.generateSessionId(ssrc1,ssrc2,clientId));

        ssrc1 = "767666";
        ssrc2 = "123444";
        clientId = "5";
        assertEquals(sessionComputationModel.generateSessionId(ssrc2,ssrc1,clientId) ,sessionComputationModel.generateSessionId(ssrc1,ssrc2,clientId));

        ssrc1 = "1";
        ssrc2 = "4";
        clientId = "5";
        assertEquals(sessionComputationModel.generateSessionId(ssrc2,ssrc1,clientId) ,sessionComputationModel.generateSessionId(ssrc1,ssrc2,clientId));
    }


    @Test
    public void determineSide() {
        String ssrc1 = "12344";
        String ssrc2 = "44444";
        assertEquals(ssrc2, sessionComputationModel.determineSide(ssrc1, ssrc2));
    }


    @Test
    public void createSessionWithOneSide(){

      Session session =  sessionComputationModel.createSession(createAvayaPacketSide1(),null);

       assertEquals("12345",session.getSsrc1());
       assertEquals("6789123451",session.getSessionindex());

    }


    private AvayaPacket createAvayaPacketSide1(){
        AvayaPacket packet = new AvayaPacket();

        packet.setClientId("1");
        packet.setSsrc1("12345");
        packet.setSsrc2("6789");
        packet.setAvgJitter(5.55d);


        return  packet;
    }

}
