package awstreams.redraccon.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import awstreams.redraccon.R;
import awstreams.redraccon.helpers.ConnectionDetector;
import awstreams.redraccon.helpers.Constants;
import awstreams.redraccon.helpers.ServicesHelper;
import awstreams.redraccon.helpers.Utils;
import awstreams.redraccon.models.Tags;

public class DetailedNews_Activity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvTitle, tvExcerpt, tvName, tvDescription, tvComments_title;
    private ImageView ivPostimage, ivAuthorimage;
    private ProgressBar pbContent, pbPageContent, pbPost_img, pbAuthorimg, pbComments;
    private LinearLayout llPage_content, llTags;
    private WebView webView, webView_facebookComments;
    private String ShareURL;
    private ShareButton btnShareFb;

    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    private List<Tags> tagsList;
    private Tags hashTags;
    private TextView[] myTextViews;
    private boolean isNotifications;
    private SharedPreferences sharedPreferences;
    private boolean isLoading;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    // the default number of comments should be visible
    // on page load.
    private static final int NUMBER_OF_COMMENTS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_news);
        initViews();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            String id = bundle.getString(getResources().getString(R.string.post_intent_key));
            cd = new ConnectionDetector(this);
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {
                setPost(id);

            } else
                Snackbar.make(llPage_content, "no internet connection ", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    private void setPost(String id) {
        ServicesHelper.getInstance().getPost(this, id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("ok")) {
                        JSONObject post = response.getJSONObject("post");
                        Picasso.with(getApplicationContext())
                                .load(post.getJSONObject("thumbnail_images").getJSONObject("full").getString("url"))
                                .into(ivPostimage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        ivPostimage.setVisibility(View.VISIBLE);
                                        pbPost_img.setVisibility(View.GONE);
                                        ivPostimage.setScaleType(ImageView.ScaleType.FIT_XY);

                                    }

                                    @Override
                                    public void onError() {
                                        pbPost_img.setVisibility(View.GONE);
                                        ivPostimage.setVisibility(View.VISIBLE);
                                        ivPostimage.setImageResource(R.mipmap.placeholder);

                                    }
                                });
                        webView.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100% !important;padding:0px !important;margin:0px !important;}</style>" + post.getString("content"), "text/html", "UTF-8", null);
                        webView.setVisibility(View.VISIBLE);
                        pbContent.setVisibility(View.GONE);
                        pbPageContent.setVisibility(View.GONE);
                        llPage_content.setVisibility(View.VISIBLE);
//                      tvTitle.setText(post.getString("title"));
                        tvExcerpt.setText(Utils.convertHTMLtoString(post.getString("excerpt")));
                        tvName.setText("Author Name: " + post.getJSONObject("author").getString("name"));
                        tvDescription.setText("Nickname: " + post.getJSONObject("author").getString("nickname"));
                        collapsingToolbarLayout.setTitle(Utils.convertHTMLtoString(post.getString("title")));

                        Picasso.with(getApplicationContext())
                                .load(post.getJSONObject("author").getString("avatar"))
                                .into(ivAuthorimage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        ivAuthorimage.setVisibility(View.VISIBLE);
                                        pbAuthorimg.setVisibility(View.GONE);
//                                        dynamicToolbarColor();


                                    }

                                    @Override
                                    public void onError() {
                                        pbAuthorimg.setVisibility(View.GONE);
                                        ivAuthorimage.setVisibility(View.VISIBLE);
                                        ivAuthorimage.setImageResource(R.mipmap.redracc_menu_logo);

                                    }
                                });
                        ShareURL = post.getString("url");
                        JSONArray tagsArray = post.getJSONArray("tags");
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        tagsList = Arrays.asList(gson.fromJson(tagsArray.toString(), Tags[].class));
                        updateTags(tagsList);

