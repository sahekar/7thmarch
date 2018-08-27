package com.dvsts.avaya.processing.core.rtcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//import net.conet.util.IPStringManipulation;

public class RTCPPacket {
    //MOS = Mean Opinion Score
	private static final Logger LOGGER			=  LoggerFactory.getLogger((RTCPPacket.class));
	protected String					ip;
	protected String					ssrc1;
	protected String					ssrc2;
	protected String					jitter;
	protected String					rtt; // round trip time
	protected String					loss; // packet loss
	protected String					cumulativeLoss;
	public long						time;
	protected String					lsr;
	protected String					dlsr;
	protected String					codec;
	protected String					sr;
	protected String					category		= null;
	public ArrayList<TraceHop>		traceroute		= null;
	public int						type			= 0;
	protected int						remotePort		= 0;
	protected HashMap<String, String>	metricMap		= new HashMap<String, String>();
	private static Random				rand			= new Random();

	protected String					name1			= null;
	protected String					name2			= null;
	protected String					transponderName	= null;
	protected String					type1			= null;
	protected String					type2			= null;
	protected boolean					hopNameLookup	= true;
	protected boolean					fromRtcp		= false;
	protected String					reportedIp		= "";
	protected String					reportedPort	= "";
	protected String					owd				= "";
	protected String					burstLoss		= "";
	protected String					burstDensity 	= "";
	protected String					gapLoss 		= "";
	protected String					gapDensity		= "";


	public int getRemotePort() {
		return remotePort;
	}

	public RTCPPacket() {

	}

	// for testing
	public RTCPPacket(long ssrcBase1, long ssrcBase2, boolean hopNameLookup) {
		ssrc1 = String.valueOf(ssrcBase1);
		ssrc2 = String.valueOf(ssrcBase2);
		this.hopNameLookup = hopNameLookup;
		ip = "0.0.0.0";
		fromRtcp = false;
		metricMap.put("ip", ip);
		jitter = String.valueOf(rand.nextInt(500));
		metricMap.put("jitter", jitter);
		rtt = String.valueOf(rand.nextInt(100));
		metricMap.put("rtt", rtt);
		loss = String.valueOf(rand.nextInt(10));
		metricMap.put("pcktLossPct", loss);
		time = System.nanoTime();
		lsr = "0";
		dlsr = "0";
		sr = "0";
		type = 0;
		remotePort = 0;
		traceroute = new ArrayList<TraceHop>();
		traceroute.add(hopNameLookup ? new TraceHop(0, ip, 0, "hostname") : new TraceHop(0, ip, 0, ""));
		for (int x = 0; x < 5; x++) {
			if (x == 3) {
				String randAddr  = "localhost:3320";
				traceroute.add(hopNameLookup ? new TraceHop(x + 1, randAddr, 10, "hostname-" + x) : new TraceHop(0, randAddr, 0, ""));
			} else {
				traceroute.add(hopNameLookup ? new TraceHop(x + 1, "1.2.3.4", 10, "hostname-" + x) : new TraceHop(0, ip, 0, ""));
			}
		}
	}

