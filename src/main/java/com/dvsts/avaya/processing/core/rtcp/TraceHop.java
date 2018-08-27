package com.dvsts.avaya.processing.core.rtcp;

//import net.conet.infastructure.InfastructureModule;

///import net.conet.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class TraceHop {

	public static final Logger LOGGER			= LoggerFactory.getLogger((TraceHop.class));
	private int											position;
	private String										ip;
	private String										reportIp;
	private int											time;
	private String										hostname;
	private static HashMap<String, TraceHopIPObject>	ipHostnameMap	= new HashMap<String, TraceHopIPObject>();

	public int getPosition() {
		return position;
	}

	/**
	 * Returns the IP for this TraceHop object
	 * 
	 * @return IP address to report
	 */
	public String getIp() {
		return this.reportIp;
	}

	public void setReportIp(String ip) {
		// intentionally not updating the host name
		// this call is only used to override the IP address of a perspective agent
		this.reportIp = ip;
	}
	
	public int getTime() {
		return time;
	}

	public String getHostname() {
		return hostname;
	}

	/**
	 * Overrides equals compares actual values (ip/hop)
	 *
	 * @return true if match
	 */
	public boolean equals(TraceHop compareTraceHop) {
		if (compareTraceHop == null)
			return false;

		return ((this.position == compareTraceHop.position) && (this.ip.equals(compareTraceHop.ip)));
	}

	public TraceHop(int position, final String ip, int time) {
		this(position, ip, time, "");

		// TODO:   String host = InfastructureModule.getAgentNameForIp(ip);
		String host ="";
		if (host == null || host.equals("")) {

			// TODO: think do we need this all
			/*if (ipHostnameMap.containsKey(ip)) {
				if (ipHostnameMap.get(ip).getExpireTime() < System.currentTimeMillis()) {
					// Remove the entry and do a find again, the expire time is
					// set to 1 hour.
					synchronized (ipHostnameMap) {
						LOGGER.debug("BKB: Time to remove entry for {} CurrTime: {} ExpTime: {}", ip, System.currentTimeMillis(), ipHostnameMap.get(ip).getExpireTime());
						ipHostnameMap.remove(ip);
					}
					ThreadPool.launch("TraceHop:findHostName", new Runnable() {
						public void run() {
							findHostName(ip);
						}
					});
				} else {
					host = ipHostnameMap.get(ip).getHostName();
				}
			} else {
				ThreadPool.launch("TraceHop:findHostName", new Runnable() {
					public void run() {
						findHostName(ip);
					}
				});
			}*/
		}
		this.hostname = host;
	}

	public TraceHop(int position, String ip, int time, String hostname) {
		this.position = position;
		this.ip = ip;
		this.reportIp = ip;
		this.time = time;
		this.hostname = hostname;
	}

	private void findHostName(String ip) {
		String host = "";
		try {
			synchronized (ipHostnameMap) {
				if (ipHostnameMap.containsKey(ip))
					return;
				ipHostnameMap.put(ip, new TraceHopIPObject("", (System.currentTimeMillis() + (60L * 60L * 1000L))));
			}
			InetAddress addr = InetAddress.getByName(ip);
			host = addr.getHostName();
			synchronized (ipHostnameMap) {
				TraceHopIPObject ipObj = ipHostnameMap.get(ip);
				ipObj.setHostName(host);
				// Set exprire time to 1 hour from now
				ipObj.setExpireTime((System.currentTimeMillis() + (60L * 60L * 1000L)));
				ipHostnameMap.put(ip, ipObj);
			}
		} catch (UnknownHostException e) {
			LOGGER.error("Unknown Host in trace hop: ", e);
			synchronized (ipHostnameMap) {
				if (ipHostnameMap.containsKey(ip))
					ipHostnameMap.remove(ip);
			}
		} catch (SecurityException e) {
			LOGGER.error("Security issue: ", e);
			synchronized (ipHostnameMap) {
				if (ipHostnameMap.containsKey(ip))
					ipHostnameMap.remove(ip);
			}
		}
	}

	private static class TraceHopIPObject {

		private String	hostName	= "";
		private long	expireTime	= 0L;

		public TraceHopIPObject(String hostName, long expireTime) {
			this.expireTime = expireTime;
			this.hostName = hostName;
		}

		public String getHostName() {
			return hostName;
		}

		public void setHostName(String hostName) {
			this.hostName = hostName;
		}

		public long getExpireTime() {
			return expireTime;
		}

		public void setExpireTime(long expireTime) {
			this.expireTime = expireTime;
		}

	}
}
