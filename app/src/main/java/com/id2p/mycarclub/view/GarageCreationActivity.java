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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Garage;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.ImageAdapter;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GarageCreationActivity extends BaseDrawerActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private EditText carMakeEditText = null;
    private EditText carModelEditText = null;
    private EditText carYearEditText = null;
    private EditText carDetailsEditText = null;
    private GridView carThumbsGridView = null;
    private static final int SELECT_PHOTO = 1;
    private static final int THUMBNAIL_WIDTH = 150;
    private static final int THUMBNAIL_HEIGHT = 150;
    private static final int IMAGE_WIDTH = 600;
    private static final int IMAGE_HEIGHT = 480;
    private List<ParseFile> imageList = null;
    private List<ParseFile> thumbnailList = null;
    private static int lastSelectedIndex = 0;
    private ImageAdapter imageListAdapter = null;
    private Garage currentGarage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseUser = ParseUser.getCurrentUser();

        if (parseUser == null) {
            ParseLoginBuilder builder = new ParseLoginBuilder(GarageCreationActivity.this);
            startActivityForResult(builder.build(), 0);
        } else {
            setContentView(R.layout.activity_garage_creation);

            carMakeEditText = (EditText) findViewById(R.id.carMake);
            carModelEditText = (EditText) findViewById(R.id.carModel);
            carYearEditText = (EditText) findViewById(R.id.carYear);
            carDetailsEditText = (EditText) findViewById(R.id.carDetails);
            carThumbsGridView = (GridView) findViewById(R.id.thumbsGrid);

            // set data adapters
            imageListAdapter = new ImageAdapter(this);
            carThumbsGridView.setAdapter(imageListAdapter);

            // register events
            carThumbsGridView.setOnItemClickListener(thumbsClickListener);

            // load our logged in user
            ParseQuery<User> query = new ParseQuery<User>("User");
            query.whereEqualTo("parseUser", parseUser);
            try {
                List<User> userList = query.find();
                if (userList != null && userList.size() > 0)
                    currentUser = userList.get(0);
                else
                    currentUser = new User();
            } catch (ParseException e) {
                Toast.makeText(GarageCreationActivity.this, "Error getting user information!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // check if we are editing an existing Event or creating a new one
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.get("garage") != null) {
                currentGarage = (Garage) bundle.get("garage");
            } else {
                currentGarage = new Garage();
            }

            thumbnailList = new ArrayList<ParseFile>();
            imageList = new ArrayList<ParseFile>();

            super.onCreateDrawer(currentUser);
        }
    }

    private GridView.OnItemClickListener thumbsClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        lastSelectedIndex = position;
        startActivityForResult(intent, SELECT_PHOTO);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    String selectedImagePath = getPath(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    setGarageImage(bitmap);
                }
                else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        setGarageImage(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setGarageImage(Bitmap adImageBitmap) {

        byte[] thumbBytes = getScaledPhoto(adImageBitmap, lastSelectedIndex, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        byte[] imageBytes = getScaledPhoto(adImageBitmap, lastSelectedIndex, IMAGE_WIDTH, IMAGE_HEIGHT);

        // Save the thumbnail image to Parse
        final ParseFile thumbFile = new ParseFile("ad_image.jpg", thumbBytes);
        thumbFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(GarageCreationActivity.this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    thumbnailList.add(thumbFile);
                    imageListAdapter.setImageAtPosition(lastSelectedIndex, thumbFile);
                }
            }
        });

        // Save the ad image to Parse
        final ParseFile photoFile = new ParseFile("ad_image.jpg", imageBytes);
        photoFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(GarageCreationActivity.this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    imageList.add(photoFile);
                }
            }
        });

    }

    private byte[] getScaledPhoto(Bitmap image, final int thumbnailIndex, int width, int height) {

        // Resize photo from camera byte array
        final Bitmap userImageScaled = Bitmap.createScaledBitmap(image, width, height
                * image.getHeight() / image.getWidth(), false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        userImageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();
        return scaledData;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_garage_creation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveGarageData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveGarageData() {
        String carMake = carMakeEditText.getText().toString();
        if (carMake.length() > 0) {
            currentGarage.setCarMake(carMake);
        } else {
            Toast.makeText(GarageCreationActivity.this, "Please enter Make for your Car!", Toast.LENGTH_SHORT).show();
            return;
        }
        String carModel = carModelEditText.getText().toString();
        if (carModel.length() > 0) {
            currentGarage.setCarModel(carModel);
        } else {
            Toast.makeText(GarageCreationActivity.this, "Please enter Model for your Car!", Toast.LENGTH_SHORT).show();
            return;
        }
        String carYear = carYearEditText.getText().toString();
        if (carYear.length() > 0) {
            currentGarage.setCarYear(Integer.parseInt(carYear));
        } else {
            Toast.makeText(GarageCreationActivity.this, "Please enter the Year of your Car!", Toast.LENGTH_SHORT).show();
            return;
        }
        String carDetails = carDetailsEditText.getText().toString();
        if (carDetails.length() > 0) {
            currentGarage.setCarDetails(carDetails);
        } else {
            // not a mandatory field
        }
        if (thumbnailList != null && thumbnailList.size() > 0) {
            currentGarage.setCarThumbImages(thumbnailList);
        }
        if (imageList != null && imageList.size() > 0) {
            currentGarage.setCarImages(imageList);
        }

        if (currentUser != null) {
            currentGarage.setOwner(currentUser);
        } else {
            Toast.makeText(GarageCreationActivity.this, "You must be logged in to create an Event!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentGarage.save(); // TODO: change to saveInBackground() later
        } catch (ParseException e) {
            e.printStackTrace();
        }

        finish();

    }

}
