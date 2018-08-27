package com.dvsts.avaya.processing.core.rtcp.util;


import com.dvsts.avaya.processing.core.rtcp.CurrentStackTrace;
import com.dvsts.avaya.processing.core.rtcp.DiffObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	private static final Logger LOGGER	= LoggerFactory.getLogger(Util.class);

	private static Pattern	longPattern	= Pattern.compile("[-+]?\\d+");

	/**
	 * Private default constructor as this is a class of static helper methods.
	 */
	private Util() {
	}

	/**
	 * Compare the new list to the current list. The returned array will be a list of DiffObjects showing the differences in the new list FROM the current list. This implementation only returns items
	 * ADDED or REMOVED. For a diff to have MODIFIED objects it would have to specifically differentiate between REMOVED and MODIFIED objects via a static variable(s)
	 *
	 * @param currentList
	 * @param newList
	 * @return
	 */
	public static ArrayList<DiffObject> diffArray(ArrayList<? extends Comparable<Object>> currentList, ArrayList<? extends Comparable<Object>> newList) {
		ArrayList<DiffObject> diffs = new ArrayList<DiffObject>();

		ArrayList<Object> clearableNewList = new ArrayList<Object>();
		ArrayList<Object> removeList = new ArrayList<Object>();
		for (Object item : newList) {
			clearableNewList.add(item);
		}

		for (Comparable<Object> item : currentList) {
			boolean itemNotFound = true;
			for (Object newItem : clearableNewList) {
				if (item.compareTo(newItem) == 0) {
					removeList.add(newItem);
					itemNotFound = false;
				}
			}
			if (itemNotFound) {
				diffs.add(new DiffObject(DiffObject.DifferenceType.REMOVED, item));
			}
		}
		for (Object item : removeList) {
			clearableNewList.remove(item);
		}

		for (Object item : clearableNewList) {
			diffs.add(new DiffObject(DiffObject.DifferenceType.ADDED, item));
		}
		return diffs;
	}

	public static String pad64(String input) {
		// pad the checkpoint number with enough zeroes for 64 bit encoding...
		input = "00000000000000000000" + input;
		return input.substring(input.length() - 20);
	}

	public static String pad64(long nInput) {
		return pad64(String.valueOf(nInput));
	}

	public static boolean is64(String input) {
		if (input.equals(pad64(input))) {
			return true;
		}
		return false;
	}

	public static int parseToInt(String sVal, int iDefault) {
		int iVal = iDefault;
		try {
			if (sVal != null && !sVal.isEmpty()) {
				iVal = Integer.parseInt(sVal);
			}
		} catch (NumberFormatException e) {
			LOGGER.debug("Invalid numeric value " + sVal, e);
		}
		return iVal;
	}

	public static short parseToShort(String sVal, short nDefault) {
		short nVal = nDefault;
		try {
			if (sVal != null && !sVal.isEmpty()) {
				nVal = Short.parseShort(sVal);
			}
		} catch (NumberFormatException e) {
			LOGGER.debug("Invalid numeric value " + sVal, e);
		}
		return nVal;
	}

	public static long parseToLong(String sVal, long lDefault) {
		long lVal = lDefault;
		try {
			if (sVal != null && !sVal.isEmpty()) {
				lVal = Long.parseLong(sVal);
			}
		} catch (NumberFormatException e) {
			LOGGER.debug("Invalid numeric value " + sVal, e);
		}
		return lVal;
	}

	public static float parseToFloat(String sVal, float nDefault) {
		float nVal = nDefault;
		try {
			if (sVal != null) {
				nVal = Float.parseFloat(sVal);
			}
		} catch (NumberFormatException e) {
		}
		return nVal;
	}

	public static double parseToDouble(String sVal, double nDefault) {
		double nVal = nDefault;
		try {
			if (sVal != null && !sVal.isEmpty()) {
				nVal = Double.parseDouble(sVal);
			}
		} catch (NumberFormatException e) {
			LOGGER.debug("Invalid numeric value " + sVal, e);
		}
		return nVal;
	}

	public static int compare(long n1, long n2) {
		return n1 < n2 ? -1 : n1 == n2 ? 0 : 1;
	}

	public static boolean isNull(String str) {
		return str == null || str.equals("");
	}

	public static String join(String sSep, String[] sStrings) {
		String sResult = "[null]";
		if (sSep == null) {
			sSep = "";
		}
		if (sStrings != null && sStrings.length > 0) {
			sResult = (sStrings[0] == null) ? "[null]" : sStrings[0];
			for (int iK = 1; iK < sStrings.length; ++iK) {
				sResult += sSep + sStrings[iK];
			}
		}
		return sResult; // join() exit
	}

	public static String join(String sSep, List<String> sStrings) {
		String sResult = "[null]";
		if (sSep == null) {
			sSep = "";
		}
		if (sStrings != null && sStrings.size() > 0) {
			sResult = sStrings.get(0);
			for (int i = 1; i < sStrings.size(); ++i) {
				sResult += sSep + sStrings.get(i);
			}
		}
		return sResult; // join() exit
	}

	public static Color getColorFromString(String color) {
		return getColorFromString(color, Color.black);
	}

	public static Color getColorFromString(String color, Color defaultColor) {
		String[] sp = color.split("#");
		if (sp.length == 3) {
			try {
				int c1 = Integer.parseInt(sp[0]);
				int c2 = Integer.parseInt(sp[1]);
				int c3 = Integer.parseInt(sp[2]);
				return new Color(c1, c2, c3);
			} catch (Exception e) {
				return defaultColor;
			}
		} else {
			return defaultColor;
		}
	}

	public static String getStringFromColor(Color color) {
		int c1 = color.getRed();
		int c2 = color.getGreen();
		int c3 = color.getBlue();
		return String.valueOf(c1) + "#" + String.valueOf(c2) + "#" + String.valueOf(c3);
	}

	// stringsToMap: Translates an array like ["a", "val1", "b", "val2"] into
	// a map a->val1 b->val2.
	public static HashMap<String, String> stringsToMap(String[] strings) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < strings.length - 1; i += 2) {
			map.put(strings[i], strings[i + 1]);
		}
		return map; // stringsToMap() exit
	}

	public static HashMap<String, Long> stringsToLongMap(String[] strings) {
		HashMap<String, Long> map = new HashMap<String, Long>();
		for (int i = 0; i < strings.length - 1; i += 2) {
			map.put(strings[i], new Long(parseToLong(strings[i + 1], 0)));
		}
		return map; // stringsToLongMap() exit
	}

	// stringToMap: Translates a string like
	// <<a=alligator val="this is a test" val2='this is a "test"'>>
	// into a map
	// This could be enhanced to handle escape characters, and
	// to improve error handling.
	public static HashMap<String, String> stringToMap(String str) {
		if (str == null) {
			str = "";
		}
		HashMap<String, String> map = new HashMap<String, String>();

		String sPat1 = "\\w+="; // key=
		String sPat2a = "'.*?'"; // 'value'
		String sPat2b = "\".*?\""; // "value"
		String sPat2c = "\\S*"; // value
		String sPattern = sPat1 + sPat2a;
		sPattern += "|" + sPat1 + sPat2b;
		sPattern += "|" + sPat1 + sPat2c;
		Pattern pat = Pattern.compile(sPattern);
		Matcher match = pat.matcher(str);
		while (match.find()) {
			String sMatch = match.group(0);
			String[] sSegs = sMatch.split("=", 2);
			if (sSegs.length == 2) {
				String sKey = sSegs[0];
				String sValue = sSegs[1];
				char cCh = sValue.length() > 0 ? sValue.charAt(0) : ' ';
				if (cCh == '\'' || cCh == '"') {
					sValue = sValue.substring(1, sValue.length() - 1);
				}
				map.put(sKey, sValue);
			}
		}

		return map; // stringToMap() exit
	}

	// mapToString reverses stringToMap
	// this will fail if string contains BOTH ' and "
	public static String mapToString(HashMap<String, String> map) {
		StringBuffer oSb = new StringBuffer();
		Set<String> keys = map.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String sKey = iter.next();
			String sValue = map.get(sKey);

			char cCh = sValue.length() > 0 ? sValue.charAt(0) : ' ';
			boolean bBlank = sValue.indexOf(' ') >= 0;
			boolean bSquo = sValue.indexOf('\'') >= 0;
			boolean bQuo = sValue.indexOf('"') >= 0;
			if (cCh == '\'' || cCh == '"' || bBlank) {

				if (bSquo && bQuo)
				{
					; // ************ ERROR (ignored here)
				}
				if (bQuo) {
					sValue = "'" + sValue + "'";
				} else {
					sValue = "\"" + sValue + "\"";
				}
			}
			if (oSb.length() > 0) {
				oSb.append(" ");
			}
			oSb.append(sKey).append("=").append(sValue);
		}
		String sResult = oSb.toString();
		return sResult; // mapToString() exit
	}

	// encodeSpecialChars - encode characters that are not permitted
	// in Java property lists
	public static String encodeSpecialChars(String sInput) {
		String sOutput = sInput;
		if (sOutput != null && !isStringEncoded(sOutput)) {
			sOutput = sOutput.replaceAll("&", "&amp;");
			sOutput = sOutput.replaceAll("#", "&#35;");
			sOutput = sOutput.replaceAll("!", "&#33;");
			sOutput = sOutput.replaceAll("=", "&#61;");
			sOutput = sOutput.replaceAll(":", "&#58;");
			sOutput = sOutput.replaceAll("\\\\", "&#92;");
			sOutput = sOutput.replaceAll("\n", "\\n");
			sOutput = sOutput.replaceAll(" ", "&nbsp;");
			// A leading "&;" can be tested anywhere
			// as a signal that this string has been encoded
			if (!sOutput.equals(sInput)) {
				sOutput = "&;" + sOutput;
			}
		}
		return sOutput; // encodeSpecialChars() exit
	}

	public static boolean isStringEncoded(String sInput) {
		boolean bEncoded = sInput != null && sInput.startsWith("&;");
		return bEncoded; // isStringEncoded() exit
	}

	public static String decodeSpecialChars(String sInput) {
		String sOutput = sInput;
		if (isStringEncoded(sOutput)) {
			sOutput = sOutput.substring(2);
			sOutput = sOutput.replaceAll("&#33;", "!");
			sOutput = sOutput.replaceAll("&#61;", "=");
			sOutput = sOutput.replaceAll("&#58;", ":");
			sOutput = sOutput.replaceAll("\\n", "\n");
			sOutput = sOutput.replaceAll("&#92;", "\\\\");
			sOutput = sOutput.replaceAll("&nbsp;", " ");
			sOutput = sOutput.replaceAll("&#35;", "#");
			sOutput = sOutput.replaceAll("&amp;", "&");
		}
		return sOutput; // decodeSpecialChars() exit
	}

	/**
	 * Replaces system-dependent line separators with a simple html line break sequence.
	 * This can be used when submitting and retrieving data from the server using the CSocket
	 * class which does not handle/support multi-line values.
	 * This does not just apply to the getMap method.
	 *
	 * @param text
	 *            The text to encode.
	 * @return The encoded text.
	 */
	public static final String encodeMultiLine(String text) {
		return (text != null) ? text.replaceAll("(\\r\\n|\\n)", "<br>") : text;
	}

	/**
	 * Replaces any simple html line break character sequence (<br>
	 * ) in the given text with
	 * the system-dependent line separator sequence.
	 *
	 * @param text
	 *            The text to decode.
	 * @return The decoded text.
	 */
	public static final String decodeMultiLine(String text) {
		return (text != null) ? text.replace("<br>", System.lineSeparator()) : text;
	}

	public static String toolTipHtml(String sText) {
		return toolTipHtml(sText, null);
	}

	public static String toolTipHtml(String sText, String sType) {
		// sType is provided for future customization
		String sHtml = "<html><body>" + "<table cellspacing=0 border=0 cellpadding=0 width=600><tr><td align=left valign=bottom>" + "<font size=30><b>" + sText + "</b></font>" + "</td></tr></table>"
				+ "</body></html>";
		return sHtml; // tooltipHtml() exit
	}

	public static String getManifestAttribute(String name) {
		Enumeration<URL> resUrls = null;
		try {
			resUrls = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
		} catch (Exception ex) {
			LOGGER.debug("Error occurred", ex);
		}
		if (resUrls != null) {
			while (resUrls.hasMoreElements()) {
				try {
					Manifest manifest = new Manifest(resUrls.nextElement().openStream());
					Attributes attributes = manifest.getMainAttributes();
					String value = attributes.getValue(name);
					if (value != null) {
						return value;
					}
				} catch (Exception ex) {
					LOGGER.debug("Error occurred", ex);
				}
			}
		}
		return "Development";
	}

	public static boolean checkSelfSigned(Certificate cert) throws CertificateException {
		try {
			if (cert != null) {
				// check whether the certificate is signed with its own public key
				cert.verify(cert.getPublicKey());
			}

			return true;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (SignatureException ex) {
			return false;
		} catch (InvalidKeyException ex) {
			return false;
		} catch (Exception ex) {
			throw (ex instanceof CertificateException) ? (CertificateException) ex : new CertificateException(ex);
		}
	}

	public static boolean isLong(String value) {
		return value != null && longPattern.matcher(value).matches();
	}

	public static float getJavaVersion() {
		String version = System.getProperty("java.version");
		Matcher matcher = Pattern.compile("[0-9]+\\.[0-9]+").matcher(version);
		return matcher.find() ? parseToFloat(matcher.group(), 0f) : 0f;
	}

	public static String getDebugStackTraceString(Exception e, String tag, boolean debug) {
		StringBuffer sb = new StringBuffer();
		if (debug) {
			sb.append("DEBUG... STACK (").append(tag).append("):\n");
		}

		StackTraceElement st[] = e.getStackTrace();
		int startAtIndex = (debug ? 1 : 0);
		if (st.length > startAtIndex) {
			for (int i = startAtIndex; i < st.length; i++) {
				sb.append("\t").append(st[i].toString()).append("\n");
			}
		} else {
			if (debug) {
				sb.append("\t").append("[no stack]\n");
			}
		}

		return sb.toString();
	}

	public static String getDebugStackTraceString(Exception e, String tag) {
		return getDebugStackTraceString(e, tag, false);
	}

	public static String getDebugStackTraceString(String tag) {
		return getDebugStackTraceString(new Exception(), tag);
	}

	public static String getDebugStackTraceString(Exception e, Object objTag) {
		return getDebugStackTraceString(e, objTag.toString());
	}

	public static String getDebugStackTraceString(Object objTag) {
		return getDebugStackTraceString(objTag.toString());
	}

	public static String getDebugStackTraceString() {
		return getDebugStackTraceString("");
	}

	public static String getDebugStackTraceString(Exception e) {
		return getDebugStackTraceString(e, "");
	}

	/**
	 * Gets a list of all the network interfaces on this RIG.
	 *
	 * @param includeLoopback
	 * @return The list of unique InetAddresses.
	 * @throws SocketException
	 *             if an I/O error occurs.
	 */
	public static final List<InetAddress> getAddresses(boolean includeLoopback) throws SocketException {
		List<InetAddress> list = new ArrayList<InetAddress>(); // ordered set to avoid possibility of duplicates

		for (Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces(); nets.hasMoreElements();) {
			NetworkInterface netint = nets.nextElement();
			for (Enumeration<InetAddress> inetAddresses = netint.getInetAddresses(); inetAddresses.hasMoreElements();) {
				InetAddress address = inetAddresses.nextElement();

				if (!(address instanceof Inet4Address)){
					continue;
				}

				if (includeLoopback || !address.isLoopbackAddress()) {
					list.add(address);
				}
			}
		}

		return list;
	}

	public static final String addressesToString(List<InetAddress> addresses, String separator) {
		StringBuilder sb = new StringBuilder();

		for (int index = 0; index < addresses.size(); index++) {
			if (index > 0) {
				sb.append(separator);
			}
			sb.append(addresses.get(index).getHostAddress());
		}

		return sb.toString();
	}

	public static Exception getCurrentStackTrace() {
		return new CurrentStackTrace();
	}

	/**
	 * Finds a unique name that doesn't already exist in the existing names by appending "copy"
	 * and an optional number.
	 * Follows the convention: name, name copy, name copy 2, name copy 3
	 *
	 * @param startingName  The starting name.
	 * @param existingNames Names that we must not clash with.
	 * @return The unique name which might be the startingName if it isn't in existingNames or the startingName plus "copy" and an optional number to guarantee uniqueness.
	 */
	public static final String findUniqueName(String startingName, List<String> existingNames) {
		String name = startingName.trim();
		String c = startingName;
		int i = 1;

		Pattern p = Pattern.compile(".* copy[ ]?[0-9]*");

		while (existingNames.contains(c)) {
			if (p.matcher(name).matches()) {
				name = name.substring(0, name.lastIndexOf(" copy"));
			}

			if (!name.toLowerCase().endsWith(" copy")) {
				c = name + " copy";
			}

			if (i > 1) {
				c += (" " + Integer.toString(i));
			}

			i++;
		}

		return c;
	}


	/**
	 * Extracts the keys (as strings) from the map collection and returns them as a (case-insensitive) sorted array.
	 *
	 * @param map
	 *            The map.
	 * @return An array of keys found in the map in string form.
	 */
	public static final String[] getSortedMapKeys(Map<?, ?> map) {
		List<String> names = new ArrayList<String>();
		for (Object key : map.keySet()) {
			names.add(key.toString());
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
		return names.toArray(new String[0]);
	}
}
