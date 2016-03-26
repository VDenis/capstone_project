package com.denis.home.sunnynotes.noteDetail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.denis.home.sunnynotes.R;

public class NoteDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            NoteDetailFragment fragment = NoteDetailFragment.newInstance(getIntent().getData());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.note_detail_container, fragment)
                    .commit();
        }

    }

}
