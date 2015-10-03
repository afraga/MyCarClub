package com.id2p.mycarclub.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.User;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

public class MainActivity extends AppCompatActivity {

    private static final int HOME_ACTION = 1;
    private static final int EVENT_LIST_ACTION = 2;
    private static final int EVENT_CREATE_ACTION = 3;
    private static final int GARAGE_LIST_ACTION = 4;
    private static final int GARAGE_CREATE_ACTION = 5;
    private static final int AD_CREATE_ACTION = 6;
    private static final int PROFILE_ACTION = 7;
    private static final int PROFILE_LOGOUT = 8;
    private static final int LOGIN_CODE = 100;

    private ParseUser parseUser = null;
    private User currentUser = null;
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private IProfile profile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            showUserLogin();
        } else {
            try {
                currentUser = User.getUser(parseUser);
            } catch (ParseException e) {
                Toast.makeText(this, "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
                finish();
            }
            showMainActivity(savedInstanceState);
        }

    }

    private void showUserLogin() {
        ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
        startActivityForResult(builder.build(), LOGIN_CODE);
    }

    private void showProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void showMainActivity(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        // setup toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup user account info
        profile = new ProfileDrawerItem().withName("Anderson Fraga").withEmail("af@id2p.ca").withIcon(getResources().getDrawable(R.drawable.profile));

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(false)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("Edit Profile").withIcon(GoogleMaterial.Icon.gmd_verified_user).withIdentifier(PROFILE_ACTION),
                        new ProfileSettingDrawerItem().withName("Log out").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(PROFILE_LOGOUT)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {
                        Intent intent = null;
                        if (iProfile.getIdentifier() == PROFILE_ACTION) {
                            intent = new Intent(MainActivity.this, ProfileActivity.class);
                        } else if (iProfile.getIdentifier() == PROFILE_LOGOUT) {
                            if (parseUser != null) {
                                parseUser.logOut();
                                showUserLogin();
                            }
                        }
                        if (intent != null) {
                            MainActivity.this.startActivity(intent);
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        // Create the Drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home").withIcon(FontAwesome.Icon.faw_home).withIdentifier(1),
                        new SectionDrawerItem().withName("Friend"),
                        new PrimaryDrawerItem().withName("Invite Friend").withIcon(FontAwesome.Icon.faw_user_plus),
                        new PrimaryDrawerItem().withName("Friends List").withIcon(FontAwesome.Icon.faw_users),
                        new SectionDrawerItem().withName("Event"),
                        new PrimaryDrawerItem().withName("View Events").withIcon(FontAwesome.Icon.faw_book).withIdentifier(EVENT_LIST_ACTION),
                        new PrimaryDrawerItem().withName("Create Event").withIcon(FontAwesome.Icon.faw_plus).withIdentifier(EVENT_CREATE_ACTION),
                        new SectionDrawerItem().withName("Garage"),
                        new PrimaryDrawerItem().withName("My Garage").withIcon(FontAwesome.Icon.faw_car).withIdentifier(GARAGE_LIST_ACTION),
                        new PrimaryDrawerItem().withName("Friend's Garage").withIcon(FontAwesome.Icon.faw_car),
                        new SectionDrawerItem().withName("Market Place"),
                        new PrimaryDrawerItem().withName("Post Ad").withIcon(FontAwesome.Icon.faw_buysellads).withIdentifier(AD_CREATE_ACTION),
                        new PrimaryDrawerItem().withName("Search Market").withIcon(FontAwesome.Icon.faw_search)
                )
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                        //if the back arrow is shown. close the activity
                        MainActivity.this.finish();
                        //return true if we have consumed the event
                        return true;
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem == null)
                            return false;

                        Intent intent = null;
                        switch (drawerItem.getIdentifier()) {
                            case EVENT_CREATE_ACTION:
                                intent = new Intent(MainActivity.this, EventCreationActivity.class);
                                break;
                            case EVENT_LIST_ACTION:
                                intent = new Intent(MainActivity.this, EventActivity.class);
                                break;
                            case GARAGE_LIST_ACTION:
                                intent = new Intent(MainActivity.this, GarageViewActivity.class);
                                break;
                            case AD_CREATE_ACTION:
                                intent = new Intent(MainActivity.this, AdCreationActivity.class);
                                break;
                        }
                        if (intent != null) {
                            MainActivity.this.startActivity(intent);
                        }
                        return false;
                    }
                })
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName("Settings").withIcon(FontAwesome.Icon.faw_cog).withIdentifier(10)
                )
                .withAnimateDrawerItems(true)
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_CODE ) {
            if (resultCode==RESULT_OK) {
                parseUser = ParseUser.getCurrentUser();
                currentUser = getCurrentUser(parseUser);
                if (parseUser.isNew()) {
                    currentUser.setParseUser(parseUser);
                    if (parseUser.getEmail() != null)
                        currentUser.setEmail(parseUser.getEmail());
                    currentUser.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                showProfileActivity();
                            }
                        }
                    });
                } else {
                    showMainActivity(null);
                }
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (result != null) {
            //add the values which need to be saved from the drawer to the bundle
            outState = result.saveInstanceState(outState);
        }
        if (headerResult != null) {
            //add the values which need to be saved from the accountHeader to the bundle
            outState = headerResult.saveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private User getCurrentUser(ParseUser parseUser) {
        User user = null;
        try {
            user = User.getUser(parseUser);
            if (user == null) {
                user = new User();
            }
        } catch (ParseException e) {
            user = new User();
        }

        return user;
    }

}


/*
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.User;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

public class MainActivity extends BaseDrawerActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private static int LOGIN_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            showUserLogin();
        } else {
            try {
                currentUser = User.getUser(parseUser);
            } catch (ParseException e) {
                Toast.makeText(this, "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
                finish();
            }
            showMainActivity();
        }
    }

    private void showUserLogin() {
        ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
        startActivityForResult(builder.build(), LOGIN_CODE);
    }

    private void showMainActivity() {
        setContentView(R.layout.activity_main);
        super.onCreateDrawer(currentUser);
    }

    private void showProfileActivity() {
        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(profileIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_CODE ) {
            if (resultCode==RESULT_OK) {
                parseUser = ParseUser.getCurrentUser();
                currentUser = getCurrentUser(parseUser);
                if (parseUser.isNew()) {
                    currentUser.setParseUser(parseUser);
                    if (parseUser.getEmail() != null)
                        currentUser.setEmail(parseUser.getEmail());
                    currentUser.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                showProfileActivity();
                            }
                        }
                    });
                } else {
                    showMainActivity();
                }
            } else {
                finish();
            }
        }
    }

    private User getCurrentUser(ParseUser parseUser) {
        User user = null;
        try {
            user = User.getUser(parseUser);
            if (user == null) {
                user = new User();
            }
        } catch (ParseException e) {
            user = new User();
        }

        return user;
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        return super.onOptionsItemSelected(item);
    }
}
*/