package com.dvsts.avaya.processing.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object representing call metrics for a particular time.
 *
 * We are making a conscious effort to reduce the memory footprint for this class.
 * Since we know that the core set of metrics are ALWAYS set we use primitive types
 * to minimize memory consumption.
 * A map is used for all other metrics (basically the video-specific ones) and the
 * map is sized from the get-go.
 *
 * This is done at the expense of set/get performance where we now have considerable
 * cost in all the conditional checks AND the conversion from primitive-to-ValueObject
 * and back again.
 *
 * Memory allocation overview: http://www.javaspecialists.eu/archive/Issue029.html
 */
public class QOSMetric {
    private static final Logger LOGGER				= LoggerFactory.getLogger(QOSMetric.class);

    // Core metric names (maintained on all calls VOICE + VIDEO)

    public static final String		TIME				= "time";
    public static final String		ALARM				= "alarm";
    public static final String		MOS					= "mos";
    public static final String		RTD					= "rtd";
    public static final String		JITTER				= "jitter";
    public static final String		LOSS				= "loss";
    public static final String		DSCP				= "dscpValue";

    // Optional core metrics

    public static final String		SEQLOSS_LARGEST		= "largestSequenceLoss";
    public static final String		SEQLOSS_INSTANCES	= "seqLossInstances";
    public static final String		OUTSEQ_LARGEST		= "largestOutOfSequence";
    public static final String		OUTSEQ_INSTANCES	= "outOfSeqInstances";
    public static final String		BURST_DENSITY		= "burstDensity";
    public static final String		BURST_DURATION		= "burstDuration";
    public static final String		GAP_DENSITY			= "gapDensity";
    public static final String		GAP_DURATION		= "gapDuration";
    public static final String		ONEWAY_DELAY		= "owd";

    // Video-specific metric names

    public static final String		PACKET_LOSS			= "pcktLossPct";
    public static final String		INTERVAL_LOSS		= AvayaPhoneDefinitions.OpenCalls.PERCENTAGE_INTERVAL_LOSS.name();

    public static final String		DEVICE_TYPE			= AvayaPhoneDefinitions.OpenCalls.DEVICE_TYPE.name();
    public static final String		CALL_DIRECTION		= AvayaPhoneDefinitions.OpenCalls.CALL_DIRECTION.name();

    public static final String		CALL_TYPE			= AvayaPhoneDefinitions.OpenCalls.CALL_TYPE.name();
    public static final String		PLACED_ON_HOLD		= AvayaPhoneDefinitions.OpenCalls.PLACED_ON_HOLD.name();
    public static final String		SET_ON_HOLD			= AvayaPhoneDefinitions.OpenCalls.SET_ON_HOLD.name();
    public static final String		MODIFY_STATE		= AvayaPhoneDefinitions.OpenCalls.MODIFY_STATE.name();
    public static final String		DROP				= AvayaPhoneDefinitions.OpenCalls.DROP.name();
    public static final String		BYTES				= AvayaPhoneDefinitions.OpenCalls.BYTES.name();
    public static final String		CHANNEL_RATE		= AvayaPhoneDefinitions.OpenCalls.CHANNEL_RATE.name();
    public static final String		MUTE				= AvayaPhoneDefinitions.OpenCalls.MUTE.name();
    public static final String		FRAME_RATE			= AvayaPhoneDefinitions.OpenCalls.FRAME_RATE.name();
    public static final String		RESOLUTIONX			= AvayaPhoneDefinitions.OpenCalls.RESOLUTIONX.name();
    public static final String		RESOLUTIONY			= AvayaPhoneDefinitions.OpenCalls.RESOLUTIONY.name();

    // In order to include (primitive) core metrics in the get/put/keySet calls this key array is maintained
    public static final String[]	CORE_METRICS		= new String[] { TIME, ALARM, MOS, RTD, JITTER, LOSS, DSCP };

    // We ALWAYS maintain this set of data so store using primitives to take up less space
    private final long				time;
    private int						rtd					= 0;
    private int						jitter				= 0;
    private int						loss				= 0;
    private int						alarm				= 0;
    private int						dscp				= 0;
    private float					mos					= 0f;

    // All other metrics are stored in a map
    private Map<String, Object>		map;


    public QOSMetric(long time) {
        super();
        this.time = time;
    }

