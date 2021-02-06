package com.example.newsgateway;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.example.newsgateway.domain.Article;
import com.example.newsgateway.domain.Source;
import com.example.newsgateway.runnables.GetSourcesRunnable;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "2191b2ac06234333a9a8fa96d2e1b90e";

    private Menu menu;
    private SubMenu topicsSubMenu;
    private SubMenu countriesSubMenu;
    private SubMenu languagesSubMenu;

    private final List<Source> sources = new ArrayList<>();
    private final Multimap<String, Article> articlesBySource =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Multimap<String, Source> topicToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Multimap<String, Source> languageToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Multimap<String, Source> countryToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Map<String, String> countryCodeToName = new HashMap<>();
    private Map<String, String> languageCodeToName = new HashMap<>();

    public static int screenWidth, screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        populateCountryNames();
        populateLanguageNames();
        requestSourceData();
    }

    private void populateLanguageNames() {
        // todo load from provided json file
    }

    private void populateCountryNames() {
        // todo load from provided json file
    }

    public void setupInitialMenu() {
        topicsSubMenu = menu.addSubMenu(getString(R.string.topics));
        topicToSources.keySet().forEach(topic -> topicsSubMenu.add(topic));
        countriesSubMenu = menu.addSubMenu(getString(R.string.countries));
        countryToSources.keySet().forEach(country -> countriesSubMenu.add(country));
        languagesSubMenu = menu.addSubMenu(getString(R.string.languages));
        languageToSources.keySet().forEach(language -> languagesSubMenu.add(language));
    }

    private void requestSourceData() {
        new Thread(new GetSourcesRunnable(API_KEY, this)).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getTitle() == getString(R.string.topics)) {
//            menu.(item.getTitle());
//            for (String category : categoryToSources.keySet()) {
//                menu.add(category);
//            }
//        } else if (item.getTitle() == getString(R.string.languages)) {
//        } else if (item.getTitle() == getString(R.string.countries)) {
//        }
        return true;
    }

    public void addSources(List<Source> newSources) {
        sources.clear();
        sources.addAll(newSources);
    }

    public void addArticlesForSource(String sourceId, List<Article> articles) {
        articlesBySource.putAll(sourceId, articles);
    }

    public void addSourceForCategory(String category, Source source) {
        topicToSources.put(category, source);
    }

    public void addSourceForLanguage(String language, Source source) {
        languageToSources.put(language, source);
    }

    public void addSourceForCountry(String country, Source source) {
        countryToSources.put(country, source);
    }
}