	public RTCPPacket(String ip, byte[] payload, int remotePort, boolean hopNameLookup) {
		this.remotePort = remotePort;
		time = System.nanoTime();
		fromRtcp = true;
		this.ip = ip;
		this.hopNameLookup = hopNameLookup;

		byte[] header = new byte[1];
		byte[] packetType = new byte[1];
		byte[] length = new byte[2];
		byte[] ssrc = new byte[4];

		int mainOffset = 0;

		byte[] rtpTimestamp = new byte[4];

		try {

			System.arraycopy(payload, 0 + mainOffset, header, 0, 1);
			System.arraycopy(payload, 1 + mainOffset, packetType, 0, 1);
			System.arraycopy(payload, 2 + mainOffset, length, 0, 2);
			System.arraycopy(payload, 4 + mainOffset, ssrc, 0, 4);
			System.arraycopy(payload, 20 + mainOffset, rtpTimestamp, 0, 4);

			byte[] ts = new byte[8];
			System.arraycopy(payload, 8 + mainOffset, ts, 0, 8);

			byte[] sr = new byte[4];
			System.arraycopy(ts, 2, sr, 0, 4);

			int v = (int) header[0];
			int version = convertSigned(header[0]);
			boolean[] head = new boolean[8];
			for (int x = 0; x < 8; x++) {
				int i = v % 2;
				if (i == 1) {
					head[7 - x] = true;
				} else {
					head[7 - x] = false;
				}
				v /= 2;
			}
			int t = convertSigned(packetType[0]);

			long ssrcnum = convertToLong(ssrc);

			if (t == 200) {
				type = 0;

				byte[] ssrc1 = new byte[4];
				byte[] fractionLost = new byte[1];
				byte[] cnpl = new byte[3];
				byte[] ehsnr = new byte[4];
				byte[] jitter = new byte[4];
				byte[] lsr = new byte[4];
				byte[] dlsr = new byte[4];

				int nws = 4 * 7;
				System.arraycopy(payload, mainOffset + nws, ssrc1, 0, 4);
				System.arraycopy(payload, mainOffset + nws + 4, fractionLost, 0, 1);
				System.arraycopy(payload, mainOffset + nws + 5, cnpl, 0, 3);
				System.arraycopy(payload, mainOffset + nws + 8, ehsnr, 0, 4);
				System.arraycopy(payload, mainOffset + nws + 12, jitter, 0, 4);
				System.arraycopy(payload, mainOffset + nws + 16, lsr, 0, 4);
				System.arraycopy(payload, mainOffset + nws + 20, dlsr, 0, 4);

				this.ssrc1 = String.valueOf(ssrcnum);
				this.ssrc2 = stringArray(ssrc1);
				int jit = (int) convertToLong(jitter);
				jit = jit / 10;
				if (jit > 500)
					jit = 500;

				this.loss = stringArray(fractionLost);
				metricMap.put("pcktLossPct", this.loss);
				this.cumulativeLoss = stringArray(cnpl);
				metricMap.put("cumulativePktLoss", this.cumulativeLoss);
				this.lsr = stringArray(lsr);
				this.dlsr = stringArray(dlsr);
				this.sr = stringArray(sr);

				// this is a packet we are expecting with one ssrc destination...
				if (version == 129) {
					// begin reading the chunks and dispatch chunk type to different sub processors...
					int chunkOffset = 13 * 4;
					while (chunkOffset < payload.length) {
						int chunkType = convertSigned(payload[chunkOffset]);
						int chunkLength = (int) convertToLong(new byte[] { payload[chunkOffset + 2], payload[chunkOffset + 3] });
						if (chunkType == 133) { // 85 Locator
							processSubType5(payload, chunkOffset);
						} else if (chunkType == 131) {
						} else if (chunkType == 132) {
							processSubType4(payload, chunkOffset);
						} else if (chunkType == 129) {
							// pull out the extension number...
							int charCount = convertSigned(payload[chunkOffset + 9]);
							byte[] chars = new byte[charCount];
							try {
								if (chunkOffset + 12 + charCount > payload.length) {
									charCount = payload.length - (chunkOffset + 11);
								}
								System.arraycopy(payload, chunkOffset + 10, chars, 0, charCount);
								String id = new String(chars);
								if (id.startsWith("ex")) {
									setType("phone");
									try {
										int iat = id.indexOf("@");

										if (iat == -1) {
											setName(id.substring(3));
										} else {
											setName(id.substring(3, iat));
										}
									} catch (Exception e) {
										LOGGER.warn("Name during problem was " + id, e);
									}
								} else if (id.startsWith("sip")) {
									setType("sip phone");
									try {
										int iat = id.indexOf("@");
										if (iat == -1) {
											setName(id.substring(4));
										} else {
											setName(id.substring(4, iat));
										}
									} catch (Exception e) {
										LOGGER.warn("Name during problem was " + id, e);
									}
								}
							} catch (Exception e) {
								LOGGER.debug("Error parsing packet", e);
								try {
									byte[] newchars = new byte[charCount - 7];
									System.arraycopy(payload, chunkOffset + 10, newchars, 0, charCount - 7);
								} catch (Exception another) {
									LOGGER.debug("Error occurred", another);
								}
							}
						} else {
						}
						int maxLength = 10;
						if (maxLength + chunkOffset > payload.length) {
							maxLength = payload.length - chunkOffset;
						}
						chunkOffset = chunkOffset + ((chunkLength + 1) * 4);
					}
				}

			} else if (t == 203) {

			} else if (t == 201) {
				type = 1;
				this.ssrc1 = String.valueOf(ssrcnum);
			} else if (t == 202) {
				LOGGER.debug("Type 202");
				type = 3;
			} else if (t == 204) {
				LOGGER.debug("Type 204");
			} else {
				type = 4;
				LOGGER.debug("Unknown type: " + t);
			}
		} catch (Exception packetDecode) {
			LOGGER.debug("Decode error from " + ip, packetDecode);
		}

		if (null == this.ssrc1 || null == this.ssrc2) {
			throw new IllegalArgumentException("SSRC1 or SSRC2 cannot be null. ssrc1="+ssrc1+"  :  ssrc2="+ssrc2);
		}
	}