//                        webView_facebookComments.loadDataWithBaseURL("http://redracc.com/",
//                                "<html><head></head><body><div id=\"fb-root\"></div><div id=\"fb-root\"></div><script>(function(d, s, id) {var js, fjs = d.getElementsByTagName(s)[0];if (d.getElementById(id)) return;js = d.createElement(s); js.id = id;js.src = \"http://connect.facebook.net/en_US/all.js#xfbml=1&appId=" + getResources().getString(R.string.facebook_app_id) + "\";fjs.parentNode.insertBefore(js, fjs);}(document, 'script', 'facebook-jssdk'));</script><div class=\"fb-comments\" data-href=\""
//                                        + "http://redracc.com/" + post.getString("slug") + "\" data-width=\"470\"></div> </body></html>", "text/html", null, null);
//                        webView_facebookComments.setWebViewClient(new WebViewClientActivity());
                        setLoading(true);
                        loadComments(ShareURL);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbPost_img.setVisibility(View.GONE);
                    pbAuthorimg.setVisibility(View.GONE);
                    ivPostimage.setVisibility(View.VISIBLE);
                    ivPostimage.setImageResource(R.mipmap.placeholder);
                    ivAuthorimage.setVisibility(View.VISIBLE);
                    ivAuthorimage.setImageResource(R.mipmap.redracc_menu_logo);


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTags(final List<Tags> tagsList) {
        LinearLayout.LayoutParams lprams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        final int noTextviews = tagsList.size();
        myTextViews = new TextView[noTextviews];
        for (int i = 0; i < noTextviews; i++) {
            hashTags = tagsList.get(i);
            hashTags.setId(tagsList.get(i).getId());
            hashTags.setSlug(tagsList.get(i).getSlug());
            hashTags.setTitle(tagsList.get(i).getTitle());
            hashTags = tagsList.get(i);
            TextView tvTag = new TextView(this);
            tvTag.setText(hashTags.getTitle() + ",");
            tvTag.setLayoutParams(lprams);
            tvTag.setId(Integer.parseInt(hashTags.getId()));
            tvTag.setTypeface(Constants.getTypeface_Medium(this));
            tvTag.setGravity(Gravity.CENTER);

            tvTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(getApplicationContext(), false, true, false));
            lprams.setMargins(5, 0, 5, 0);
            llTags.addView(tvTag);
            myTextViews[i] = tvTag;
            final int finalI = i;
            tvTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tags clickedTag = tagsList.get(finalI);
                    String tagId = clickedTag.getId();
                    String title = clickedTag.getTitle();
                    Intent intent = new Intent(DetailedNews_Activity.this, TagActivity.class);
                    intent.putExtra(getResources().getString(R.string.tagid_intent_key), tagId);
                    intent.putExtra(getResources().getString(R.string.tagtitle_intent_key), title);
                    startActivity(intent);
                }
            });
        }
    }

    private void initViews() {
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);
        pbComments = (ProgressBar) findViewById(R.id.progressBar);
        pbComments.setVisibility(View.VISIBLE);

        tvTitle = (TextView) findViewById(R.id.title_tv);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(this, true, false, false));

        tvExcerpt = (TextView) findViewById(R.id.excerpt_tv);
        tvExcerpt.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(this, false, true, false));

        tvName = (TextView) findViewById(R.id.author_name_tv);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(this, false, true, false));

        tvDescription = (TextView) findViewById(R.id.author_desc_tv);
        tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(this, false, true, false));

        tvComments_title = (TextView) findViewById(R.id.comment_title_tv);
        tvComments_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(this, true, false, false));


        ivPostimage = (ImageView) findViewById(R.id.news_iv);
        ivAuthorimage = (ImageView) findViewById(R.id.author_iv);

        btnShareFb = (ShareButton) findViewById(R.id.fb_share_btn);
        btnShareFb.setOnClickListener(this);

        pbContent = (ProgressBar) findViewById(R.id.content_pb);
        pbPageContent = (ProgressBar) findViewById(R.id.page_pb);
        pbPost_img = (ProgressBar) findViewById(R.id.top_pb);
        pbAuthorimg = (ProgressBar) findViewById(R.id.author_ivpanel);


        llPage_content = (LinearLayout) findViewById(R.id.page_ll);
        llTags = (LinearLayout) findViewById(R.id.tags_ll);


        tvTitle.setTypeface(Constants.getTypeface_Medium(this));
        tvExcerpt.setTypeface(Constants.getTypeface_Light(this));
        tvComments_title.setTypeface(Constants.getTypeface_Medium(this));
        tvName.setTypeface(Constants.getTypeface_Light(this));
        tvDescription.setTypeface(Constants.getTypeface_Light(this));

        webView = (WebView) findViewById(R.id.webview);
        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView_facebookComments = (WebView) findViewById(R.id.comments_webView);
        CookieSyncManager.createInstance(this);
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookie();
        webView_facebookComments.getSettings().setJavaScriptEnabled(true);
        webView_facebookComments.getSettings().setDomStorageEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTextAppearance();
        collapsingToolbarLayout.setTitle("");
    }

