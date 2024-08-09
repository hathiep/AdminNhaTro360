package com.example.adminnhatro360.mainActivity;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.adminnhatro360.R;
import com.example.adminnhatro360.mainActivity.manageRoomFragment.detailListFragment.DetailListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MainViewPager viewPager;
    private MainViewPagerAdapter mainViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
            getWindow().setStatusBarColor(getColor(R.color.blue2));
            getWindow().setNavigationBarColor(getColor(R.color.blue2));
        } else {
            // For API < 30
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(getResources().getColor(R.color.blue2));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.blue2));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        init();
        setUpViewPager();
        setOnMenuSelected();
        sendNotificationToUser("7SXh1oL16TChdN1NFSKp");
    }

    public void init(){
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setUpViewPager(){
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(mainViewPagerAdapter);
    }

    private void setOnMenuSelected() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (isDetailFragmentVisible()) {
                getSupportFragmentManager().popBackStack(); // Loại bỏ DetailListFragment nếu đang hiển thị
                viewPager.setVisibility(View.VISIBLE);
            }
            if (itemId == R.id.nav_dashboard) {
                mainViewPagerAdapter.reloadFragment(0);
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.nav_manage_room) {
                mainViewPagerAdapter.reloadFragment(1);
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_manage_user) {
                mainViewPagerAdapter.reloadFragment(2);
                viewPager.setCurrentItem(2, false);
                return true;
            }
//            else if (itemId == R.id.nav_account) {
//                mainViewPagerAdapter.reloadFragment(3);
//                viewPager.setCurrentItem(3, false);
//                return true;
//            }
            return false;
        });
    }

    private boolean isDetailFragmentVisible() {
        Fragment detailFragment = getSupportFragmentManager().findFragmentByTag("DetailListFragment");
        return detailFragment != null && detailFragment.isVisible();
    }

    public void showDetailListFragment(Bundle args) {
        viewPager.setVisibility(View.GONE); // Ẩn ViewPager
        DetailListFragment detailListFragment = new DetailListFragment();
        detailListFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailListFragment, "DetailListFragment")
                .addToBackStack("DetailListFragment")
                .commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof DetailListFragment) {
            // Quay lại ManageRoomFragment
            fragmentManager.popBackStack();
            viewPager.setVisibility(View.VISIBLE); // Hiển thị lại ViewPager
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            showExitConfirmationDialog();
        }
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_app)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(true);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(true);
        });
        dialog.show();
    }

    private void sendNotificationToUser(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String token = documentSnapshot.getString("token");

                        // Gửi thông báo đến token
                        String title = "Phòng đã được duyệt";
                        String body = "Phòng của bạn đã được duyệt và hiển thị trên ứng dụng.";
                        new SendNotificationTask().execute(token, title, body);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "get failed with ", e));
    }

    private class SendNotificationTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String token = params[0];
            String title = params[1];
            String body = params[2];

            String projectId = "nhatro360-1dfaa"; // Thay thế bằng project ID của bạn
            String fcmUrl = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");

            JSONObject json = new JSONObject();
            try {
                JSONObject message = new JSONObject();
                message.put("token", token);

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);

                message.put("notification", notification);
                message.put("android", new JSONObject().put("ttl", "3600s")); // Thêm thời gian sống của thông báo nếu cần

                json.put("message", message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                @SuppressLint("WrongThread") String accessToken = new GetAccessTokenTask().execute().get(); // Sử dụng AsyncTask để lấy access token
                RequestBody bodyRequest = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(fcmUrl)
                        .post(bodyRequest)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Notification sent successfully: " + response.body().string());
                        } else {
                            Log.d(TAG, "Failed to send notification: " + response.body().string());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class GetAccessTokenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                return getAccessToken();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private String getAccessToken() throws IOException {
        InputStream serviceAccount = getAssets().open("service-account-file.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        credentials.refreshIfExpired();

        return credentials.getAccessToken().getTokenValue();
    }
}
