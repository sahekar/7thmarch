package com.dvsts.avaya.processing.logic;

public class QOSThreshold {
    private final int		alert;
    private final int		rtd;
    private final int		jitter;
    private final int		loss;
    private final double	mos;
    private final double	intervalLoss;

    public QOSThreshold(String alert, String rtd, String jitter, String loss, String mos, String intervalLoss) {
        this.alert = getInt(alert);
        this.rtd = getInt(rtd);
        this.jitter = getInt(jitter);
        this.loss = getInt(loss);
        this.mos = getDouble(mos);
        this.intervalLoss = getDouble(intervalLoss);
    }

    public boolean matches(int r, int j, int l, double m, double il) {
        if (intervalLoss > -1d && il >= intervalLoss && intervalLoss != 0) {
            return true;
        }
        return (rtd > -1 && r >= rtd) || (jitter > -1 && j >= jitter) || (loss > -1 && l >= loss) || (mos > -1d && 0d < m && m < mos) || (intervalLoss > -1d && 0d < il && il < intervalLoss);
    }

    public int getAlert() {
        return alert;
    }

    public int getRtd() {
        return rtd;
    }

    public int getJitter() {
        return jitter;
    }

    public int getLoss() {
        return loss;
    }

    public double getMos() {
        return mos;
    }

    public double getIntervalLoss() {
        return intervalLoss;
    }

    private static boolean isValid(String value) {
        return (value != null && value.length() > 0);
    }

    private static int getInt(String value) {
        int val;


        try {
            val = isValid(value) ? Integer.parseInt(value) : -1;
        } catch (NumberFormatException ex) {
            val = -1;
        }

        return val;
    }

    private static double getDouble(String value) {
        double val;

        try {
            val = isValid(value) ? Double.parseDouble(value) : -1d;
        } catch (NumberFormatException ex) {
            val = -1d;
        }

        return val;
    }
}
