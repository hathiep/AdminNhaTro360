package com.example.adminnhatro360.controller.mainActivity.manageRoomFragment.roomDetailActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.adminnhatro360.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FullScreenMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private ImageView btnCancel, btnDirection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_map, container, false);

        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction().remove(FullScreenMapFragment.this).commit());

        // Nhận tọa độ từ Bundle
        if (getArguments() != null) {
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnDirection = view.findViewById(R.id.btn_direction);
        btnDirection.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Kích hoạt thao tác với bản đồ trong FullScreenMapFragment
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Cập nhật vị trí bản đồ
        LatLng roomLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(roomLocation).title("Vị trí phòng"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(roomLocation, 15));
    }
}
