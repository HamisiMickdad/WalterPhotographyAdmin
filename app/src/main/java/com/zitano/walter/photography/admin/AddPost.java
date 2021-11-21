package com.zitano.walter.photography.admin;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AddPost extends AppCompatActivity {
    DatabaseReference mRef;
    AutoCompleteTextView mTitleEt, mDescrEt;
    ImageView mPostIv;
    Button mUploadBtn;

    //Folder path for firebase storage
    String mStoragePath = "All_Image_Upload/";
    //Root database name for firebase database
    String mDatabasePath = "Data";

    //creating uri
    Uri mFilePathUri;

    DatabaseReference nref;
    String selection;


    //creating Storage reference and database reference
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    //Progress Dialog
    ProgressDialog mProgressDialog;

    //Image request code for choosing image
    int IMAGE_REQUEST_CODE = 5;
    //intent data will be stored in this variables
    String currentTitle, cDescr, cLink, currentImage;

    Spinner spinner;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static String LEGACY_SERVER_KEY="AIzaSyCutabQ7kh1hqd7a8czGG2p68Wmt801GX8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post Activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //mTitleEt = findViewById(R.id.pTitleEt);
        mTitleEt = findViewById(R.id.pTitleEt);
        mDescrEt = findViewById(R.id.pDescrEt);
        //mLinkEt = findViewById(R.id.pLinkEt);
        mPostIv = findViewById(R.id.pImageIv);
        mUploadBtn = findViewById(R.id.pUploadBtn);
        spinner = findViewById(R.id.spinner);

        //try to get data from intent if not null
        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            //postKey = getIntent().getExtras().getString("postKey");
            selection=getIntent().getExtras().getString("selection");
            currentTitle = intent.getString("currentTitle");
            cDescr = intent.getString("cDescr");
            cLink = intent.getString("cLink");
            currentImage = intent.getString("currentImage");

            //set data to their views
            mTitleEt.setText(currentTitle);
            mDescrEt.setText(cDescr);
            //mLinkEt.setText(cLink);
            Picasso.get().load(currentImage).into(mPostIv);
            //change title of the actionbar
            actionBar.setTitle("Update Data");
            mUploadBtn.setText("Update");
        }

        // image click to choose image
        mPostIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating intent
                Intent intent = new Intent();
                //setting intent type as image to select image from phone storage
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);

            }
        });

        // Button click to upload data to firebase
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadBtn.getText().equals("Upload")){
                    //Call method to upload data to firebase
                    uploadDataToFirebase();
                }
                else {
                    //begin update
                    beginUpdate();

                }


            }
        });

        //Assign FirebaseStorage instance to storage reference object
        mStorageReference = getInstance().getReference();
        //assign firebase database instance with root database name
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child(spinner.getSelectedItem().toString().toLowerCase());

        //progress dialog
        mProgressDialog = new ProgressDialog(AddPost.this);
    }

    private void beginUpdate() {
        mProgressDialog.setTitle("Updating....");
        mProgressDialog.show();
        //first we delete the previous image
        deletePreviousImage();

    }

    private void deletePreviousImage() {
        StorageReference mPictureRef = getInstance().getReferenceFromUrl(currentImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddPost.this, "Previous image deleted...", Toast.LENGTH_SHORT).show();
                uploadNewImage();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });

    }

    private void uploadNewImage() {
        //image name
        String imageName = System.currentTimeMillis()+ ".jpg";
        //storage reference
        StorageReference storageReference2 = mStorageReference.child(mStoragePath + imageName);

        //get bitmap from imageview
        Bitmap bitmap = ((BitmapDrawable)mPostIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //compress image
        bitmap.compress(Bitmap.CompressFormat.JPEG,20, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageReference2.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddPost.this, "Uploaded", Toast.LENGTH_SHORT).show();

                //get url of newly uploaded image
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                updateDatabase(downloadUri.toString());


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });
    }

    private void updateDatabase(final String toString) {
        //new values to update to previous
        final String title = mTitleEt.getText().toString();
        final String descr = mDescrEt.getText().toString();
        //final String link = mLinkEt.getText().toString();
       /* FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mFirebaseDatabase.getReference().child("Data").child(selection).child(postKey);*/
        //if (selection != null) {

            //mRef = FirebaseDatabase.getInstance().getReference().child("Data").child(selection).child(postKey);
            FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
            mRef = mFirebaseDatabase.getReference().child("Data").child(selection);

        //nref = mRef.child("Data").child(selection).child(postKey);
        Query query = mRef.orderByChild("title").equalTo(currentTitle);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //update data
                    ds.getRef().child("title").setValue(title);
                    ds.getRef().child("search").setValue(title.toLowerCase());
                    ds.getRef().child("description").setValue(descr);
                    //ds.getRef().child("link").setValue(link);
                    ds.getRef().child("image").setValue(toString);
                }
                mProgressDialog.dismiss();
                Toast.makeText(AddPost.this, "Database Updated...", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                //finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadDataToFirebase() {
        //check whether filepathuri is empty or not
        if (mFilePathUri != null) {
            //setting progress bar title
            mProgressDialog.setTitle("Uploading......");
            //show progress dialog
            mProgressDialog.show();
            //create second StorageReference
            StorageReference storageReference2nd = mStorageReference.child(mStoragePath + System.currentTimeMillis()+ "." + getFileExtension(mFilePathUri));
            FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mRef = mFirebaseDatabase.getReference("Data");
            nref = mRef.child(spinner.getSelectedItem().toString().toLowerCase());
            //adding on success listener to StorageReference2nd
            storageReference2nd.putFile(mFilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();
                            //get title
                            String mPostTitle = mTitleEt.getText().toString().trim();
                            //get description
                            String mPostDescr = mDescrEt.getText().toString().trim();
                            //String mPostLink = mLinkEt.getText().toString().trim();
                            //hide progress dialog
                            mProgressDialog.dismiss();


                            //Show toast that image is successfully uploaded
                            Toast.makeText(AddPost.this, "Successfully Uploaded....", Toast.LENGTH_SHORT).show();
                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mPostTitle, mPostDescr, downloadUri.toString(), mPostTitle.toLowerCase());
                            //getting image upload id
                            String imageUploadId = nref.push().getKey();
                            //adding image Upload id's child element into databaseReference
                            nref.child(imageUploadId).setValue(imageUploadInfo);
                            mTitleEt.setText("");
                            mDescrEt.setText("");
                            //mLinkEt.setText("");
                            sendNotification(mPostTitle);

                        }
                    })
                    //if something goes wrong such as network failure etc
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //hide progress dialog
                            mProgressDialog.dismiss();
                            //show toast
                            Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.setTitle("Uploading....");
                        }
                    });
        }
        else {
            Toast.makeText(this, "Please select image or add image name", Toast.LENGTH_SHORT).show();
        }
    }

    //method to get the selected image file extension from file path uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        //returning the file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
// Handle back press go back to the previous activity

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            mFilePathUri = data.getData();


            try {
                //getting selected image into bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePathUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byte[] bytes = stream.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                //setting bitmap into imageview
                mPostIv.setImageBitmap(bmp);
            }
            catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void sendNotification(final String sTitle) {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json=new JSONObject();
                    JSONObject dataJson=new JSONObject();
                    dataJson.put("title",sTitle);
                    dataJson.put("body",getString(R.string.notify));
                    //dataJson.put("body",sBody);
                    json.put("data",dataJson);
                    json.put("to","/topics/walter");
                    //json.put("to","/topics/sportytipstest");
                    RequestBody body = RequestBody.create(AddPost.JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization","key=AAAA20WlKhc:APA91bHkmteA6HFMggL2ZUdVlmXpzZUCHK69b1vxFGIz6vvF60UFA4KGnnMKO_bPs3J8YYEDDJycEkhYebnnfJLNyykpJFwf-HW6IUB2wTDr5MBQlRRJGehmqYtUninLG8YUTX8lrv2P")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();

                }catch (Exception e){
                }
                return null;
            }
        }.execute();

    }

}