    public Object get(String key) {
        Object value = null;

        // NOTE the use of valueOf to get a ValueObject which should be faster than cast/new'ing

        if (key.equals(TIME)) {
            value = Long.valueOf(time);
        } else if (key.equals(MOS)) {
            value = Float.valueOf(mos);
        } else if (key.equals(ALARM)) {
            value = Integer.valueOf(alarm);
        } else if (key.equals(JITTER)) {
            value = Integer.valueOf(jitter);
        } else if (key.equals(LOSS)) {
            value = Integer.valueOf(loss);
        } else if (key.equals(RTD)) {
            value = Integer.valueOf(rtd);
        } else if (key.equals(DSCP)) {
            value = Integer.valueOf(dscp);
        }

        if ((map != null) && (value == null)) {
            value = map.get(key);
        }

        return value;
    }

    private float parseFloat(Object v) throws NumberFormatException {
        if (v instanceof String) {
            return Float.valueOf(v.toString());
        }

        if (v instanceof Number) {
            return ((Number)v).floatValue();
        }

        throw new NumberFormatException();
    }

    /**
     * Associates the specified metric value with the specified metric key in this map.
     * If its a known typed metric and the value is not a number then it tries to convert
     * the supplied object value to the expected number value.
     *
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key.
     */
    public void put(String key, Object value) {
        // RTCPPacket:getMetric often returns a null value - ignore these
        if (value == null) {
            return;
            // throw new IllegalArgumentException("null value for key: " + key);
        }

        try {
            if (key.equals(MOS)) {
                mos = parseFloat(value);
                return;
            }
            if (key.equals(ALARM)) {
                alarm = parseInt(value);
                return;
            }
            if (key.equals(JITTER)) {
                jitter = parseInt(value);
                return;
            }
            if (key.equals(LOSS)) {
                loss = parseInt(value);
                return;
            }
            if (key.equals(RTD)) {
                rtd = parseInt(value);
                return;
            }
            if (key.equals(DSCP)) {
                dscp = parseInt(value);
                return;
            }

            if (key.equals(PACKET_LOSS) || key.equals(INTERVAL_LOSS) ||
                    key.equals(BURST_DENSITY) || key.equals(GAP_DENSITY)) {
                value = parseFloat(value);
            } else if (key.equals(SEQLOSS_LARGEST) || key.equals(SEQLOSS_INSTANCES) ||
                    key.equals(OUTSEQ_LARGEST) || key.equals(OUTSEQ_INSTANCES) ||
                    key.equals(BURST_DURATION) || key.equals(GAP_DURATION) || key.equals(ONEWAY_DELAY)) {
                value = parseInt(value);
            }
        } catch (NumberFormatException e) {
            LOGGER.error(String.format("Failed to convert metric value '%s' for '%s'", value, key));
            return;
        }

        if (map == null) {
            map = new HashMap<String, Object>();
        }

        // The set of metric keys has probably changed - flush the cache
        cachedKeySet = null;

        map.put(key, value);
    }

    public int size() {
        int mapSize = (map != null) ? map.size() : 0;
        return CORE_METRICS.length + mapSize;
    }

    private Set<String>	cachedKeySet	= null;

    public Set<String> keySet() {
        if (cachedKeySet == null) {
            int mapSize = (map != null) ? map.size() : 0;
            Set<String> keySet = new HashSet<String>(CORE_METRICS.length + mapSize);
            // Add all the primitive metric keys (that won't be in the map)
            for (String metric : CORE_METRICS) {
                keySet.add(metric);
            }

            if (map != null) {
                // Now add all the other metric keys from the map
                keySet.addAll(map.keySet());
            }

            // Lock the set
            cachedKeySet = Collections.unmodifiableSet(keySet);
        }

        return cachedKeySet;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public void setJitter(int jitter) {
        this.jitter = jitter;
    }

    public void setLoss(int loss) {
        this.loss = loss;
    }

    public void setRtd(int rtd) {
        this.rtd = rtd;
    }

    public void setMos(float mos) {
        this.mos = mos;
    }

    public void setDscp(int dscp) {
        this.dscp = dscp;
    }

    private int parseInt(Object v) throws NumberFormatException {
        if (v instanceof String) {
            return Integer.valueOf(v.toString());
        }

        if (v instanceof Number) {
            return ((Number) v).intValue();
        }

        throw new NumberFormatException();
    }

    public static QOSMetric toMetric(Map<String, String> map) {
        // Remove the time value from the last as its needed to construct the metric
        long time = Long.parseLong(map.remove(TIME));

        QOSMetric metric = new QOSMetric(time);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            metric.put(entry.getKey(), entry.getValue());
        }

        return metric;
    }
}