package com.example.mushroommanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class AllMushrooms extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_mushrooms);


        Fragment fragment = new AllMushroomsMapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();

    }
}