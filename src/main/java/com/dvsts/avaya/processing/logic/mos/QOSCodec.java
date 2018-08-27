package com.dvsts.avaya.processing.logic.mos;


import com.dvsts.avaya.processing.core.rtcp.QOSStandardCodecRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 
 * QOSCodec holds Ie values ("loss vs Ie values variant" for specified Codecs)
 * for each codecs type used in MOS computation.
 * 
 * 
 * @author hweng
 * 
 */
public class QOSCodec implements Comparable<QOSCodec> {
	private static final Logger LOGGER				= LoggerFactory.getLogger(QOSCodec.class);

	/**
	 * With random packet loss Bpl is to be set
	 */
	private double					_Bpl;
	private int						_packetLossIe;

	/**
	 * Determines whether packet has special Bpl from Appendix I from ITU-REC-G
	 * 113-200711
	 */
	private boolean					specialVals	= false;
	/**
	 * specifies the codec Algorithm type. Resolves potential of Codec name
	 * conflicting.
	 */
	private CODECTYPE				_type;

	/**
	 * G.711,G.723.1,G.729, Codec document name.
	 */
	private String					_referenceName;

	/**
	 * 711,723,729,internal codec int ID
	 */
	private int						_referenceNumber;

	/**
	 * Codec bitrate. same type codec can have different bit rate
	 */
	private double					_bitRate;

	/**
	 * Codec equipment impairment factor. refer G.113
	 */
	private int						_IeValue;
	private Map<Double, Integer>	_Ls_IeMap;

	public QOSCodec(CODECTYPE type, String refName, double brate, int IeValue, double Bpl, int packetLossIe) {
		this(type, refName, brate, IeValue);
		_Bpl = Bpl;
		_packetLossIe = packetLossIe;
		specialVals = true;
	}

	public QOSCodec(CODECTYPE type, String refName, double brate, int IeValue) {
		_type = type;
		_referenceName = refName;
		_referenceNumber = QOSStandardCodecRef.getCodecRefInt(_referenceName);
		_bitRate = brate;
		_IeValue = IeValue;

		/**
		 * refer : G.113 table 1.2 "Provisional planning values for the
		 * equipment impairment factor Ie under conditions of packet loss,
		 * codecs G.729 A+VAD and G.723.1-A + VAD
		 */
		if (_referenceName.equals("G.729")) {
			_Ls_IeMap = new HashMap<Double, Integer>();
			_Ls_IeMap.put(Double.valueOf(0), Integer.valueOf(11));
			_Ls_IeMap.put(Double.valueOf(0.5), Integer.valueOf(13));
			_Ls_IeMap.put(Double.valueOf(1.0), Integer.valueOf(15));
			_Ls_IeMap.put(Double.valueOf(1.5), Integer.valueOf(17));
			_Ls_IeMap.put(Double.valueOf(2), Integer.valueOf(19));
			_Ls_IeMap.put(Double.valueOf(3), Integer.valueOf(23));
			_Ls_IeMap.put(Double.valueOf(4), Integer.valueOf(26));
			_Ls_IeMap.put(Double.valueOf(8), Integer.valueOf(36));
			_Ls_IeMap.put(Double.valueOf(16), Integer.valueOf(40));
			_IeValue = 40;
		} else if (_referenceName.equals("G.723.1")) {
			_Ls_IeMap = new HashMap<Double, Integer>();
			_Ls_IeMap.put(Double.valueOf(0), Integer.valueOf(15));
			_Ls_IeMap.put(Double.valueOf(0.5), Integer.valueOf(17));
			_Ls_IeMap.put(Double.valueOf(1.0), Integer.valueOf(19));
			_Ls_IeMap.put(Double.valueOf(1.5), Integer.valueOf(22));
			_Ls_IeMap.put(Double.valueOf(2), Integer.valueOf(24));
			_Ls_IeMap.put(Double.valueOf(3), Integer.valueOf(27));
			_Ls_IeMap.put(Double.valueOf(4), Integer.valueOf(32));
			_Ls_IeMap.put(Double.valueOf(8), Integer.valueOf(40));
			_Ls_IeMap.put(Double.valueOf(16), Integer.valueOf(40));
			_IeValue = 40;
		} else {
			_Ls_IeMap = null;
		}

	}

	public String toString() {
		return this._referenceName;
	}

	public int compareTo(QOSCodec cdc) {
		if (cdc._referenceNumber == _referenceNumber)
			return 0;
		else
			return -1;
	}

	public CODECTYPE get_type() {
		return _type;
	}

	public String get_referenceName() {
		return _referenceName;
	}

	public int get_referenceNumber() {
		return _referenceNumber;
	}

	public double get_bitRate() {
		return _bitRate;
	}

	private int get_IeValue() {
		return _IeValue;
	}

	public int getIeValue(double pkloss) {
		LOGGER.debug("getIeValue called with a packet loss of: " + pkloss);
		if (_referenceName.equals("G.729") || _referenceName.equals("G.723.1")) {
			if (pkloss >= 0 && pkloss <= 16) {
				Set<Double> keys = _Ls_IeMap.keySet();
				LOGGER.debug("Key set size returned: " + keys.size());
				ArrayList<Double> keychain = new ArrayList<Double>(keys);
				Collections.sort(keychain);
				double prev = 0;
				for (Double d : keychain) {
					int Ie = _Ls_IeMap.get(d);
					if (d.doubleValue() == pkloss) {
						LOGGER.debug("");
						return Ie;
					} else {
						if (prev <= pkloss && d.doubleValue() > pkloss) {
							int ie1 = _Ls_IeMap.get(prev);
							int ie2 = _Ls_IeMap.get(d.doubleValue());
							return ie1 + (int) ((ie2 - ie1) * (pkloss - prev) / (d.doubleValue() - prev));
						}
					}
					prev = d;
				}
			} else {
				int ie = _Ls_IeMap.get(Double.valueOf(16));
				LOGGER.debug("Pkloss is greater then value of 16.  Returning: " + ie);
				return ie;
			}
		}
		LOGGER.debug("Not using codec G.729 or G.723.1.  Returning a value of: " + get_IeValue());
		return get_IeValue();
	}

	public double get_Bpl() {
		return _Bpl;
	}

	public int get_packetLossIe() {
		return _packetLossIe;
	}

	public boolean isSpecialVals() {
		return specialVals;
	}

}
