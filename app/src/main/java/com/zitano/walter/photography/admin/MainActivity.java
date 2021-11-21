package com.zitano.walter.photography.admin;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;



public class MainActivity extends AppCompatActivity {

    LinearLayoutManager mLayoutManager; //for sorting
    SharedPreferences mSharedPref; //For saving sort settings

    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;

    Spinner spinner;
    Button btnGo;

    FirebaseRecyclerAdapter<Model, ViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Model> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseApp.initializeApp(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //Action Bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Blog Lists");

        mSharedPref = getSharedPreferences("SortSettings", MODE_PRIVATE);
        String mSorting = mSharedPref.getString("Sort", "newest"); //where if no setting is selected newest is default

        if (mSorting.equals("newest")) {
            mLayoutManager = new LinearLayoutManager(this);
            //this will load the items from bottom means newest first
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
        } else if (mSorting.equals("oldest")) {
            mLayoutManager = new LinearLayoutManager(this);
            //this will load the items from top means oldest first
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);
        }


        //recyclerview
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        spinner = findViewById(R.id.spinner);
        btnGo=findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                displayRecycler(spinner.getSelectedItem().toString().toLowerCase());
                //showData();
                //set as linearlayout
                mRecyclerView.setLayoutManager(mLayoutManager);
                firebaseRecyclerAdapter.startListening();
                //set adapter to firebase recycler view
                mRecyclerView.setAdapter(firebaseRecyclerAdapter);
            }
        });


    }

    private void displayRecycler(final String select) {
        //send query to firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference().child("Data").child(select);

        options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(mRef, Model.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Model model) {
                viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getDescription(), model.getImage());



            }
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating layout row.xml
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                ViewHolder viewHolder = new ViewHolder(itemView);

                //item click listener
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //get data from firebase at the position clicked
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();
                        String mImage = getItem(position).getImage();


                        //Pass data to the post detail activity
                        Intent intent = new Intent(view.getContext(), PostDetails.class);
                        intent.putExtra("title", mTitle);// put title
                        intent.putExtra("description", mDesc);// put description
                        intent.putExtra("image", mImage);//put image url
                        startActivity(intent);//start activity

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        final String item_key = getRef(position).getKey();
                        //get Current title
                        final String currentTitle = getItem(position).getTitle();
                        //get Current description
                        final String cDescr = getItem(position).getDescription();

                        //final String cLink = getItem(position).getLink();
                        //get current image url
                        final String currentImage = getItem(position).getImage();

                        //show dialog on long click
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        //options to display in dialog
                        String[] options = {"Update", "Delete"};
                        //set to dialog
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle dialog item clicks
                                if (which == 0){
                                    //update clicked
                                    //start activity with putting current data

                                    Intent intent = new Intent(MainActivity.this, AddPost.class);
                                    intent.putExtra("selection",select);
                                    intent.putExtra("postKey", item_key);
                                    intent.putExtra("currentTitle", currentTitle);
                                    intent.putExtra("cDescr", cDescr);
                                    //intent.putExtra("cLink", cLink);
                                    intent.putExtra("currentImage", currentImage);
                                    startActivity(intent);

                                }
                                if (which == 1){
                                    //delete clicked
                                    //method call
                                    showDeleteDataDialog(currentTitle, currentImage);
                                }
                            }
                        });
                        builder.create().show();



                    }
                });

                return viewHolder;
            }
        };

    }

    private void showDeleteDataDialog(final String currentTitle, final String currentImage) {
        //alart dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete");
        builder.setMessage("Are you Sure you want to delete this post?");
        //set positive/yes button
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Query mQuery = mRef.orderByChild("image").equalTo(currentImage);
                mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ds.getRef().removeValue();//remove value from firebase where title matches

                        }
                        Toast.makeText(MainActivity.this, "Post Successfully deleted...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
                //delete image using reference of url from firebase storage.
                StorageReference mPictureReference = getInstance().getReferenceFromUrl(currentImage);
                mPictureReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //delete successfully
                        Toast.makeText(MainActivity.this, "Post deleted successfully....", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //unable to delete
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //set negative/no button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //show data
    //private void showData(){
     /*   options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(mRef, Model.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Model model) {
                viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getDescription(), model.getImage());



            }
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating layout row.xml
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                ViewHolder viewHolder = new ViewHolder(itemView);

                //item click listener
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //get data from firebase at the position clicked
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();
                        String mImage = getItem(position).getImage();


                        //Pass data to the post detail activity
                        Intent intent = new Intent(view.getContext(), PostDetails.class);
                        intent.putExtra("title", mTitle);// put title
                        intent.putExtra("description", mDesc);// put description
                        intent.putExtra("image", mImage);//put image url
                        startActivity(intent);//start activity

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        //get Current title
                        final String currentTitle = getItem(position).getTitle();
                        //get Current description
                        final String cDescr = getItem(position).getDescription();

                        final String item_key = getRef(position).getKey();

                        //final String cLink = getItem(position).getLink();
                        //get current image url
                        final String currentImage = getItem(position).getImage();

                        //show dialog on long click
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        //options to display in dialog
                        String[] options = {"Update", "Delete"};
                        //set to dialog
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle dialog item clicks
                                if (which == 0){
                                    //update clicked
                                    //start activity with putting current data

                                    Intent intent = new Intent(MainActivity.this, AddPost.class);
                                    intent.putExtra("selection",select);
                                    intent.putExtra("postKey", item_key);
                                    intent.putExtra("currentTitle", currentTitle);
                                    intent.putExtra("cDescr", cDescr);
                                    //intent.putExtra("cLink", cLink);
                                    intent.putExtra("currentImage", currentImage);
                                    startActivity(intent);

                                }
                                if (which == 1){
                                    //delete clicked
                                    //method call
                                    showDeleteDataDialog(currentTitle, currentImage);
                                }
                            }
                        });
                        builder.create().show();



                    }
                });

                return viewHolder;
            }
        };



    }*/
    //Search data
    private void firebaseSearch(String searchText) {
        //convert string entered in the search view into lower case
        String query = searchText.toLowerCase();

        final Query firebaseSearchQuery = mRef.orderByChild("search").startAt(query).endAt(query + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(firebaseSearchQuery, Model.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Model model) {
                viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getDescription(), model.getImage());


            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating layout row.xml
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                ViewHolder viewHolder = new ViewHolder(itemView);

                //item click listener
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //get data from firebase at the position clicked
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();
                        //String mLink = getItem(position).getLink();
                        String mImage = getItem(position).getImage();


                        //Pass data to the post detail activity
                        Intent intent = new Intent(view.getContext(), PostDetails.class);
                        intent.putExtra("title", mTitle);// put title
                        intent.putExtra("description", mDesc);// put description
                        //intent.putExtra("link", mLink);// put description
                        intent.putExtra("image", mImage);//put image url
                        startActivity(intent);//start activity

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        /*//get title to delete data from firebase
                        String currentTitle = getItem(position).getTitle();
                        //get current image url to delete image from firebase storage
                        String currentImage = getItem(position).getImage();
                        //method call
                        showDeleteDataDialog(currentTitle, currentImage);*/
                        //get Current title
                        final String currentTitle = getItem(position).getTitle();
                        //get Current description
                        final String cDescr = getItem(position).getDescription();
                        //final String cLink = getItem(position).getLink();
                        //get current image url
                        final String currentImage = getItem(position).getImage();

                        //show dialog on long click
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        //options to display in dialog
                        String[] options = {"Update", "Delete"};
                        //set to dialog
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle dialog item clicks
                                if (which == 0){
                                    //update clicked
                                    //start activity with putting current data
                                    Intent intent = new Intent(MainActivity.this, AddPost.class);
                                    intent.putExtra("currentTitle", currentTitle);
                                    intent.putExtra("cDescr", cDescr);
                                    //intent.putExtra("cLink", cLink);
                                    intent.putExtra("currentImage", currentImage);
                                    startActivity(intent);

                                }
                                if (which == 1){
                                    //delete clicked
                                    //method call
                                    showDeleteDataDialog(currentTitle, currentImage);
                                }
                            }
                        });
                        builder.create().show();



                    }
                });

                return viewHolder;
            }
        };

        //set as linearlayout
        mRecyclerView.setLayoutManager(mLayoutManager);
        firebaseRecyclerAdapter.startListening();
        //set adapter to firebase recycler view
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    //Load data into recyclerview on start
   /* @Override
    protected void onStart() {
        super.onStart();
        if (firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.startListening();
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter as you search
                firebaseSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //Handle other action bar clicks here
        if (id == R.id.action_sort) {
            //Display alart dialog to choose sorting
            showSortDialog();
            return true;
        }
        if (id == R.id.action_add) {
            //start add post activity
            startActivity(new Intent(MainActivity.this, AddPost.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        //options to display in dialog
        String[] sortOptions = {"Newest", "Oldest"};
        //create alart dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setIcon(R.drawable.ic_action_sort)
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        //0 means "Newest" and 1 means "oldest"
                        if (which == 0) {
                            // Sort from newest
                            //Edit our shared preferences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "newest"); //where 'Sort' is key & 'newest' is value
                            editor.apply(); // apply/save the value in our shared preferences
                            recreate(); //restart activity to take effect


                        } else if (which == 1) {
                            //sort from oldest
                            //Edit our shared preferences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "oldest"); //where 'Sort' is key & 'oldest' is value
                            editor.apply(); // apply/save the value in our shared preferences
                            recreate(); //restart activity to take effect
                        }
                    }
                });
        builder.show();
    }
}
