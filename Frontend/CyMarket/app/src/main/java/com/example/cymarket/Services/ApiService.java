package com.example.cymarket.Services;

import com.example.cymarket.LoginSignup.User;
import com.example.cymarket.Reporting.Report;
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

/**
 * Retrofit API service interface for backend communication in CyMarket.
 * Defines endpoints for user management, profile images, messaging, and reports.
 *
 * @author Tyler Mestery
 */public interface ApiService {
    /**
     * Uploads a profile image for a user.
     *
     * @param username the username of the user
     * @param image    the image file to upload
     * @return Call object for Retrofit
     */
    @Multipart
    @POST("/users/{username}/profile-image")
    Call<ResponseBody> uploadProfileImage(
            @Path("username") String username,
            @Part MultipartBody.Part image
    );

    /**
     * Retrieves the join date of a user.
     *
     * @param email    the user's email (encoded)
     * @param password the user's password (encoded)
     * @return Call object returning the join date as a string
     */
    @GET("/users/joinDate/{email}/{password}")
    Call<String> getUserJoinDate(
            @Path(value = "email", encoded = true) String email,
            @Path(value = "password", encoded = true) String password
    );

    /**
     * Deletes a user from the database.
     *
     * @param username the username of the user to delete
     * @return Call object returning Void
     */

    /**
     * Deletes a user's profile image.
     *
     * @param username the username of the user
     * @return Call object returning a string response
     */
    @DELETE("users/u/{username}")
    Call<Void> deleteUser(@Path("username") String username);

    /**
     * Deletes a user's profile image.
     *
     * @param username the username of the user
     * @return Call object returning a string response
     */
    @DELETE("users/{username}/profile-" + "image")
    Call<String> deleteProfileImage(@Path("username") String username);

    /**
     * Retrieves a list of all users.
     *
     * @return Call object returning a list of User objects
     */
    @GET("/users")
    Call<List<User>> getAllUsers();

    /**
     * Retrieves a user's name by email.
     *
     * @param email the user's email
     * @return Call object returning the user's name as a string
     */
    @GET("users/getName/{email}")
    Call<String> getName(@Path("email") String email);

    /**
     * Retrieves reports for admin dashboard.
     *
     * @param id       admin id
     * @param email    admin email
     * @param password admin password
     * @return Call object returning a Reports object
     */
    @GET("/adminUser/getReports/{id}/{email}/{password}/")
    Call<Reports> getReports(@Path("id") int id, @Path("email") String email, @Path("password") String password);

    /**
     * Adds a user to a group.
     *
     * @param groupID  the ID of the group
     * @param username the username to add
     * @return Call object returning Void
     */
    @POST("/groups/group/add-user/{id}/{username}")
    Call<Void> addUserToGroup(@Path("id") int groupID, @Path("username") String username);

    /**
     * Retrieves all reports for an admin.
     *
     * @param email    admin email
     * @param password admin password
     * @return Call object returning a Reports object
     */
    // Get all report IDs for admin
    @GET("/adminUser/getAllReportIds/{email}")
    Call<List<Integer>> getReportIds(@Path("email") String email, @Query("password") String password);

    // Get single report by ID
    @GET("/reports/{id}")
    Call<Report> getReportById(@Path("id") int id);

    /**
     * Gets the total sales across the application.
     *
     * @return total sales Double
     */
    @GET("/sales/total")
    Call<Double> getTotalSales();
}