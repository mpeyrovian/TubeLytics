package models.services;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.entities.Video;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
 * The YouTubeService class provides utility methods to interact with the YouTube Data API,
 * parse video details, and extract metadata such as tags. It relies on configuration values
 * for API key and URL, which are loaded from an external configuration file.
 */
public class YouTubeService {
    private static final Config config = ConfigFactory.load();
    private static final String API_KEY = config.getString("youtube.api.key");
    private static final String API_URL = config.getString("youtube.api.url");
    private static final String BASE_VIDEO_URL = "https://www.youtube.com/watch?v=";

    /**
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Constructs a YouTubeService instance and loads the necessary API configuration.
     */
    public YouTubeService() {
    }

    /**
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Retrieves the YouTube API key from the configuration.
     *
     * @return The API key as a String.
     */
    public String getApiKey() {
        return API_KEY;
    }

    /**
     * Retrieves the YouTube API URL from the configuration.
     *
     * @return The API URL as a String.
     */
    public String getApiUrl() {
        return API_URL;
    }

    /**
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Parses a JSONObject to create a Video object, extracting details such as title, description,
     * channel information, thumbnail URL, and video URL.
     *
     * @param item The JSONObject representing a video item from the YouTube API response.
     * @return A Video object populated with the parsed details or null if the item is empty.
     */
    public Video parseVideo(JSONObject item) {
        if (item.isEmpty()) {
            return null;
        }

        JSONObject snippet = item.optJSONObject("snippet");

        // Extract video details with default values where applicable
        String title = snippet.optString("title", "No Title");
        String description = snippet.optString("description", "");
        String channelTitle = snippet.optString("channelTitle", "Unknown Channel");
        String channelId = snippet.optString("channelId", "Unknown Channel ID");
        String thumbnailUrl = snippet.optJSONObject("thumbnails")
                .optJSONObject("default")
                .optString("url", "");

        // Handle different types of 'id' structures
        String videoId = null;
        Object idField = item.opt("id");
        if (idField instanceof JSONObject) {
            videoId = ((JSONObject) idField).optString("videoId", null);
        } else if (idField instanceof String) {
            videoId = (String) idField;
        }

        // Construct the video URL
        String videoUrl = BASE_VIDEO_URL + videoId;

        // Create and return the Video object
        return new Video(title, description, channelTitle, thumbnailUrl, videoId, channelId, videoUrl);
    }

    /**
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Parses a JSONArray containing video items and converts each item into a Video object.
     *
     * @param items A JSONArray of video items from the YouTube API response.
     * @return A list of Video objects parsed from the JSONArray.
     */
    public List<Video> parseVideos(JSONArray items) {
        List<Video> videos = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            Video video = parseVideo(item);
            videos.add(video);
        }

        return videos;
    }

    /**
     * @author: Zahra Rasoulifar, Hosna Habibi,Mojtaba Peyrovian, Kasra Karaji
     * Parses a JSONArray to extract tags from the first item in the array using Java Streams.
     *
     * @param items A JSONArray of video items, with each item potentially containing tags.
     * @return A list of tags if present, otherwise an empty list.
     */
    public List<String> parseTags(JSONArray items) {
        if (items.length() > 0) {
            JSONObject item = items.getJSONObject(0);
            JSONObject snippet = item.getJSONObject("snippet");

            if (snippet.has("tags")) {
                JSONArray tagArray = snippet.getJSONArray("tags");

                // Use streams to map each tag to its encoded URL and collect them into a list
                return IntStream.range(0, tagArray.length())
                        .mapToObj(tagArray::getString)
                        .collect(Collectors.toList());
            }
        }

        return List.of(); // Return an empty list if no tags are found
    }
}
