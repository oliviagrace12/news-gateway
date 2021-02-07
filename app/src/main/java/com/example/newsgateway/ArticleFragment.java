package com.example.newsgateway;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.newsgateway.domain.Article;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class ArticleFragment extends Fragment {

    private static final String TAG = "ArticleFragment";

    public ArticleFragment() {
    }

    public static ArticleFragment newInstance(Article article, int index, int total) {
        ArticleFragment articleFragment = new ArticleFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable("article", article);
        bundle.putInt("index", index);
        bundle.putInt("total", total);
        articleFragment.setArguments(bundle);
        return articleFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout = inflater.inflate(
                R.layout.article_fragment, container, false);
        TextView titleView = fragmentLayout.findViewById(R.id.fragmentTitle);
        TextView dateView = fragmentLayout.findViewById(R.id.fragmentDate);
        TextView sourceView = fragmentLayout.findViewById(R.id.fragmentSource);
        ImageView imageView = fragmentLayout.findViewById(R.id.fragmentImage);
        TextView descriptionView = fragmentLayout.findViewById(R.id.fragmentDescription);
        TextView fragmentNumberView = fragmentLayout.findViewById(R.id.fragmentNumber);

        Bundle args = getArguments();
        if (args == null) {
            return null;
        }

        Article article = (Article) args.getSerializable("article");

        titleView.setText(article.getTitle());
        dateView.setText(article.getPublishedAt());
        sourceView.setText(article.getSourceName());
        setImage(article.getUrlToImage(), imageView);
        descriptionView.setText(article.getDescription());
        fragmentNumberView.setText(
                getString(R.string.fragment_number, args.getInt("index"), args.getInt("total")));

        return fragmentLayout;
    }

    private void setImage(CharSequence imageUrl, ImageView imageView) {
        Picasso.get().load(imageUrl.toString())
                .error(R.drawable.noimage)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Could not load image for fragment: " + e.getLocalizedMessage());
                    }
                });
    }
}
