package rmit.ad.rmitrides;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class PassengerHistoryFragment extends Fragment {

    AppCompatActivity thisActivity;

//    public PassengerHistoryFragment() {
//        // Required empty public constructor
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        thisActivity = (AppCompatActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_passenger_history, container, false);
//
//
//
//        RecyclerView recyclerView = view.findViewById(R.id.passenger_recycler);
//        list = new ArrayList<>();
//
//
//        adapter = new HistoryFragment_Adapter(thisActivity, list,"passenger");
//
//        RecyclerView.LayoutManager manager = new LinearLayoutManager(thisActivity);
//        recyclerView.setLayoutManager(manager);
//        recyclerView.setAdapter(adapter);
//
//        FireBaseRef.FBBookingRef.whereEqualTo("status","finished")
//                                .whereEqualTo("bookerID", FireBaseRef.mAuth.getUid())
//                                .addSnapshotListener(thisActivity, new EventListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                                        if (e != null) Toast.makeText(thisActivity, "Getting Booking: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                                            else {
//                                                if (queryDocumentSnapshots != null){
//                                                    list.clear();
//                                                    for (DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
//                                                        Booking temp = snapshot.toObject(Booking.class);
//                                                        temp.setReference(snapshot.getReference());
//                                                            list.add(temp);
//
//                                                    }
//                                                    adapter.notifyDataSetChanged();
//                                                }
//                                            }
//                                        }
//                                    });
        return view;
    }

    List<Booking> list;
    HistoryFragment_Adapter adapter;


}
