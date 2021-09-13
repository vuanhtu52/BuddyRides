package rmit.ad.rmitrides;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyMessageFragment extends Fragment {
    CircleImageView avatar;
    TextView userName;
    ImageButton send;
    EditText message;
    MessageAdapter messageAdapter;
    List<Chat> mChats;
    RecyclerView recyclerView;
    Button backButton;

    private Booking booking;
    private String driverId;
    private String bookerId;
    private String currentRole = "";
    private String bookingID;

    public MyMessageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.my_message_fragment, container, false);

        avatar = layout.findViewById(R.id.profile_image);
        userName = layout.findViewById(R.id.username);
        send = layout.findViewById(R.id.button_send);
        message = layout.findViewById(R.id.text_send);
        recyclerView = layout.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        backButton = layout.findViewById(R.id.back_button);

//        FragmentTransaction ftt = getFragmentManager().beginTransaction();
//        ftt.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
//        ftt.replace(R.id.fragment_container, new PassengerOnRideFragment()).commit();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentRole.equals("booker")) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    ft.replace(R.id.fragment_container, new PassengerOnRideFragment()).commit();
                } else if (currentRole.equals("driver")) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    ft.replace(R.id.fragment_container, new DriverOnRideFragment()).commit();
                }

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        booking = ((MainActivity) getActivity()).getBooking();
        Log.i("MyMesFrag", "current booking: " + booking);
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        FireBaseRef.FBUsersRef.document(FireBaseRef.mAuth.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        User user = task.getResult().toObject(User.class);
                        if (user.getCurrentBooking_AsDriver() != null) {
                            bookingID = user.getCurrentBooking_AsDriver();

                        } else if (user.getCurrentBooking_AsPassenger() != null) {
                            bookingID = user.getCurrentBooking_AsPassenger();
                        }
                        FireBaseRef.FBBookingRef.document(bookingID).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        booking= task.getResult().toObject(Booking.class);
                                        ((MainActivity)getActivity()).setBooking(booking);

                                        driverId = booking.getAcceptedDriver();
                                        bookerId = booking.getBookerID();

                                        Log.i("MyMesFrag", "driverid: " + driverId);
                                        Log.i("MyMesFrag", "bookerid: " + bookerId);

//        Intent intent = getIntent();
//        final String userId = intent.getStringExtra("userId");
//        userName.setText(intent.getStringExtra("username"));  //friend

//        final FirebaseUser currentUser = FireBaseRef.mAuth.getCurrentUser();


                                        Log.i("current user", currentUser.getUid());
                                        if (currentUser.getUid().equals(driverId)) {
                                            currentRole = "driver";
                                        }
                                        if (currentUser.getUid().equals(bookerId)) {
                                            currentRole = "booker";
                                        }


                                        Log.i("current user role: ", currentRole);
                                        switch (currentRole) {
                                            case "driver": {
                                                readMessages(driverId, bookerId, "default");
                                                send.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        String msg = message.getText().toString();
                                                        if (!TextUtils.isEmpty(msg)) {
                                                            sendMessage(driverId, bookerId, msg);
                                                            message.setText("");
                                                        }

                                                    }
                                                });

                                                break;
                                            }
                                            case "booker": {
                                                readMessages(bookerId, driverId, "default");
                                                send.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        String msg = message.getText().toString();
                                                        if (!TextUtils.isEmpty(msg)) {
                                                            sendMessage(bookerId, driverId, msg);
                                                            message.setText("");
                                                        }

                                                    }
                                                });
                                                break;
                                            }

                                        }





                                    }
                                });

                    }


                });




//        readMessages(currentUser.getUid(),driverId,"default");
        //check update change(avatar...) on server
//        FireBaseRef.FBUsersRef.document(driverId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w("error", "Listen failed."+driverId.toString(), e);
//                    return;
//                }
//
//                String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
//                        ? "Local" : "Server";
//
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    Log.d("onEvent listen", source + " data: " + documentSnapshot.getData());
//                    //todo: update to user.
//
//
//                    readMessages(currentUser.getUid(),userId,"default");
//                } else {
//                    Log.d("listen null", source + " data: null");
//                }
//
//            }
//        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    sendMessage(currentUser.getUid(), driverId, msg);
                    message.setText("");
                }

            }
        });

        return layout;
    }


    public void sendMessage(String sender, String receiver, String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        FireBaseRef.FBChatRef.add(hashMap);

    }


    public void readMessages(final String myId, final String friendID, final String avatar) {
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
//                            messageAdapter = new MessageAdapter(getContext(),mChats,avatar);
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
                                (String) dc.getDocument().getData().get("receiver"),
                                (String) dc.getDocument().getData().get("message")
                        );
//                                chat.setId(document.getId());
                        //get message belongs to receiver (send or receive)
                        if ((chat.getReceiver().equals(friendID) && chat.getSender().equals(myId))
                                || (chat.getReceiver().equals(myId) && chat.getSender().equals(friendID))
                        ) {
                            mChats.add(chat);
                        }

                        messageAdapter = new MessageAdapter(getContext(), mChats, avatar);
                        recyclerView.setAdapter(messageAdapter);

                    }
                }

            }
        });
    }
}
