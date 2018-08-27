package com.dvsts.avaya.processing.core.rtcp;


import com.dvsts.avaya.processing.core.rtcp.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class RTCPPacket2 extends RTCPPacket {

	private static final Logger LOGGER	= LoggerFactory.getLogger(RTCPPacket2.class);



	public RTCPPacket2(String ip, byte[] payload, int remotePort, boolean hopNameLookup) {
		this.remotePort = remotePort;
		time = System.nanoTime();
		fromRtcp = true;
		this.ip = ip;
		this.hopNameLookup = hopNameLookup;

		int mainOffset = 0;
		int totalLength = payload.length;
		boolean keepgoing = true;
		StringBuilder builder = new StringBuilder();
		try {
			type = 0;
			int rollCount = 0;
			while (keepgoing) {
				rollCount++;
				if (rollCount > 5) {
					keepgoing = false;
				}

				byte[] header = new byte[1];
				byte[] packetType = new byte[1];
				byte[] packetLength = new byte[2];

				System.arraycopy(payload, 0 + mainOffset, header, 0, 1);
				System.arraycopy(payload, 1 + mainOffset, packetType, 0, 1);
				System.arraycopy(payload, 2 + mainOffset, packetLength, 0, 2);

				int subtype = (int) (header[0] & 0x1f);
				int thisType = convertSigned(packetType[0]);
				int thisLength = (int) convertToLong(packetLength);

				if (builder.length() > 0) {
					builder.append(", ");
				}
				builder.append(thisType);

				switch (thisType) {
				case 200:
					processSenderReport(payload, mainOffset, subtype);
					break;
				case 201:
					processReceiverReport(payload, mainOffset, subtype);
					break;
				case 202:
					processSourceDescription(payload, mainOffset, subtype);
					break;
				case 203:
					processGoodbye(payload, mainOffset, subtype);
					break;
				case 204:
					builder.append('[').append(subtype).append(']');
					processAppSpecificReport(payload, mainOffset, subtype);
					break;
				default:
					throw new IllegalStateException("RTCP packet from [" + this.ip + ":" + this.remotePort + "] is invalid: packet type=" + thisType);
				}

				mainOffset = mainOffset + (thisLength * 4) + 4;
				if (mainOffset >= totalLength) {
					keepgoing = false;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred", e);
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received compound RTCP packet from [" + this.ip + ":" + this.remotePort + "], ssrc1=" + ssrc1 + ", ssrc2=" + ssrc2 + ", containing " + builder.toString());
		}
		if (this.ssrc1 == null || this.ssrc2 == null || this.ssrc1.equals("0") || this.ssrc2.equals("0")) {
			throw new IllegalStateException("Compound RTCP packet from [" + this.ip + ":" + this.remotePort + "] is invalid: ssrc1=" + ssrc1 + ", ssrc2=" + ssrc2);
		}
	}

	private void processAppSpecificReport(byte[] payload, int offset, int subtype) {
		byte[] ssrc = new byte[4];
		byte[] ssrc2 = new byte[4];

		System.arraycopy(payload, offset + 4, ssrc, 0, 4);
		this.ssrc1 = String.valueOf(convertToLong(ssrc));

		switch (subtype){
		case 4:
			System.arraycopy(payload, offset + 12, ssrc2, 0, 4);
			this.ssrc2 = String.valueOf(convertToLong(ssrc2));

			processSubType4(payload, offset);
			break;
		case 5:
			System.arraycopy(payload, offset + 12, ssrc2, 0, 4);
			this.ssrc2 = String.valueOf(convertToLong(ssrc2));

			processSubType5(payload, offset);
			break;
		}
	}

	private void processGoodbye(byte[] payload, int offset, int count) {
		byte[] ssrc = new byte[4];

		if (count > 0) {
			System.arraycopy(payload, offset + 4, ssrc, 0, 4);
			this.ssrc1 = String.valueOf(convertToLong(ssrc));
		}

		type = 1;
	}

	private void processSourceDescription(byte[] payload, int offset, int count) {
		if (count > 0) {
			byte[] length = new byte[1];
			System.arraycopy(payload, 9 + offset, length, 0, 1);
			int thisLength = (int) length[0];
			if (thisLength < 0)
				thisLength = thisLength + 256;
			byte[] namebytes = new byte[thisLength];
			System.arraycopy(payload, 10 + offset, namebytes, 0, thisLength);
			// yank the name out of the source report...
			String name = new String(namebytes);
			if (name.startsWith("ex")) {
				setType("phone");
				try {
					int iat = name.indexOf("@");
					if (iat == -1) {
						setName(name.substring(3));
					} else {
						setName(name.substring(3, iat));
					}
				} catch (Exception e) {
					LOGGER.warn("Name during problem was " + name, e);
				}
			} else if (name.startsWith("sip")) {
				setType("sip phone");
				try {
					int iat = name.indexOf("@");
					if (iat == -1) {
						setName(name.substring(4));
					} else {
						setName(name.substring(4, iat));
					}
				} catch (Exception e) {
					LOGGER.warn("Name during problem was " + name, e);
				}
			} else if (name.startsWith("pq")) {
				setType("PQOS");
				setName(name.substring(2));
			}
		}
	}

	private void processSenderReport(byte[] payload, int offset, int count) {
		byte[] ssrc2 = new byte[4];
		byte[] fractionLost = new byte[1];
		byte[] cnpl = new byte[3];
		byte[] ehsnr = new byte[4];
		byte[] jitter = new byte[4];
		byte[] lsr = new byte[4];
		byte[] dlsr = new byte[4];
		byte[] ssrc = new byte[4];

		System.arraycopy(payload, offset + 4, ssrc, 0, 4);
		this.ssrc1 = String.valueOf(convertToLong(ssrc));

		// +8 = NTP timestamp MSW
		// +12 = NTP timestamp LSW
		// +16 = RTP Timestamp
		// +20 = Sender packet count
		// +24 = Sender octet count
		// +28 = ssrc2...

		byte[] ts = new byte[8];
		System.arraycopy(payload, offset + 8, ts, 0, 8);
		byte[] sr = new byte[4];
		System.arraycopy(ts, 2, sr, 0, 4);
		this.sr = stringArray(sr);

		if (count > 0) {
			System.arraycopy(payload, offset + 28, ssrc2, 0, 4);
			System.arraycopy(payload, offset + 32, fractionLost, 0, 1);
			System.arraycopy(payload, offset + 33, cnpl, 0, 3);
			System.arraycopy(payload, offset + 36, ehsnr, 0, 4);
			System.arraycopy(payload, offset + 40, jitter, 0, 4);
			System.arraycopy(payload, offset + 44, lsr, 0, 4);
			System.arraycopy(payload, offset + 48, dlsr, 0, 4);

			this.ssrc2 = stringArray(ssrc2);

			long jit = convertToLong(jitter);
			if (jit > 500L)
				jit = 500L;
			metricMap.put("jitter", String.valueOf(jit));

			this.loss = stringArray(fractionLost);
			int pls = (int) ((Util.parseToInt(loss, 0) * 100d) / 256d);

			metricMap.put("pcktLossPct", String.valueOf(pls));

			this.cumulativeLoss = stringArray(cnpl);
			metricMap.put("cumulativePktLoss", this.cumulativeLoss);

			this.lsr = stringArray(lsr);
			this.dlsr = stringArray(dlsr);

		}
	}

	private void processReceiverReport(byte[] payload, int offset, int count) {
		byte[] ssrc2 = new byte[4];
		byte[] fractionLost = new byte[1];
		byte[] cnpl = new byte[3];
		byte[] ehsnr = new byte[4];
		byte[] jitter = new byte[4];
		byte[] lsr = new byte[4];
		byte[] dlsr = new byte[4];
		byte[] ssrc = new byte[4];

		System.arraycopy(payload, offset, ssrc, 0, 4);
		this.ssrc1 = String.valueOf(convertToLong(ssrc));

		if (count > 0) {
			System.arraycopy(payload, offset + 8, ssrc2, 0, 4);
			System.arraycopy(payload, offset + 12, fractionLost, 0, 1);
			System.arraycopy(payload, offset + 13, cnpl, 0, 3);
			System.arraycopy(payload, offset + 16, ehsnr, 0, 4);
			System.arraycopy(payload, offset + 20, jitter, 0, 4);
			System.arraycopy(payload, offset + 24, lsr, 0, 4);
			System.arraycopy(payload, offset + 28, dlsr, 0, 4);

			byte[] ts = new byte[8];
			System.arraycopy(payload, 8 + offset, ts, 0, 8);
			byte[] sr = new byte[4];
			System.arraycopy(ts, 2, sr, 0, 4);

			this.ssrc2 = stringArray(ssrc2);

			long jit = convertToLong(jitter);
			if (jit > 500L)
				jit = 500L;
			metricMap.put("jitter", String.valueOf(jit));

			this.loss = stringArray(fractionLost);
	       int pls = (int) ((Util.parseToInt(this.loss, 0) * 100d) / 256d);


			metricMap.put("pcktLossPct", String.valueOf(pls));

			this.cumulativeLoss = stringArray(cnpl);
			metricMap.put("cumulativePktLoss", this.cumulativeLoss);

			this.lsr = stringArray(lsr);
			this.dlsr = stringArray(dlsr);
			this.sr = stringArray(sr);
		}
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
		for (int x = 0; x < byte4.length; x++) {
			if (inclusion[x] == 1) {
				byte[] theseBytes = new byte[byte4[x].byteLength];
				for (int y = 0; y < byte4[x].byteLength; y++) {
					theseBytes[y] = payload[nextOffset + y + offset];
				}
				if (byte4[x].name.equals("remoteIpAndPort")) {
					reportedIp = convertToIp(theseBytes);
					reportedPort = String.valueOf(convertToPort(theseBytes, 4));
					metricMap.put("remoteAddress", reportedIp);
					metricMap.put("remotePort", reportedPort);
				} else if (byte4[x].name.equals("burstLoss")) {
					long value = convertToLong(theseBytes);
					long lost = (value >> 16) & 0xffff;
					long rcvd = value & 0xffff;
					long total = lost + rcvd;
					if (total > 0) {
						double density = ((double) (lost * 100) / total);
						metricMap.put("burstDensity",  String.valueOf(density));
					} else {
						metricMap.put("burstDensity",  "0");						
					}
					metricMap.put("burstDuration", String.valueOf(total));
				} else if (byte4[x].name.equals("gapLoss")) {
					long value = convertToLong(theseBytes);
					long lost = (value >> 16) & 0xffff;
					long rcvd = value & 0xffff;
					long total = lost + rcvd;
					if (total > 0) {
						double density = ((double) (lost * 100) / total);
						metricMap.put("gapDensity",  String.valueOf(density));
					} else {
						metricMap.put("gapDensity",  "0");						
					}
					metricMap.put("gapDuration", String.valueOf(total));
				} else {
					long value = convertToLong(theseBytes);
					if (byte4[x].name.equals("payloadType") && isPQOS()) {
						// transponder uses 100 for g726-32 and 102 for g726-40
						if (value == 100) {
						 	codec = QOSStandardCodecRef._G726_32;
						} else if (value == 102) {
							codec = QOSStandardCodecRef._G726_40;
						}
					}
					metricMap.put(byte4[x].name, String.valueOf(value));
				}
				nextOffset = nextOffset + byte4[x].byteLength;
			}
		}
	}

	static ByteValue[]	byte4	= new ByteValue[] { new ByteValue("rtpPacketCount", 4), // Received RTP Packets
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
			new ByteValue("remoteIpAndPort", 6), // IPv4 Address & RTCP Port Number of the Remote Endpoint
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
			new ByteValue("burstLoss", 4), // burst loss density/duration
			new ByteValue("gapLoss", 4) }; // gap loss density/duration


	@Override
	public String toString() {
		return "RTCPPacket2{" +
				"ip='" + ip + '\'' +
				", ssrc1='" + ssrc1 + '\'' +
				", ssrc2='" + ssrc2 + '\'' +
				", jitter='" + jitter + '\'' +
				", rtt='" + rtt + '\'' +
				", loss='" + loss + '\'' +
				", cumulativeLoss='" + cumulativeLoss + '\'' +
				", time=" + time +
				", lsr='" + lsr + '\'' +
				", dlsr='" + dlsr + '\'' +
				", codec='" + codec + '\'' +
				", sr='" + sr + '\'' +
				", category='" + category + '\'' +
				", traceroute=" + traceroute +
				", type=" + type +
				", remotePort=" + remotePort +
				", metricMap=" + metricMap +
				", name1='" + name1 + '\'' +
				", name2='" + name2 + '\'' +
				", transponderName='" + transponderName + '\'' +
				", type1='" + type1 + '\'' +
				", type2='" + type2 + '\'' +
				", hopNameLookup=" + hopNameLookup +
				", fromRtcp=" + fromRtcp +
				", reportedIp='" + reportedIp + '\'' +
				", reportedPort='" + reportedPort + '\'' +
				", owd='" + owd + '\'' +
				", burstLoss='" + burstLoss + '\'' +
				", burstDensity='" + burstDensity + '\'' +
				", gapLoss='" + gapLoss + '\'' +
				", gapDensity='" + gapDensity + '\'' +
				'}';
	}


	public Map<String,Object> toMap(){
		Map<String,Object> result = new HashMap<>();

		result.put("ssrc1",ssrc1);
		result.put("ssrc2",ssrc2);
		result.put("jitter",jitter);
		result.put("rtt",rtt);
		result.put("loss",loss);
		result.put("cumulativeLoss",cumulativeLoss);
		result.put("time",time);
		result.put("lsr",lsr);
		result.put("dlsr",dlsr);
		result.put("codec",codec);
		result.put("sr",sr);
		result.put("name1",name1);
		result.put("name2",name2);
		result.put("transponderName",transponderName);
		result.put("type1",type1);
		result.put("type2",type2);
		result.put("hopNameLookup",hopNameLookup);
		result.put("fromRtcp",fromRtcp);
		result.put("reportedIp",reportedIp);
		result.put("reportedPort",reportedPort);
		result.put("owd",owd);
		result.put("burstLoss",burstLoss);
		result.put("burstDensity",burstDensity);
		result.put("gapLoss",gapLoss);
		result.put("gapDensity",gapDensity);


		return result;
	}


}
