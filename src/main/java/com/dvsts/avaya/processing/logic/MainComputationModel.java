package com.dvsts.avaya.processing.logic;

import com.dvsts.avaya.processing.core.rtcp.QOSStandardCodecRef;
import com.dvsts.avaya.processing.logic.mos.QOSMOSComputationModel;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dvsts.avaya.processing.logic.TransformationConfig.DEFAULT_FULLDATEFORMAT;

public class MainComputationModel {

    private static final Logger LOGGER	= LoggerFactory.getLogger(MainComputationModel.class);
    public static final String	CALL_INDEX = "callIndex";

    private boolean	 usePqosRtcpIp		= true;
    private boolean	 normalization	= false;


    private static final SimpleDateFormat SDF = new SimpleDateFormat(DEFAULT_FULLDATEFORMAT);

    public AvayaPacket calculatesCallMetric(GenericRecord value, AvayaPacket previousPacket )  {

        final long currentTime = System.currentTimeMillis();
        final AvayaPacket packet = create(value,"create");
        final double intervalLoss =0;
        final String catagory = "";   //TODO: add here the needing  method to get catagery;
        final float mos1 = calculateMos(packet);
        final int alarm1  = rateCall(catagory,packet.getRtd(),packet.getJitter(),packet.getLoss(),mos1,intervalLoss);

        packet.setMos1(mos1);
        packet.setAlarm(alarm1);

        long firstTimeMill = 0L;
        long lastTimeMill = 0L;
        long timeDiff = 0L;
        int timeDiffInSecs = 0;

        float minMos = previousPacket.getMinMos();
        int maxRTD = previousPacket.getMaxrtd();
        int maxJitter = previousPacket.getMaxJitter();
        int maxLoss = previousPacket.getMaxLoss();

        long firstTime = previousPacket.getFirstTime(); // TODO: check this
        long lastTime = previousPacket.getLastTime();   // TODO: check this

        if(firstTime == 0) firstTimeMill = currentTime;
        else firstTimeMill = firstTime;

        if(lastTime == 0) lastTimeMill = currentTime;
        else lastTimeMill = lastTime;

        timeDiff = currentTime - lastTimeMill;
        timeDiffInSecs = ((int) timeDiff) / 1000;
        long totalTime = currentTime - firstTimeMill;
        long totalLoss = previousPacket.getTotalLoss() + ((packet.getLoss()) * timeDiff);

        double lossAverage = 0d;

        if (totalTime > 0L) {
            lossAverage = ((double) totalLoss) / ((double) totalTime);
        }

        long totalRTD = previousPacket.getTotalRtd() + ((packet.getRtd()) * timeDiff); // TODO: pay attention on this


        double rtdAverage = 0d;

        if (totalTime > 0L) {
            rtdAverage = ((double) totalRTD) / ((double) totalTime);
        }

       long totalJitter = previousPacket.getTotalJitter() + ((packet.getJitter()) * timeDiff);
        double jitterAverage = 0d;
        if (totalTime > 0L) {
            jitterAverage = ((double) totalJitter) / ((double) totalTime);
        }

       long  totalMos = previousPacket.getTotalMos() + (long) (mos1 * timeDiff);

        double mosAverage = 0d;
        if (totalTime > 0L) {
            mosAverage = ((double) totalMos) / ((double) totalTime);
        }

        int highestAlertLevel = 1;
        if (alarm1 > highestAlertLevel) {
            highestAlertLevel = alarm1;
        }

        int alert1Seconds = previousPacket.getAlert1();
        int alert2Seconds = previousPacket.getAlert2();
        int alert3Seconds = previousPacket.getAlert3();
        int alert4Seconds = previousPacket.getAlert4();
        int alert5Seconds = previousPacket.getAlert5();

        if (highestAlertLevel == 1) {
            alert1Seconds += timeDiffInSecs;
        } else if (highestAlertLevel == 2) {
            alert2Seconds += timeDiffInSecs;
        } else if (highestAlertLevel == 3) {
            alert3Seconds += timeDiffInSecs;
        } else if (highestAlertLevel == 4) {
            alert4Seconds += timeDiffInSecs;
        } else if (highestAlertLevel == 5) {
            alert5Seconds += timeDiffInSecs;
        }

        packet.setAlert1(alert1Seconds);
        packet.setAlert2(alert2Seconds);
        packet.setAlert3(alert3Seconds);
        packet.setAlert4(alert4Seconds);
        packet.setAlert5(alert5Seconds);

         int maxAlertLevel = packet.getMaxAlert();

        if (highestAlertLevel > maxAlertLevel) {
            packet.setMaxAlert(maxAlertLevel);
        }

       // TODO: lool pn this int newAlertLevel = normalization ? ThresholdNormalizationEntry.evaluateCall(alert1Seconds, alert2Seconds, alert3Seconds, alert4Seconds, alert5Seconds) : highestAlertLevel;


        packet.setTotalLoss(totalLoss); // TODO: chech why is long and not int .... also add test
        packet.setTotalRtd(totalRTD);
        packet.setAvgLoss(lossAverage);
        packet.setAvgRtd(rtdAverage);
        packet.setTotalJitter(totalJitter);
        packet.setAvgJitter(jitterAverage);
        packet.setTotalMos(totalMos);
        packet.setMosAverage(mosAverage);
        packet.setLastPacketTime(currentTime);
        packet.setMinMos(Math.min(previousPacket.getMinMos(),packet.getMos1()));
        packet.setMaxJitter(Math.max(previousPacket.getMaxJitter(),packet.getJitter()));
        packet.setMaxLoss(Math.max(previousPacket.getMaxLoss(),packet.getLoss()));



        return packet;
    }

