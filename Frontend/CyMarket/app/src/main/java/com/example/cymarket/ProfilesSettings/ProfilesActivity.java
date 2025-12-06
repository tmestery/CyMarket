package com.example.cymarket.ProfilesSettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.Services.ApiService;
import com.example.cymarket.BuyActivity;
import com.example.cymarket.MainActivity;
import com.example.cymarket.Messages.MessagesActivity;
import com.example.cymarket.NotificationsActivity;
import com.example.cymarket.R;
import com.example.cymarket.SellActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Activity for displaying and managing the user's profile.
 * Users can view their username, join date, profile picture, and navigate
 * to other parts of the app. Also allows uploading a new profile image.
 *
 * @author Tyler Mestery
 */
public class ProfilesActivity extends AppCompatActivity {

    private Button homeButton, settingsButton, notifsButton;
    private TextView usernameText, joinDateText;
    private ImageView profileImage;
    private static final int PICK_IMAGE = 1;
    private ApiService apiService;
    private ApiService apiServiceScalars;

    /**
     * Initializes the activity, sets up views, navigation, profile loading,
     * and image selection/upload functionality.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        usernameText = findViewById(R.id.username_text);
        profileImage = findViewById(R.id.profile_image_view);
        joinDateText = findViewById(R.id.textView2);
        homeButton = findViewById(R.id.prfls_home_page_btn);
        settingsButton = findViewById(R.id.prfls_setting_btn);
        notifsButton = findViewById(R.id.prfls_notifications_btn);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        usernameText.setText(username);

        // Load PFP
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/users/" + username + "/profile-image";
        RequestQueue queue = Volley.newRequestQueue(this);
        ImageRequest imageRequest = new ImageRequest(
                url,
                response -> {
                    if (response != null) {
                        profileImage.setImageBitmap(response);
                    } else {
                        profileImage.setImageResource(R.drawable.pfp);
                    }
                },
                0, 0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                error -> profileImage.setImageResource(R.drawable.pfp)
        );
        queue.add(imageRequest);

        Retrofit retrofitScalars = new Retrofit.Builder()
                .baseUrl("http://coms-3090-056.class.las.iastate.edu:8080/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        apiServiceScalars = retrofitScalars.create(ApiService.class);

        // Get Join Date
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
            String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString());

            Call<String> call = apiServiceScalars.getUserJoinDate(encodedEmail, encodedPassword);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String rawDate = response.body().replace("\"", ""); // remove quotes

                        try {
                            // Parse the ISO date with offset
                            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(rawDate);

                            // Convert to local timezone
                            java.time.ZonedDateTime localZdt = odt.atZoneSameInstant(java.time.ZoneId.systemDefault());

                            // Format to readable style
                            java.time.format.DateTimeFormatter formatter =
                                    java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");

                            String formattedDate = localZdt.format(formatter);
                            joinDateText.setText("Join Date: " + formattedDate);

                        } catch (Exception e) {
                            joinDateText.setText("Join Date: " + rawDate); // fallback
                        }
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

        // Click to upload new PFP
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Buttons
        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        notifsButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilesActivity.this, SettingsActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(ProfilesActivity.this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(ProfilesActivity.this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(ProfilesActivity.this, MessagesActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Handles result from image picker intent and uploads selected profile image.
     *
     * @param requestCode the request code originally supplied to startActivityForResult()
     * @param resultCode  the result code returned by the child activity
     * @param data        the intent data returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                    profileImage.setImageBitmap(bitmap);
                    inputStream.close();
                }

                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String username = prefs.getString("username", null);
                if (username != null && !username.isEmpty()) {
                    uploadProfilePic(username, imageUri);
                } else {
                    Toast.makeText(this, "Username is empty", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Reads all bytes from an InputStream.
     *
     * @param inputStream the InputStream to read
     * @return byte array of the InputStream contents
     * @throws IOException if an I/O error occurs
     */
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Uploads a profile image to the backend server.
     *
     * @param username the username of the user
     * @param imageUri the URI of the selected image
     */
    private void uploadProfilePic(String username, Uri imageUri) {
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
            apiService = retrofit.create(ApiService.class);

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