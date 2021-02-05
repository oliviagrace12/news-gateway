package com.example.newsgateway.runnables;

import android.net.Uri;
import android.util.Log;

import com.example.newsgateway.MainActivity;
import com.example.newsgateway.domain.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetArticlesRunnable implements Runnable {

    private static final String TAG = "GetArticlesRunnable";

    private final String apiKey;
    private final MainActivity mainActivity;
    private final String sourceId;

    public GetArticlesRunnable(String apiKey, MainActivity mainActivity, String sourceId) {
        this.apiKey = apiKey;
        this.mainActivity = mainActivity;
        this.sourceId = sourceId;
    }

    @Override
    public void run() {
        String responseJson = requestData(sourceId);
        try {
            mainActivity.addArticlesForSource(sourceId, parse(responseJson));
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse sources: " + e.getLocalizedMessage());
        }
    }

    private List<Article> parse(String responseJson) throws JSONException {
        JSONObject responseObject = new JSONObject(responseJson);
        JSONArray articlesArray = responseObject.getJSONArray("articles");
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < articlesArray.length(); i++) {
            JSONObject articleJson = articlesArray.getJSONObject(i);
            Article article = new Article();
            article.setAuthor(articleJson.getString("author"));
            article.setDescription(articleJson.getString("description"));
            article.setPublishedAt(articleJson.getString("publishedAt"));
            article.setTitle(articleJson.getString("title"));
            article.setUrl(articleJson.getString("url"));
            article.setUrlToImage(articleJson.getString("urlToImage"));

            articles.add(article);
        }

        return articles;
    }

    private String createUrlString(String sourceId) {
        return new Uri.Builder()
                .scheme("https")
                .authority("newsapi.org")
                .appendPath("v2")
                .appendPath("top-headlines")
                .appendQueryParameter("sources", sourceId)
                .appendQueryParameter("apiKey", apiKey)
                .build().toString();
    }

    private String requestData(String sourceId) {
        String urlString = createUrlString(sourceId);
        Log.i(TAG, "Requesting data using URL: " + urlString);
        HttpURLConnection conn = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.setRequestProperty("Accept", "application/json");

            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return "";
            }
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            Log.d(TAG, "Response: " + stringBuilder.toString());
        } catch (IOException ex) {
            Log.e(TAG, "Error in getting info: " + ex.getLocalizedMessage(), ex);
            return "";
        }

        return stringBuilder.toString();
    }
}
