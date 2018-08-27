package com.dvsts.avaya.processing.core.rtcp;


import java.util.HashMap;
import java.util.Map;

/**
 * This is a global constants holding class where different Codecs symbol string
 * are defined for Internal standards parameter usage.
 *
 * Use constant string as the MOS computation parameter.
 *
 * The int constant associated with each codec strings are used in computation
 * extension later.
 *
 *
 * @author hweng
 *
 */

public class QOSStandardCodecRef {

	public static final String			_G711			= "G.711";
	public static final String			_G721			= "G.721";

	public static final String			_G722			= "G.722";

	public static final String			_G726_40		= "G.726 40";
	public static final String			_G726_32		= "G.726 32";
	public static final String			_G726_24		= "G.726 24";
	public static final String			_G726_16		= "G.726 16";

	public static final String			_G727_40		= "G.727 40";
	public static final String			_G727_32		= "G.727 32";
	public static final String			_G727_24		= "G.727 24";
	public static final String			_G727_16		= "G.727 16";

	public static final String			_G728_16		= "G.728 16";
	public static final String			_G728_12		= "G.728 12.8";

	public static final String			_G729			= "G.729";
	public static final String			_G729APlsVAD	= "G.729A";

	public static final String			_G723A			= "G.723.1A";
	public static final String			_G723M			= "G.723.1M";
	public static final String			_IS_54			= "IS-54";
	public static final String			_IS_641			= "IS-641";
	public static final String			_IS_96			= "IS-96a";
	public static final String			_IS_127			= "IS-127";
	public static final String			_Jpn_PDC		= "Japanese PDC";
	public static final String			_GS0610F		= "GSM 06.10 Full Rate";
	public static final String			_GS0620H		= "GSM 06.20 Half Rate";
	public static final String			_GS0660EF		= "GSM 06.60 Enhanced Full Rate";

	public static final String			_UNKNOWN		= "Encrypted";

	public static final int				_G711n			= 711;
	public static final int				_G721n			= 721;
	public static final int				_G722n			= 722;
	public static final int				_G726_40n		= 7264;
	public static final int				_G726_32n		= 72632;
	public static final int				_G726_24n		= 72624;
	public static final int				_G726_16n		= 72616;

	public static final int				_G727_40n		= 72740;
	public static final int				_G727_32n		= 72732;
	public static final int				_G727_24n		= 72724;
	public static final int				_G727_16n		= 72716;

	public static final int				_G728_16n		= 728;
	public static final int				_G728_12n		= 7281;

	public static final int				_G729n			= 729;
	public static final int				_G729APlsVADn	= 7290;

	public static final int				_G723An			= 72311;
	public static final int				_G723Mn			= 72312;

	public static final int				_IS_54n			= 1854;
	public static final int				_IS_641n		= 18641;
	public static final int				_IS_96n			= 1896;
	public static final int				_IS_127n		= 18127;
	public static final int				_Jpn_PDCn		= 5732;
	public static final int				_GS0610Fn		= 74610;
	public static final int				_GS0620Hn		= 74620;
	public static final int				_GS0660EFn		= 74660;

	public static Map<String, Integer>	_strIntMap;
	public static QOSStandardCodecRef	_this			= new QOSStandardCodecRef();

	public QOSStandardCodecRef() {
		if (_strIntMap == null) {
			_strIntMap = new HashMap<String, Integer>();
			_strIntMap.put(_G711, _G711n);
			_strIntMap.put(_G721, _G721n);
			_strIntMap.put(_G722, _G722n);
			_strIntMap.put(_G726_40, _G726_40n);
			_strIntMap.put(_G726_32, _G726_32n);
			_strIntMap.put(_G726_24, _G726_24n);
			_strIntMap.put(_G726_16, _G726_16n);

			_strIntMap.put(_G727_40, _G727_40n);
			_strIntMap.put(_G727_32, _G727_32n);
			_strIntMap.put(_G727_24, _G727_24n);
			_strIntMap.put(_G727_16, _G727_16n);

			_strIntMap.put(_G728_16, _G728_16n);
			_strIntMap.put(_G728_12, _G728_12n);

			_strIntMap.put(_G729, _G729n);
			_strIntMap.put(_G729APlsVAD, _G729APlsVADn);
			_strIntMap.put(_G723A, _G723An);
			_strIntMap.put(_G723M, _G723Mn);

			_strIntMap.put(_IS_54, _IS_54n);
			_strIntMap.put(_IS_641, _IS_641n);
			_strIntMap.put(_IS_96, _IS_96n);
			_strIntMap.put(_IS_127, _IS_127n);
			_strIntMap.put(_Jpn_PDC, _Jpn_PDCn);
			_strIntMap.put(_GS0610F, _GS0610Fn);
			_strIntMap.put(_GS0620H, _GS0620Hn);
			_strIntMap.put(_GS0660EF, _GS0660EFn);
		}
	}

	public static int getCodecRefInt(String strRef) {
		return _strIntMap.get(strRef);
	}

}

