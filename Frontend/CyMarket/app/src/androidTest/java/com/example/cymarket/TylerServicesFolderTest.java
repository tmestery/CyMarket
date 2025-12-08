package com.example.cymarket;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.example.cymarket.Services.*;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Logic/unit tests for Services folder
 * Covers ApiService, RetroClient, VolleySingleton
 * Note: WebSocketService cannot be fully tested in local JVM tests.
 */
public class TylerServicesFolderTest {

    /* =====================================================
       API SERVICE + RETROFIT CLIENT
       ===================================================== */

    @Test
    public void testRetroClientReturnsApiService() {
        ApiService api = RetroClient.getApiService();
        assertNotNull(api);
    }

    @Test
    public void testApiServiceMethodCreation() {
        ApiService api = RetroClient.getApiService();

        MultipartBody.Part dummyPart =
                MultipartBody.Part.createFormData(
                        "image",
                        "test.png",
                        RequestBody.create(null, new byte[]{})
                );

        Call<?> call1 = api.uploadProfileImage("user", dummyPart);
        Call<?> call2 = api.getUserJoinDate("email", "pass");
        Call<?> call3 = api.deleteUser("user");
        Call<?> call4 = api.deleteProfileImage("user");
        Call<?> call5 = api.getAllUsers();
        Call<?> call6 = api.getName("email");
        Call<?> call7 = api.getReports(1, "email", "pass");
        Call<?> call8 = api.addUserToGroup(1, "user");
        Call<?> call9 = api.getReportIds("email", "pass");
        Call<?> call10 = api.getReportById(1);
        Call<?> call11 = api.getTotalSales();

        assertNotNull(call1);
        assertNotNull(call2);
        assertNotNull(call3);
        assertNotNull(call4);
        assertNotNull(call5);
        assertNotNull(call6);
        assertNotNull(call7);
        assertNotNull(call8);
        assertNotNull(call9);
        assertNotNull(call10);
        assertNotNull(call11);
    }

    /* =====================================================
       VOLLEY SINGLETON
       ===================================================== */

    @Test
    public void testVolleySingletonInstanceAndQueue() {
        Context ctx = ApplicationProvider.getApplicationContext();

        VolleySingleton v1 = VolleySingleton.getInstance(ctx);
        VolleySingleton v2 = VolleySingleton.getInstance(ctx);

        assertNotNull(v1);
        assertSame(v1, v2); // singleton check
        assertNotNull(v1.getRequestQueue());
        assertNotNull(v1.getImageLoader());
    }

    /* =====================================================
       WEB SOCKET SERVICE (logic checks only)
       ===================================================== */

    @Test
    public void testWebSocketServiceMapInitialization() {
        WebSocketService wsService = new WebSocketService();
        // cannot start service, but sockets map should exist via constructor
        // note: we would need to make 'sockets' protected/public or add a getter for full test
        assertNotNull(wsService);
    }
}