package com.dvsts.avaya.processing.logic.mos;

/**
 * 
 * Questions: 1. parameters relation to codecs 2. T, Tr, and Ta. how related to
 * Jitter, RTD in AVAYA system.
 * 
 * 
 * @author hweng
 * 
 */

public class QOSMOSParameters {

	public QOSCodec		_codecObj;
	public int			_codec;
	/**
	 * the default range and default value are updated to G.107 2009
	 */

	/**
	 * (0 ~ 18) Send Loudness Rating
	 */
	public final int	SLR		= 8;

	/**
	 * (-5 ~ 14) Receive Loudness Rating ?
	 */
	public final int	RLR		= 2;

	/**
	 * (10 ~ 20) Sidetone Masking Rating ?
	 */
	public final int	STMR	= 15;

	/**
	 * (13 ~ 23) Listener Sidetone Rating ?
	 */
	public final int	LSTR	= 18;

	/**
	 * (-3 ~ 3) D- Value of Telephone, Send Side
	 */

	public final int	Ds		= 3;

	/**
	 * (-3 ~ 3) D- Value of Telephone Receive Side
	 */
	public final int	Dr		= 3;

	/**
	 * (5 ~ 65) Talker Echo Loadness Rating
	 */
	public final int	TELR	= 65;

	/**
	 * (5 ~ 110) Weighted Echo Path Loss
	 */
	public final int	WEPL	= 110;

	/**
	 * (0 ~ 500) Mean one-way Delay of the Echo Path
	 */
	public final int	T		= 0;

	/**
	 * (0 ~ 1000) Round trip Delay in 4-wire Loop ?
	 */
	public final int	Tr		= 0;

	/**
	 * (0 ~ 500) Absolute Delay in echo-free Connections
	 */
	public final int	Ta		= 0;

	/**
	 * (1 ~ 14) Number of Quantization Distortion Units
	 */
	public final int	qdu		= 1;

	/**
	 * (0 ~ 40) Equipment Impairments Factor
	 */
	public final int	Ie		= 0;

	/**
	 * (4.3 ~ 40) Packet-loss Robustness G.107 2011 updated
	 */
	public final double	Bpl		= 4.3;

	/**
	 * (0 ~ 20)% Random Packet-loss Probability
	 */
	public final double	Ppl		= 0;

	/**
	 * (1...8) Burst Ratio version G.107 2009 updated. Defined as: (Average
	 * length of observed bursts in an arrival sequence)/(Average length of
	 * burst expected for the network under "random" loss)
	 */
	public final int	BurstR	= 1;

	/**
	 * (-80 ~ -40)Circuit Noise referred to 0 dBr-point ?
	 */
	public final int	Nc		= -70;

	/**
	 * Noise floor at the receive side
	 */
	public final int	Nfor	= -64;

	/**
	 * (35 ~ 85) Room Noise at send side
	 */
	public final int	Ps		= 35;

	/**
	 * (35 ~ 85) Room Noise at receive side
	 */
	public final int	Pr		= 35;

	/**
	 * (0 ~ 20) Advantage Factor
	 */
	public final int	A		= 0;

	private int			_SLR	= SLR;
	private int			_RLR	= RLR;
	private int			_STMR	= STMR;
	private int			_LSTR	= LSTR;
	private int			_Ds		= Ds;
	private int			_Dr		= Dr;
	private int			_TELR	= TELR;
	private int			_WEPL	= WEPL;
	private int			_T		= T;
	private int			_Tr		= Tr;
	private int			_Ta		= Ta;
	private int			_qdu	= qdu;
	private int			_Ie		= Ie;
	private double		_Bpl	= Bpl;
	private double		_Ppl	= Ppl;
	private int			_BurstR	= BurstR;
	private int			_Nc		= Nc;
	private int			_Nfor	= Nfor;
	private int			_Ps		= Ps;
	private int			_Pr		= Pr;
	private int			_A		= A;