	public String getCategory() {
		return category;
	}

	public void setCodec(String codec) {
		this.codec = codec;
	}

	public Double getPrecalculatedMOS() {
		return null;
	}

	public String getName() {
		return name1;
	}

	public String getName2() {
		return name2;
	}

	public String getType() {
		return type1;
	}

	public String getType2() {
		return type2;
	}

	public void setType(String t) {
		type1 = t;
	}

	public void setType2(String t) {
		type2 = t;
	}

	public void setName(String n) {
		name1 = n;
	}

	public void setTransponderName(String n) {
		transponderName = n;
	}

	public void setName2(String n) {
		name2 = n;
	}

	public boolean isFromRtcp() {
		return fromRtcp;
	}

	public void setMetric(String name, String value) {
		metricMap.put(name, value);
	}

	public String getMetric(String which) {
		return (String) metricMap.get(which);
	}

	public String getSsrc1() {
		return ssrc1;
	}

	public String getSsrc2() {
		return ssrc2;
	}

	public String getJitter() {
		return jitter;
	}

	public String getRtt() {
		return rtt;
	}

	public String getLoss() {
		return loss;
	}

	public String getCumulativeLoss() {
		return cumulativeLoss;
	}

	public long getTime() {
		return time;
	}

	public String getLsr() {
		return lsr;
	}

	public String getDlsr() {
		return dlsr;
	}

	public String getCodec() {
		return codec;
	}

	public String getIp() {
		return ip;
	}

	public String getSr() {
		return sr;
	}

	public boolean isPQOS() {
		if (((type1 != null) && type1.equals("PQOS")) || ((type2 != null) && type2.equals("PQOS"))) {
			return true;
		}
		return false;
	}

	public String getReportedIp() {
		return reportedIp;
	}

	public String getReportedPort() {
		return reportedPort;
	}

	protected int convertSigned(byte b) {
		int r = (int) b;
		if (r < 0) {
			return r + 256;
		}
		return r;
	}

