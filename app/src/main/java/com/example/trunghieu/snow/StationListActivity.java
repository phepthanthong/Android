package com.example.trunghieu.snow;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by TrungHieu on 03-Jul-15.
 */
public class StationListActivity extends ListActivity {

    private List<String> list;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        prefs = getSharedPreferences("MaPreference", MODE_PRIVATE);

        list = new ArrayList<>();
        Map<String,?> keys = prefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()) {
            list.add(entry.getValue().toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(StationListActivity.this, R.layout.activity_list, list);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
