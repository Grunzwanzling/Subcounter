/**
 * 
 */
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import twitter4j.auth.AccessToken;
import essentials.Essentials;
import essentials.SimpleLog;

/**
 * @author Maximilian
 *
 */
public class Main {

	final static String TOKEN_PATH = "/home/ftp/token.txt";

	SimpleLog log;
	String YouTubeAPIkey;

	public Main() {
		log = new SimpleLog(new File("/var/www/html/log.txt"), true, true);
		log.startupMessage("Starting Aboerfolg-Bot...");

		YouTubeAPIkey = loadToken("youtube");
	}

	public static String getSubs(String username, Main main) {
		try {
			try {
				String result = Essentials.sendHTTPRequest(new URL(
						"https://www.googleapis.com/youtube/v3/channels?part=statistics&forUsername="
								+ username + "&key=" + main.YouTubeAPIkey));
				return result.substring(result.indexOf("subscriberCount") + 19,
						result.indexOf("\"",
								result.indexOf("subscriberCount") + 19));
			} catch (MalformedURLException e) {
				return "0";
			}
		} catch (IOException e) {
			main.log.error("Error occured while getting subscribers of "
					+ username);
			main.log.logStackTrace(e);
			return null;
		}
	}

	public static AccessToken loadAccessToken(String id) {
		Properties prop = new Properties();
		InputStream in;
		try {
			in = new FileInputStream(new File(TOKEN_PATH));

			prop.load(in);
			String token = prop.getProperty(id + ".token");
			String tokenSecret = prop.getProperty(id + ".tokenSecret");
			return new AccessToken(token, tokenSecret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String loadToken(String id) {
		Properties prop = new Properties();
		InputStream in;
		try {
			in = new FileInputStream(new File(TOKEN_PATH));

			prop.load(in);
			String token = prop.getProperty(id);
			return token;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
