package com.id2p.mycarclub.view;

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
