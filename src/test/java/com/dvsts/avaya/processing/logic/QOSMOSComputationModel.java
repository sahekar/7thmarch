package com.dvsts.avaya.processing.logic;

import org.junit.Test;

import java.lang.annotation.Target;

public class QOSMOSComputationModel {


    @Test
    public void simpleSampleTest(){
         System.out.println(com.dvsts.avaya.processing.logic.mos.QOSMOSComputationModel.calculateMOS("df",5,5,2));
    }

}
