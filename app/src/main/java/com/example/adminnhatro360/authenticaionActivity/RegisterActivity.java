package com.example.adminnhatro360.authenticaionActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.adminnhatro360.R;
import com.example.adminnhatro360.model.Validate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText editTextName, editTextEmail, editTextPhone, editTextPassword, editTextPasswordAgain;
    private Button btnRegister;
    private ImageView imV_back, imV_eye1, imV_eye2;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Integer eye1, eye2;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        eye1 = 0;
        eye2 = 0;
        setUiEye(imV_eye1, editTextPassword, 1);
        setUiEye(imV_eye2, editTextPasswordAgain, 2);
        setOnClickListener();
    }
    private void initUi(){
        editTextEmail = findViewById(R.id.email);
        editTextName = findViewById(R.id.fullname);
        editTextPhone = findViewById(R.id.phone);
        editTextPassword = findViewById(R.id.password);
        editTextPasswordAgain = findViewById(R.id.password_again);
        btnRegister = findViewById(R.id.btn_register);
//        progressBar = findViewById(R.id.progress_bar);
        imV_back = findViewById(R.id.imV_back);
        imV_eye1 = findViewById(R.id.imV_eye1);
        imV_eye2 = findViewById(R.id.imV_eye2);
        auth = FirebaseAuth.getInstance();
    }
    private void setUiEye(ImageView imv_eye, EditText edt, int x){
        imv_eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int eye = 0;
                if(x==1) eye = eye1;
                if(x==2) eye = eye2;
                if(eye == 0){
                    // Chuyển icon unhide thành hide
                    imv_eye.setImageResource(R.drawable.ic_hide);
                    // Chuyển text từ hide thành unhide
                    edt.setInputType(InputType.TYPE_CLASS_TEXT);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edt.setSelection(edt.getText().length());
                    // Đảo lại trạng thái mắt
                    if(x==1) eye1 = 1;
                    else eye2 = 1;
                }
                else {
                    // Chuyển icon hide thành unhide
                    imv_eye.setImageResource(R.drawable.ic_unhide);
                    // Chuyển text từ unhide thành hide
                    int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    edt.setInputType(inputType);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edt.setSelection(edt.getText().length());
                    // Đảo lại trạng thái mắt
                    if(x==1) eye1 = 0;
                    else eye2 = 0;
                }
            }
        });
    }
    private void setOnClickListener(){
        imV_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy các giá trị từ input
                String name, email, phone, password, passwordagain;
                name = editTextName.getText().toString().trim();
                email = editTextEmail.getText().toString().trim();
                phone = editTextPhone.getText().toString().trim();
                password = editTextPassword.getText().toString().trim();
                passwordagain = editTextPasswordAgain.getText().toString().trim();
                // Gọi đối tượng validate
                Validate validate = new Validate(RegisterActivity.this);
                if(!validate.validateRegister(name, email, phone, password, passwordagain)) return;
                // Chạy vòng loading
//                progressBar.setVisibility(View.VISIBLE);
                // Check đăng ký
                createUserWithEmailAndPassword(email, password);
            }


        });
    }

    private void createUserWithEmailAndPassword(String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            if (user != null) {
                                sendEmailVerify(email);
                            }

                        } else {
                            show_dialog("Email đã được sử dụng.Vui lòng thử nhập email khác!", 2);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    editTextEmail.requestFocus(); // Yêu cầu EditText edt_name nhận focus
                                    editTextEmail.setSelection(editTextEmail.getText().length()); // Di chuyển con trỏ nháy đến cuối của edt_name
                                }
                            }, 100);
                        }
                    }
                });
    }
    private void sendEmailVerify(String email){
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Email xác thực đã được gửi thành công
                    show_dialog("Tạo tài khoản thành công. Email xác thực đã được gửi đến " + email + "\nVui lòng truy cập email để xác nhận!", 3);
                    // Lưu thông tin user vào Firestore Database
                    insertUserToFirestoreDatabase();
                    //Back to login
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 4000);
                } else {
                    // Không thể gửi email xác thực
                    show_dialog("Không thể gửi email xác thực. Vui lòng kiểm tra lại email!", 3);
                }
            }
        });
    }

    //Hàm khởi tạo thông tin User trên FirestoreDatabase
    private void insertUserToFirestoreDatabase(){
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        List<String> listSavedRoom = new ArrayList<>(); listSavedRoom.add("x");
        List<String> listPostedRoom = new ArrayList<>(); listPostedRoom.add("x");
        if (user != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("fullName", editTextName.getText().toString());
            userMap.put("email", editTextEmail.getText().toString());
            userMap.put("phone", editTextPhone.getText().toString());
            userMap.put("listSavedRoom", listSavedRoom);
            userMap.put("listPostedRoom", listPostedRoom);

            // Add the user to the Firestore db
            db.collection("users")
                    .add(userMap)
                    .addOnSuccessListener(documentReference -> {
                        // Successfully added user to Firestore
                        Log.d("Firestore", "User added successfully with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        // Failed to add user to Firestore
                        Log.d("Firestore", "Error adding user", e);
                    });
        } else {
            Log.d("Firestore", "No current user logged in");
        }
    }
    private void show_dialog(String s, int time){
        ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Thông báo");
        progressDialog.setMessage(s);
        progressDialog.show();

        // Sử dụng Handler để gửi một tin nhắn hoạt động sau một khoảng thời gian
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Ẩn Dialog sau khi đã qua một khoảng thời gian nhất định
                progressDialog.dismiss();
            }
        }, time * 1000); // Số milliseconds bạn muốn Dialog biến mất sau đó
    }
}