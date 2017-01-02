/**
 * 
 */
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
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
	private String YouTubeAPIkey;
	private static DecimalFormat nf = new DecimalFormat();

	TwitterAccount[] basicAccounts = new TwitterAccount[1];
	TwitterAccount intAccount;

	String[][] topURLs;
	String[] intTopURLs;

	Properties globalList = new Properties();

	public Main() {
		log = new SimpleLog(new File("/var/www/html/log.txt"), true, true);
		log.startupMessage("Starting Aboerfolg-Bot...");

		YouTubeAPIkey = loadToken("youtube");

		topURLs[0] = new String[] {
				"http://socialblade.com/youtube/top/country/de/mostviewed",
				"http://socialblade.com/youtube/top/country/de/mostsubscribed",
				"http://socialblade.com/youtube/top/country/de" };

		intTopURLs = new String[] {
				"http://socialblade.com/youtube/top/500/mostsubscribed",
				"http://socialblade.com/youtube/top/500",
				"http://socialblade.com/youtube/top/5001d" };

		basicAccounts[0] = new TwitterAccount(this,
				"/home/ftp/subs.properties", "AboerfolgeDE",
				"/home/ftp/congratulations.txt", "abos", topURLs[0],
				"/home/ftp/info.properties", log);

		intAccount = new TwitterAccount(this, "/home/ftp/subs.properties",
				"AboerfolgeDE", "/home/ftp/congratulations.txt", "abos",
				intTopURLs, "/home/ftp/info.properties", log);

	}

	private void loadLists() throws FileNotFoundException, IOException {

		for (TwitterAccount account : basicAccounts) {
			account.updateList();

			Properties props = new Properties();
			props.load(new FileInputStream(new File(account.path)));

			for (String channel : account.list) {

				if (props.containsKey(channel)) {
					globalList.setProperty(channel, props.getProperty(channel));
				} else {
					props.setProperty(channel, getSubs(channel));
					globalList.setProperty(channel, getSubs(channel));
				}

			}
			props.store(new FileOutputStream(new File(account.path)), null);

		}

	}

	String getSubs(String username) {

		try {
			try {
				String result = Essentials.sendHTTPRequest(new URL(
						"https://www.googleapis.com/youtube/v3/channels?part=statistics&forUsername="
								+ username + "&key=" + YouTubeAPIkey));
				return result.substring(result.indexOf("subscriberCount") + 19,
						result.indexOf("\"",
								result.indexOf("subscriberCount") + 19));
			} catch (MalformedURLException e) {
				return "0";
			}
		} catch (IOException e) {
			log.error("Error occured while getting subscribers of " + username);
			log.logStackTrace(e);
			return null;
		}
	}

	static AccessToken loadAccessToken(String id) {
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

	static String loadToken(String id) {
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

	static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
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

	private static String round(String subs) {
		if (subs == null)
			return null;
		try {
			int sub = Integer.parseInt(subs);
			int down = sub / 100000;
			if (sub > 3000000) {
				down = down / 10;
				down = down * 10;
			}

			return String.valueOf(nf.format(down * 100000));
		} catch (NumberFormatException e) {
			return "0";
		}
	}
}
