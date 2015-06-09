package com.id2p.mycarclub;

import android.app.Application;

import com.id2p.mycarclub.model.Ad;
import com.id2p.mycarclub.model.Event;
import com.id2p.mycarclub.model.EventRegistration;
import com.id2p.mycarclub.model.Route;
import com.id2p.mycarclub.model.User;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;

/**
 * Created by Anderson on 2015-05-08.
 */
public class MyCarClubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Register Custom Parse types
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(EventRegistration.class);
        ParseObject.registerSubclass(Route.class);
        ParseObject.registerSubclass(Ad.class);

        // Enable Local Datastore
        Parse.enableLocalDatastore(this);

        // Initialize Parse
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Initialize login services
        ParseFacebookUtils.initialize(this);
        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

        // set default ACL
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
