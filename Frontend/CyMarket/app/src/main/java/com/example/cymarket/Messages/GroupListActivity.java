package com.example.cymarket.Messages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Button;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.BuyActivity;
import com.example.cymarket.Group;
import com.example.cymarket.R;
import com.example.cymarket.SellActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Activity responsible for displaying all message groups that
 * the currently logged-in user belongs to.
 * <p>
 * Allows users to:
 * <ul>
 *     <li>View joined groups</li>
 *     <li>Select a group to open its messages</li>
 *     <li>Create a new group</li>
 *     <li>Navigate via bottom navigation</li>
 * </ul>
 *
 * @author Tyler Mestery
 */
public class GroupListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupListAdapter adapter;
    private ArrayList<Group> groups = new ArrayList<>();
    private RequestQueue queue;
    private Button createGroupBtn;

    private static final String BASE_URL = "http://coms-3090-056.class.las.iastate.edu:8080";

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes UI components, sets up RecyclerView,
     * loads user groups from the backend, and configures
     * bottom navigation.
     *
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        recyclerView = findViewById(R.id.groupRecyclerView);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GroupListAdapter(groups, group -> {
            Intent intent = new Intent(GroupListActivity.this, MessagesActivity.class);
            intent.putExtra("groupID", group.getId());
            intent.putExtra("groupName", group.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);

        loadGroups();

        createGroupBtn.setOnClickListener(v ->
                startActivity(new Intent(GroupListActivity.this, CreateGroupActivity.class))
        );

        setupBottomNav();
    }

    /**
     * Fetches the list of groups associated with the
     * currently logged-in user from the backend.
     */
    private void loadGroups() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "No user session", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/groups/list/" + username;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> updateGroupList(response),
                error -> Toast.makeText(this, "Failed to load groups", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    /**
     * Updates the RecyclerView with the data returned
     * from the server.
     *
     * @param response JSON array containing group data
     */
    private void updateGroupList(JSONArray response) {
        try {
            groups.clear();

            if (response == null || response.length() == 0) {
                Toast.makeText(this, "No groups found", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                return;
            }

            // Debug: log full JSON
            System.out.println("GROUPS JSON: " + response.toString());

            for (int i = 0; i < response.length(); i++) {
                JSONObject groupObj = response.getJSONObject(i);
                int id = groupObj.getInt("id");        // might fail if "id" is missing
                String name = groupObj.getString("name");
                groups.add(new Group(id, name));
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Opens the message view for a selected group.
     *
     * @param group selected group
     */
    private void openGroupChat(Group group) {
        Intent intent = new Intent(GroupListActivity.this, MessagesActivity.class);
        intent.putExtra("groupID", group.getId());
        intent.putExtra("groupName", group.getName());
        startActivity(intent);
    }

    /**
     * Sets up the bottom navigation bar for switching
     * between Buy, Sell, and Chat sections.
     */
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_buy)
                startActivity(new Intent(GroupListActivity.this, BuyActivity.class));
            else if (item.getItemId() == R.id.nav_sell)
                startActivity(new Intent(GroupListActivity.this, SellActivity.class));
            else if (item.getItemId() == R.id.nav_chat)
                startActivity(new Intent(GroupListActivity.this, GroupListActivity.class));
            return true;
        });
    }
}