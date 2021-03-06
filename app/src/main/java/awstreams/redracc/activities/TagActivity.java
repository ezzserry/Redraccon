package awstreams.redracc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import awstreams.redracc.R;
import awstreams.redracc.adapters.NewslistAdapter;
import awstreams.redracc.helpers.ConnectionDetector;
import awstreams.redracc.helpers.Constants;
import awstreams.redracc.helpers.ServicesHelper;
import awstreams.redracc.interfaces.OnNewsItemClickListener;
import awstreams.redracc.models.NewsItem;

public class TagActivity extends AppCompatActivity implements OnNewsItemClickListener {

    private TextView tvTagTitle;
    private GridLayoutManager lLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout fragmentLinearLayout;
    private NewslistAdapter newslistAdapter;
    private List<NewsItem> postItemsList;
    private String sCategory_id, sTitle;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Boolean bRefresh;
    private FrameLayout flTop, flbottom;
    private ImageView gif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        initViews();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            sCategory_id = bundle.getString(getResources().getString(R.string.tagid_intent_key));
            sTitle = bundle.getString(getResources().getString(R.string.tagtitle_intent_key));
            tvTagTitle.setText(sTitle);

            cd = new ConnectionDetector(this);
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {
                bRefresh = false;
                getTags(sCategory_id);
            } else
                Snackbar.make(mRecyclerView, "no internet connection", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

    }

    private void initViews() {
        gif = (ImageView) findViewById(R.id.gif);
        Glide.with(getApplicationContext())
                .load(R.raw.loading_gif)
                .into(new GlideDrawableImageViewTarget(gif));
        progressBar = (ProgressBar) findViewById(R.id.loadingpanel);
        fragmentLinearLayout = (LinearLayout) findViewById(R.id.fragment_ll);

        tvTagTitle = (TextView) findViewById(R.id.tagtitle_tv);
        tvTagTitle.setTypeface(Constants.getTypeface_Medium(this));
        tvTagTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.getTextAppSize(this, true, false, false));
        lLayout = new GridLayoutManager(this, 2);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lLayout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (sCategory_id != null) {
                    bRefresh = true;
                    getTags(sCategory_id);
                } else {
                    Snackbar.make(mRecyclerView, "could't refresh now", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        flTop = (FrameLayout) findViewById(R.id.top_contatiner);
        flTop.setVisibility(View.GONE);

        flbottom = (FrameLayout) findViewById(R.id.bottom_container);
        mRecyclerView.setNestedScrollingEnabled(false);


    }

    private void getTags(String id) {
        ServicesHelper.getInstance().getTag(
                this, id, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("ok")) {
                                postItemsList = new ArrayList<>();
                                JSONArray jsonArray = response.getJSONArray("posts");
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                postItemsList = Arrays.asList(gson.fromJson(jsonArray.toString(), NewsItem[].class));
                                updateUI();
                                progressBar.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                tvTagTitle.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            if (bRefresh)
                                swipeRefreshLayout.setRefreshing(false);
                        }

                        gif.setVisibility(View.GONE);
                        fragmentLinearLayout.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TagActivity.this, "connection error ", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        if (bRefresh)
                            swipeRefreshLayout.setRefreshing(false);
                        gif.setVisibility(View.GONE);
                        fragmentLinearLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void updateUI() { /* adapter*/
        newslistAdapter = new NewslistAdapter(TagActivity.this, postItemsList);
        mRecyclerView.setAdapter(newslistAdapter);
        newslistAdapter.notifyDataSetChanged();
        if (bRefresh) swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onNewsItemClick(NewsItem newsItem) {
        String id = newsItem.getId();
        Intent intent = new Intent(this, DetailedNews_Activity.class);
        intent.putExtra(getResources().getString(R.string.post_by_id_intent_key), id);
        intent.putExtra(getResources().getString(R.string.post_by_slug_intent_key), "");
        startActivity(intent);
    }
}
