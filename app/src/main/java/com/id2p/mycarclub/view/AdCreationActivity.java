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
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.id2p.mycarclub.R;
import com.id2p.mycarclub.model.Ad;
import com.id2p.mycarclub.model.Route;
import com.id2p.mycarclub.model.User;
import com.id2p.mycarclub.utils.adapter.ImageAdapter;
import com.id2p.mycarclub.utils.adapter.PlaceArrayAdapter;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
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

public class AdCreationActivity extends BaseDrawerActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private GoogleApiClient googleApiClient = null;
    private PlaceArrayAdapter placeArrayAdapter = null;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(28.70, -127.50), new LatLng(48.85, -55.90));
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private EditText adNameText = null;
    private EditText adDescriptionText = null;
    private Spinner adTypeSpinner = null;
    private Spinner adCategorySpinner = null;
    private EditText adPriceText = null;
    private AutoCompleteTextView adItemLocation = null;
    private GridView adThumbsView = null;
    private Ad currentAd = null;
    private static final String LOG_TAG = "AdCreationActivity";
    private Route lastClickedLocation = new Route();
    private static final LatLngBounds BOUNDS_NORTH_AMERICA = new LatLngBounds(
            new LatLng(28.70, -127.50), new LatLng(48.85, -55.90));
    private ImageAdapter adImageListAdapter = null;
    private static final int SELECT_PHOTO = 1;
    private static final int THUMBNAIL_WIDTH = 150;
    private static final int THUMBNAIL_HEIGHT = 150;
    private static final int IMAGE_WIDTH = 600;
    private static final int IMAGE_HEIGHT = 480;
    private List<ParseFile> adImageList = null;
    private List<ParseFile> adThumbnailList = null;
    private static int lastSelectedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseUser = ParseUser.getCurrentUser();

        if (parseUser == null) {
            ParseLoginBuilder builder = new ParseLoginBuilder(AdCreationActivity.this);
            startActivityForResult(builder.build(), 0);
        } else {
            setContentView(R.layout.activity_ad_creation);

            googleApiClient = new GoogleApiClient.Builder(AdCreationActivity.this)
                    .addApi(Places.GEO_DATA_API)
                    .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                    .addConnectionCallbacks(this)
                    .build();

            // init UI elements
            adNameText = (EditText) findViewById(R.id.adName);
            adDescriptionText = (EditText) findViewById(R.id.adDescription);
            adTypeSpinner = (Spinner) findViewById(R.id.adType);
            adCategorySpinner = (Spinner) findViewById(R.id.adCategory);
            adPriceText = (EditText) findViewById(R.id.adPrice);
            adItemLocation = (AutoCompleteTextView) findViewById(R.id.adItemLocationText);
            adThumbsView = (GridView) findViewById(R.id.thumbsGrid);

            // set data adapters
            placeArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_NORTH_AMERICA, null);
            adItemLocation.setAdapter(placeArrayAdapter);

            adImageListAdapter = new ImageAdapter(this);
            adThumbsView.setAdapter(adImageListAdapter);

            // register events
            adThumbsView.setOnItemClickListener(adThumbsClickListener);
            adItemLocation.setOnItemClickListener(autoCompleteClickListener);

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
                Toast.makeText(AdCreationActivity.this, "Error getting user information!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // check if we are editing an existing Event or creating a new one
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.get("ad") != null) {
                currentAd = (Ad) bundle.get("ad");
            } else {
                currentAd = new Ad();
            }

            // load list of chapters
            loadAdTypesAndCategories();

            adThumbnailList = new ArrayList<ParseFile>();
            adImageList = new ArrayList<ParseFile>();

            super.onCreateDrawer(currentUser);
        }
    }

    private GridView.OnItemClickListener adThumbsClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//            Toast.makeText(AdCreationActivity.this, "" + position, Toast.LENGTH_SHORT).show();

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
                    setAdImage(bitmap);
                }
                else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        setAdImage(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setAdImage(Bitmap adImageBitmap) {

        byte[] thumbBytes = getScaledPhoto(adImageBitmap, lastSelectedIndex, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        byte[] imageBytes = getScaledPhoto(adImageBitmap, lastSelectedIndex, IMAGE_WIDTH, IMAGE_HEIGHT);

        // Save the thumbnail image to Parse
        final ParseFile thumbFile = new ParseFile("ad_image.jpg", thumbBytes);
        thumbFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(AdCreationActivity.this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    adThumbnailList.add(thumbFile);
                    adImageListAdapter.setImageAtPosition(lastSelectedIndex, thumbFile);
                }
            }
        });

        // Save the ad image to Parse
        final ParseFile photoFile = new ParseFile("ad_image.jpg", imageBytes);
        photoFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(AdCreationActivity.this,
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    adImageList.add(photoFile);
                }
            }
        });

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

    /*
     * ParseQueryAdapter loads ParseFiles into a ParseImageView at whatever size
     * they are saved. Since we never need a full-size image in our app, we'll
     * save a scaled one right away.
     */
    private byte[] getScaledPhoto(Bitmap image, final int thumbnailIndex, int width, int height) {

        // Resize photo from camera byte array
        final Bitmap userImageScaled = Bitmap.createScaledBitmap(image, width, height
                * image.getHeight() / image.getWidth(), false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        userImageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();
        return scaledData;
    }

    private AdapterView.OnItemClickListener autoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView adapterView, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = placeArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(updatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private void loadAdTypesAndCategories() {
        String[] typeArray = { "Buy", "Sell", "Trade" };
        ArrayAdapter mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, typeArray);
        adTypeSpinner.setAdapter(mAdapter);

        String[] categoryArray = { "Accessories", "Cars", "Parts", "Services", "Other" };
        ArrayAdapter mCatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categoryArray);
        adCategorySpinner.setAdapter(mCatAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ad_creation, menu);
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
            saveAdData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAdData() {

        String adName = adNameText.getText().toString();
        if (adName.length() > 0) {
            currentAd.setName(adName);
        } else {
            Toast.makeText(AdCreationActivity.this, "Please enter a Name for the Ad!", Toast.LENGTH_SHORT).show();
            return;
        }
        String adDescription = adDescriptionText.getText().toString();
        if (adDescription.length() > 0) {
            currentAd.setDescription(adDescription);
        } else {
            Toast.makeText(AdCreationActivity.this, "Please enter a Name for the Ad!", Toast.LENGTH_SHORT).show();
            return;
        }
        String adType = adTypeSpinner.getSelectedItem().toString();
        if (adType.length() > 0) {
            currentAd.setType(adType);
        } else {
            Toast.makeText(AdCreationActivity.this, "Please select a Type for your Ad!", Toast.LENGTH_SHORT).show();
            return;
        }
        String adCategory = adCategorySpinner.getSelectedItem().toString();
        if (adCategory.length() > 0) {
            currentAd.setCategory(adCategory);
        } else {
            Toast.makeText(AdCreationActivity.this, "Please select a Category for your Ad!", Toast.LENGTH_SHORT).show();
            return;
        }
        String priceStr = adPriceText.getText().toString();
        double price = Double.parseDouble(priceStr);
        if (price >= 0) {
            currentAd.setPrice(price);
        } else {
            Toast.makeText(AdCreationActivity.this, "Please set a Price for your Ad!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastClickedLocation.getAddress().length() > 0) {
            currentAd.setLocation(lastClickedLocation);
        } else {
            Toast.makeText(AdCreationActivity.this, "Please set a Location for your Ad!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (adThumbnailList != null && adThumbnailList.size() > 0) {
            currentAd.setThumbImages(adThumbnailList);
        }
        if (adImageList != null && adImageList.size() > 0) {
            currentAd.setImages(adImageList);
        }

        if (currentUser != null) {
            currentAd.setOwner(currentUser);
        } else {
            Toast.makeText(AdCreationActivity.this, "You must be logged in to create an Event!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentAd.save(); // TODO: change to saveInBackground() later
        } catch (ParseException e) {
            e.printStackTrace();
        }

        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        placeArrayAdapter.setGoogleApiClient(googleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        placeArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    private ResultCallback<PlaceBuffer> updatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            Route previousLocation = null;
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            ParseGeoPoint newGeoPoint = new ParseGeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);

            lastClickedLocation.setAddress(place.getAddress().toString());
            lastClickedLocation.setGeoPoint(newGeoPoint);
            adItemLocation.setText(Html.fromHtml(place.getAddress().toString()));
        }
    };

}
