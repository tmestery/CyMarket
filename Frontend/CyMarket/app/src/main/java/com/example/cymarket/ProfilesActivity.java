package com.example.cymarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfilesActivity extends AppCompatActivity {
    private Button homeButton, messagesButton, settingsButton;
    private TextView usernameText;
    private ImageView profileImage;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        // Initialize views
        usernameText = findViewById(R.id.username_text);
        profileImage = findViewById(R.id.profile_image_view);
        homeButton = findViewById(R.id.prfls_home_page_btn);
        messagesButton = findViewById(R.id.prfls_messages_btn);
        settingsButton = findViewById(R.id.prfls_setting_btn);

        // Get username from intent and display it
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String email = getIntent().getStringExtra("email");
        usernameText.setText(username);

        // Set PFP if there
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedUri = prefs.getString("profile_image_uri", null);
        if (savedUri != null) {
            Uri uri = Uri.parse(savedUri);
            profileImage.setImageURI(uri);
            if (profileImage.getDrawable() == null) {
                profileImage.setImageResource(R.drawable.pfp); // fallback
            }
        } else {
            profileImage.setImageResource(R.drawable.pfp);
        }

        // GET join date data here
        TextView joinDateText = findViewById(R.id.textView2);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://coms-3090-056.class.las.iastate.edu:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
            String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString());

            Call<String> call = apiService.getUserJoinDate(encodedEmail, encodedPassword);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        joinDateText.setText("Joined: " + response.body());
                    } else {
                        joinDateText.setText("Join date unavailable");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    joinDateText.setText("Error loading join date");
                    Toast.makeText(getApplicationContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            joinDateText.setText("Encoding error");
        }

        // Open image picker when profile image is clicked
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Navigation buttons
        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        messagesButton.setOnClickListener(v -> startActivity(new Intent(this, MessagesActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit().putString("profile_image_uri", imageUri.toString()).apply();

            String username = usernameText.getText().toString();
            if (!username.isEmpty()) {
                addProfilePic(username, imageUri);
            } else {
                Toast.makeText(this, "Username is empty, cannot upload image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Might need to change backend, hard to change from URI to JPG image
    private void addProfilePic(String username, Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytes(inputStream);

            File file = File.createTempFile("profile", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(imageBytes);
            fos.close();

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://coms-3090-056.class.las.iastate.edu:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<ResponseBody> call = apiService.uploadProfileImage(username, body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(getApplicationContext(), "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}