	/**
		 * 
		 */
	public QOSMOSParameters() {

	}

	/**
	 * keep current usage for time being
	 * 
	 * @param codec
	 */
	public QOSMOSParameters(int codec) {
		_codec = codec;
	}

	public void change_SLR(int newValue) throws IllegalStateException {

		if (newValue >= 0 && newValue <= 18)
			_SLR = newValue;
		else
			throw new IllegalStateException("The Send Loudness Rating, SLR: " + newValue + "is inproper value. SLR value range is 0 to 18");
	}

	public void change_RLR(int newValue) throws IllegalStateException {

		if (newValue >= -5 && newValue <= 14)
			_RLR = newValue;
		else
			throw new IllegalStateException("The Receive Loudness Rating, RLR: " + newValue + "is inproper value. RLR value range is -5 to 14");
	}

	public void change_STMR2(int newValue) throws IllegalStateException {
		if (newValue >= 0 && newValue <= 40)
			_STMR = newValue;
		else
			throw new IllegalStateException("The Sidetone Masking Rating, STMR: " + newValue + "is inproper value. STMR value range is 10 to 20");
	}

	public void change_STMR(int newValue) throws IllegalStateException {
		if (newValue >= 10 && newValue <= 20)
			_STMR = newValue;
		else
			throw new IllegalStateException("The Sidetone Masking Rating, STMR: " + newValue + "is inproper value. STMR value range is 10 to 20");
	}

	public void change_LSTR(int newValue) throws IllegalStateException {
		if (newValue >= 13 && newValue <= 23)
			_LSTR = newValue;
		else
			throw new IllegalStateException("The Listener Sidetone Rating, LSTR: " + newValue + "is inproper value. LSTR value range is 13 to 23");

	}

	public void change_Ds(int newValue) throws IllegalStateException {
		if (newValue >= -3 && newValue <= 3)
			_Ds = newValue;
		else
			throw new IllegalStateException("The D Value on Send Side, Ds: " + newValue + "is inproper value. Ds value range is -3 to 3");

	}

	public void change_Dr(int newValue) throws IllegalStateException {
		if (newValue >= -3 && newValue <= 3)
			_Dr = newValue;
		else
			throw new IllegalStateException("The D Value on Receive, SideDr: " + newValue + "is inproper value. Dr value range is -3 to 3");
	}

	public void change_TELR(int newValue) throws IllegalStateException {
		if (newValue >= 5 && newValue <= 65)
			_TELR = newValue;
		else
			throw new IllegalStateException("The Talker Echo Loadness Rating, TELR: " + newValue + "is inproper value. TELR value range is 5 to 65");

	}

	public void change_WEPL(int newValue) throws IllegalStateException {
		if (newValue >= 5 && newValue <= 111)
			_WEPL = newValue;
		else
			throw new IllegalStateException("The Weighted Echo Path Loss, WEPL: " + newValue + "is inproper value. WEPL value range is 5 to 111");

	}

	public void change_T(int newValue) throws IllegalStateException {
		if (newValue >= 0)
			_T = newValue;
		else
			throw new IllegalStateException("The Mean One-way Delay of the Echo Path, T: " + newValue + "is inproper value. T value must > 0 and the proper range is 0 to 500");

	}

	public void change_Tr(int newValue) throws IllegalStateException {
		if (newValue >= 0)
			_Tr = newValue;
		else
			throw new IllegalStateException("The Round trip Delay, Tr: " + newValue + "is inproper value. Tr value must > 0 and has a range of 0 to 1000");

	}

	public void change_Ta(int newValue) throws IllegalStateException {
		if (newValue >= 0)
			_Ta = newValue;
		else
			throw new IllegalStateException("The Absolute Delay in echo-free Connections, Ta: " + newValue + "is inproper value. Ta value must > 0 and has range of 0 to 500");

	}

