package com.dvsts.avaya.processing.logic;

public class TransformationConfig {

    public static final String		DEFAULT_COMPANYNAME		= "Nectar";
    public static final String		DEFAULT_TIMEFORMAT		= "HH:mm:ss";
    public static final String		DEFAULT_DATEFORMAT		= "MM/dd/yyyy";
    public static final String		DEFAULT_DATEFILEFORMAT	= "MM-dd-yyyy";
    public static final String		DEFAULT_FULLDATEFORMAT	= "MM/dd/yy hh:mm:ss aa (EE) z";
    public static final int			DEFAULT_MOTHERPORT		= 443;
    public static final String		DEFAULT_SATELLITETYPE	= "RIG";

    public static final String[]							POTENTIAL_FIELDS			= {
            "jitter",
            "pcktLossPct",
            "rtd",
            "payloadType",
            "rtpPacketCount",
            "largestSequenceJump",
            "largestSequenceFall",
            "seqJumpInstances",
            "seqFallInstances",
            "jitterBufferUnderrun",
            "jitterBufferOverrun",
            "cumulativePktLoss",
            "owd",
            "burstDensity",
            "burstDuration",
            "gapDensity",
            "gapDuration"																};

    public static final String[]							AVG_FIELDS					= {
            "firstPacketTime1", // 0
            "firstPacketTime2", // 1
            "lastPacketTime1", // 2
            "lastPacketTime2", // 3
            "totalLoss1", // 4
            "totalLoss2", // 5
            "avgLoss", // 6
            "avgLoss2", // 7
            "totalJitter1", // 8
            "totalJitter2", // 9
            "avgJitter1", // 10
            "avgJitter2", // 11
            "totalRTD1", // 12
            "totalRTD2", // 13
            "avgRTD1", // 14
            "avgRTD2", // 15
            "totalMos1", // 16
            "totalMos2", // 17
            "avgMos1", // 18
            "avgMos2", // 19
            "alert1", // 20
            "alert2", // 21
            "alert3", // 22
            "alert4", // 23
            "alert5", // 24
            "minMos", // 25
            "maxRTD", // 26
            "maxJitter", // 27
            "maxLoss" // 28
    };

}
