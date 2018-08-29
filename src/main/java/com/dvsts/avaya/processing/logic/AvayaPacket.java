package com.dvsts.avaya.processing.logic;

import lombok.Data;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;

@Data
public class  AvayaPacket {

    private String status;
    private String callStart;
    private String startCall;
    private String lastPacket;
    private String ip1;
    private String type1;
    private String name1;
    private String ssrc1;
    private String ssrc2;
    private String pcktLossPct;
   // private String sessionIndex; //TODO: need to think about this field do we really this data ??
    private String codec;
    private String payloadTypeText;
    private String rtpDSCP;
    private String traceStatus;
    private String host1Cap;
    private String ended;
    private int jitter;
    private int rtd;
    private int loss;
    private float mos1;
    private int alarm;

}
