package com.dvsts.avaya.processing.logic.mos;

/**
 * 
 * This enum specifies all contemporary codec types and their descriptions.
 * 
 * 
 * @author hweng
 * 
 */

public enum CODECTYPE {
	ADPCM("Adaptive Differential Pulse Code Modulation"),
	LD_CELP("Low Delay Code Excited Linear Prediction"),
	CS_ACELP("Conjugate Structure Algebraic Code-Excited Linear Prediction "),
	VSELP("Vector sum excited linear prediction "),
	ACELP("Algebraic Code Excited Linear Prediction"),
	QCELP("Qualcomm Code Excited Linear Prediction"),
	RCELP("Relaxed Code Excited Linear Prediction "),
	RPE_LTP("Excited-Long Term Prediction"),
	MP_MLQ("Multi-Pulse - Maximum Likelihood Quantizer");

	private CODECTYPE(String desc) {
		_desc = desc;
	}

	private String	_desc;

	public String desc() {
		return _desc;
	}
}
