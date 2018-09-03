package com.dvsts.avaya.processing.logic;


import com.dvsts.avaya.processing.core.rtcp.QOSStandardCodecRef;
import com.dvsts.avaya.processing.logic.mos.CODECTYPE;
import com.dvsts.avaya.processing.logic.mos.QOSCodec;
import com.dvsts.avaya.processing.logic.mos.QOSMOSParameters;
import org.jfree.data.xy.XYSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * This implementation of MOS E model computation including default values and
 * value ranges are updated to G.107 2009. (4.8.2010)
 * 
 * 
 * 
 */
public class QOSMOSComputationModel {

	public enum Columns {
		LOSS,
		JITTER,
		CODEC,
		MOS
	}

	private static final Logger LOGGER				= LoggerFactory.getLogger(QOSMOSComputationModel.class);

	protected long					_jitterDelay;
	protected long					_roundTripDelay;
	protected double				_packageLoss;

	protected double				_R_;
	protected double				_Ro_;
	protected double				_No_;
	protected double				_Nos_;
	protected double				_Nor_;
	protected double				_Nfo_;
	protected double				_Pre_;
	protected double				_Is_;
	protected double				_IoIr_;
	protected double				_Ist_;
	protected double				_Iq_;
	protected double				_Id_;
	// protected double _Ie_;
	protected double				_Ie_eff_;
	protected double				_Idte_;
	protected double				_Idtes_		= -1;
	protected double				_Idle_;
	protected double				_Idd_;

	protected double				_Y_, _Z_, _G_, _Q_, _OLR_;
	protected double				_STMRo_, _XoIr_, _Roe_, _Re_, _Rle_, _TERVs_;
	protected boolean				_debugPrint	= false;
	protected PrintWriter			_fileWriter;
	protected QOSMOSParameters _param;
	protected int					_codec;
	protected String				_codecRef;
	protected Map<String, QOSCodec>	_qosCodecs	= new HashMap<String, QOSCodec>();

	protected void initDump2File(String name) {

		if (_debugPrint) {
			String fileName = name + ".txt";
			try {
				_fileWriter = new PrintWriter(new FileWriter(fileName));
			} catch (Exception ie) {
				LOGGER.debug("Error occurred", ie);
			}
		}
	}

	public QOSMOSComputationModel(int codec) { // codec 711, 729, 723
		this();
	}


	public QOSMOSComputationModel() {
		// initDump2File("MOS");
		_param = new QOSMOSParameters();
		initCodecs();
	}

