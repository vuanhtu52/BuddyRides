package rmit.ad.rmitrides;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverFragment extends Fragment {
    FragmentManager fm;
    FrameLayout map_container;
    AppCompatActivity thisActivity;
    int state = 0;
    ListenerRegistration listener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_driver, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        thisActivity = (AppCompatActivity) getActivity();
        thisActivity.setSupportActionBar(toolbar);
        ActionBar actionBar = thisActivity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        ImageButton bt = view.findViewById(R.id.yolo);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton bt = (ImageButton) v;
                Toast.makeText(thisActivity, "YOLO", Toast.LENGTH_SHORT).show();
                state = 1 - state;
                if (state == 0){
                    bt.setBackground(getResources().getDrawable(R.drawable.button_driver_not_available, null));
                    bt.setImageResource(R.drawable.ic_power_off);
                }
                else if (state == 1){
                    bt.setBackground(getResources().getDrawable(R.drawable.button_driver_available, null));
                    bt.setImageResource(R.drawable.ic_power_on);
                }
            }
        });


        RecyclerView recyclerView = view.findViewById(R.id.list);

        list = new ArrayList<>();
        FireBaseRef.FBBookingRef.whereEqualTo("accepted", false)
                                .whereEqualTo("status", "pending")

                .addSnapshotListener(thisActivity, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) Toast.makeText(thisActivity, "Getting Booking: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        else {
                            if (snapshots != null){
                                list.clear();
                                for (DocumentSnapshot snapshot: snapshots.getDocuments()){
                                    Booking temp = snapshot.toObject(Booking.class);
                                    temp.setReference(snapshot.getReference());
                                    if (!temp.getBookerID().equals(FireBaseRef.mAuth.getCurrentUser().getUid()) ) {
                                        list.add(temp);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        adapter = new DriverFragment_Adapter(thisActivity, list);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(thisActivity);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    List<Booking> list;
    DriverFragment_Adapter adapter;

    //Gets called when a button on the toolbar is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //When user presses up button
            case android.R.id.home:
                //Go back to HomeFragment
                getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right).replace(R.id.fragment_container, new HomeFragment()).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadFragment(Fragment fragment, int layout){
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        try {
            fragmentTransaction.replace(layout, fragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onPause() {
//        listener.remove();
        super.onPause();
    }
}
