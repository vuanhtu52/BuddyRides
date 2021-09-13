package rmit.ad.rmitrides;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    UserDapterTutor userDapterTutor;
    List<User> mUsers;
    String currentUserId = FireBaseRef.mAuth.getCurrentUser().getUid();
    List<User> chatUsers;



    public ChatFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        getUsers();
        /////
        readChats();

        return view;
    }

    private void readChats() {

        chatUsers = new ArrayList<>();
        FireBaseRef.FBChatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("listen update", "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Log.d("listen update", "New message: " + dc.getDocument().getData());
                        Chat chat = new Chat((String) dc.getDocument().getData().get("sender"),
                                (String)dc.getDocument().getData().get("receiver"),
                                (String)dc.getDocument().getData().get("message")
                        );
                        //those i sent message to
                        if (chat.getSender().equals(currentUserId)){
                            User receiver = findUserById(chat.receiver);
                            if(receiver != null){
                                chatUsers.add(receiver);
                            }

                        }
                        //those who sent message to me
                        if (chat.getReceiver().equals(currentUserId)){
                            User sender = findUserById(chat.sender);
                            if(sender != null){
                                chatUsers.add(sender);
                            }

                        }
//
                        userDapterTutor = new UserDapterTutor(getContext(),chatUsers);
                        recyclerView.setAdapter(userDapterTutor);
//                        userDapterTutor = new MessageAdapter(MyMessageFragment.this,mChats,avatar);
//                        recyclerView.setAdapter(messageAdapter);

                    }
                }

            }
        });
    }

    User findUserById(String receiverId){
        for (int i = 0; i < mUsers.size(); i++) {
            if(mUsers.get(i).getId().equals(receiverId)){
                return mUsers.get(i);
            }
        }
        return null;
    }
    private void getUsers() {
//        FirebaseUser firebaseUser = FireBaseRef.mAuth.getCurrentUser();
        FireBaseRef.FBUsersRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("get users", document.getId() + " => " + document.getData());
//                                if(!document.getId().equals(FireBaseRef.mAuth.getCurrentUser().getUid())){
                                User user = new User((String) document.getData().get("email"),
                                        (String)document.getData().get("username"),
                                        (String)document.getData().get("fullName")
                                );
                                user.setId(document.getId());
                                if(!FireBaseRef.mAuth.getCurrentUser().getEmail().equals(document.getData().get("email"))){
                                    mUsers.add(user);
                                }

//                                }


                            }
//                            userDapterTutor = new UserDapterTutor(getContext(),mUsers);
//                            recyclerView.setAdapter(userDapterTutor);
                        } else {
                            Log.w("get user fail", "Error getting documents.", task.getException());
                        }
                    }
                });

        FireBaseRef.FBUsersRef .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("jjgj", "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        Log.d("dwfgsf", "New city: " + dc.getDocument().getData());
                    }
                }

            }
        });

    }


}
