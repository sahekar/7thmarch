package com.dvsts.avaya.processing.logic;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class  AvayaPacket {

    private String clientId;
    private String status;
    private String callStart;
    /**
     * time of current packet
     */
    private long startCall;
    /**
     * startCall the previous packet
     */
    private long lastTime;
    private long firstTime;
    private long lastPacketTime;
    private String ip1;
    private String type1;
    private String name1;
    private String ssrc1;
    private String ssrc2;
    private String pcktLossPct;
    // private String sessionIndex; //TODO: need to think about this field do we really this data ??
    private String codec;
    private String payloadType;
    private String payloadTypeText;
    private String rtpDSCP;
    private String traceStatus;
    private String host1Cap;
    private String ended;
    private int jitter;
    private int maxJitter=0;
    private long totalJitter=0;
    private double avgJitter=0;
    private int rtd;
    private int Maxrtd;
    private int loss;
    private int maxLoss;
    private double avgLoss;
    private long totalLoss;
    private float mos1=0;
    private float minMos =0;
    private long totalMos;
    private double mosAverage;
    private int alarm;
    private long totalRtd;
    private double avgRtd;
    private String lastPacket;
    private int alert1;
    private int alert2;
    private int alert3;
    private int alert4;
    private int alert5;
    private int maxAlert;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime insertTime = LocalDateTime.now();

}
