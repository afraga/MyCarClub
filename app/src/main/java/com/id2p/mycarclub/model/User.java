package com.id2p.mycarclub.model;

import android.widget.Toast;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("User")
public class User extends ParseObject {

    public User() {

    }

    public void setParseUser(ParseUser parseUser) {
        put("parseUser", parseUser);
    }

    public ParseUser getParseUser() {
        return getParseUser("parseUser");
    }

    public void setFirstName(String firstName) {
        put("firstName", firstName);
    }

    public String getFirstName() {
        return getString("firstName");
    }

    public void setLastName(String lastName) {
        put("lastName", lastName);
    }

    public String getLastName() {
        return getString("lastName");
    }

    public void setNickName(String nickName) {
        put("nickName", nickName);
    }

    public String getNickName() {
        return getString("nickName");
    }

    public void setEmail(String email) {
        put("email", email);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setChapter(String chapter) {
        put("chapter", chapter);
    }

    public String getChapter() {
        return getString("chapter");
    }

    public void setPicture(ParseFile file) {
        put("picture", file);
    }

    public ParseFile getPicture() {
        return getParseFile("picture");
    }

    public void removePicture() { remove("picture"); }

    public static User getUser(ParseUser parseUser) throws ParseException {
        ParseQuery<User> query = new ParseQuery<User>("User");
        query.whereEqualTo("parseUser", parseUser);

        return query.getFirst();
    }
}
