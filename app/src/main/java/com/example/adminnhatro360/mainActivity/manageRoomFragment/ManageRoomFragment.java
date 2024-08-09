package com.example.adminnhatro360.mainActivity.manageRoomFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminnhatro360.R;
import com.example.adminnhatro360.mainActivity.manageRoomFragment.roomDetailActivity.RoomDetailActivity;
import com.example.adminnhatro360.model.Room;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ManageRoomFragment extends Fragment implements OnRoomClickListener, ManageRoomAdapter.OnRoomClickListener {

    private static final String TAG = "ManageRoomFragment";
    private RecyclerView recyclerViewRooms;
    private ManageRoomAdapter adapter;
    private List<Object> itemList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_room, container, false);

        init(view);
        db = FirebaseFirestore.getInstance();
        fetchRoomsFromFirestore();
        return view;
    }

    private void init(View view) {
        itemList = new ArrayList<>();
        recyclerViewRooms = view.findViewById(R.id.recycler_view_manage_room);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2); // 2 columns
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (itemList.get(position) instanceof String) ? 2 : 1;
            }
        });
        recyclerViewRooms.setLayoutManager(layoutManager);
        adapter = new ManageRoomAdapter(itemList, this);
        recyclerViewRooms.setAdapter(adapter);
    }

    private void fetchRoomsFromFirestore() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> roomList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            room.setId(document.getId());
                            roomList.add(room);
                        }

                        sortRoomsByTimePosted(roomList);

                        List<Room> unapprovedRooms = new ArrayList<>();
                        List<Room> approvedRooms = new ArrayList<>();

                        for (Room room : roomList) {
                            if (room.getStatus() == 0) {
                                unapprovedRooms.add(room);
                            } else if (room.getStatus() == 1) {
                                approvedRooms.add(room);
                            }
                        }

                        itemList.clear();
                        if (!unapprovedRooms.isEmpty()) {
                            itemList.add("title_unapproved");
                            itemList.addAll(unapprovedRooms);
                        }

                        if (!approvedRooms.isEmpty()) {
                            itemList.add("title_approved");
                            itemList.addAll(approvedRooms);
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void sortRoomsByTimePosted(List<Room> rooms) {
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room room1, Room room2) {
                if (room1.getTimePosted() == null || room2.getTimePosted() == null) {
                    return 0;
                }
                return room2.getTimePosted().compareTo(room1.getTimePosted());
            }
        });
    }

    @Override
    public void onRoomClick(Room room) {
        Log.d(TAG, "Room clicked: " + room.getAddress());
        Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
        intent.putExtra("roomId", room.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
