package com.example.newsgateway.runnables;

import android.net.Uri;
import android.util.Log;

import com.example.newsgateway.MainActivity;
import com.example.newsgateway.domain.Source;

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
import java.util.stream.Collectors;

public class GetSourcesRunnable implements Runnable {

    private static final String TAG = "GetSourcesRunnable";

    private final String apiKey;
    private final MainActivity mainActivity;

    public GetSourcesRunnable(String apiKey, MainActivity mainActivity) {
        this.apiKey = apiKey;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        String jsonResponse = requestData();
        List<Source> sources;
        try {
            sources = parse(jsonResponse);
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse sources: " + e.getLocalizedMessage());
            return;
        }

        mainActivity.addAllSources(sources.stream().map(Source::getName).collect(Collectors.toList()));
        sources.forEach(source -> {
            mainActivity.addSourceForCategory(source.getCategory(), source.getName());
            mainActivity.addSourceForLanguage(source.getLanguage(), source.getName());
            mainActivity.addSourceForCountry(source.getCountry(), source.getName());
        });

        mainActivity.runOnUiThread(mainActivity::setupInitialMenu);
    }

    private List<Source> parse(String jsonResponse) throws JSONException {
        JSONObject object = new JSONObject(jsonResponse);
        JSONArray sourcesArray = object.getJSONArray("sources");
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < sourcesArray.length(); i++) {
            JSONObject sourceJson = sourcesArray.getJSONObject(i);
            Source source = new Source();
            source.setCategory(sourceJson.getString("category"));
            source.setCountry(sourceJson.getString("country"));
            source.setId(sourceJson.getString("id"));
            source.setLanguage(sourceJson.getString("language"));
            source.setName(sourceJson.getString("name"));

            sources.add(source);
        }

        return sources;
    }

    private String createUrlString() {
        return new Uri.Builder()
                .scheme("https")
                .authority("newsapi.org")
                .appendPath("v2")
                .appendPath("sources")
                .appendQueryParameter("apiKey", apiKey)
                .build().toString();
    }

    private String requestData() {
        String urlString = createUrlString();
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
