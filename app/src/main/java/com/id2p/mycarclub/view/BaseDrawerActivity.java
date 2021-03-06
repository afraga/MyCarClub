package com.id2p.mycarclub.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.User;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;


public class BaseDrawerActivity extends AppCompatActivity {

    private View mDrawerHeader;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ParseImageView mUserPicture;
    private TextView mUserName;
    private String mActivityTitle;

    protected void onCreateDrawer(User loggedUser) {
        mDrawerHeader = getLayoutInflater().inflate(R.layout.basedrawer_header, null);
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        mUserName = (TextView) mDrawerHeader.findViewById(R.id.user_name);
        mUserPicture = (ParseImageView) mDrawerHeader.findViewById(R.id.user_image);

        mDrawerList.addHeaderView(mDrawerHeader, null, false);

        addDrawerItems();
        setupDrawer();

        if (loggedUser != null) {
            StringBuilder userName = new StringBuilder();
            if (loggedUser.getFirstName() != null)
                userName.append(loggedUser.getFirstName());
            if (loggedUser.getLastName() != null) {
                userName.append(" ");
                userName.append(loggedUser.getLastName());
            }
            if (userName.length() == 0 && loggedUser.getEmail() != null)
                userName.append(loggedUser.getEmail());

            mUserName.setText(userName);
            mUserPicture.setParseFile(loggedUser.getPicture());
            mUserPicture.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    mUserPicture.setVisibility(View.VISIBLE);
                }
            });
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void addDrawerItems() {
        String[] osArray = { "Home", "Profile", "Garage", "Events", "Friends", "Market Place", "Logout" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case  1:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                        break;
                    case 2:
                        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(profileIntent);
                        break;
                    case 3:
                        Intent garageIntent = new Intent(getApplicationContext(), GarageViewActivity.class);
                        garageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(garageIntent);
                        break;
                    case 4:
                        Intent eventIntent = new Intent(getApplicationContext(), EventListActivity.class);
                        eventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(eventIntent);
                        break;
                    case 5:
                        Toast.makeText(BaseDrawerActivity.this, "Friends", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Intent adIntent = new Intent(getApplicationContext(), AdCreationActivity.class);
                        adIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(adIntent);
                        break;
                    case 7:
                        if (ParseUser.getCurrentUser() != null)
                            ParseUser.getCurrentUser().logOut();

                        Intent homeIntent2 = new Intent(getApplicationContext(), MainActivity.class); // TODO: should I create another homeIntent or keep already loaded ones globally and use them, lookup best practices on android
                        homeIntent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent2);

                        break;
                    default:
                        break;
                }

            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
