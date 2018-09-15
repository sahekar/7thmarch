package com.dvsts.avaya.processing.logic;


import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Target;

public class QOSMOSComputationModelTest {


    @Test
    public void mosConvTest() {
        QOSMOSComputationModel model = new QOSMOSComputationModel();
        double a = 0;


        Assert.assertEquals(Precision.round(model.calculateMOS("0",0,2,0),2),4.03,0.0);
        Assert.assertEquals(Precision.round(model.calculateMOS("0",0,2,33),2),1.02,0.0);
        Assert.assertEquals(Precision.round(model.calculateMOS("0",0,2,33),2),1.02,0.0);
        Assert.assertEquals(Precision.round(model.calculateMOS("0",24,72,3),2),2.66,0.0);

    }

}
