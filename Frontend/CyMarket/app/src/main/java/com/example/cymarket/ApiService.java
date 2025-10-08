package com.example.cymarket;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

// Gonna need to update others to use this, can make code more organized
public interface ApiService {
    @Multipart
    @POST("/users/{username}/profile-image")
    Call<ResponseBody> uploadProfileImage(
            @Path("username") String username,
            @Part MultipartBody.Part image
    );

    @GET("/users/joinDate/{email}/{password}")
    Call<String> getUserJoinDate(
            @Path(value = "email", encoded = true) String email,
            @Path(value = "password", encoded = true) String password
    );

    @DELETE("users/u/{username}")
    Call<Void> deleteUser(@Path("username") String username);
}