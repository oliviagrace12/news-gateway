package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
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
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String API_KEY = "2191b2ac06234333a9a8fa96d2e1b90e";

    private MyProjectSharedPreference sharedPreferences;

    private Menu menu;
    private SubMenu topicsSubMenu;
    private SubMenu countriesSubMenu;
    private SubMenu languagesSubMenu;

    private final List<String> allSources = new ArrayList<>();
    private final Multimap<String, String> topicToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private final Multimap<String, String> languageToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private final Multimap<String, String> countryToSources =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private final Map<String, String> countryCodeToName = new HashMap<>();
    private final Map<String, String> languageCodeToName = new HashMap<>();

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
        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.round_menu_white_20);
        }

        populateCountryNames();
        populateLanguageNames();
        requestSourceData();
    }

    private void selectItem(int position) {
        pager.setBackground(null);
        new Thread(new GetArticlesBySourceRunnable(API_KEY, this, sourceNames.get(position))).start();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void setFragments(String sourceName, List<Article> articles) {
        Log.i(TAG, "Setting new articles in fragments: [" + articles.stream().map(Article::getSourceName).collect(Collectors.joining(",")) + "]");
        if (getSupportActionBar() != null) {
            if (articles.isEmpty()) {
                setTitleWithNumberOfChoices(sourceNames.size());
            } else {
                getSupportActionBar().setTitle(sourceName);
            }
        }

        for (int i = 0; i < pageAdapter.getCount(); i++) {
            pageAdapter.notifyChangeInPosition(i);
        }
        fragments.clear();

        for (int i = 0; i < articles.size(); i++) {
            fragments.add(
                    ArticleFragment.newInstance(articles.get(i), i + 1, articles.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);

        if (articles.isEmpty()) {
            pager.setBackground(ContextCompat.getDrawable(this, R.drawable.newspaper_coffee));
            showAlertDialogue(sourceName);
        }
    }

    private void showAlertDialogue(String sourceName) {
        new AlertDialog.Builder(this)
                .setTitle("No articles found for " + sourceName)
                .setPositiveButton("OK", ((dialog, which) -> {
                }))
                .create().show();
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
        countriesSubMenu.add(R.string.lower_case_all);
        countryToSources.keySet().stream().sorted().forEach(country -> countriesSubMenu.add(country));

        languagesSubMenu = menu.addSubMenu(getString(R.string.languages));
        languagesSubMenu.add(R.string.lower_case_all);
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

//        sourceNames.clear();
//        mDrawerLayout.closeDrawer(mDrawerList);

        String selection = item.getTitle().toString();
        if (selection.equalsIgnoreCase("all")) {
            sourceNames.clear();
            sourceNames.addAll(allSources);
            mDrawerLayout.openDrawer(mDrawerList);
        } else if (countryToSources.containsKey(selection)) {
            Collection<String> sourcesFromCountry = countryToSources.get(selection);
            List<String> newSourceNames = sourceNames.stream().filter(sourcesFromCountry::contains)
                    .collect(Collectors.toList());
            sourceNames.clear();
            sourceNames.addAll(newSourceNames);
            mDrawerLayout.openDrawer(mDrawerList);
        } else if (languageToSources.containsKey(selection)) {
            Collection<String> sourcesFromLanguage = languageToSources.get(selection);
            List<String> newSourceNames = sourceNames.stream().filter(sourcesFromLanguage::contains)
                    .collect(Collectors.toList());
            sourceNames.clear();
            sourceNames.addAll(newSourceNames);
            mDrawerLayout.openDrawer(mDrawerList);
        } else if (topicToSources.containsKey(selection)) {
            Collection<String> sourcesFromTopic = topicToSources.get(selection);
            List<String> newSourceNames = sourceNames.stream().filter(sourcesFromTopic::contains)
                    .collect(Collectors.toList());
            sourceNames.clear();
            sourceNames.addAll(newSourceNames);
            mDrawerLayout.openDrawer(mDrawerList);
        }

        ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        setTitleWithNumberOfChoices(sourceNames.size());

        return true;
    }

    public void setTitleWithNumberOfChoices(int choices) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name) + "(" + choices + ")");
        }
    }

    public void addSourceForCategory(String category, String source) {
        topicToSources.put(category, source);
    }

    public void addSourceForLanguage(String language, String source) {
        languageToSources.put(languageCodeToName.get(language.toUpperCase()), source);
    }

    public void addSourceForCountry(String countryCode, String source) {
        countryToSources.put(countryCodeToName.get(countryCode.toUpperCase()), source);
    }

    public void addAllSources(List<String> sources) {
        allSources.addAll(sources.stream().sorted().collect(Collectors.toList()));
    }

    public void populateAllSourcesInDrawer() {
        sourceNames.addAll(allSources);
        ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        setTitleWithNumberOfChoices(sourceNames.size());
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }

}