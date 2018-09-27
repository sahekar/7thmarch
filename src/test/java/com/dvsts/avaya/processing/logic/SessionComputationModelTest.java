package com.dvsts.avaya.processing.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SessionComputationModelTest {

    private SessionComputationModel sessionComputationModel = new SessionComputationModel();

    @Test
    public void sessionCreate(){

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
}
