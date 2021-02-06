package com.example.newsgateway;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.newsgateway.domain.Article;
import com.example.newsgateway.domain.Source;
import com.example.newsgateway.runnables.GetSourcesRunnable;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "ccd9717c681c4e59a0194a092a52a1a9";
    private final List<Source> sources = new ArrayList<>();
    private final Multimap<String, Article> articlesBySource =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Multimap<String, Source> categoryToNewsSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        requestSourceData();
    }

    private void requestSourceData() {
        new Thread(new GetSourcesRunnable(API_KEY, this)).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.choiceTopics) {
            // todo
        } else if (item.getItemId() == R.id.choiceCountries) {
            // todo
        } else if (item.getItemId() == R.id.choiceLanguages) {
            // todo
        }
        return true;
    }

    public void addSources(List<Source> newSources) {
        sources.clear();
        sources.addAll(newSources);
    }

    public void addArticlesForSource(String sourceId, List<Article> articles) {
        articlesBySource.putAll(sourceId, articles);
    }

    public void addSourceForCategory(String category, Source sources) {
        categoryToNewsSources.put(category, sources);
    }

}