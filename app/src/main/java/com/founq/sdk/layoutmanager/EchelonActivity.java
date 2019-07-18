package com.founq.sdk.layoutmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

public class EchelonActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private EchelonAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echelon);
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new EchelonAdapter(this);
        SlideLayoutManager manager = new SlideLayoutManager();
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
