package models.services;

import models.entities.Video;
import models.services.ChannelProfileService;
import models.services.YouTubeService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ChannelProfileService class.
 * This test class verifies the methods for fetching channel information and videos
 * using the YouTube API, ensuring proper handling of various response scenarios.
 *
 * @author: Zahra Rasoulifar
 */
public class ChannelProfileServiceTest {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private ChannelProfileService channelProfileService;

    /**
     * Initializes mock objects and sets up the ChannelProfileService instance
     * with a mocked HttpClient before each test.
     * @author: Zahra Rasoulifar
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        channelProfileService = new ChannelProfileService(youTubeService) {
            @Override
            protected HttpClient createHttpClient() {
                return httpClient; // Return the mocked HttpClient
            }
        };
    }

    /**
     * Tests the getChannelInfo method to ensure that it correctly retrieves channel
     * information from the YouTube API and parses the response.
     *
     * @throws Exception if an error occurs during the test.
     * @author: Zahra Rasoulifar
     */
    @Test
    public void testGetChannelInfo() throws Exception {
        String channelId = "testChannelId";
        when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
        when(youTubeService.getApiKey()).thenReturn("testApiKey");

        String responseBody = "{\"items\":[{\"snippet\":{\"title\":\"Test Video\",\"description\":\"Test Description\"}}]}";

        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpClient.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(futureResponse);
        when(httpResponse.body()).thenReturn(responseBody);

        CompletionStage<JSONObject> result = channelProfileService.getChannelInfo(channelId);

        JSONObject snippet = result.toCompletableFuture().get();
        assertEquals("Test Video", snippet.getString("title"));
        assertEquals("Test Description", snippet.getString("description"));

        // Verify the correct URL was called
        verify(httpClient).sendAsync(argThat(request -> request.uri().toString().equals(
                "http://api.youtube.com/search?part=snippet&type=video&channelId=" + channelId + "&key=testApiKey"
        )), eq(HttpResponse.BodyHandlers.ofString()));
    }

    /**
     * Tests the getChannelVideos method to ensure it correctly retrieves a list of videos
     * from a specified YouTube channel and parses the response.
     *
     * @throws Exception if an error occurs during the test.
     * @author: Zahra Rasoulifar
     */
    @Test
    public void testGetChannelVideos() throws Exception {
        String channelId = "testChannelId";
        int maxResults = 10;
        when(youTubeService.getApiUrl()).thenReturn("http://api.youtube.com");
        when(youTubeService.getApiKey()).thenReturn("testApiKey");

        String responseBody = "{\"items\":[{\"snippet\":{\"title\":\"Test Video 1\",\"description\":\"Test Description 1\"}}," +
                "{\"snippet\":{\"title\":\"Test Video 2\",\"description\":\"Test Description 2\"}}]}";

        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpClient.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(futureResponse);
        when(httpResponse.body()).thenReturn(responseBody);

        List<Video> mockVideoList = mock(List.class);
        when(youTubeService.parseVideos(any(JSONArray.class))).thenReturn(mockVideoList);

        CompletionStage<List<Video>> result = channelProfileService.getChannelVideos(channelId, maxResults);

        List<Video> videos = result.toCompletableFuture().get();
        assertEquals(mockVideoList, videos);

        // Verify the correct URL was called
        verify(httpClient).sendAsync(argThat(request -> request.uri().toString().equals(
                "http://api.youtube.com/search?part=snippet&type=video&channelId=" + channelId +
                        "&maxResults=" + maxResults + "&key=testApiKey"
        )), eq(HttpResponse.BodyHandlers.ofString()));

        // Verify parseVideos was called with any JSONArray
        verify(youTubeService).parseVideos(any(JSONArray.class));
    }

    /**
     * Tests the createHttpClient method to ensure it returns a valid HttpClient instance.
     * @author: Zahra Rasoulifar
     */
    @Test
    public void testCreateHttpClient() {
        ChannelProfileService service = new ChannelProfileService(youTubeService);
        HttpClient client = service.createHttpClient();
        assertNotNull("HttpClient should not be null", client);
        assertTrue("Returned object should be an instance of HttpClient", client instanceof HttpClient);
    }
}
