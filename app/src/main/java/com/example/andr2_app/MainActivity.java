package com.example.andr2_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.MapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mf = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.frameLayout_map, mf).commit();
    }
}