package com.sample.simpleastronomypod;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;

/*
    Written by Jon-Luke West
 */
@Keep
public class PictureActivity extends AppCompatActivity {

    //Toolbar menu item for going to the HD URL for downloading
    private MenuItem mDownloadMenuItem;
    //The button used to go to the source article
    private MenuItem mGoToSourceMenuItem;

    //The picture of the day
    private ImageView mPodImage;
    //The title of the picture of the day
    private TextView mTitleText;
    //The explanation for the picture of the day
    private TextView mExplanationText;
    //The divider between the title and explanation (not visible while loading)
    private View mDivider;
    //The progress spinner showing that work is being done
    private ProgressBar mLoadProgress;
    //The loading text shown to users
    private TextView mLoadText;
    //The button used to go to the YouTube video if available
    private ImageView mGoToYouTubeImageButton;

    //The Volley request queue
    private RequestQueue mRequestQueue;

    //Today's Astronomy Picture of the Day
    private PodEntry mTodaysPodEntry;
    //The URL for the YouTube video for today's APOD
    private String mTodaysPodYouTubeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        //Configure toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_picture_top);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        //Get view IDs
        mPodImage = findViewById(R.id.image_picture_pod);
        mTitleText = findViewById(R.id.text_picture_title);
        mExplanationText = findViewById(R.id.text_picture_explanation);
        mDivider = findViewById(R.id.divider_picture_content);
        mLoadProgress = findViewById(R.id.progress_picture_load);
        mLoadText = findViewById(R.id.text_picture_load);
        mGoToYouTubeImageButton = findViewById(R.id.image_picture_play);
        mGoToYouTubeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToYouTubeVideo();
            }
        });

        //Create new volley request queue
        mRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Initiate a request to get the latest pod
        GetTodaysPod();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_picture, menu);
        //Get a handle for the download menu item
        mDownloadMenuItem = menu.findItem(R.id.menu_picture_download);
        mGoToSourceMenuItem = menu.findItem(R.id.menu_picture_source);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_picture_download:

                GoToHdImageUrl();
                return true;
            case R.id.menu_picture_source:

                GoToSource();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void GetTodaysPod() {

        //Build the request
        String url = getString(R.string.url_nasa_apod);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Success!!!!
                        try {

                            //Deserialize the entry
                            Gson gson = new Gson();
                            mTodaysPodEntry = gson.fromJson(response, PodEntry.class);

                            //Show data
                            ShowPod(mTodaysPodEntry);
                            mDivider.setVisibility(View.VISIBLE);
                            mGoToSourceMenuItem.setVisible(true);
                            mLoadText.setVisibility(View.INVISIBLE);
                        }
                        catch (Exception exception) {

                            Log.e("SimpleAstronomyPOD", "Error while deserializing entry from NASA");
                            exception.printStackTrace();
                            //There was a problem deserializing or showing the POD
                            ShowError();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Something bad happened
                Log.e("SimpleAstronomyPOD", "Error while getting entry from NASA");
                error.printStackTrace();

                ShowError();
            }
        });

        //Add the request to the queue to get processed
        mRequestQueue.add(stringRequest);
    }

    private void ShowPod(PodEntry todaysPod) {

        if (todaysPod == null) {
            Log.e("SimpleAstronomyPOD", "Error while showing picture of the day");
            Log.e("SimpleAstronomyPOD", "POD is NULL");
            ShowError();
            return;
        }

        if (todaysPod.isImage()) {
            //If today's POD is just an image... show it
            GetAndShowImageFromUrl(todaysPod.getUrl());
        } else if (todaysPod.isVideo()) {
            //Today's video is tagged as a video
            try {
                //Try to load it as a youtube video
                String youtubeUrl = getString(R.string.url_youtube_thumbnail);
                youtubeUrl = youtubeUrl.replace("{0}", todaysPod.getYouTubeKey());
                mTodaysPodYouTubeUrl = todaysPod.getUrl();
                mGoToYouTubeImageButton.setVisibility(View.VISIBLE);
                GetAndShowImageFromUrl((youtubeUrl));
            } catch (UnsupportedOperationException exception) {
                Log.e("SimpleAstronomyPOD", "Error while loading image");
                exception.printStackTrace();

                //try to load the image directly from the URL anyway to see if it works
                GetAndShowImageFromUrl(todaysPod.getUrl());
            }
        }
        mTitleText.setText(todaysPod.getTitle());
        mExplanationText.setText(todaysPod.getExplanation());

        if (todaysPod.hasHdUrl()) {
            mDownloadMenuItem.setVisible(true);
        }
    }

    private void GetAndShowImageFromUrl(String imageUrl) {

        Glide.with(this).load(imageUrl).addListener(new RequestListener<Drawable>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                //Hide progress bar
                mLoadProgress.setVisibility(View.INVISIBLE);
                return false;
            }
        }).apply(new RequestOptions().fitCenter()).into(mPodImage);
    }

    private void ShowError() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_error_title);
        builder.setMessage(R.string.dialog_error_message);
        builder.setPositiveButton(R.string.dialog_error_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    private void GoToSource() {

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.url_nasa_source)));
        startActivity(i);
    }

    private void GoToHdImageUrl() {

        if (mTodaysPodEntry == null ||
                mTodaysPodEntry.getHdurl() == null ||
                mTodaysPodEntry.getHdurl().length() == 0) {
            //TODO show error to user
            return;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(mTodaysPodEntry.getHdurl()));
        startActivity(i);
    }

    private void GoToYouTubeVideo() {

        if (mTodaysPodYouTubeUrl == null || mTodaysPodYouTubeUrl.length() == 0) {
            //TODO show error to user
            return;
        }

        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
        youtubeIntent.setData(Uri.parse(mTodaysPodYouTubeUrl));
        startActivity(youtubeIntent);
    }
}
