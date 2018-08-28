package com.dvsts.avaya.processing.logic;

import com.dvsts.avaya.processing.core.rtcp.QOSStandardCodecRef;
import com.dvsts.avaya.processing.core.rtcp.RTCPPacket;
import com.dvsts.avaya.processing.core.rtcp.util.Util;
import com.dvsts.avaya.processing.core.rtcp.util.VarBind;
import com.dvsts.avaya.processing.logic.mos.QOSMOSComputationModel;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import com.dvsts.avaya.processing.transformers.SchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.dvsts.avaya.processing.logic.TransformationConfig.DEFAULT_FULLDATEFORMAT;
import static com.dvsts.avaya.processing.logic.TransformationConfig.POTENTIAL_FIELDS;

public class Transformation {

    private static final Logger LOGGER				= LoggerFactory.getLogger(Transformation.class);
    public static final String	CALL_INDEX = "callIndex";



    private boolean	 usePqosRtcpIp		= true;

    private static final SimpleDateFormat SDF = new SimpleDateFormat(DEFAULT_FULLDATEFORMAT);

    public AvayaPacket logicForCurrentSession( GenericRecord value )  {

        final AvayaPacket packet = create(value,"create");
        final double intervalLoss =0;
        final String catagory = "";   //TODO: add here the needing  method to get catagery;
        final float mos1 = calculateMos(packet);
        packet.setMos1(mos1);

        final int rateCall = rateCall(catagory,packet.getRtd(),packet.getJitter(),packet.getLoss(),mos1,intervalLoss);
         //int alertLevel =  extractMetrics( currentHolder);
        return packet;

    }



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

