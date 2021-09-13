package rmit.ad.rmitrides;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    CircleImageView avatar;
    TextView username;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        avatar = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
//        Log.d("curent user", "currentuser: " +FireBaseRef.mAuth.getUid() );
        FireBaseRef.FBUsersRef.whereEqualTo("email", FireBaseRef.mAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("fgsdgf", document.getId() + " => " + document.getData());
                                User currentUser = new User((String) document.getData().get("email"),
                                (String)document.getData().get("username"),
                                (String)document.getData().get("fullName")
                        );
                        username.setText(currentUser.getFullName());
                        if(currentUser.avatar.equals("default")){
                            avatar.setImageResource(R.drawable.profileimage);
                        }else {
                            Glide.with(getContext()).load(currentUser.avatar).into(avatar);
                        }

                            }
                        } else {
                            Log.d("vbfb", "Error getting documents: ", task.getException());
                        }
                    }
                });




        FireBaseRef.FBUsersRef.document(FireBaseRef.mAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("dfasf", "Listen failed.", e);
                            return;
                        }

                        String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Log.d("dfasf", source + " data: " + documentSnapshot.getData());
                            User currentUser = new User((String) documentSnapshot.getData().get("email"),
                                    (String)documentSnapshot.getData().get("username"),
                                    (String)documentSnapshot.getData().get("fullName")
                            );
                            username.setText(currentUser.getUsername());
                            if(currentUser.avatar.equals("default")){
                                avatar.setImageResource(R.drawable.profileimage);
                            }else {
                                Glide.with(getContext()).load(currentUser.avatar).into(avatar);
                            }

                        } else {
                            Log.d("dfasf", source + " data: null");
                        }

                    }
                });




        return view;
    }

}
