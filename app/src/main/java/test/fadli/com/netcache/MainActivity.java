package test.fadli.com.netcache;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;
import test.fadli.com.netcache.adapter.DataAdapter;
import test.fadli.com.netcache.api.API;
import test.fadli.com.netcache.api.ErrorCallback;
import test.fadli.com.netcache.api.ResponseCallback;
import test.fadli.com.netcache.app.Constant;
import test.fadli.com.netcache.model.DataModel;
import test.fadli.com.netcache.realm.RealmDataAdapter;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvPull;
    private MaterialSearchView searchView;
    private DataAdapter adapter;
    private String search;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        tvPull = (TextView) findViewById(R.id.tvPull);

        linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setFocusable(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setVisibility(View.GONE);
                tvPull.setVisibility(View.GONE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (Prefs.getString(Constant.MODEL_DATA, Constant.NO_PREF).equals(Constant.NO_PREF)) {
                            getDataFromAPI();
                        } else {
                            getDataFromLocalDB();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1700);
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (Prefs.getString(Constant.MODEL_DATA, Constant.NO_PREF).equals(Constant.NO_PREF)) {
                    Toast.makeText(MainActivity.this, "getDataFromAPI", Toast.LENGTH_LONG).show();
                    getDataFromAPI();
                } else {
                    Toast.makeText(MainActivity.this, "getDataFromLocalDB", Toast.LENGTH_LONG).show();
                    getDataFromLocalDB();
                }
            }
        }, 2000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void getDataFromAPI() {
        if (isNetworkAvailable()) {
            API.getData(this, "", new ResponseCallback() {
                        @Override
                        public int doAction(String response, int httpCode) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            try {
                                JSONArray dataArray = new JSONArray(response);
                                Prefs.putString(Constant.MODEL_DATA, response);

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject data = (JSONObject) dataArray.get(i);

                                    DataModel datas = new DataModel();
                                    datas.userId = "" + data.getString("userId");
                                    datas.id = "" + data.getString("id");
                                    datas.title = "" + data.getString("title");
                                    datas.body = "" + data.getString("body");

                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    realm.copyToRealm(datas);
                                    realm.commitTransaction();
                                }

                                Realm realm = Realm.getDefaultInstance();
                                adapter = new DataAdapter(MainActivity.this);
                                recyclerView.setAdapter(adapter);
                                setRealmAdapter(realm.where(DataModel.class).findAll().sort("title"));
                                realm.refresh();

                            } catch (Exception e) {
                                Log.e("ResponseCatch", "" + e.toString());
                            }
                            return httpCode;
                        }
                    },
                    new ErrorCallback() {
                        @Override
                        public void doAction() {
                            Log.e("ErrorCallback", "ErrorCallback Called");
                            tvPull.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            tvPull.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setRealmAdapter(RealmResults<DataModel> datas) {

        RealmDataAdapter realmAdapter = new RealmDataAdapter(datas);
        // Set the data and tell the RecyclerView to draw
        adapter.setRealmAdapter(realmAdapter);
        adapter.notifyDataSetChanged();
    }

    private void getDataFromLocalDB() {
        //read by query
        Realm realm = Realm.getDefaultInstance();
        adapter = new DataAdapter(this);
        recyclerView.setAdapter(adapter);

        setRealmAdapter(realm.where(DataModel.class).findAll().sort("title"));
        realm.refresh();

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_btn_search);
        searchView.setMenuItem(item);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic

                search = query;
                Realm realm = Realm.getDefaultInstance();
                adapter = new DataAdapter(MainActivity.this);
                recyclerView.setAdapter(adapter);

                setRealmAdapter(realm.where(DataModel.class)
                        .contains("title", search).findAll().sort("title"));
                realm.refresh();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic

                if (!newText.equals("")) {
                    Realm realm = Realm.getDefaultInstance();
                    adapter = new DataAdapter(MainActivity.this);
                    recyclerView.setAdapter(adapter);

                    setRealmAdapter(realm.where(DataModel.class)
                            .contains("title", "" + newText).findAll().sort("title"));
                    realm.refresh();
                }

                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);

    }
}
