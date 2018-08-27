package com.dvsts.avaya.processing.logic;

public class VideoVKMConstants {
    public enum PacketType {
        AUDIO_PACKET,
        REMOTE_AUDIO_PACKET,
        VIDEO_PACKET,
        REMOTE_VIDEO_PACKET
    };

    public static final String	REQUEST_PROCESSOR_NAME		= "Request_Processor_";

    public static final String	VIDEO_HEADER				= "Video-";
    public static final String	VIDEO_TYPE					= VIDEO_HEADER + "Video";
    public static final String	AUDIO_TYPE					= VIDEO_HEADER + "Audio";
    public static final String	DATA_TYPE					= VIDEO_HEADER + "Data";
    public static final String	END_POINT_CRON_STRING		= "0 0 0 * * ?";
    public static final String	END_POINT_IDLE_LABEL		= "Idle";
    public static final String	END_POINT_IN_CALL_LABEL		= "In Call";
    public static final String	END_POINT_NO_RESPONSE_LABEL	= "No Response";
    public static final String	TMS_CRON_STRING				= "0 0 0 * * ?";
    public static final String	SQL_COLLECTION				= "sql";
    public static final String	SQL_END_POINT_IDLE			= "1";
    public static final String	SQL_END_POINT_IN_CALL		= "2";
    public static final String	SQL_IP_ADDRESS_TAG			= "IPAddress";
    public static final String	SQL_STATUS_ID_TAG			= "Status_Id";
    public static final String	TMS_DEPENDENCY_TREE_PREFIX	= "TMS_Device_";
    public static final String	EP_DEPENDENCY_TREE_PREFIX	= "Video_Device_";
    public static final String	WEB_COLLECTION				= "web";
    public static final String	WEB_END_POINT_IDLE			= "Idle";
    public static final String	WEB_END_POINT_IN_CALL		= "InCall";
    public static final String	WEB_END_POINT_NO_RESPONSE	= "NoResponse";
    public static final String	WEB_IP_ADDRESS_TAG			= "IPAddress";
    public static final String	WEB_STATUS_TAG				= "SystemStatus";

    public static final String	URL_TYPE_F_VERSION			= "/getxml?";
    public static final String	URL_TYPE_OTHERS				= "/getxml?location=";
    public static final String	VKM_TAG						= "videoVKM";
    public static final String	VKM_FOLDER					= "videoVKM";
    public static final String	JITTER_POLLER				= "jitterPoller";
    public static final String	LOSS_POLLER					= "lossPoller";

    public static final int		TYPE_LENGTH					= 20;
    public static final int		DISCOVERY_THREAD_COUNT		= 10;
    public static final int		REQUEST_PROCESSOR_COUNT		= 10;
    public static final int		CALL_MONITOR_INTERVAL		= 5;
    public static final int		TMS_LOOKUP_INTERVAL			= 5;

    public static final String	AUDIO_CHANNNEL				= "Audio";
    public static final String	LEGACY_CHANNNEL				= "Legacy";
    public static final String	MAIN_CHANNNEL				= "Main";
    public static final String	PRESENTATION_CHANNNEL		= "Presentation";

    public static final String	ACTIVE_TMS					= "Active";
    public static final String	STANDBY_TMS					= "Standby";
}
