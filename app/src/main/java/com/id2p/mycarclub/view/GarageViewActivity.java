package com.id2p.mycarclub.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Garage;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.adapter.GarageCardAdapter;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

public class GarageViewActivity extends BaseDrawerActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private RecyclerView mRecyclerView = null;
    private RecyclerView.LayoutManager mLayoutManager = null;
    private RecyclerView.Adapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            ParseLoginBuilder builder = new ParseLoginBuilder(GarageViewActivity.this);
            startActivityForResult(builder.build(), 0);

            currentUser = new User();
        } else {

            // load our logged in user
            currentUser = getLoggedUser();

            setContentView(R.layout.activity_garage_view);

            mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            try {
                mAdapter = new GarageCardAdapter(currentUser);
                mRecyclerView.setAdapter(mAdapter);
            } catch (ParseException e) {
                e.printStackTrace();
                finish();
            }

            super.onCreateDrawer(currentUser);
        }

    }

    private User getLoggedUser() {
        User user = null;
        try {
            user = User.getUser(parseUser);
            if (user == null)
                user = new User();
        } catch (ParseException e) {
            Toast.makeText(GarageViewActivity.this, "Error getting user information!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_garage_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_car) {
            Intent garageIntent = new Intent(getApplicationContext(), GarageCreationActivity.class);
            startActivity(garageIntent);
        }

        return super.onOptionsItemSelected(item);
    }

}