//    private void dynamicToolbarColor() {
//        ivPostimage.buildDrawingCache();
//        Bitmap bitmap = ivPostimage.getDrawingCache();
//
//        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//
//            @Override
//            public void onGenerated(Palette palette) {
//                collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(R.attr.colorPrimary));
//                collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimaryDark));
//            }
//        });
//    }

    private void toolbarTextAppearance() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fb_share_btn:
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(ShareURL))
                        .build();
                btnShareFb.setShareContent(content);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isNotifications = sharedPreferences.getBoolean(Constants.isNotification, false);
        if (isNotifications) {
            Intent intent = new Intent(DetailedNews_Activity.this, Base_Activity.class);
            startActivity(intent);
            finish();
        } else
            finish();
        super.onBackPressed();
    }

    public class WebViewClientActivity extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            System.out.println("onPageStarted: " + url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            System.out.println("onPageFinished: " + url);

        }
    }


    private void loadComments(String shareURL) {
        webView_facebookComments.setWebViewClient(new UriWebViewClient());
        webView_facebookComments.setWebChromeClient(new UriChromeClient());
        webView_facebookComments.getSettings().setJavaScriptEnabled(true);
        webView_facebookComments.getSettings().setAppCacheEnabled(true);
        webView_facebookComments.getSettings().setDomStorageEnabled(true);
        webView_facebookComments.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView_facebookComments.getSettings().setSupportMultipleWindows(true);
        webView_facebookComments.getSettings().setSupportZoom(false);
        webView_facebookComments.getSettings().setBuiltInZoomControls(false);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            webView_facebookComments.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView_facebookComments, true);
        }

        // facebook comment widget including the article url
        String html = "<!doctype html> <html lang=\"en\"> <head></head> <body> " +
                "<div id=\"fb-root\"></div> <script>(function(d, s, id) { var js, fjs = d.getElementsByTagName(s)[0]; if (d.getElementById(id)) return; js = d.createElement(s); js.id = id; js.src = \"//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.6\"; fjs.parentNode.insertBefore(js, fjs); }(document, 'script', 'facebook-jssdk'));</script> " +
                "<div class=\"fb-comments\" data-href=\"" + shareURL + "\" " +
                "data-numposts=\"" + NUMBER_OF_COMMENTS + "\" data-order-by=\"reverse_time\">" +
                "</div> </body> </html>";

        webView_facebookComments.loadDataWithBaseURL("http://www.nothing.com", html, "text/html", "UTF-8", null);
        webView_facebookComments.setMinimumHeight(200);
    }

    private void setLoading(boolean isLoading) {
        this.isLoading = isLoading;

        if (isLoading)
            pbComments.setVisibility(View.VISIBLE);
        else
            pbComments.setVisibility(View.GONE);

        invalidateOptionsMenu();
    }

    private class UriWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            String host = Uri.parse(url).getHost();

            return !host.equals("m.facebook.com");

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String host = Uri.parse(url).getHost();
            setLoading(false);
            if (url.contains("/plugins/close_popup.php?reload")) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        mContainer.removeView(mWebviewPop);
                        loadComments(ShareURL);
                    }
                }, 600);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            setLoading(false);
        }
    }

    class UriChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(getApplicationContext());
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new UriWebViewClient());
            mWebviewPop.setWebChromeClient(this);
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setDomStorageEnabled(true);
            mWebviewPop.getSettings().setSupportZoom(false);
            mWebviewPop.getSettings().setBuiltInZoomControls(false);
            mWebviewPop.getSettings().setSupportMultipleWindows(true);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.i("TAG", "onConsoleMessage: " + cm.message());
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!isLoading) {
            getMenuInflater().inflate(R.menu.fb_comments, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.action_refresh) {
            webView_facebookComments.reload();
        }

        return super.onOptionsItemSelected(item);
    }

}