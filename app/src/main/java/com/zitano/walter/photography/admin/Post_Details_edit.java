package com.zitano.walter.photography.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Post_Details_edit extends AppCompatActivity {

   /* DatabaseReference mRef;

    EditText  mTitleEt, mDescrEt;

    ImageView mPostIv;
    ProgressDialog pd;

    String selection,postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setContentView(R.layout.activity_post_details_edit);

        postKey = getIntent().getExtras().getString("postKey");
        selection=getIntent().getExtras().getString("selection");

        mDescrEt = findViewById(R.id.mDescrEt);
        mTitleEt = findViewById(R.id.mTitleEt);
        mPostIv = findViewById(R.id.pImageIv);
        pd=new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();


        if (postKey != null) {

            mRef = FirebaseDatabase.getInstance().getReference().child("Data").child(selection).child(postKey);
        }
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    String title = dataSnapshot.child("title").getValue().toString();
                    String description = dataSnapshot.child("description").getValue().toString();
                    String search = dataSnapshot.child("search").getValue().toString();
                    if (title != null) {
                        mTitleEt.setText(title.toUpperCase());
                        pd.dismiss();
                    } else {
                        Toast.makeText(Post_Details_edit.this, "Check your internet connection and try again", Toast.LENGTH_SHORT).show();
                    }
                    if (description != null) {
                        mDescrEt.setText(description);

                    }
                    if (search != null) {
                        //tvLinks.setText(link);

                    }
                    if (dataSnapshot.hasChild("image")){
                        String image = (String) dataSnapshot.child("image").getValue();

                    }

                }catch (Exception e){

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        int edit=0;
        if (id == android.R.id.home) {
            finish();
        }
        if(id==R.id.delete){
            mRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(Post_Details_edit.this, "deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        if(id==R.id.edit){

            item.setVisible(false);

            final FloatingActionButton fab= (FloatingActionButton) findViewById(R.id.fab_done);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("title", mTitleEt.getText().toString());
                    map.put("description", mDescrEt.getText().toString());
                    map.put("search", mTitleEt.getText().toString().toLowerCase());
                    mRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            item.setVisible(true);
                            fab.setVisibility(View.GONE);
                        }
                    });
                }
            });



        }

        return super.onOptionsItemSelected(item);
    }*/
}