	protected long convertToLong(byte[] b) {
		long ret = 0;

		if (b.length > 3) {
			ret += convertSigned(b[0]) * 16777216;
			ret += convertSigned(b[1]) * 65536;
			ret += convertSigned(b[2]) * 256;
			ret += convertSigned(b[3]);
		} else if (b.length > 2) {
			ret += convertSigned(b[0]) * 65536;
			ret += convertSigned(b[1]) * 256;
			ret += convertSigned(b[2]);
		} else if (b.length > 1) {
			ret += convertSigned(b[0]) * 256;
			ret += convertSigned(b[1]);
		} else {
			ret = convertSigned(b[0]);
		}

		if (ret < 0) {
			ret = ret + 4294967296L;
		}

		return ret;
	}

	protected String stringArray(byte[] array) {
		return String.valueOf(convertToLong(array));
	}

	static ByteValue[]	byte132	= new ByteValue[] { new ByteValue("rtpPacketCount", 4), // Received RTP Packets
			new ByteValue("rtpOctetCount", 4), // Received RTP Octets
			new ByteValue("rtd", 2), // Round Trip Time (RTT)
			new ByteValue("jitterBufferDelay", 2), // Jitter Buffer Delay
			new ByteValue("largestSequenceJump", 1), // Largest Sequence Jump
			new ByteValue("largestSequenceFall", 1), // Largest Sequence Fall
			new ByteValue("rsvpReceiverStatus", 1), // RSVP Status
			new ByteValue("maxJitter", 4), // Maximum Jitter
			new ByteValue("jitterBufferUnderrun", 1), // Number of Jitter Buffer Underruns
			new ByteValue("jitterBufferOverrun", 1), // Number of Jitter Buffer Overruns
			new ByteValue("seqJumpInstances", 4), // Number of Sequence Jump Instances
			new ByteValue("seqFallInstances", 4), // Number of Sequence Fall Instances
			new ByteValue("echoTailLength", 1), // Echo Tail Length
			new ByteValue("remoteAddress", 4), // IPv4 Address
			new ByteValue("remotePort", 2), // RTCP Port Number of the Remote Endpoint
			new ByteValue("payloadType", 1), // RTP Payload Type
			new ByteValue("frameSize", 1), // Frame Size
			new ByteValue("rtpTTL", 1), // Time To Live / Hop Count
			new ByteValue("rtpDSCP", 1), // DiffServ Code Point
			new ByteValue("rtp8021D", 2), // 802.1D
			new ByteValue("mediaEncryption", 1), // Media Encryption
			new ByteValue("silenceSuppression", 1), // Silence Suppression
			new ByteValue("echoCancellation", 1), // Acoustic Echo Cancellation
			new ByteValue("inRtpSrcPort", 2), // RTP Source Port Number of the Incoming Stream
			new ByteValue("inTypDestPort", 2), // RTP Destination Port Number of the Incoming Stream
			new ByteValue("remoteIpv6Address", 16), // ipv6 address
			new ByteValue("remoteIpv6Port", 2), // ipv6 port
			new ByteValue("ipv6FlowLabel", 3),  // ipv6 flow label
			new ByteValue("owd", 2), // one way delay
			new ByteValue("burstDensity", 1), // burst loss density
			new ByteValue("burstDuration", 3), // burst loss duration
			new ByteValue("gapDensity", 1), // gap loss density
			new ByteValue("gapDuration", 3) }; // gap loss duration

	static class ByteValue {

		String	name		= "";
		int		byteLength	= 0;

		public ByteValue(String name, int byteLength) {
			this.name = name;
			this.byteLength = byteLength;
		}
	}

	protected int[] breakdown(byte[] input) {
		int[] ret = new int[input.length * 8];
		for (int x = 0; x < input.length; x++) {
			byte m = input[x];
			int[] to = breakdownByte(m);
			for (int y = 0; y < 8; y++) {
				ret[x * 8 + y] = to[y];
			}
		}
		return ret;
	}

	protected int[] breakdownByte(byte input) {
		int[] out = new int[8];
		int len = 0;
		for (int k = 7; k >= 0; k--) {
			out[len++] = (int) ((input >>> k) & 0x01);
		}
		return out;
	}