    private int safeParse(String s, int defaultValue) {
        return Util.parseToInt(s, defaultValue);
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

    public void mainLogic(RTCPPacket entry){
                   // empty the queue continuously up to 1 second, then release...
                   long timeStartProcess = System.currentTimeMillis();
                   long nanoNow = System.nanoTime();
                       // this entry is a SENDER REPORT...
                   if (nanoNow - entry.time < 25000000000L || (nanoNow - entry.time < 40000000000L && entry.traceroute != null)) {
                           long nanoLag = nanoNow - entry.time;
                           long secLag = nanoLag / 1000000000;
                           //TODO: Do we need this parameter   updateSecLag(secLag);
                           if (entry.type == 0) {

                               String s1 = entry.getSsrc1();
                               String s2 = entry.getSsrc2();

                               String collisionName = "";
                               if (s1.compareTo(s2) > 0) {
                                   collisionName = s1 + ":" + s2;
                               } else {
                                   collisionName = s2 + ":" + s1;
                               }

                               long tt = System.currentTimeMillis();
                               String thisTime = String.valueOf(tt);
                               String thisPacketHost = "name1";

                               int sessionSide = 0;
                               long callTarget = -1L;
                               int alertLevel = 0;
                               String sessionIndex = "";

                               String ssrcName = collisionName;
                               if (!sessionIndex.isEmpty()) {
                                   ssrcName = collisionName + ":" + sessionIndex;
                               }


                               // TODO: think do we need this     SSRCHolder currentHolder = currentCalls.get(ssrcName);
                               // if a new session than this logic
                             //  SSRCHolder currentHolder = null;

                                /*   currentHolder.touch();
                                   boolean isSide1 = currentHolder.isSide1(s1, entry.transponderName);
                                   if (isSide1) {
                                       callTarget = currentHolder.getTableIndex();
                                       sessionSide = 0;
                                       processDscp(entry, sessionSide, currentHolder);
                                       alertLevel = extractMetrics(callTarget, entry, sessionSide, false);
                                   } else {
                                       currentHolder.setTransponder2(entry.transponderName);
                                       thisPacketHost = "name2";
                                       callTarget = currentHolder.getTableIndex();
                                       sessionSide = 1;
                                       processDscp(entry, sessionSide, currentHolder);
                                       alertLevel = extractMetrics(callTarget, entry, sessionSide, false);
                                   }*/



                              /* if (entry.traceroute != null) {
                                   String callIndex = server.getValue("currentCallTable", callTarget, CALL_INDEX);
                                   String hasACall = server.getValue("currentCallTable", callTarget, "traceStatus");
                                   String host1 = server.getValue("currentCallTable", callTarget, "host1Cap");
                                   String host2 = server.getValue("currentCallTable", callTarget, "host2Cap");

                                   if (!hasACall.equals("captured")) {
                                       server.updateRow("currentCallTable", callTarget, "traceStatus", "captured");
                                   }
                                   if (thisPacketHost.equals("name1") && host1.equals("")) {
                                       server.updateRow("currentCallTable", callTarget, "host1Cap", "true");
                                   }
                                   if (thisPacketHost.equals("name2") && host2.equals("")) {
                                       server.updateRow("currentCallTable", callTarget, "host2Cap", "true");
                                   }
                                   checkAndInsertTraceRoute(sessionSide, thisPacketHost, callIndex, entry.traceroute);
                               }
                               if (entry.isPQOS() && entry.fromRtcp) {
                                   RTPTestModule tm = RTPTestModule.getInstance();
                                   if (tm != null) {
                                       String rtpDscp = entry.getMetric("rtpDSCP");
                                       if (entry.transponderName != null) {
                                           try {
                                               tm.transponderContact(entry.transponderName, rtpDscp, entry.traceroute, alertLevel, entry.ssrc1, entry.ssrc2);
                                           } catch (ArrayIndexOutOfBoundsException e) {
                                               // do nothing - session must be in the process of being destroyed
                                               // and we are receiving packets
                                           }
                                       }
                                   }
                               }
                           } else if (entry.type == 1) {

                               // bye signal...
                               String s1 = entry.ssrc1;
                               String s2 = entry.ssrc2;

                               String collisionName = "";
                               if (s1.compareTo(s2) > 0) {
                                   collisionName = s1 + ":" + s2;
                               } else {
                                   collisionName = s2 + ":" + s1;
                               }
                               SSRCHolder currentHolder = findCollisionEntry(collisionName, entry);
                               if (currentHolder != null) {
                                   currentHolder.done();
                                   // construct lookup differently for pqos via rtcp
                                   Funcs funcs1 = getTableQuery(entry, currentHolder);

                                   LOGGER.debug("recevied bye " + currentHolder.getSsrcName());
                                   long[] l1 = server.query("currentCallTable", new GXQuery(funcs1));

                                   if (l1.length > 0) {
                                       server.updateRow("currentCallTable", l1[0], "status", "ended");
                                   }
                               }
                           }

                           if (System.currentTimeMillis() - timeStartProcess > 500) {
                               entry = null;
                           } else {
                               entry = queue.poll();
                               if (entry != null) {
                                   packetMeter.mark();
                               }
                           }

                       } else {
                           // if we show up here, then flush the entire queue...
                           queue.clear();
                           queueFlushMeter.mark();
                           entry = null;
                       }

                   }
               } catch (Exception majorException) {
                   LOGGER.error("Exception in QOSThread run loop", majorException);
               }*/
                           }


                       }


                   }

    private void checkMax(List<VarBind> updateAvg, String metricName, String maxValue, int value) {
        if (maxValue.equals("")) {
            updateAvg.add(new VarBind(metricName, String.valueOf(value)));
        } else {
            try {
                int cm = Integer.parseInt(maxValue);
                if (cm < value) {
                    updateAvg.add(new VarBind(metricName, String.valueOf(value)));
                }
            } catch (Exception te) {
                updateAvg.add(new VarBind(metricName, String.valueOf(value)));
            }
        }
    }

    private void checkMin(List<VarBind> updateAvg, String metricName, String minValue, double value) {
        if (minValue.equals("")) {
            updateAvg.add(new VarBind(metricName, String.valueOf(value)));
        } else {
            try {
                double cm = Double.parseDouble(minValue);
                if (cm > value) {
                    updateAvg.add(new VarBind(metricName, String.valueOf(value)));
                }
            } catch (Exception te) {
                updateAvg.add(new VarBind(metricName, String.valueOf(value)));
            }
        }
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
        packet.setStartCall(SDF.format(new Date(tt)));
       // packet.setIp1( entry.get("ip").toString());
        packet.setType1( entry.get("type1").toString());
        packet.setSsrc1(entry.get("ssrc1").toString());
        packet.setSsrc2( entry.get("ssrc2").toString());

       if(entry.get("pcktLossPct") != null)  packet.setPcktLossPct(entry.get("pcktLossPct").toString());

        if( entry.get("rtpDSCP") == null ){ packet.setRtpDSCP("0"); } else { packet.setRtpDSCP("0"); }

        return packet;

    }
}
