package com.dvsts.avaya.processing.logic;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Target;

public class QOSMOSComputationModelTest {


    @Test
    public void simpleSampleTest(){
        QOSMOSComputationModel model = new QOSMOSComputationModel();
        double a = 0;


        Assert.assertEquals(model.calculateMOS("18",2,0,a),4.1,0);
        Assert.assertEquals(model.calculateMOS("18",5,42,0),4.05,0);

    }

}
