package test.fadli.com.netcache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private String TAG = "DetailActivity";
    private TextView tvTitle, tvBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getExtras().getString("title"));
        tvBody = (TextView) findViewById(R.id.tvBody);
        tvBody.setText(getIntent().getExtras().getString("body"));

    }
}
