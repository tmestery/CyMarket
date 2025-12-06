package com.example.cymarket.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cymarket.Reporting.Report;
import com.example.cymarket.Services.ApiService;
import com.example.cymarket.Messages.FriendsAdapter;
import com.example.cymarket.R;
import com.example.cymarket.Reporting.Reports;
import com.example.cymarket.Reporting.ReportsAdapter;
import com.example.cymarket.Services.RetroClient;
import com.example.cymarket.LoginSignup.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AdminDashboardActivity provides the main administrative interface for the CYMarket app.
 * <p>
 * This activity allows admins to:
 * <ul>
 *     <li>View all registered users</li>
 *     <li>View sales-related UI sections</li>
 *     <li>View and manage user reports</li>
 * </ul>
 * The interface dynamically switches between views based on button selections and
 * uses Retrofit for backend communication.
 *
 * @author Tyler Mestery
 */
public class AdminDashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView totalSalesText;
    private Button salesButton;
    private Button usersButton;
    private Button reportButton;
    private RecyclerView reportsRecyclerView;
    private ReportsAdapter reportsAdapter;
    private FriendsAdapter adapter;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up RecyclerViews,
     * configures button listeners, and loads users by default.
     *
     * @param savedInstanceState previously saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Set buttons:
        salesButton = findViewById(R.id.sales_btn);
        usersButton = findViewById(R.id.users_btn);
        reportButton = findViewById(R.id.report_btn);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        totalSalesText = findViewById(R.id.total_sales_amount);

        // Setting up view reports for admin:
        reportsRecyclerView = findViewById(R.id.recyclerViewReports);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch total sales;
        fetchTotalSales();

        // Set sales and users info displays that will be hidden/brought out:
        LinearLayout bottomNaveSales = findViewById(R.id.top_nav_sales);
        LinearLayout bottomNaveSales2 = findViewById(R.id.bottom_nav_sales_2);
        LinearLayout bottomNavUsers = findViewById(R.id.bottom_nav_users);
        LinearLayout bottomReport = findViewById(R.id.bottom_report_users);

        // Setting and getting all users on screen
        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchUsers();

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavUsers.setVisibility(View.INVISIBLE);
                bottomNaveSales.setVisibility(View.INVISIBLE);
                bottomNaveSales2.setVisibility(View.INVISIBLE);
                bottomReport.setVisibility(View.VISIBLE);

                fetchReports();
            }
        });

        usersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavUsers.setVisibility(View.VISIBLE);
                bottomNaveSales.setVisibility(View.INVISIBLE);
                bottomNaveSales2.setVisibility(View.INVISIBLE);
                bottomReport.setVisibility(View.INVISIBLE);
            }
        });

        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavUsers.setVisibility(View.INVISIBLE);
                bottomNaveSales.setVisibility(View.VISIBLE);
                bottomNaveSales2.setVisibility(View.VISIBLE);
                bottomReport.setVisibility(View.INVISIBLE);

                fetchTotalSales(); // ← THIS WAS MISSING
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, AdminSettingsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, AdminProfilesActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Fetches all users from the backend API and displays them
     * in the RecyclerView using the FriendsAdapter.
     */
    private void fetchUsers() {
        ApiService apiService = RetroClient.getApiService();
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();

                    // Set profile image URL
                    for (User user : users) {
                        user.setProfileImageUrl("http://coms-3090-056.class.las.iastate.edu:8080/users/" + user.getUsername() + "/profile-image");
                    }

//                   Not needed since admin won't need to start gc with them at this moment:
                    adapter = new FriendsAdapter(users, null);

                    // Set adapter on RecyclerView here
                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches all user reports associated with the logged-in admin
     * and displays them in the reports RecyclerView.
     * Uses stored admin credentials from SharedPreferences.
     */
    private void fetchReports() {
        ApiService apiService = RetroClient.getApiService();
        List<Report> allReports = new ArrayList<>();
        int maxAttempt = 100; // maximum number of IDs to try
        int startId = 1;

        for (int id = startId; id <= maxAttempt; id++) {
            final int currentId = id;
            Log.d("AdminDashboard", "Fetching report with ID: " + currentId);

            apiService.getReportById(currentId).enqueue(new Callback<Report>() {
                @Override
                public void onResponse(Call<Report> call, Response<Report> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Report report = response.body();
                        Log.d("AdminDashboard", "Fetched report ID " + currentId + ": " + report);
                        allReports.add(report);

                        // Update RecyclerView
                        if (reportsAdapter == null) {
                            reportsAdapter = new ReportsAdapter(allReports);
                            reportsRecyclerView.setAdapter(reportsAdapter);
                        } else {
                            reportsAdapter.setReports(allReports);
                        }
                    } else if (response.code() == 404) {
                        Log.d("AdminDashboard", "Report ID " + currentId + " not found (404).");
                    } else {
                        Log.e("AdminDashboard", "Failed to fetch report ID " + currentId + ". Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Report> call, Throwable t) {
                    Log.e("AdminDashboard", "Error fetching report ID " + currentId + ": " + t.getMessage());
                }
            });
        }
    }

    /**
     * Fetches the total sales amount from the backend and displays it.
     */
    private void fetchTotalSales() {
        ApiService apiService = RetroClient.getApiService();

        apiService.getTotalSales().enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double totalSales = response.body();

                    totalSalesText.setText(
                            String.format("$%,.2f", totalSales)
                    );
                } else {
                    totalSalesText.setText("$0.00");
                }
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                totalSalesText.setText("$0.00");
            }
        });
    }
}