/**
 * 
 */
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import essentials.Essentials;
import essentials.SimpleLog;

/**
 * @author Maximilian
 *
 */
public class TwitterAccount {

	Main main;

	String path;
	String name;
	String congrats;
	String tokenID;
	String[] topURL;
	String YouTubeAPIkey;
	String infoPath;

	Random rand = new Random();
	String[] congratulations;
	SimpleLog log;
	static DecimalFormat nf = new DecimalFormat();

	TwitterFactory factory;
	AccessToken accessToken;
	Twitter twitter;

	static DateFormat dateFormat = new SimpleDateFormat("mm");

	public TwitterAccount(Main main, String path, String name, String congrats,
			String tokenID, String[] topURLs, String infoPath, SimpleLog log) {
		this.main = main;
		this.path = path;
		this.name = name;
		this.congrats = congrats;
		this.tokenID = tokenID;
		this.log = log;
		this.topURL = topURLs;
		this.infoPath = infoPath;
		congratulations = getCongratulations();

		// Connecting to Twitter API
		factory = new TwitterFactory();
		accessToken = Main.loadAccessToken(tokenID);

		twitter = factory.getInstance();
		twitter.setOAuthAccessToken(accessToken);
		log.info("("
				+ name
				+ ") Successfully loaded congratulations and connected to Twitter");
	}

	private String[] getCongratulations() {
		try {
			BufferedReader bufr = new BufferedReader(new FileReader(new File(
					congrats)));
			ArrayList<String> list = new ArrayList<String>();
			String line = bufr.readLine();
			while (line != null) {
				list.add(line);
				line = bufr.readLine();
			}
			bufr.close();
			congratulations = new String[list.size()];
			for (int i = 0; i < list.size(); i++)
				congratulations[i] = list.get(i);
		} catch (IOException e) {
			log.error("(" + name
					+ ") Error occured while getting congratulations");
			log.logStackTrace(e);
		}
		return congratulations;

	}

	private ArrayList<String> getTopYouTubers(String topURL, int size) {
		try {
			String result = Essentials.sendHTTPRequest(new URL(topURL));
			String channel = "d";
			int index = 0;
			ArrayList<String> channels = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				index = result.indexOf("<a href=\"/youtube/user/", index) + 23;
				int newIndex = result.indexOf("\"", index);
				channel = result.substring(index, newIndex);
				channels.add(channel);
				index = newIndex;
			}
			return channels;
		} catch (IOException e) {
			log.error("Error occured while getting top Youtubers");
			log.logStackTrace(e);
			return null;
		}

	}

	private ArrayList<String> getChannelsToCheck(String[] paths)
			throws FileNotFoundException, IOException {
		ArrayList<String> list = new ArrayList<String>();
		Properties props = new Properties();
		props.load(new FileInputStream(new File(path)));

		Set<Object> s = props.keySet();
		for (Object object : s) {
			list.add((String) object);
		}
		for (String path : paths) {
			int size = 100;
			if (path.contains("500"))
				size = 500;
			ArrayList<String> top = getTopYouTubers(path, size);
			if (top != null)
				for (String string : top) {
					if (!list.contains(string))
						list.add(string);
				}
		}

		return list;
	}

	private ArrayList<String> getNumberOfTopChannels(ArrayList<String> channel,
			int amount) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String string : channel) {
			map.put(string, Integer.parseInt(Main.getSubs(string, main)));
		}
		LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) sortByValue(map);
		ArrayList<String> result = new ArrayList<String>();
		int i = 0;
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			String key = entry.getKey();
			result.add(key);
			if (i > amount)
				break;
		}
		return result;
	}

	
}
