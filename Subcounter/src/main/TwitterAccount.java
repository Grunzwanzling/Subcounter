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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import essentials.Essentials;
import essentials.SimpleLog;

/**
 * @author Maximilian
 *
 */
public class TwitterAccount {

	private Main main;
	String path;
	String name;
	private String congrats;
	private String tokenID;
	private String[] topURL;
	private String infoPath;
	private SimpleLog log;

	private Random rand = new Random();
	private String[] congratulations;
	private TwitterFactory factory;
	private AccessToken accessToken;
	private Twitter twitter;

	ArrayList<String> list;

	private static DateFormat dateFormat = new SimpleDateFormat("mm");

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

	public void updateList() {
		try {
			list = getNumberOfTopChannels(getChannelsToCheck(topURL), 1500);
		} catch (IOException e) {
			log.error("(" + name + ") Failed to update list");
			log.logStackTrace(e);
		}
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
			map.put(string, Integer.parseInt(main.getSubs(string)));
		}
		LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) Main
				.sortByValue(map);
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

	private void updateProfile(int count) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(new File(infoPath)));

			twitter.updateProfile(
					props.getProperty("name"),
					props.getProperty("url"),
					props.getProperty("location"),
					props.getProperty("bio").replaceAll("@count",
							String.valueOf(count)));
		} catch (TwitterException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean createPost(String username, String subs, String title) {
		try {
			// Generate the message
			int index = new Random().nextInt(congratulations.length);
			String message = congratulations[index];
			message = message.replaceAll("@user", username);
			message = message.replaceAll("@title", title);
			message = message.replaceAll("@subs", subs);
			if (message.length() > 140) {

				log.warning("Status was to long. Will still pretend it worked");
				return true;// It didn't work but we will pretend so
			}
			System.out.println(message);
			StatusUpdate statusUpdate = new StatusUpdate(message);
			Status status = twitter.updateStatus(statusUpdate);
			log.info("Successfully updated the status to [" + status.getText()
					+ "].");
			return true;
		} catch (TwitterException e) {
			log.error("Error occured while creating Post");

			log.logStackTrace(e);
			return false;
		}
	}
}
