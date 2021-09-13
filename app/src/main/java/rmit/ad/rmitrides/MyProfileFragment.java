package rmit.ad.rmitrides;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyProfileFragment extends Fragment {

    //Views
    private ImageButton changeProfileButton;
    private ImageButton changeBackgroundButton;
    private ImageView profileImage;
    private ImageView backgroundImage;
    private Button editButton;
    private Button saveButton;
    private TextView fullNameText;
    private EditText fullNameEditText;
    private EditText emailText;
    private EditText usernameText;
    private EditText birthdateText;
    private Spinner spinner;
    private ImageButton backButton;

    //Firebase properties
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Other properties
    private Integer REQUEST_CAMERA = 1, SELECT_PROFILE = 0, SELECT_BACKGROUND = 2;
    private User user;

    public MyProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_my_profile, container, false);

        //Initialize the views
        //toolbar = layout.findViewById(R.id.toolbar);
        changeProfileButton = layout.findViewById(R.id.change_profile_button);
        changeBackgroundButton = layout.findViewById(R.id.change_background_button);
        profileImage = layout.findViewById(R.id.profile_image);
        backgroundImage = layout.findViewById(R.id.background);
        editButton = layout.findViewById(R.id.edit_button);
        saveButton = layout.findViewById(R.id.save_button);
        fullNameText = layout.findViewById(R.id.fullNameText);
        fullNameEditText = layout.findViewById(R.id.fullNameEditText);
        emailText = layout.findViewById(R.id.email);
        usernameText = layout.findViewById(R.id.username);
        birthdateText = layout.findViewById(R.id.birthdate);
        spinner = layout.findViewById(R.id.spinner);
        backButton = layout.findViewById(R.id.back_button);

        //Disable spinner by default
        spinner.setEnabled(false);

        //Get user information from MainActivity
        user = new User(((MainActivity)getActivity()).getUser());

        //Go back to SettingsFragment when user presses back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                ft.replace(R.id.fragment_container, new SettingsFragment()).commit();
            }
        });

        //Display user info to the views
        fullNameText.setText(user.getFullName());
        fullNameEditText.setText(user.getFullName());
        emailText.setText(user.getEmail());
        usernameText.setText(user.getUsername());
        switch (user.getGender()) {
            case "":
                spinner.setSelection(3);
                break;
            case "Male":
                spinner.setSelection(0);
                break;
            case "Female":
                spinner.setSelection(1);
                break;
            case "Other":
                spinner.setSelection(2);
                break;
        }
        birthdateText.setText(user.getBirthdate());

        //Set profile image
        if (((MainActivity)getActivity()).getProfileImage() != null) {
            profileImage.setImageBitmap(((MainActivity)getActivity()).getProfileImage());
        }

        //Set background image
        if (((MainActivity)getActivity()).getBackgroundImage() != null) {
            backgroundImage.setImageBitmap(((MainActivity)getActivity()).getBackgroundImage());
        }

        //Set up toolbar
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        toolbar.setTitle("");
//        toolbar.bringToFront();

        //Allow toolbar to have action options
        //setHasOptionsMenu(true);

        //When user wants to change profile image
        changeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(SELECT_PROFILE);
            }
        });

        //When user wants to change background image
        changeBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(SELECT_BACKGROUND);
            }
        });

        //When user presses edit button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editButton.setVisibility(View.INVISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                //Enable the views to edit information
                fullNameText.setVisibility(View.INVISIBLE);
                fullNameEditText.setVisibility(View.VISIBLE);
                //emailText.setEnabled(true);
                usernameText.setEnabled(true);
                birthdateText.setEnabled(true);
                spinner.setEnabled(true);
            }
        });

        //When user changes birthdate
        birthdateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });

        //When user presses save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Disable the views
                //emailText.setEnabled(false);
                usernameText.setEnabled(false);
                birthdateText.setEnabled(false);
                spinner.setEnabled(false);

                //Update information in usernameText
                if (usernameText.getText().equals("") || usernameText.equals(user.getUsername())) {
                    usernameText.setText(user.getUsername());
                }

                //Update fullNameText and fullNameEditText
                if (fullNameEditText.getText().toString().equals("")) {
                    fullNameEditText.setText(user.getFullName());
                    fullNameText.setText(user.getFullName());
                } else {
                    fullNameText.setText(fullNameEditText.getText().toString());
                }

                //Update user in this fragment
                user.setFullName(fullNameEditText.getText().toString());
                user.setUsername(usernameText.getText().toString());
                user.setBirthdate(birthdateText.getText().toString());
                user.setGender(String.valueOf(spinner.getSelectedItem()));

                //Update user in MainActivity
                ((MainActivity)getActivity()).getUser().setFullName(fullNameEditText.getText().toString());
                ((MainActivity)getActivity()).getUser().setUsername(usernameText.getText().toString());
                ((MainActivity)getActivity()).getUser().setBirthdate(birthdateText.getText().toString());
                ((MainActivity)getActivity()).getUser().setGender(String.valueOf(spinner.getSelectedItem()));

                //Update user in firebase
                db.collection("users")
                        .document(user.getId())
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("MyProfileFragment", "User information updated");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("MyProfileFragment", "User information update failed");
                            }
                        });

                //Show appropriate buttons and views
                saveButton.setVisibility(View.INVISIBLE);
                editButton.setVisibility(View.VISIBLE);
                fullNameText.setVisibility(View.VISIBLE);
                fullNameEditText.setVisibility(View.INVISIBLE);
            }
        });

        return layout;
    }

    private void selectImage(final int CODE) {
        final CharSequence[] items = {"Camera", "Gallery", "Remove Image", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), CODE);
                } else if (items[i].equals("Remove Image")) {
                    if (CODE == SELECT_PROFILE) {
                        //Set default profile image
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.profileimage));
                        ((MainActivity)getActivity()).setProfileImage(null);
                        //Delete profile image on firebase storage
                        StorageReference profileImageRef = storageRef.child(mAuth.getUid()).child("myprofile.jpg");
                        profileImageRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("MyProfileFragment", "Profile image deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("MyProfileFragment", "Error deleting profile image");
                                    }
                                });
                    } else if (CODE == SELECT_BACKGROUND) {
                        //Set default background image
                        backgroundImage.setImageDrawable(getResources().getDrawable(R.drawable.black_background));
                        ((MainActivity)getActivity()).setBackgroundImage(null);
                        //Delete profile image on firebase storage
                        StorageReference backgroundImageRef = storageRef.child(mAuth.getUid()).child("mybackground.jpg");
                        backgroundImageRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("MyProfileFragment", "Background image deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("MyProfileFragment", "Error deleting background image");
                                    }
                                });
                    }

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                if (requestCode == SELECT_PROFILE) {
                    profileImage.setImageBitmap(bmp);
                } else if (requestCode == SELECT_BACKGROUND) {
                    backgroundImage.setImageBitmap(bmp);
                }
            } else if (requestCode == SELECT_PROFILE) {
                //Complete profile changing
                final Uri selectedImageUri = data.getData();
                //Upload profile image to firebase storage
                StorageReference imagesRef = storageRef.child(mAuth.getUid()).child("myprofile.jpg");
                imagesRef.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Set new profile image to imageview
                                profileImage.setImageURI(selectedImageUri);
                                //Update profile image in MainActivity
                                try {
                                    Bitmap profile = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                                    ((MainActivity)getActivity()).setProfileImage(profile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.i("MyProfileFragment", "Profile image uploaded");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("MyProfileFragment", "Error uploading profile image");
                            }
                        });
            } else if (requestCode == SELECT_BACKGROUND) {
                //Complete background changing
                final Uri selectedImageUri = data.getData();
                //Upload background image to firebase storage
                StorageReference imagesRef = storageRef.child(mAuth.getUid()).child("mybackground.jpg");
                imagesRef.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Set new background image to imageview
                                backgroundImage.setImageURI(selectedImageUri);
                                //Update background image in MainActivity
                                try {
                                    Bitmap background = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                                    ((MainActivity)getActivity()).setBackgroundImage(background);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("MyProfileFragment", "Error uploading background image");
                            }
                        });
            }
        }
    }

    private void openDatePicker() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                birthdateText.setText(i2 + "/" + String.valueOf(i1 + 1) + "/" + i);
            }
        };

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
