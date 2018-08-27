package com.dvsts.avaya.processing.logic.mos;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.dvsts.avaya.processing.core.rtcp.QOSStandardCodecRef;
import org.jfree.data.xy.XYSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // protected double _Ie_;

//    protected QOSMOSParameters		_param;

  //  protected String				_codecRef;
    protected static final Map<String, QOSCodec>	_qosCodecs	= new HashMap<String, QOSCodec>();


    // TODO: check if need this methods initDump2File()
   /* protected static void initDump2File(String name) {

        if (_debugPrint) {
            String fileName = name + ".txt";
            try {
                _fileWriter = new PrintWriter(new FileWriter(fileName));
            } catch (Exception ie) {
                LOGGER.debug("Error occurred", ie);
            }
        }
    }*/

    public QOSMOSComputationModel(int codec) { // codec 711, 729, 723
        this();
    }


    public  QOSMOSComputationModel() {
        // initDump2File("MOS");
      //  _param = new QOSMOSParameters();

    }

    static {
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
    private static void initCodecs() {
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

  /*  public static void setUnknownCodec(String codec) {
        _qosCodecs.put(QOSStandardCodecRef._UNKNOWN, _qosCodecs.get(codec));
        LOGGER.debug("Unkown codec: " + codec + " used.  Added to _qosCodecs");
    }

    public static void changeCodec(int codec) {
        if (codec == 711 || codec == 729 || codec == 723 || codec == 722) {
            LOGGER.debug("Codec changed to : " + codec);
            _codec = codec;
        }
    }

    public  static  QOSMOSParameters getComputationParameters() {
        return _param;
    }*/

   /* public static synchronized void resetDefaulParamteters() {
        _param = new QOSMOSParameters();
    }*/

    public static    double calculateMOS(final String codecRef, final int jitterSide, final int rtdSide, final double lossSide) throws IllegalStateException {
        if (LOGGER.isDebugEnabled())  LOGGER.debug("Start CalculateMOS called with the following: Codec: " + codecRef + " JitterSide: " + jitterSide + " RtdSide: " + rtdSide + " LossSide:" + lossSide);

       // resetDefaulParamteters();

        final String  _codecRef = codecRef;
        final QOSMOSParameters	 _param = new QOSMOSParameters();
        final MosDto mosDto = new MosDto();


        QOSCodec codecObj = _qosCodecs.get(_codecRef);
        if (codecObj == null) {
            codecObj = _qosCodecs.get(QOSStandardCodecRef._UNKNOWN);
        }

        int delay = rtdSide + jitterSide;


                _param.change_Ie(codecObj.getIeValue(lossSide));
                _param.change_T(delay);
                _param.change_Ta(delay);
                _param.change_Tr(2 * delay);
                _param.change_Ppl(lossSide);

                if (lossSide > 0 && codecObj.isSpecialVals()) {
                    _param.change_Ie(codecObj.get_packetLossIe());
                    _param.change_Bpl(codecObj.get_Bpl());
                }



        compute_params(mosDto,_param);
        compute_TERVs_(mosDto,_param);
        compute_Roe_Re_Rle_(mosDto,_param);
        compute_Ro_(mosDto, _param);
        compute_Is_(mosDto,_param);
        compute_Id_(mosDto,_param);
        compute_Ie_eff_(mosDto,_param);

        mosDto._R_ = mosDto._Ro_ - mosDto._Is_ - mosDto._Id_ - mosDto._Ie_eff_ + _param.get_A();
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("R factor: " + mosDto._R_);

        double mosFloor = 1.0;
        double mosCelling = 4.5;
        double rFloor = 0;
        double rCelling = 100;
        double p1 = 0.035;
        double p2 = 60.0;
        double p3 = 100.0;
        double p4 = 7.0 / 1000000.0;

        double MOS = 1.0;
        if (mosDto._R_ <= rFloor) {
            MOS = mosFloor;
            LOGGER.debug("MOS score when R factor less than " + mosFloor + ": " + MOS);
        } else if (mosDto._R_ > rFloor && mosDto._R_ <= rCelling) {
            MOS = 1 + p1 * mosDto._R_ + mosDto._R_ * (mosDto._R_ - p2) * (p3 - mosDto._R_) * p4;
            LOGGER.debug("MOS score when R factor between " + rFloor + " and " + rCelling + ": " + MOS);
        } else {
            MOS = mosCelling;
            LOGGER.debug("MOS score over ceiling: " + mosCelling);
        }
        return MOS;
    }


    // TODO: check if need this methods getRfactorVsSTMRProfile()  and getMOSVsRfactorProfile()
/*    // assuming G711
    public static XYSeries getRfactorVsSTMRProfile() {
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

    public static XYSeries getMOSVsRfactorProfile() {
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
    }*/

    public static   void compute_Ie_eff_(final MosDto mosDto,final QOSMOSParameters	_param) {
        double p1 = 95.0;
        double pcn = _param.get_Ppl();
        mosDto._Ie_eff_ = _param.get_Ie() + (p1 - _param.get_Ie()) * (pcn / (pcn / _param.get_BurstR() + _param.get_Bpl()));
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Ie-eff: " + mosDto._Ie_eff_);

    }

    // f(T, Tr, Ta, Ro)
    public  static void compute_Id_(final MosDto mosDto,final QOSMOSParameters	_param) {

        compute_Idte_(mosDto,_param); // f(T, Tr)
        compute_Idle_(mosDto, _param);
        compute_Idd_(mosDto, _param); // f(Ta)
        String debugString = "";
        if (mosDto._Idtes_ > -1) {
            mosDto._Id_ = mosDto._Idtes_ + mosDto._Idle_ + mosDto._Idd_;
            debugString += "\nIdtes greater than -1: " + "Idte: " + mosDto._Idte_ + "\n Idle: " + mosDto._Idle_ + "\n Idd: " + mosDto._Idd_ + "\n\n Id: " + mosDto._Id_;
            LOGGER.trace(debugString);
        } else {
            mosDto._Id_ = mosDto._Idte_ + mosDto._Idle_ + mosDto._Idd_;
            if (LOGGER.isTraceEnabled()) {
                debugString += "\nIdtes not greater than -1: " + "Idte: " + mosDto._Idte_ + "\n Idle: " + mosDto._Idle_ + "\n Idd: " + mosDto._Idd_ + "\n\n Id: " + mosDto._Id_;
                LOGGER.trace(debugString);
            }

        }

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Id = " + mosDto._Id_);
        }
    }

    // f(Tr, T))
    public  static void compute_Idte_(final MosDto mosDto,final QOSMOSParameters	_param) {
        compute_Roe_Re_Rle_(mosDto, _param); // f(Tr, T)
        double f1 = (mosDto._Roe_ - mosDto._Re_) / 2.0;
        double f2 = Math.sqrt(Math.pow((mosDto._Roe_ - mosDto._Re_), 2.0) / 4.0 + 100.0) - 1;
        double f3 = (1 - Math.exp(-_param.get_T())); // f(T)
        if (_param.get_STMR() < 1)
            mosDto._Idte_ = 0;
        else
            mosDto._Idte_ = (f1 + f2) * f3; // when _param.get_STMR() < 9 or
        // _param.get_STMR() >= 9 the _TERVs_ is
        // different

        if (_param.get_STMR() > 20) {

            mosDto._Idtes_ = Math.sqrt(Math.pow(mosDto._Idte_, 2.0) + Math.pow(mosDto._Ist_, 2.0));
        } else
            mosDto._Idtes_ = -1;

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Idte = " + mosDto._Idte_);
        }
    }

    //
    public  static void compute_Idle_(final MosDto mosDto,final QOSMOSParameters	_param) {
        double V1 = Math.pow(mosDto._Ro_ - mosDto._Rle_, 2.0) / 4.0 + 169;
        mosDto._Idle_ = (mosDto._Ro_ - mosDto._Rle_) / 2.0 + Math.sqrt(V1);
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Idle = " + mosDto._Idle_);
        }
    }

    // f(Ta)
    public   static   void compute_Idd_(final MosDto mosDto,final QOSMOSParameters	_param) {
        if (_param.get_Ta() <= 100)
            mosDto._Idd_ = 0.0;
        else {
            double p1 = 1.0 / 6.0;
            double x = Math.log10(_param.get_Ta() / 100.0) / Math.log10(2.0);
            mosDto._Idd_ = 25 * (Math.pow((1 + Math.pow(x, 6.0)), p1) - 3.0 * Math.pow(1 + Math.pow(x / 3.0, 6.0), p1) + 2.0);
        }
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Idd = " + mosDto._Idd_);
        }
    }

    // f(Tr, T)
    public  static    void compute_Roe_Re_Rle_(final MosDto mosDto,final QOSMOSParameters	_param) {
        compute_TERVs_(mosDto,_param); // (T)
        mosDto._Roe_ = -1.5 * (mosDto._No_ - _param.get_RLR());
        mosDto._Re_ = 80.0 + 2.5 * (mosDto._TERVs_ - 14.0);
        mosDto._Rle_ = 10.5 * (_param.get_WEPL() + 7) * Math.pow(_param.get_Tr() + 1, -0.25); // f(Tr)
        if (LOGGER.isTraceEnabled()) {
            String debugString = "\nComputing Roe, Re, and Rle values.  Values as follows:" + "Roe: " + mosDto._Roe_ + "\n Re: " + mosDto._Re_ + "\n Rle " + mosDto._Rle_;
            LOGGER.trace(debugString);
        }

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Rle = " + mosDto._Rle_ + " Roe = " + mosDto._Roe_ + " Re = " + mosDto._Re_);
        }
    }

    // f(T)
    public  static void compute_TERVs_(final MosDto mosDto,final QOSMOSParameters	 _param) {

        mosDto._TERVs_ = _param.get_TELR() - 40 * Math.log10((1 + _param.get_T() / 10.0) / (1 + _param.get_T() / 150.0)) + 6 * Math.exp(-0.3 * Math.pow(_param.get_T(), 2.0));
        if (_param.get_STMR() < 9) {
            mosDto._TERVs_ = mosDto._TERVs_ + mosDto._Ist_ / 2.0;
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("\nTERVS with STMR less than 9: " +mosDto._TERVs_);
        } else {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("\nTERVs greater then 9: " + mosDto._TERVs_);
        }

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("TERVs = " + mosDto._TERVs_);
        }
    }

    // f(T, No, Y) is not much influenced by T.
    public static    void compute_Is_(final MosDto mosDto,final QOSMOSParameters	 _param) {
        compute_IoIr_(mosDto, _param); // ~T
        compute_Ist_(mosDto, _param); // f(T)
        compute_Iq_(mosDto, _param);
        mosDto._Is_ = mosDto._IoIr_ + mosDto._Ist_ + mosDto._Iq_;
        if (LOGGER.isTraceEnabled()) {
            String debugString = "\nIs calculated. Variables as follows:" + "IoIr: " + mosDto._IoIr_ + "\n Ist: " + mosDto._Ist_ + "\n Iq: " + mosDto._Iq_ + "\n\n Is: " + mosDto._Is_;
            LOGGER.trace(debugString);
        }
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Is=" + mosDto._Is_);
        }
    }

    // Is 1 //~T
    public static    void compute_Iq_(final MosDto mosDto,final QOSMOSParameters _param) {
        compute_params(mosDto, _param);
        mosDto._Iq_ = 15.0 * Math.log10(1.0 + Math.pow(10.0, mosDto._Y_) + Math.pow(10.0, mosDto._Z_));
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Iq = " + mosDto._Iq_);
        }
    }

    // Is 2 //f(T)
    public  static void compute_Ist_(final MosDto mosDto,final QOSMOSParameters	 _param) {
        compute_STMRo_(mosDto, _param); // (T)

        double V1 = Math.pow(1 + Math.pow((mosDto._STMRo_ - 13.0) / 6.0, 8.0), 1.0 / 8.0);
        double V2 = Math.pow(1 + Math.pow((mosDto._STMRo_ + 1) / 19.4, 35), 1.0 / 35);
        double V3 = Math.pow(1 + Math.pow((mosDto._STMRo_ - 3) / 33.0, 13.0), 1.0 / 13.0);
        mosDto._Ist_ = 12.0 * V1 - 28.0 * V2 - 13.0 * V3 + 29;
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Ist = " + mosDto._Ist_);
        }
    }

    // Is 3
    public static   void compute_IoIr_(final MosDto mosDto,final QOSMOSParameters	 _param) {
        compute_XoIr_(mosDto, _param);
        mosDto._IoIr_ = 20.0 * (Math.pow(1 + Math.pow( mosDto._XoIr_ / 8.0, 8.0), 1.0 / 8.0) -  mosDto._XoIr_ / 8.0);
        if ( mosDto._debugPrint) {
            mosDto._fileWriter.println("IoIr = " +  mosDto._IoIr_);
        }
    }

    // Is 4
    public static   void compute_XoIr_(final MosDto mosDto,final QOSMOSParameters	 _param) {
        mosDto._XoIr_ = mosDto._OLR_ + 0.2 * (64.0 + mosDto._No_ - _param.get_RLR());
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("XoIr = " + mosDto._XoIr_);
        }
    }

    /**
     *
     */
    public static void compute_STMRo_(final MosDto mosDto,final QOSMOSParameters	 _param) {
        mosDto._STMRo_ = -10.0 * Math.log10(Math.pow(10.0, -_param.get_STMR() / 10.0) + Math.exp(-_param.get_T() / 4.0) * Math.pow(10, -_param.get_TELR() / 10.0));
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("STMRo = " + mosDto._STMRo_);
        }
    }

    // ~(T) Basic signal-2-noise ratio
    public  static void compute_Ro_(final MosDto mosDto,QOSMOSParameters _param) {
        compute_No_(mosDto, _param);
        double p1 = 15.0;
        double p2 = 1.5;
        double p3 = 46.0 / 8.4;
        double p4 = 9.0;

        mosDto._Ro_ = p1 - p2 * (_param.get_SLR() + mosDto._No_);
        mosDto._Y_ = (mosDto._Ro_ - 100.0) / p1 + p3 - mosDto._G_ / p4;
        if (LOGGER.isTraceEnabled()) {
            String debugString = "\nSignal to noise ration calculated:" + "Ro: " + mosDto._Ro_ + "\n\n Y (Signal to noise): " + mosDto._Y_;
            LOGGER.trace(debugString);
        }

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Ro = " + mosDto._Ro_);
        }
    }

    // Ro 1
    public static   void compute_Pre_(final MosDto mosDto,QOSMOSParameters _param) {
        mosDto._Pre_ = _param.get_Pr() + 10.0 * Math.log10(1 + Math.pow(10, (10 - _param.get_LSTR()) / 10));
        if ( mosDto._debugPrint) {
            mosDto._fileWriter.println("Pre = " +  mosDto._Pre_);
        }
    }

    // Ro 2
    public  static   void compute_Nfo_(final MosDto mosDto,QOSMOSParameters _param) {
        mosDto._Nfo_ = _param.get_Nfor() + _param.get_RLR();
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Nfo = " + mosDto._Nfo_);
        }
    }

    // Ro 3
    public  static void compute_Nor_(final MosDto mosDto,QOSMOSParameters _param) {
        compute_Pre_(mosDto, _param);
        double p1 = 121;
        double p2 = 0.008 * Math.pow((mosDto._Pre_ - 35), 2);
        mosDto._Nor_ = _param.get_RLR() - p1 + mosDto._Pre_ + p2;
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Nor = " + mosDto._Nor_);
        }
    }

    // Ro 4
    public static   void compute_Nos_(final MosDto mosDto,QOSMOSParameters _param) {
        mosDto._Nos_ = _param.get_Ps() - _param.get_SLR() - _param.get_Ds() - 100 + 0.004 * Math.pow((_param.get_Ps() - mosDto._OLR_ - _param.get_Ds() - 14), 2);
        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("Nos = " + mosDto._Nos_);
        }
    }

    // Ro 5. noise sources
    public static   void compute_No_(final MosDto mosDto,QOSMOSParameters _param) {
        compute_Nfo_(mosDto, _param);
        compute_Nor_(mosDto, _param);
        compute_Nos_(mosDto, _param);
        double p1 = 10.0;
        mosDto._No_ = p1 * Math.log10(Math.pow(p1, _param.get_Nc() / p1) + Math.pow(p1, mosDto._Nos_ / p1) + Math.pow(10, mosDto._Nor_ / p1) + Math.pow(10, mosDto._Nfo_ / p1));
        if (LOGGER.isTraceEnabled()) {
            String debugString = "\nNo completed calculations. Variables list below: " + "\nNfo: " + mosDto._Nfo_ + "\n Nor: " + mosDto._Nor_ + "\n Nos: " + mosDto._Nos_ + "\n\n No: " + mosDto._No_;
            LOGGER.trace(debugString);
        }

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("No = " + mosDto._No_);
        }
    }

    public static void compute_params(final MosDto mosDto,QOSMOSParameters	 _param ) {
        double p1 = 46.0 / 30.0;
        double p2 = 40.0;
        double p3 = 37.0;
        double p4 = 1.07;
        double p5 = 15.0;
        double p6 = 0.258;
        double p7 = 0.0602;
        mosDto._OLR_ = _param.get_SLR() + _param.get_RLR();
        double Qi = _param.get_Ie() == 0 ? p5 * Math.log10(_param.get_qdu()) : 0;
        mosDto._Q_ = p3 - Qi;
        mosDto. _G_ = p4 + p6 * mosDto._Q_ + p7 * Math.pow(mosDto._Q_, 2);
        mosDto._Z_ = p1 - mosDto._G_ / p2;
        if (LOGGER.isTraceEnabled()) {
            String debugString = "\nParameters calculated.  Output as follows: \n" + "OLR: " + mosDto._OLR_ + "\n Qi: " + Qi + "\n Q: " + mosDto._Q_ + "\n G: " + mosDto._G_ + "\n Z: " + mosDto._Z_;
            LOGGER.trace(debugString);
        }

        if (mosDto._debugPrint) {
            mosDto._fileWriter.println("SLR = " + _param.get_SLR());
            mosDto._fileWriter.println("RLR = " + _param.get_RLR());
            mosDto._fileWriter.println("OLR = " + mosDto._OLR_);
            mosDto._fileWriter.println("Q = " + mosDto._Q_);
            mosDto._fileWriter.println("G_ = " + mosDto._G_);
            mosDto._fileWriter.println("Z = " + mosDto._Z_);
        }
    }

    private static    QOSCodec getCodec(int codecNum) {
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