	protected String convertToIp(byte[] b) {
		byte[] real = b;

		int octet1 = convertSigned(real[0]);
		int octet2 = convertSigned(real[1]);
		int octet3 = convertSigned(real[2]);
		int octet4 = convertSigned(real[3]);

		return String.valueOf(octet1) + "." + String.valueOf(octet2) + "." + String.valueOf(octet3) + "." + String.valueOf(octet4);
	}

	protected long convertToPort(byte[] b, int offset) {
		long ret = 0;

		if (b.length - offset > 1) {
			ret += convertSigned(b[offset + 0]) * 256;
			ret += convertSigned(b[offset + 1]);
		} else {
			ret = convertSigned(b[offset + 0]);
		}

		if (ret < 0) {
			ret = ret + 4294967296L;
		}

		return ret;
	}

	protected void processSubType4(byte[] payload, int offset) {
		// yank the RTT and Jitter...
		byte[] metricMask = new byte[4];
		metricMask[0] = payload[offset + 16];
		metricMask[1] = payload[offset + 17];
		metricMask[2] = payload[offset + 18];
		metricMask[3] = payload[offset + 19];

		int[] inclusion = breakdown(metricMask);
		int nextOffset = 20;
		for (int x = 0; x < byte132.length; x++) {
			if (inclusion[x] == 1) {
				byte[] theseBytes = new byte[byte132[x].byteLength];
				for (int y = 0; y < byte132[x].byteLength; y++) {
					theseBytes[y] = payload[nextOffset + y + offset];
				}
				if (byte132[x].name.equals("remoteAddress")) {
					reportedIp = convertToIp(theseBytes);
					reportedPort = String.valueOf(convertToPort(theseBytes, 4));
					metricMap.put("remoteAddress", reportedIp);
					metricMap.put("remotePort", reportedPort);
				} else {
					long value = convertToLong(theseBytes);
					metricMap.put(byte132[x].name, String.valueOf(value));
				}
				nextOffset = nextOffset + byte132[x].byteLength;
			}
		}
	}

	protected void processSubType5(byte[] payload, int offset) {
		byte[] metricMask = new byte[4];
		metricMask[0] = payload[offset + 16];
		metricMask[1] = payload[offset + 17];
		metricMask[2] = payload[offset + 18];
		metricMask[3] = payload[offset + 19];

		int[] inclusion = breakdown(metricMask);
		int nextOffset = 20 + offset;

		if (inclusion[0] == 1) {
			nextOffset = nextOffset + 4;
		}
		if (inclusion[1] == 1 && inclusion[2] == 1) {
			// really pull the traceroute with the offset...
			int traceLength = convertSigned(payload[nextOffset]);
			nextOffset = nextOffset + 1;
			traceroute = new ArrayList<TraceHop>();
			traceroute.add(hopNameLookup ? new TraceHop(0, ip, 0) : new TraceHop(0, ip, 0, ""));
			try {
				for (int x = 0; x < traceLength; x++) {
					int traceOffset = nextOffset + (x * 6);
					int octet1 = convertSigned(payload[traceOffset]);
					int octet2 = convertSigned(payload[traceOffset + 1]);
					int octet3 = convertSigned(payload[traceOffset + 2]);
					int octet4 = convertSigned(payload[traceOffset + 3]);
					byte[] hopByte = new byte[] { payload[traceOffset + 4], payload[traceOffset + 5] };
					int msHop = (int) convertToLong(hopByte);
					String hopIp = String.valueOf(octet1) + "." + String.valueOf(octet2) + "." + String.valueOf(octet3) + "." + String.valueOf(octet4);
					traceroute.add(hopNameLookup ? new TraceHop(x + 1, hopIp, msHop) : new TraceHop(x + 1, hopIp, msHop, ""));
				}
			} catch (Exception e) {
				LOGGER.debug("Error parsing tracehop", e);
			}
		}
	}
}
