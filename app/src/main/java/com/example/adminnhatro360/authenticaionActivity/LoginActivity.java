package com.example.adminnhatro360.authenticaionActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.adminnhatro360.R;
import com.example.adminnhatro360.mainActivity.MainActivity;
import com.example.adminnhatro360.model.Validate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText edt_email, edt_password;
    Button btn_login;
    TextView tv_forgot_password, tv_register;
//    ProgressBar progressBar;
    FirebaseAuth mAuth;
    ImageView imV_eye;
    Integer eye;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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
        // Ánh xạ view
        initUi();
        // Set logic ẩn mật khẩu
        setUiEye();
        // Gọi các onClickListener
        onClickListener();
    }
    // Hàm ánh xạ view
    private void initUi(){
        mAuth = FirebaseAuth.getInstance();
        edt_email = findViewById(R.id.edt_email);
        edt_password = findViewById(R.id.edt_password);
        imV_eye = findViewById(R.id.imV_eye);
        tv_forgot_password = findViewById(R.id.tv_forgot_password);
        tv_register = findViewById(R.id.tv_register);
        btn_login = findViewById(R.id.btn_login);
    }
    // Hàm logic ẩn mật khẩu
    private void setUiEye(){
        eye = 0;
        imV_eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eye == 0){
                    // Chuyển icon unhide thành hide
                    imV_eye.setImageResource(R.drawable.ic_hide);
                    // Chuyển txt từ hide thành unhide
                    edt_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edt_password.setSelection(edt_password.getText().length());
                    eye = 1;
                }
                else {
                    // Chuyển icon hide thành unhide
                    imV_eye.setImageResource(R.drawable.ic_unhide);
                    // Chuyển text từ unhide thành hide
                    int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    edt_password.setInputType(inputType);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edt_password.setSelection(edt_password.getText().length());
                    eye = 0;
                }
            }
        });
    }
    // Hàm gọi các onClick
    private void onClickListener(){
        // OnClick đổi mật khẩu
        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
//                startActivity(intent);
            }
        });
        // OnClick đăng ký
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        // OnClick đăng nhập
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy các giá trị từ input
                String email, password;
                email = edt_email.getText().toString();
                password = edt_password.getText().toString();
                // Gọi đối tượng validate
                Validate validate = new Validate(LoginActivity.this);
                if(!validate.validateLogin(email, password)) return;
                // Check đăng nhập
//                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    checkVerified();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    show_dialog("Thông tin không đúng, vui lòng thử lại!", 2);

                                }
                            }
                        });
            }
        });
    }
    // Hàm kiểm tra đã xác thực email chưa
    private void checkVerified(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                // Email đã được xác thực, chuyển hướng người dùng đến màn hình chính
                show_dialog("Đăng nhập thành công!", 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 1000);

            } else {
                // Email chưa được xác thực, hiển thị thông báo hoặc hướng dẫn người dùng xác thực email
                show_dialog("Vui lòng xác thực email của bạn trước khi đăng nhập!", 3);
            }
        }
    }
    // Hàm hiển thị thông báo
    private void show_dialog(String s, int time){
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
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