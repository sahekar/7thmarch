package com.dvsts.avaya.processing.logic;

public class AvayaPhoneDefinitions {

    public enum OpenCalls {
        DEVICE_TYPE(""),
        DEVICE_TYPE1("DeviceType1"),
        DEVICE_TYPE2("DeviceType2"),
        TOTAL_LOSS(""),
        TOTAL_LOSS1("TotalLoss1"),
        TOTAL_LOSS2("TotalLoss2"),
        LAST_INTERVAL_RCV(""),
        LAST_INTERVAL_RCV1("LastIntervalRecieved 1"),
        LAST_INTERVAL_RCV2("LastIntervalRecieved 2"),
        LAST_INTERVAL_LOSS(""),
        LAST_INTERVAL_LOSS1("LastIntervalLoss1"),
        LAST_INTERVAL_LOSS2("LastIntervalLoss2"),
        PERCENTAGE_INTERVAL_LOSS(""),
        PERCENTAGE_INTERVAL_LOSS1("%IntervalLoss1"),
        PERCENTAGE_INTERVAL_LOSS2("%IntervalLoss2"),
        DROP(""),
        DROP1("Drop1"),
        DROP2("Drop2"),
        BYTES(""),
        BYTES1("Bytes1"),
        BYTES2("Bytes2"),
        CHANNEL_RATE(""),
        CHANNEL_RATE1("ChannelRate1"),
        CHANNEL_RATE2("ChannelRate2"),
        CALL_DIRECTION(""),
        CALL_DIRECTION1("CallDirection1"),
        CALL_DIRECTION2("CallDirection2"),
        CALL_TYPE(""),
        CALL_TYPE1("CallType1"),
        CALL_TYPE2("CallType2"),
        SET_ON_HOLD(""),
        SET_ON_HOLD1("SetOnHold1"),
        SET_ON_HOLD2("SetOnHold2"),
        PLACED_ON_HOLD(""),
        PLACED_ON_HOLD1("PlacedOnHold1"),
        PLACED_ON_HOLD2("PlacedOnHold2"),
        MODIFY_STATE(""),
        MODIFY_STATE1("ModifyState1"),
        MODIFY_STATE2("ModifyState2"),
        MUTE(""),
        MUTE1("Mute1"),
        MUTE2("Mute2"),
        FRAME_RATE(""),
        FRAME_RATE1("FrameRate1"),
        FRAME_RATE2("FrameRate2"),
        RESOLUTIONX(""),
        RESOLUTIONX1("ResolutionX1"),
        RESOLUTIONX2("ResolutionX2"),
        RESOLUTIONY(""),
        RESOLUTIONY1("ResolutionY1"),
        RESOLUTIONY2("ResolutionY2");

        private final String	prettyLabel;

        private OpenCalls(String prettyLabel) {
            this.prettyLabel = prettyLabel;
        }

        public String toString() {
            return prettyLabel;
        }

        public static String getTableName() {
            return "openCalls";
        }

        public static OpenCalls getSiteInformation(int i) {
            return OpenCalls.values()[i];
        }
    }

    public enum ThresholdNormalization {
        ORDER("Order"),
        CURRENT_ALERT("Current Alert Level"),
        SECONDS_REQUIRED("Number of Seconds in Alarm State"),
        NEW_ALERT("New Alert Level");

        private final String	prettyLabel;

        private ThresholdNormalization(String prettyLabel) {
            this.prettyLabel = prettyLabel;
        }

        public String toString() {
            return prettyLabel;
        }

        public static String getTableName() {
            return "thresholdNormalization";
        }
    }
}
