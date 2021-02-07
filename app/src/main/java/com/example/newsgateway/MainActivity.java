package com.example.newsgateway;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.newsgateway.domain.Article;
import com.example.newsgateway.domain.Source;
import com.example.newsgateway.runnables.GetArticlesBySourceRunnable;
import com.example.newsgateway.runnables.GetSourcesRunnable;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String API_KEY = "2191b2ac06234333a9a8fa96d2e1b90e";

    private Menu menu;
    private SubMenu topicsSubMenu;
    private SubMenu countriesSubMenu;
    private SubMenu languagesSubMenu;

    private final List<Source> sources = new ArrayList<>();
    private Multimap<String, Source> topicToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Multimap<String, Source> languageToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Multimap<String, Source> countryToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private Map<String, String> countryCodeToName = new HashMap<>();
    private Map<String, String> languageCodeToName = new HashMap<>();
    private Map<String, String> sourceNameToId = new HashMap<>();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    private MyPageAdapter pageAdapter;
    private ViewPager pager;
    private List<String> sourceNames = new ArrayList<>();
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

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);
        fragments = new ArrayList<>();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        mDrawerList.setAdapter(new ArrayAdapter<>(this,   // <== Important!
                R.layout.drawer_list_item, sourceNames));

        // Set up the drawer item click callback method
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(   // <== Important!
                this,                /* host Activity */
                mDrawerLayout,             /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        // ??
        if (getSupportActionBar() != null) {  // <== Important!
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.round_menu_white_20);
        }
        //

        populateCountryNames();
        populateLanguageNames();
        requestSourceData();
    }

    private void selectItem(int position) {
        pager.setBackground(null);
        new Thread(new GetArticlesBySourceRunnable(API_KEY, this, sourceNames.get(position))).start();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void populateLanguageNames() {
        try {
            InputStream is = getResources().openRawResource(R.raw.language_codes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray languagesArray = new JSONObject(sb.toString()).getJSONArray("languages");
            for (int i = 0; i < languagesArray.length(); i++) {
                JSONObject languageJson = languagesArray.getJSONObject(i);
                String code = languageJson.getString("code");
                String name = languageJson.getString("name");
                languageCodeToName.put(code, name);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Could not parse language codes/names: " + e.getLocalizedMessage());
        }
    }

    private void populateCountryNames() {
        try {
            InputStream is = getResources().openRawResource(R.raw.country_codes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray countriesArray = new JSONObject(sb.toString()).getJSONArray("countries");
            for (int i = 0; i < countriesArray.length(); i++) {
                JSONObject countryJson = countriesArray.getJSONObject(i);
                String code = countryJson.getString("code");
                String name = countryJson.getString("name");
                countryCodeToName.put(code, name);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Could not parse country codes/names: " + e.getLocalizedMessage());
        }
    }

    public void setupInitialMenu() {
        topicsSubMenu = menu.addSubMenu(getString(R.string.topics));
        topicsSubMenu.add(R.string.lower_case_all);
        topicToSources.keySet().stream().sorted().forEach(topic -> topicsSubMenu.add(topic));

        countriesSubMenu = menu.addSubMenu(getString(R.string.countries));
        countriesSubMenu.add(R.string.upper_case_all);
        countryToSources.keySet().stream().sorted().forEach(country -> countriesSubMenu.add(country));

        languagesSubMenu = menu.addSubMenu(getString(R.string.languages));
        languagesSubMenu.add(R.string.upper_case_all);
        languageToSources.keySet().stream().sorted().forEach(language -> languagesSubMenu.add(language));
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        String selection = item.getTitle().toString();
        if (countryToSources.containsKey(selection)) {
            sourceNames.clear();
            Collection<Source> sources = countryToSources.get(selection);
            sourceNames.addAll(sources.stream().map(Source::getName).collect(Collectors.toList()));
            ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        } else if (languageToSources.containsKey(selection)) {
            sourceNames.clear();
            Collection<Source> sources = languageToSources.get(selection);
            sourceNames.addAll(sources.stream().map(Source::getName).collect(Collectors.toList()));
            ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        } else if (topicToSources.containsKey(selection)) {
            sourceNames.clear();
            Collection<Source> sources = topicToSources.get(selection);
            sourceNames.addAll(sources.stream().map(Source::getName).collect(Collectors.toList()));
            ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        }

        return true;
    }

    public void addSources(List<Source> newSources) {
        sources.clear();
        sources.addAll(newSources);
    }

    public void addSourceForCategory(String category, Source source) {
        topicToSources.put(category, source);
    }

    public void addSourceForLanguage(String language, Source source) {
        languageToSources.put(languageCodeToName.get(language.toUpperCase()), source);
    }

    public void addSourceForCountry(String countryCode, Source source) {
        countryToSources.put(countryCodeToName.get(countryCode.toUpperCase()), source);
    }

    public void addSourceIdForName(String sourceId, String sourceName) {
        sourceNameToId.put(sourceName, sourceId);
    }

    public void setFragments(String sourceName, List<Article> articles) {
        pager.setBackground(getDrawable(R.drawable.loading));
        if (articles.isEmpty()) {
            getSupportActionBar().setTitle(sourceName);
            fragments.clear();
            pageAdapter.notifyDataSetChanged();
            pager.setCurrentItem(0);
            pager.setBackground(getDrawable(R.drawable.newspaper_coffee));
            return;
        }
        getSupportActionBar().setTitle(sourceName);
        for (int i = 0; i < pageAdapter.getCount(); i++) {
            pageAdapter.notifyChangeInPosition(i);
        }
        fragments.clear();

        for (int i = 0; i < articles.size(); i++) {
            fragments.add(
                    ArticleFragment.newInstance(articles.get(i), i+1, articles.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
        pager.setBackground(null);
    }
}