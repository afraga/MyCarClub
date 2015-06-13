package com.id2p.mycarclub.view;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.User;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ProfileActivity extends BaseDrawerActivity implements View.OnClickListener {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private ParseImageView userImage = null;
    private EditText firstNameText = null;
    private EditText lastNameText = null;
    private EditText nickNameText = null;
    private EditText emailText = null;
    private Spinner chapterSpinner = null;
    private TextView changePicture = null;
    private TextView removePicture = null;
    private final int SELECT_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            ParseLoginBuilder builder = new ParseLoginBuilder(ProfileActivity.this);
            startActivityForResult(builder.build(), 0);

            currentUser = new User();
        } else {
            setContentView(R.layout.activity_profile);

            // init UI elements
            userImage = (ParseImageView) findViewById(R.id.user_image);
            firstNameText = (EditText) findViewById(R.id.firstName);
            lastNameText = (EditText) findViewById(R.id.lastName);
            emailText = (EditText) findViewById(R.id.email);
            nickNameText = (EditText) findViewById(R.id.nickName);
            chapterSpinner = (Spinner) findViewById(R.id.chapterSpinner);
            changePicture = (TextView) findViewById(R.id.changePicture);
            removePicture = (TextView) findViewById(R.id.removePicture);
            changePicture.setOnClickListener(this);
            removePicture.setOnClickListener(this);

            // load our logged in user
            currentUser = getLoggedUser();

            // load list of chapters
            loadChapters();

            // load user data into fields
            loadUserData();

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
            Toast.makeText(ProfileActivity.this, "Error getting user information!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePicture:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO);
                break;
            case R.id.removePicture:
                currentUser.removePicture();
                userImage.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                break;
        }

    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    String selectedImagePath = getPath(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    saveScaledPhoto(bitmap);
                }
                else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        saveScaledPhoto(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadChapters() {
        String[] chapterArray = { "Ottawa", "Montreal", "Toronto" };
        ArrayAdapter mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chapterArray);
        chapterSpinner.setAdapter(mAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveUserData();
    }

    private void loadUserData() {
        if (currentUser.getFirstName() != null)
            firstNameText.setText(currentUser.getFirstName());
        if (currentUser.getLastName() != null)
            lastNameText.setText(currentUser.getLastName());
        if (currentUser.getEmail() != null)
            emailText.setText(currentUser.getEmail());
        if (currentUser.getNickName() != null)
            nickNameText.setText(currentUser.getNickName());
        if (currentUser.getChapter() != null) {
            for (int i = 0; i < chapterSpinner.getCount(); i++) {
                String item = (String) chapterSpinner.getItemAtPosition(i);
                if (item.equals(currentUser.getChapter())) {
                    chapterSpinner.setSelection(i);
                    break;
                }
            }
        }
        if (currentUser.getPicture() != null) {
            userImage.setParseFile(currentUser.getPicture());
            userImage.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    userImage.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void saveUserData() {
        if (firstNameText.getText().length() > 0)
            currentUser.setFirstName(firstNameText.getText().toString());
        if (lastNameText.getText().length() > 0)
            currentUser.setLastName(lastNameText.getText().toString());
        if (emailText.getText().length() > 0)
            currentUser.setEmail(emailText.getText().toString());
        if (nickNameText.getText().length() > 0)
            currentUser.setNickName(nickNameText.getText().toString());
        if (chapterSpinner.getSelectedItem().toString().length() > 0)
            currentUser.setChapter(chapterSpinner.getSelectedItem().toString());
        if (parseUser != null)
            currentUser.setParseUser(parseUser);

        try {
            currentUser.save(); // TODO: change to saveInBackground() later
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

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

    private Bitmap getScaledBitmap(String picturePath, int width, int height) {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, sizeOptions);

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(picturePath, sizeOptions);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /*
     * ParseQueryAdapter loads ParseFiles into a ParseImageView at whatever size
     * they are saved. Since we never need a full-size image in our app, we'll
     * save a scaled one right away.
     */
    private void saveScaledPhoto(Bitmap image) {

        // Resize photo from camera byte array
        final Bitmap userImageScaled = Bitmap.createScaledBitmap(image, 100, 100
                * image.getHeight() / image.getWidth(), false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        userImageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();

        // Save the scaled image to Parse
        final ParseFile photoFile = new ParseFile("user_photo.jpg", scaledData);
        photoFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(ProfileActivity.this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    userImage.setParseFile(photoFile);
                    userImage.loadInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            userImage.setVisibility(View.VISIBLE);
                        }
                    });
                    currentUser.setPicture(photoFile);
                }
            }
        });

    }


}