	/**
	 * refer to G.113(1998) Table 1.1 Provisional planning values for the
	 * equipment impairment factor Ie
	 * 
	 * ************UPDATE 11/7/2013: refer to G.113(2007) Table 1.1 Provisional
	 * planning values for the equipment impairment factor Ie. Changes made to
	 * G726_16, G727_16, IS-641, IS-96 Ie values. CMP-2262
	 * E-Model_Ie_Extension_GC.xls contains necessary data for Ie, and Bpl
	 * values for codecs. Changes below reflect those values.
	 */
	private void initCodecs() {
		_qosCodecs.put(QOSStandardCodecRef._G711, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G711, 64, 13, 4.3, 0));
		_qosCodecs.put(QOSStandardCodecRef._G721, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G721, 32, 13));
		_qosCodecs.put(QOSStandardCodecRef._G722, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G722, 64, 0, 13, 14));
		_qosCodecs.put(QOSStandardCodecRef._G723A, new QOSCodec(CODECTYPE.ACELP, QOSStandardCodecRef._G723A, 5.3, 19, 16.1, 15));
		_qosCodecs.put(QOSStandardCodecRef._G723M, new QOSCodec(CODECTYPE.MP_MLQ, QOSStandardCodecRef._G723M, 6.3, 15, 20, 15));
		_qosCodecs.put(QOSStandardCodecRef._G726_40, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G726_40, 40, 2, 24, 7));
		_qosCodecs.put(QOSStandardCodecRef._G726_32, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G726_32, 32, 13, 24, 12));
		_qosCodecs.put(QOSStandardCodecRef._G726_24, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G726_24, 24, 19, 38, 25));
		_qosCodecs.put(QOSStandardCodecRef._G726_16, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G726_16, 16, 50, 27, 16));
		_qosCodecs.put(QOSStandardCodecRef._G727_40, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G727_40, 40, 2));
		_qosCodecs.put(QOSStandardCodecRef._G727_32, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G727_32, 32, 7));
		_qosCodecs.put(QOSStandardCodecRef._G727_24, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G727_24, 24, 25));
		_qosCodecs.put(QOSStandardCodecRef._G727_16, new QOSCodec(CODECTYPE.ADPCM, QOSStandardCodecRef._G727_16, 16, 50));

		_qosCodecs.put(QOSStandardCodecRef._G728_16, new QOSCodec(CODECTYPE.LD_CELP, QOSStandardCodecRef._G728_16, 16, 7));
		_qosCodecs.put(QOSStandardCodecRef._G728_12, new QOSCodec(CODECTYPE.LD_CELP, QOSStandardCodecRef._G728_12, 12.8, 20));

		_qosCodecs.put(QOSStandardCodecRef._G729, new QOSCodec(CODECTYPE.CS_ACELP, QOSStandardCodecRef._G729, 8, 10, 17, 11));
		_qosCodecs.put(QOSStandardCodecRef._G729APlsVAD, new QOSCodec(CODECTYPE.CS_ACELP, QOSStandardCodecRef._G729APlsVAD, 8, 11, 19.0, 11));

		_qosCodecs.put(QOSStandardCodecRef._GS0610F, new QOSCodec(CODECTYPE.RPE_LTP, QOSStandardCodecRef._GS0610F, 13, 20));
		_qosCodecs.put(QOSStandardCodecRef._GS0620H, new QOSCodec(CODECTYPE.VSELP, QOSStandardCodecRef._GS0620H, 5.6, 23));
		_qosCodecs.put(QOSStandardCodecRef._GS0660EF, new QOSCodec(CODECTYPE.ACELP, QOSStandardCodecRef._GS0660EF, 12.2, 5));

		_qosCodecs.put(QOSStandardCodecRef._IS_54, new QOSCodec(CODECTYPE.VSELP, QOSStandardCodecRef._IS_54, 8, 20));
		_qosCodecs.put(QOSStandardCodecRef._IS_641, new QOSCodec(CODECTYPE.ACELP, QOSStandardCodecRef._IS_641, 7.4, 10));
		_qosCodecs.put(QOSStandardCodecRef._IS_96, new QOSCodec(CODECTYPE.QCELP, QOSStandardCodecRef._IS_96, 8, 21));
		_qosCodecs.put(QOSStandardCodecRef._IS_127, new QOSCodec(CODECTYPE.RCELP, QOSStandardCodecRef._IS_127, 8, 6));
		_qosCodecs.put(QOSStandardCodecRef._Jpn_PDC, new QOSCodec(CODECTYPE.VSELP, QOSStandardCodecRef._Jpn_PDC, 6.7, 24));

		_qosCodecs.put(QOSStandardCodecRef._UNKNOWN, _qosCodecs.get(QOSStandardCodecRef._G711));
		LOGGER.debug("Initialized codecs");
	}

	public void setUnknownCodec(String codec) {
		_qosCodecs.put(QOSStandardCodecRef._UNKNOWN, _qosCodecs.get(codec));
		LOGGER.debug("Unkown codec: " + codec + " used.  Added to _qosCodecs");
	}

	public void changeCodec(int codec) {
		if (codec == 711 || codec == 729 || codec == 723 || codec == 722) {
			LOGGER.debug("Codec changed to : " + codec);
			_codec = codec;
		}
	}

	public QOSMOSParameters getComputationParameters() {
		return _param;
	}

	public synchronized void resetDefaulParamteters() {
		_param = new QOSMOSParameters();
	}

	public synchronized double calculateMOS(final String codecRef, final int jitterSide, final int rtdSide, final double lossSide) throws IllegalStateException {
		LOGGER.debug("Start CalculateMOS called with the following: Codec: {} JitterSide: {} RtdSide: {} LossSide:{}", codecRef, jitterSide, rtdSide, lossSide);
		resetDefaulParamteters();
		_codecRef = codecRef;
		QOSCodec codecObj = _qosCodecs.get(_codecRef);
		if (codecObj == null) {
			codecObj = _qosCodecs.get(QOSStandardCodecRef._UNKNOWN);
		}
		int delay = rtdSide + jitterSide;
		synchronized (_param) {
			try {
				_param.change_Ie(codecObj.getIeValue(lossSide));
				_param.change_T(delay);
				_param.change_Ta(delay);
				_param.change_Tr(2 * delay);
				_param.change_Ppl(lossSide);

				if (lossSide > 0 && codecObj.isSpecialVals()) {
					_param.change_Ie(codecObj.get_packetLossIe());
					_param.change_Bpl(codecObj.get_Bpl());
				}

			} catch (IllegalStateException ie) {
				throw ie;
			}
		}
		compute_params();
		compute_TERVs_();
		compute_Roe_Re_Rle_();
		compute_Ro_();
		compute_Is_();
		compute_Id_();
		compute_Ie_eff_();

		_R_ = _Ro_ - _Is_ - _Id_ - _Ie_eff_ + _param.get_A();
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("R factor: " + _R_);

		double mosFloor = 1.0;
		double mosCelling = 4.5;
		double rFloor = 0;
		double rCelling = 100;
		double p1 = 0.035;
		double p2 = 60.0;
		double p3 = 100.0;
		double p4 = 7.0 / 1000000.0;

		double MOS = 1.0;
		if (_R_ <= rFloor) {
			MOS = mosFloor;
			LOGGER.debug("MOS score when R factor less than " + mosFloor + ": " + MOS);
		} else if (_R_ > rFloor && _R_ <= rCelling) {
			MOS = 1 + p1 * _R_ + _R_ * (_R_ - p2) * (p3 - _R_) * p4;
			LOGGER.debug("MOS score when R factor between {} and {}: {}", rFloor, rCelling, MOS);
		} else {
			MOS = mosCelling;
			LOGGER.debug("MOS score over ceiling: " + mosCelling);
		}
		return MOS;
	}

	// assuming G711
	public XYSeries getRfactorVsSTMRProfile() {
		XYSeries xys = new XYSeries("R vs STMR");
		_param = new QOSMOSParameters();
		compute_params();
		int stmrValue = 0;
		try {

			for (int i = 0; i < 40; i++) {
				_param.change_STMR2(stmrValue);

				compute_TERVs_();
				compute_Roe_Re_Rle_();
				compute_Ro_();
				compute_Is_();
				compute_Id_();
				compute_Ie_eff_();
				_R_ = _Ro_ - _Is_ - _Id_ - _Ie_eff_ + _param.get_A();
				LOGGER.debug("R factor calculated in getRfactorVsSTMRProfile method: " + _R_);
				xys.add(stmrValue, _R_);
				stmrValue++;
			}

		} catch (Exception ex) {
			LOGGER.error("Error in getRfactorVsSTMRProfile method", ex);
		}
		return xys;
	}

	public XYSeries getMOSVsRfactorProfile() {
		XYSeries xys = new XYSeries("Mos vs R");
		double R = 0;
		double p1 = 0.035;
		double p2 = 60;
		double p3 = 100;
		double p4 = 7.0 / 1000000.0;
		for (int i = 0; i <= 20; i++) {
			double m = 1 + p1 * R + R * (R - p2) * (p3 - R) * p4;
			xys.add(R, m);
			LOGGER.debug("Key: " + R + " value: " + m);
			R += 5;
		}
		return xys;
	}

	public void compute_Ie_eff_() {
		double p1 = 95.0;
		double pcn = _param.get_Ppl();
		_Ie_eff_ = _param.get_Ie() + (p1 - _param.get_Ie()) * (pcn / (pcn / _param.get_BurstR() + _param.get_Bpl()));
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("Ie-eff: " + _Ie_eff_);

	}

	// f(T, Tr, Ta, Ro)
	public void compute_Id_() {

		compute_Idte_(); // f(T, Tr)
		compute_Idle_();
		compute_Idd_(); // f(Ta)
		String debugString = "";
		if (_Idtes_ > -1) {
			_Id_ = _Idtes_ + _Idle_ + _Idd_;
			debugString += "\nIdtes greater than -1: " + "Idte: " + _Idte_ + "\n Idle: " + _Idle_ + "\n Idd: " + _Idd_ + "\n\n Id: " + _Id_;
			LOGGER.trace(debugString);
		} else {
			_Id_ = _Idte_ + _Idle_ + _Idd_;
			if (LOGGER.isTraceEnabled()) {
				debugString += "\nIdtes not greater than -1: " + "Idte: " + _Idte_ + "\n Idle: " + _Idle_ + "\n Idd: " + _Idd_ + "\n\n Id: " + _Id_;
				LOGGER.trace(debugString);
			}

		}

		if (_debugPrint) {
			_fileWriter.println("Id = " + _Id_);
		}
	}

	// f(Tr, T))
	public void compute_Idte_() {
		compute_Roe_Re_Rle_(); // f(Tr, T)
		double f1 = (_Roe_ - _Re_) / 2.0;
		double f2 = Math.sqrt(Math.pow((_Roe_ - _Re_), 2.0) / 4.0 + 100.0) - 1;
		double f3 = (1 - Math.exp(-_param.get_T())); // f(T)
		if (_param.get_STMR() < 1)
			_Idte_ = 0;
		else
			_Idte_ = (f1 + f2) * f3; // when _param.get_STMR() < 9 or
		// _param.get_STMR() >= 9 the _TERVs_ is
		// different

		if (_param.get_STMR() > 20) {

			_Idtes_ = Math.sqrt(Math.pow(_Idte_, 2.0) + Math.pow(_Ist_, 2.0));
		} else
			_Idtes_ = -1;

		if (_debugPrint) {
			_fileWriter.println("Idte = " + _Idte_);
		}
	}

	//
	public void compute_Idle_() {
		double V1 = Math.pow(_Ro_ - _Rle_, 2.0) / 4.0 + 169;
		_Idle_ = (_Ro_ - _Rle_) / 2.0 + Math.sqrt(V1);
		if (_debugPrint) {
			_fileWriter.println("Idle = " + _Idle_);
		}
	}

	// f(Ta)
	public void compute_Idd_() {
		if (_param.get_Ta() <= 100)
			_Idd_ = 0.0;
		else {
			double p1 = 1.0 / 6.0;
			double x = Math.log10(_param.get_Ta() / 100.0) / Math.log10(2.0);
			_Idd_ = 25 * (Math.pow((1 + Math.pow(x, 6.0)), p1) - 3.0 * Math.pow(1 + Math.pow(x / 3.0, 6.0), p1) + 2.0);
		}
		if (_debugPrint) {
			_fileWriter.println("Idd = " + _Idd_);
		}
	}

	// f(Tr, T)
	public void compute_Roe_Re_Rle_() {
		compute_TERVs_(); // (T)
		_Roe_ = -1.5 * (_No_ - _param.get_RLR());
		_Re_ = 80.0 + 2.5 * (_TERVs_ - 14.0);
		_Rle_ = 10.5 * (_param.get_WEPL() + 7) * Math.pow(_param.get_Tr() + 1, -0.25); // f(Tr)
		if (LOGGER.isTraceEnabled()) {
			String debugString = "\nComputing Roe, Re, and Rle values.  Values as follows:" + "Roe: " + _Roe_ + "\n Re: " + _Re_ + "\n Rle " + _Rle_;
			LOGGER.trace(debugString);
		}

		if (_debugPrint) {
			_fileWriter.println("Rle = " + _Rle_ + " Roe = " + _Roe_ + " Re = " + _Re_);
		}
	}

	// f(T)
	public void compute_TERVs_() {

		_TERVs_ = _param.get_TELR() - 40 * Math.log10((1 + _param.get_T() / 10.0) / (1 + _param.get_T() / 150.0)) + 6 * Math.exp(-0.3 * Math.pow(_param.get_T(), 2.0));
		if (_param.get_STMR() < 9) {
			_TERVs_ = _TERVs_ + _Ist_ / 2.0;
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("\nTERVS with STMR less than 9: " + _TERVs_);
		} else {
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("\nTERVs greater then 9: " + _TERVs_);
		}

		if (_debugPrint) {
			_fileWriter.println("TERVs = " + _TERVs_);
		}
	}

	// f(T, No, Y) is not much influenced by T.
	public void compute_Is_() {
		compute_IoIr_(); // ~T
		compute_Ist_(); // f(T)
		compute_Iq_();
		_Is_ = _IoIr_ + _Ist_ + _Iq_;
		if (LOGGER.isTraceEnabled()) {
			String debugString = "\nIs calculated. Variables as follows:" + "IoIr: " + _IoIr_ + "\n Ist: " + _Ist_ + "\n Iq: " + _Iq_ + "\n\n Is: " + _Is_;
			LOGGER.trace(debugString);
		}
		if (_debugPrint) {
			_fileWriter.println("Is=" + _Is_);
		}
	}

	// Is 1 //~T
	public void compute_Iq_() {
		compute_params();
		_Iq_ = 15.0 * Math.log10(1.0 + Math.pow(10.0, _Y_) + Math.pow(10.0, _Z_));
		if (_debugPrint) {
			_fileWriter.println("Iq = " + _Iq_);
		}
	}

	// Is 2 //f(T)
	public void compute_Ist_() {
		compute_STMRo_(); // (T)

		double V1 = Math.pow(1 + Math.pow((_STMRo_ - 13.0) / 6.0, 8.0), 1.0 / 8.0);
		double V2 = Math.pow(1 + Math.pow((_STMRo_ + 1) / 19.4, 35), 1.0 / 35);
		double V3 = Math.pow(1 + Math.pow((_STMRo_ - 3) / 33.0, 13.0), 1.0 / 13.0);
		_Ist_ = 12.0 * V1 - 28.0 * V2 - 13.0 * V3 + 29;
		if (_debugPrint) {
			_fileWriter.println("Ist = " + _Ist_);
		}
	}

	// Is 3
	public void compute_IoIr_() {
		compute_XoIr_();
		_IoIr_ = 20.0 * (Math.pow(1 + Math.pow(_XoIr_ / 8.0, 8.0), 1.0 / 8.0) - _XoIr_ / 8.0);
		if (_debugPrint) {
			_fileWriter.println("IoIr = " + _IoIr_);
		}
	}

	// Is 4
	public void compute_XoIr_() {
		_XoIr_ = _OLR_ + 0.2 * (64.0 + _No_ - _param.get_RLR());
		if (_debugPrint) {
			_fileWriter.println("XoIr = " + _XoIr_);
		}
	}

	/**
	 * 
	 */
	public void compute_STMRo_() {
		_STMRo_ = -10.0 * Math.log10(Math.pow(10.0, -_param.get_STMR() / 10.0) + Math.exp(-_param.get_T() / 4.0) * Math.pow(10, -_param.get_TELR() / 10.0));
		if (_debugPrint) {
			_fileWriter.println("STMRo = " + _STMRo_);
		}
	}

	// ~(T) Basic signal-2-noise ratio
	public void compute_Ro_() {
		compute_No_();
		double p1 = 15.0;
		double p2 = 1.5;
		double p3 = 46.0 / 8.4;
		double p4 = 9.0;

		_Ro_ = p1 - p2 * (_param.get_SLR() + _No_);
		_Y_ = (_Ro_ - 100.0) / p1 + p3 - _G_ / p4;
		if (LOGGER.isTraceEnabled()) {
			String debugString = "\nSignal to noise ration calculated:" + "Ro: " + _Ro_ + "\n\n Y (Signal to noise): " + _Y_;
			LOGGER.trace(debugString);
		}

		if (_debugPrint) {
			_fileWriter.println("Ro = " + _Ro_);
		}
	}

	// Ro 1
	public void compute_Pre_() {
		_Pre_ = _param.get_Pr() + 10.0 * Math.log10(1 + Math.pow(10, (10 - _param.get_LSTR()) / 10));
		if (_debugPrint) {
			_fileWriter.println("Pre = " + _Pre_);
		}
	}

	// Ro 2
	public void compute_Nfo_() {
		_Nfo_ = _param.get_Nfor() + _param.get_RLR();
		if (_debugPrint) {
			_fileWriter.println("Nfo = " + _Nfo_);
		}
	}

	// Ro 3
	public void compute_Nor_() {
		compute_Pre_();
		double p1 = 121;
		double p2 = 0.008 * Math.pow((_Pre_ - 35), 2);
		_Nor_ = _param.get_RLR() - p1 + _Pre_ + p2;
		if (_debugPrint) {
			_fileWriter.println("Nor = " + _Nor_);
		}
	}

	// Ro 4
	public void compute_Nos_() {
		_Nos_ = _param.get_Ps() - _param.get_SLR() - _param.get_Ds() - 100 + 0.004 * Math.pow((_param.get_Ps() - _OLR_ - _param.get_Ds() - 14), 2);
		if (_debugPrint) {
			_fileWriter.println("Nos = " + _Nos_);
		}
	}

	// Ro 5. noise sources
	public void compute_No_() {
		compute_Nfo_();
		compute_Nor_();
		compute_Nos_();
		double p1 = 10.0;
		_No_ = p1 * Math.log10(Math.pow(p1, _param.get_Nc() / p1) + Math.pow(p1, _Nos_ / p1) + Math.pow(10, _Nor_ / p1) + Math.pow(10, _Nfo_ / p1));
		if (LOGGER.isTraceEnabled()) {
			String debugString = "\nNo completed calculations. Variables list below: " + "\nNfo: " + _Nfo_ + "\n Nor: " + _Nor_ + "\n Nos: " + _Nos_ + "\n\n No: " + _No_;
			LOGGER.trace(debugString);
		}

		if (_debugPrint) {
			_fileWriter.println("No = " + _No_);
		}
	}

	public void compute_params() {
		double p1 = 46.0 / 30.0;
		double p2 = 40.0;
		double p3 = 37.0;
		double p4 = 1.07;
		double p5 = 15.0;
		double p6 = 0.258;
		double p7 = 0.0602;
		_OLR_ = _param.get_SLR() + _param.get_RLR();
		double Qi = _param.get_Ie() == 0 ? p5 * Math.log10(_param.get_qdu()) : 0;
		_Q_ = p3 - Qi;
		_G_ = p4 + p6 * _Q_ + p7 * Math.pow(_Q_, 2);
		_Z_ = p1 - _G_ / p2;
		if (LOGGER.isTraceEnabled()) {
			String debugString = "\nParameters calculated.  Output as follows: \n" + "OLR: " + _OLR_ + "\n Qi: " + Qi + "\n Q: " + _Q_ + "\n G: " + _G_ + "\n Z: " + _Z_;
			LOGGER.trace(debugString);
		}

		if (_debugPrint) {
			_fileWriter.println("SLR = " + _param.get_SLR());
			_fileWriter.println("RLR = " + _param.get_RLR());
			_fileWriter.println("OLR = " + _OLR_);
			_fileWriter.println("Q = " + _Q_);
			_fileWriter.println("G_ = " + _G_);
			_fileWriter.println("Z = " + _Z_);
		}
	}

	private QOSCodec getCodec(int codecNum) {
		QOSCodec c = null;
		Set<String> keys = _qosCodecs.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			c = _qosCodecs.get(iter.next());
			if (c.get_referenceNumber() == codecNum)
				return c;
		}
		return _qosCodecs.get("G711");
	}

}
