package com.example.adminnhatro360.controller.mainActivity;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.adminnhatro360.controller.mainActivity.manageRoomFragment.CustomViewPager;
import com.example.adminnhatro360.R;
import com.example.adminnhatro360.controller.mainActivity.manageRoomFragment.detailListFragment.DetailListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CustomViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

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
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setOnMenuSelected() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (isDetailFragmentVisible()) {
                getSupportFragmentManager().popBackStack(); // Loại bỏ DetailListFragment nếu đang hiển thị
                viewPager.setVisibility(View.VISIBLE);
            }
            if (itemId == R.id.nav_dashboard) {
                viewPagerAdapter.reloadFragment(0);
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.nav_manage_room) {
                viewPagerAdapter.reloadFragment(1);
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_manage_user) {
                viewPagerAdapter.reloadFragment(2);
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.nav_account) {
                viewPagerAdapter.reloadFragment(3);
                viewPager.setCurrentItem(3, false);
                return true;
            }
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
                        sendNotification(token, title, body);
                    }
                });
    }

    private void sendNotification(String token, String title, String body) {
        // Thay YOUR_SERVER_KEY bằng server key của bạn từ Firebase Console > Project settings > Cloud Messaging > Server key
        String serverKey = "YOUR_SERVER_KEY";
        String fcmUrl = "https://fcm.googleapis.com/v1/projects/nhatro360-1dfaa/messages:send";

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
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody rqbody = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(fcmUrl)
                .post(rqbody)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Notification sent successfully: " + response.body().string());
            }
        });
    }

    private String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("path/to/your/service-account-file.json"))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
