package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.htlgrieskirchen.pos.dreic.socialert.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail_auto_reply_task);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            finish();
        }

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragDetail);
        AutoReplyTask task = (AutoReplyTask) intent.getSerializableExtra("task");
        getSupportActionBar().setTitle("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        detailFragment.show(task);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // go back to MainActivity
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}