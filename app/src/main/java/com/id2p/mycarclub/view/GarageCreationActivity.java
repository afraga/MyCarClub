package com.id2p.mycarclub.view;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
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
import com.id2p.mycarclub.utils.ImageUtils;
import com.id2p.mycarclub.utils.adapter.ImageAdapter;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GarageCreationActivity extends AppCompatActivity {

    private ParseUser parseUser = null;
    private User currentUser = null;
    private EditText carMakeEditText = null;
    private EditText carModelEditText = null;
    private EditText carYearEditText = null;
    private EditText carDetailsEditText = null;
    private GridView carThumbsGridView = null;
    private static final int SELECT_PHOTO = 1;
    private List<ParseFile> imageList = null;
    private List<ParseFile> thumbnailList = null;
    private static int lastSelectedIndex = 0;
    private ImageAdapter imageListAdapter = null;
    private Garage currentGarage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_creation);

        parseUser = ParseUser.getCurrentUser();
        try {
            currentUser = User.getUser(parseUser);
        } catch (ParseException e) {
            Toast.makeText(this, "Unable to get user login information. Please try later! ", Toast.LENGTH_LONG).show();
            finish();
        }

        // init UI elements
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

        // check if we are editing an existing Event or creating a new one
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.get("garageId") != null) {
            String garageId = bundle.getString("garageId");
            try {
                currentGarage = Garage.getGarageById(garageId);
                loadGarageData();
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(GarageCreationActivity.this, "Unable to find item in Garage with this id! Try again later.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            currentGarage = new Garage();
            thumbnailList = new ArrayList<ParseFile>();
            imageList = new ArrayList<ParseFile>();
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
                    String selectedImagePath = ImageUtils.getPath(this, selectedImageUri);
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
        byte[] thumbBytes = ImageUtils.getScaledPhoto(adImageBitmap, ImageUtils.THUMBNAIL_WIDTH, ImageUtils.THUMBNAIL_HEIGHT);
        byte[] imageBytes = ImageUtils.getScaledPhoto(adImageBitmap, ImageUtils.IMAGE_WIDTH, ImageUtils.IMAGE_HEIGHT);

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
        // TODO: does the above mean I need to change parent so that back button loads Main activity instead of Drawer?????
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveGarageData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadGarageData() {
        carMakeEditText.setText(currentGarage.getCarMake());
        carModelEditText.setText(currentGarage.getCarModel());
        carYearEditText.setText(currentGarage.getCarYear().toString());
        carDetailsEditText.setText(currentGarage.getCarDetails());
        thumbnailList = currentGarage.getCarThumbImages();
        imageList = currentGarage.getCarImages();

        int i = 0;
        for (ParseFile file : imageList) {
            imageListAdapter.setImageAtPosition(i, file);
            i++;
        }
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

        final ProgressDialog progress = new ProgressDialog(GarageCreationActivity.this);
        progress.setMessage("Saving...");
        progress.show();

        currentGarage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progress.dismiss();
            }
        });

    }

}
