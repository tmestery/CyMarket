package com.example.cymarket;

import com.example.cymarket.LoginSignup.User;
import com.example.cymarket.Reporting.Reports;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Gonna need to update others to use this, can make code more organized
public interface ApiService {
    // Can be used to post a profile picture for the user
    @Multipart
    @POST("/users/{username}/profile-image")
    Call<ResponseBody> uploadProfileImage(
            @Path("username") String username,
            @Part MultipartBody.Part image
    );

    // Can be used to get user's data for when he signed up for CyMarket
    @GET("/users/joinDate/{email}/{password}")
    Call<String> getUserJoinDate(
            @Path(value = "email", encoded = true) String email,
            @Path(value = "password", encoded = true) String password
    );

    // Can be used to delete a user from the database
    @DELETE("users/u/{username}")
    Call<Void> deleteUser(@Path("username") String username);

    // Can be used to delete user's profile picture
    @DELETE("users/{username}/profile-" + "image")
    Call<String> deleteProfileImage(@Path("username") String username);

    // Can be used to display all users - for messaging in the future + friends??
    @GET("/users")
    Call<List<User>> getAllUsers();

    // Can be used to get the user's name
    @GET("users/getName/{email}")
    Call<String> getName(@Path("email") String email);

    // Can be used to get the reports for admin dashboard (determines outcome)
    @GET("/adminUser/getReports/{id}/{email}/{password}/")
    Call<Reports> getReports(@Path("id") int id, @Path("email") String email, @Path("password") String password);

    @POST("/groups/group/add-user/{id}/{username}")
    Call<Void> addUserToGroup(@Path("id") int groupID, @Path("username") String username);

    @GET("/adminUser/getAllReports/{email}")
    Call<Reports> getAllReports(
            @Path("email") String email,
            @Query("password") String password
    );
}