    private final static AtomicBoolean reload = new AtomicBoolean(true);

    //always called from QOSThread - so we know won't be running in multiple places
  /*  public  boolean evaluateCall(int alert1, int alert2, int alert3, int alert4, int alert5) {


        if (isCallMatch(alert1, alert2, alert3, alert4, alert5)) {
            return tne.newAlert;
        }
        //no adjustments necessary, so just pass it through
        return 1;
    }*/

   /* public boolean isCallMatch(int alert1, int alert2, int alert3, int alert4, int alert5) {

        int seconds = 0;
        if (currentAlert.equals("5")) seconds = alert5;
        if (currentAlert.equals("4")) seconds = alert4;
        if (currentAlert.equals("3")) seconds = alert3;
        if (currentAlert.equals("2")) seconds = alert2;
        if (currentAlert.equals("1")) seconds = alert1;

        if (seconds < secondsRequired) {
            return false;
        }

        return true;
    }*/

    private String lookupPayloadCodecCode(String input) {
        if (input.equals("0")) {
            return QOSStandardCodecRef._G711;
        }
        if (input.equals("4")) {
            return QOSStandardCodecRef._G723A;
        }
        if (input.equals("8")) {
            return QOSStandardCodecRef._G711;
        }
        if (input.equals("9")) {
            return QOSStandardCodecRef._G722;
        }
        if (input.equals("15"))
        {
            return QOSStandardCodecRef._G728_16; // 728
        }
        if (input.equals("18"))
        {
            return QOSStandardCodecRef._G729; // 729
        }
        return QOSStandardCodecRef._UNKNOWN;
    }

    public static final float	MOS_MIN	= 0f;
    public static final float	MOS_MAX	= 5f;

    private float clipMos(double mos) {
        double value = mos;
        if (mos > MOS_MAX) {
            value = MOS_MAX;
            LOGGER.error(String.format("Mos value of %s is not in valid range (%s..%s). Changing to %s", mos, MOS_MIN, MOS_MAX, MOS_MAX));
        }
        if (mos < MOS_MIN) {
            value = MOS_MIN;
            LOGGER.error(String.format("Mos value of %s is not in valid range (%s..%s). Changing to %s", mos, MOS_MIN, MOS_MAX, MOS_MAX));
        }

        long t = (long) (value * 100d);
        double f = t / 100d;

        return (float) f;
    }

