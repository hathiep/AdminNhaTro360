package com.example.adminnhatro360.controller.mainActivity.manageRoomFragment.roomDetailActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.adminnhatro360.R;
import com.example.adminnhatro360.model.Room;
import com.example.adminnhatro360.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RoomDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RoomDetailActivity";

    private TextView tvPrice, tvTitle, tvAddress, tvContact, tvArea, tvTimePosted, tvUtilities, tvInfor;
    private List<TextView> listTvUtilites = new ArrayList<>();
    private List<ImageView> listImvUtilites = new ArrayList<>();
    private ViewPager viewPager;
    private FirebaseFirestore db;
    private GoogleMap mMap;
    private LatLng roomLatLng;
    private ImageView imvBack, imvSave;
    private boolean save;
    private static Room room;
    private FirebaseUser currentUser;
    private static User user;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        init();

        String roomId = getIntent().getStringExtra("roomId");
        if (roomId != null) {
            fetchRoomDetailsFromFirestore(roomId);
        } else {
            Log.e("RoomDetailActivity", "Room ID is null");
        }

        // Setup Floating Action Button
        ImageView imvMenu = findViewById(R.id.imV_menu);
        imvMenu.setOnClickListener(view -> showMenuDialog());

        getCurrentUser(new FirestoreCallback() {
            @Override
            public void onCallback(User userData) {
                user = userData;
                // Tiến hành các xử lý liên quan đến user sau khi dữ liệu đã được lấy
                handleUserData(roomId);
            }
        });

        imvBack.setOnClickListener(v -> onBackPressed());
    }

    private void handleUserData(String roomId) {
        Log.e(TAG, user.getEmail() + " " + user.getId() + " " + user.getListSavedRoom());
        if (user.getListSavedRoom() != null) {
            if (user.getListSavedRoom().contains(roomId)) {
                imvSave.setImageResource(R.drawable.ic_save);
                save = true;
            } else {
                imvSave.setImageResource(R.drawable.ic_unsave);
                save = false;
            }
        }

        imvSave.setOnClickListener(view -> {
            if (save) {
                imvSave.setImageResource(R.drawable.ic_unsave);
                user.getListSavedRoom().remove(roomId);
                save = false;
                Toast.makeText(this, "Bỏ lưu thành công", Toast.LENGTH_SHORT).show();
            } else {
                imvSave.setImageResource(R.drawable.ic_save);
                user.getListSavedRoom().add(roomId);
                save = true;
                Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
            }
            updateUser();
        });
    }

    // Initialize views
    @SuppressLint("SuspiciousIndentation")
    private void init() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        imvBack = findViewById(R.id.imv_back);
        imvSave = findViewById(R.id.imv_save);
        room = new Room();
        user = new User();
        save = false;
        tvPrice = findViewById(R.id.tv_price);

        tvTitle = findViewById(R.id.tv_title);
        tvAddress = findViewById(R.id.tv_address);
        tvContact = findViewById(R.id.tv_contact);
        tvArea = findViewById(R.id.tv_area);
        tvTimePosted = findViewById(R.id.tv_time_posted);
        tvUtilities = findViewById(R.id.tv_utilities);
        viewPager = findViewById(R.id.view_pager);

        setViewPagerHeight();
        listImvUtilites.add(findViewById(R.id.imv_wifi));
        listImvUtilites.add(findViewById(R.id.imv_wc));
        listImvUtilites.add(findViewById(R.id.imv_parking));
        listImvUtilites.add(findViewById(R.id.imv_free_time));
        listImvUtilites.add(findViewById(R.id.imv_kitchen));
        listImvUtilites.add(findViewById(R.id.imv_air_conditioner));
        listImvUtilites.add(findViewById(R.id.imv_fridge));
        listImvUtilites.add(findViewById(R.id.imv_washing_machine));
        listTvUtilites.add(findViewById(R.id.tv_wifi));
        listTvUtilites.add(findViewById(R.id.tv_wc));
        listTvUtilites.add(findViewById(R.id.tv_parking));
        listTvUtilites.add(findViewById(R.id.tv_free_time));
        listTvUtilites.add(findViewById(R.id.tv_kitchen));
        listTvUtilites.add(findViewById(R.id.tv_air_conditioner));
        listTvUtilites.add(findViewById(R.id.tv_fridge));
        listTvUtilites.add(findViewById(R.id.tv_washing_machine));
        tvInfor = findViewById(R.id.tv_infor);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            View mapView = mapFragment.getView();
            if (mapView != null) {
                mapView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openFullScreenMap();
                        Log.e(TAG, "Map was clicked");
                    }
                });
            }
        }
    }

    private void getCurrentUser(FirestoreCallback callback) {
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("users").whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            User user = userDoc.toObject(User.class);
                            user.setId(userDoc.getId());
                            callback.onCallback(user);
                        } else {
                            // Xử lý lỗi nếu có
                        }
                    });
        }
    }

    private void updateUser(){
        DocumentReference userRef = db.collection("users").document(user.getId());
        userRef.update("listSavedRoom", user.getListSavedRoom())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Thông báo cập nhật thành công
                        Log.d("Firestore", "User attribute updated successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Thông báo lỗi
                        Log.w("Firestore", "Error updating user attribute.", e);
                    }
                });
    }

    private void fetchRoomDetailsFromFirestore(String roomId) {
        db.collection("rooms").document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        room = documentSnapshot.toObject(Room.class);
                        if (room != null) {
                            // Hiển thị thông tin phòng lên các TextView
                            updateUI();

                            // Lấy tọa độ từ địa chỉ
                            getLatLngFromAddress(room.getAddress(), false);
                        } else {
                            Log.d("RoomDetailActivity", "Room object is null");
                        }
                    } else {
                        Log.d("RoomDetailActivity", "Document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RoomDetailActivity", "Error fetching document", e);
                });
    }

    private void updateUI() {
        // Set text views
        tvPrice.setText(formatPrice(room.getPrice()) + "/tháng");

        // Set up ViewPager with images
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, room.getImages());
            viewPager.setAdapter(adapter);
        }

        tvTitle.setText(room.getTitle());
        tvAddress.setText(room.getAddress());
        tvContact.setText(room.getPhone() + " - " +  room.getHost());
        tvArea.setText("DT " + room.getArea() +" m2");

        // Update tvTimePosted with calculated time
        updatePostedTime(room.getTimePosted());

        int num_utilities = 0;
        for(int i=0; i<8; i++){
            num_utilities+= setUtilities(i);
        }
        tvUtilities.setText("Tiện ích phòng (" + num_utilities + ")");
        tvInfor.setText(Html.fromHtml(room.getDetail().replaceAll("\n", "<br>")));
    }

    private void updatePostedTime(Timestamp timePosted) {
        long timeDiff = Timestamp.now().getSeconds() - timePosted.getSeconds();

        if (timeDiff < TimeUnit.HOURS.toSeconds(1)) {
            long minutes = TimeUnit.SECONDS.toMinutes(timeDiff);
            tvTimePosted.setText(minutes + " phút");
        } else if (timeDiff < TimeUnit.DAYS.toSeconds(1)) {
            long hours = TimeUnit.SECONDS.toHours(timeDiff);
            tvTimePosted.setText(hours + " giờ");
        } else {
            long days = TimeUnit.SECONDS.toDays(timeDiff);
            tvTimePosted.setText(days + " ngày");
        }
    }

    private int setUtilities(int i){
        if(room.getUtilities().get(i)){
            listImvUtilites.get(i).setColorFilter(ContextCompat.getColor(listImvUtilites.get(i).getContext(), R.color.blue2), PorterDuff.Mode.SRC_IN);
            listTvUtilites.get(i).setTextColor(ContextCompat.getColor(listTvUtilites.get(i).getContext(), R.color.blue2));
            return 1;
        }
        return 0;
    }

    private void setViewPagerHeight() {
        // Lấy chiều rộng của màn hình
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        // Tính chiều cao dựa trên tỉ lệ 16:9
        int height = (int) (screenWidth * 10.0 / 16.0);

        // Đặt chiều cao cho ViewPager
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = height;
        viewPager.setLayoutParams(params);
    }

    private void getLatLngFromAddress(String address, boolean retried) {
        Log.d(TAG, "Fetching lat/lng for address: " + address);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NominatimAPI nominatimAPI = retrofit.create(NominatimAPI.class);

        Call<List<NominatimResponse>> call = nominatimAPI.getGeocoding(address, "json", 1);
        call.enqueue(new Callback<List<NominatimResponse>>() {
            @Override
            public void onResponse(Call<List<NominatimResponse>> call, Response<List<NominatimResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NominatimResponse> results = response.body();
                    if (!results.isEmpty()) {
                        double lat = Double.parseDouble(results.get(0).getLat());
                        double lon = Double.parseDouble(results.get(0).getLon());
                        updateMap(lat, lon);
                    } else {
                        Log.e(TAG, "No results found for the address.");
                        if (!retried) {
                            String newAddress = removeStreetFromAddress(address);
                            getLatLngFromAddress(newAddress, true);
                        }
                    }
                } else {
                    Log.e(TAG, "Geocoding API response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<List<NominatimResponse>> call, Throwable t) {
                Log.e(TAG, "Geocoding API request failed", t);
            }
        });
    }

    private String removeStreetFromAddress(String address) {
        // This is a simple implementation. You might need a more complex logic depending on your address format.
        int index = address.indexOf(",");
        if (index != -1) {
            return address.substring(index + 1).trim();
        }
        return address;
    }

    private void updateMap(double lat, double lng) {
        Log.d(TAG, "Updating map to lat: " + lat + ", lng: " + lng);

        if (mMap != null) {
            roomLatLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(roomLatLng).title("Vị trí phòng"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(roomLatLng, 14.5F));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Disable map gestures
        mMap.getUiSettings().setAllGesturesEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                openFullScreenMap();
                Log.e(TAG, "Map area was clicked");
            }
        });
    }

    private void openFullScreenMap() {
        FullScreenMapFragment fullScreenMapFragment = new FullScreenMapFragment();

        // Truyền tọa độ bản đồ qua Bundle
        Bundle bundle = new Bundle();
        if (roomLatLng != null) {
            bundle.putDouble("latitude", roomLatLng.latitude);
            bundle.putDouble("longitude", roomLatLng.longitude);
        }
        fullScreenMapFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fullScreenMapFragment)
                .addToBackStack(null)
                .commit();
    }

    public interface NominatimAPI {
        @GET("search")
        Call<List<NominatimResponse>> getGeocoding(@Query("q") String address, @Query("format") String format, @Query("limit") int limit);
    }

    public static class NominatimResponse {
        @SerializedName("lat")
        private String lat;

        @SerializedName("lon")
        private String lon;

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }
    }


    private void showMenuDialog() {
        View overlay = findViewById(R.id.overlay);
        overlay.setVisibility(View.VISIBLE);

        // Create a dialog with custom menu layout
        Dialog dialog = new Dialog(RoomDetailActivity.this);
        dialog.setContentView(R.layout.dialog_menu_contact);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set dialog width to match parent and height to wrap content
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(layoutParams);

        // Show dialog
        dialog.show();

        // Set click listeners for the menu items
        dialog.findViewById(R.id.action_call).setOnClickListener(v -> {
            makeCall();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.action_message).setOnClickListener(v -> {
            sendMessage();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.action_directions).setOnClickListener(v -> {
            getDirections();
            dialog.dismiss();
        });

        dialog.setOnDismissListener(dialogInterface -> {
            // Hide overlay when dialog is dismissed
            overlay.setVisibility(View.GONE);
        });
    }

    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + tvContact.getText().toString().split(" - ")[0]));
        startActivity(intent);
    }

    private void sendMessage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + tvContact.getText().toString().split(" - ")[0]));
        intent.putExtra("sms_body", "Tôi quan tâm đến nhà trọ của bạn ở địa chỉ " + tvAddress.getText().toString());
        startActivity(intent);
    }

    private void getDirections() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(tvAddress.getText().toString()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private String formatPrice(String price){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        double millions = Integer.parseInt(price) / 1_000_000.0;
        return decimalFormat.format(millions) + " triệu";
    }

    private interface FirestoreCallback {
        void onCallback(User user);
    }
}
