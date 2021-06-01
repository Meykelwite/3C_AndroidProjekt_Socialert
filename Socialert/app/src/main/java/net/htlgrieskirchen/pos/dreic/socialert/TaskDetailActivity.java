package net.htlgrieskirchen.pos.dreic.socialert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            finish();
        }

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        TaskDetailFragment detailFragment = (TaskDetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragDetail);
        String task = intent.getStringExtra("task");

        getSupportActionBar().setTitle(task);
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