    private float calculateMos(AvayaPacket packet) {

         double ila = packet.getLoss();

       // TODO: add late  final String  codec1 = lookupPayloadCodecCode(packet.getPayloadTypeText());
        String codec1 = "18";

        if (ila < 0) {
            ila = 0;
        }

        if (ila > 100) {
            ila = 100;
        }


        return clipMos(QOSMOSComputationModel.calculateMOS(codec1, packet.getJitter(), packet.getRtd(), ila));
    }

    private int rateCall(String category, int rtd, int jitter, int loss, double mos, double intervalLoss) {

            List<QOSThreshold> thresh = null; // TODO: here set a list of category;
            if (thresh == null) {
                return 1;
            }
            Iterator<QOSThreshold> i = thresh.iterator();
            int maxAlert = 1;
            while (i.hasNext()) {
                QOSThreshold q = i.next();
                if (q.matches(rtd, jitter, loss, mos, intervalLoss)) {
                    int alert = q.getAlert();
                    if (alert > maxAlert) {
                        maxAlert = alert;
                    }
                }
            }
            return maxAlert;

    }

    private long getNextIndex(long thisNextIndex) {
        long timeNow = System.currentTimeMillis();
        if (timeNow <= thisNextIndex) {
            thisNextIndex++;
        } else {
            thisNextIndex = timeNow;
        }
        return thisNextIndex;
    }

    private AvayaPacket create(GenericRecord entry,String status){
        long tt = System.currentTimeMillis();
        String thisTime = String.valueOf(tt);

        AvayaPacket packet = new AvayaPacket();
        packet.setStatus("active");

       if(packet.getStartCall() == 0) packet.setStartCall(tt);
       // packet.setIp1( entry.get("ip").toString());

        GenericRecord sourceDescription = (GenericRecord) entry.get("sourceDescription");
        packet.setType1( (String) sourceDescription.get("type1"));
        packet.setSsrc1(entry.get("ssrc1").toString());
        packet.setSsrc2( entry.get("ssrc2").toString());
        packet.setClientId( entry.get("clientId").toString());

       if(entry.get("pcktLossPct") != null)  packet.setPcktLossPct(entry.get("pcktLossPct").toString());

        if( entry.get("rtpDSCP") == null ){ packet.setRtpDSCP("0"); } else { packet.setRtpDSCP("0"); }

        packet.setInsertTime(LocalDateTime.now());

        return packet;

    }

    public static void main(String[] args) {
          final String[]							AVG_FIELDS					= {
                "firstPacketTime1",																																											// 0
                "firstPacketTime2",																																											// 1
                "lastPacketTime1",																																											// 2
                "lastPacketTime2",																																											// 3
                "totalLoss1",																																												// 4
                "totalLoss2",																																												// 5
                "avgLoss1",																																													// 6
                "avgLoss2",																																													// 7
                "totalJitter1",																																												// 8
                "totalJitter2",																																												// 9
                "avgJitter1",																																												// 10
                "avgJitter2",																																												// 11
                "totalRTD1",																																												// 12
                "totalRTD2",																																												// 13
                "avgRTD1",																																													// 14
                "avgRTD2",																																													// 15
                "totalMos1",																																												// 16
                "totalMos2",																																												// 17
                "avgMos1",																																													// 18
                "avgMos2",																																													// 19
                "alert1",																																													// 20
                "alert2",																																													// 21
                "alert3",																																													// 22
                "alert4",																																													// 23
                "alert5",																																													// 24
                "minMos",																																													// 25
                "maxRTD",																																													// 26
                "maxJitter",																																												// 27
                "maxLoss"																																													// 28
        };

       // System.out.print(AVG_FIELDS[12]);
        String al1 = AVG_FIELDS[20];
        String al2 = AVG_FIELDS[21];
        String al3 = AVG_FIELDS[22];
        String al4 = AVG_FIELDS[23];
        String al5 = AVG_FIELDS[24];

        System.out.println(al1);
        System.out.println(al2);
        System.out.println(al3);
        System.out.println(al4);
        System.out.println(al5);
    }
}
