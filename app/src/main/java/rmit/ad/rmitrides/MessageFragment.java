package rmit.ad.rmitrides;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {
    CircleImageView avatar;
    TextView userName;
    ImageButton send;
    EditText message;
    MessageAdapter messageAdapter;
    List<Chat> mChats;
    RecyclerView recyclerView;

    public MessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_message, container, false);
        avatar = layout.findViewById(R.id.profile_image);
        userName = layout.findViewById(R.id.username);
        send = layout.findViewById(R.id.button_send);
        message = layout.findViewById(R.id.text_send);
        recyclerView = layout.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = "userid";   //Get driver id from listener

        Log.i("current user", currentUser.getUid());
        readMessages(currentUser.getUid(),userId,"default");
        //check update change(avatar...) on server
        FireBaseRef.FBUsersRef.document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("error", "Listen failed."+userId.toString(), e);
                    return;
                }

                String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d("onEvent listen", source + " data: " + documentSnapshot.getData());
                    //todo: update to user.


//                    readMessages(currentUser.getUid(),userId,"default");
                } else {
                    Log.d("listen null", source + " data: null");
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if(!TextUtils.isEmpty(msg)){
                    sendMessage(currentUser.getUid(),userId,msg);
                    message.setText("");
                }

            }
        });

        return layout;
    }

    public void sendMessage(String sender, String receiver, String message){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        FireBaseRef.FBChatRef.add(hashMap);

    }


    public void readMessages(String myId, final String userId, final String avatar){
        mChats = new ArrayList<>();

//        FireBaseRef.FBChatRef.get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("get users", document.getId() + " => " + document.getData());
//                                Chat chat = new Chat((String) document.getData().get("sender"),
//                                        (String)document.getData().get("receiver"),
//                                        (String)document.getData().get("message")
//                                );
////                                chat.setId(document.getId());
//                                mChats.add(chat);
//
//                            }
//                            messageAdapter = new MessageAdapter(MyMessageFragment.this,mChats,avatar);
//                            recyclerView.setAdapter(messageAdapter);
//                        } else {
//                            Log.w("get user fail", "Error getting documents.", task.getException());
//                        }
//                    }
//                });
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
//                                chat.setId(document.getId());
                        if(chat.getReceiver().equals(userId) || chat.getSender().equals(userId)){
                            mChats.add(chat);
                        }

                        messageAdapter = new MessageAdapter(getContext(),mChats,avatar);
                        recyclerView.setAdapter(messageAdapter);

                    }
                }

            }
        });
    }

}