	public void change_qdu(int newValue) throws IllegalStateException {
		if (newValue >= 1 && newValue <= 14)
			_qdu = newValue;
		else
			throw new IllegalStateException("The Number of Quantization Distortion Units, qdu: " + newValue + "is inproper value.  qdu range is 1 to 14");

	}

	public void change_Ie(int newValue) throws IllegalStateException {

		if (newValue >= 0 && newValue <= 40)
			_Ie = newValue;
		else
			throw new IllegalStateException("The Equipment Impairments Factor, Ie: " + newValue + "is inproper value.  Ie range is 0 to 40");
	}

	public void change_Bpl(double newValue) throws IllegalStateException {
		if (newValue >= 4.3 && newValue <= 40)
			_Bpl = newValue;
		else
			throw new IllegalStateException("The Packet-loss Robustness, Bpl: " + newValue + "is inproper value.  Bpl range is 1 to 40");

	}

	public void change_Ppl(double newValue) throws IllegalStateException {
		// if(newValue >= 0 && newValue <= 20)
		_Ppl = newValue;
		// else
		// throw new
		// IllegalStateException("The Random Packet-loss Probability, Ppl: " +
		// newValue + "is inproper value.  Ppl range is 0 to 100");
	}

	public void change_BurstR(int newValue) throws IllegalStateException {
		if (newValue >= 1 && newValue <= 2)
			_BurstR = newValue;
		else
			throw new IllegalStateException("The Burst Ratio BurstR: " + newValue + "is inproper value.  BurstR range is 1 to 2");
	}

	public void change_Nc(int newValue) throws IllegalStateException {
		if (newValue >= -80 && newValue <= -40)
			_Nc = newValue;
		else
			throw new IllegalStateException("The Circuit Noise referred to 0 dBr-point, Nc: " + newValue + "is inproper value.  Nc range is -80 to -40");

	}

	public void change_Ps(int newValue) throws IllegalStateException {
		if (newValue >= 35 && newValue <= 85)
			_Ps = newValue;
		else
			throw new IllegalStateException("The Room Noise at send side, Ps: " + newValue + "is inproper value.  Ps range is 35 to 85");
	}

	public void change_Pr(int newValue) throws IllegalStateException {
		if (newValue >= 35 && newValue <= 85)
			_Pr = newValue;
		else
			throw new IllegalStateException("The Noise floor at the receive side, Pr: " + newValue + "is inproper value.  Pr range is 35 to 85");

	}

	public void change_A(int newValue) throws IllegalStateException {
		if (newValue >= 0 && newValue <= 20)
			_A = newValue;
		else
			throw new IllegalStateException("The Advantage Factor, A: " + newValue + "is inproper value.  A range is 0 to 20");
	}

	public QOSCodec get_codecObj() {
		return _codecObj;
	}

	public int get_codec() {
		return _codec;
	}

	public int get_SLR() {
		return _SLR;
	}

	public int get_RLR() {
		return _RLR;
	}

	public int get_STMR() {
		return _STMR;
	}

	public int get_LSTR() {
		return _LSTR;
	}

	public int get_Ds() {
		return _Ds;
	}

	public int get_Dr() {
		return _Dr;
	}

	public int get_TELR() {
		return _TELR;
	}

	public int get_WEPL() {
		return _WEPL;
	}

	public int get_T() {
		return _T;
	}

	public int get_Tr() {
		return _Tr;
	}

	public int get_Ta() {
		return _Ta;
	}

	public int get_qdu() {
		return _qdu;
	}

	public int get_Ie() {
		return _Ie;
	}

	public double get_Bpl() {
		return _Bpl;
	}

	public double get_Ppl() {
		return _Ppl;
	}

	public int get_BurstR() {
		return _BurstR;
	}

	public int get_Nc() {
		return _Nc;
	}

	public int get_Nfor() {
		return _Nfor;
	}

	public int get_Ps() {
		return _Ps;
	}

	public int get_Pr() {
		return _Pr;
	}

	public int get_A() {
		return _A;
	}

}
