package com.example.cymarket;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ServiceScenario;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.cymarket.Services.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import static org.junit.Assert.*;

/**
 * System + logic tests for Services folder
 * Covers ApiService, RetroClient, VolleySingleton, WebSocketService
 */
@RunWith(AndroidJUnit4.class)
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
       WEB SOCKET SERVICE LIFECYCLE
       ===================================================== */

    @Test
    public void testWebSocketServiceCreateAndBind() {
        try (ServiceScenario<WebSocketService> scenario =
                     ServiceScenario.launch(WebSocketService.class)) {
            scenario.onService(service -> assertNotNull(service));
        }
    }

    @Test
    public void testWebSocketServiceConnectIntentPath() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                WebSocketService.class
        );
        intent.setAction("WS_CONNECT");
        intent.putExtra("key", "testKey");
        intent.putExtra("url", "ws://example.com");

        ServiceScenario<WebSocketService> scenario =
                ServiceScenario.launch(intent);

        scenario.close();
    }

    @Test
    public void testWebSocketServiceDisconnectIntentPath() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                WebSocketService.class
        );
        intent.setAction("WS_DISCONNECT");
        intent.putExtra("key", "missingKey");

        ServiceScenario<WebSocketService> scenario =
                ServiceScenario.launch(intent);

        scenario.close();
    }

    /* =====================================================
       WEB SOCKET SEND BROADCAST PATH
       ===================================================== */

    @Test
    public void testWebSocketSendBroadcastWithNoSocket() {
        ServiceScenario<WebSocketService> scenario =
                ServiceScenario.launch(WebSocketService.class);

        Intent send = new Intent("WS_SEND");
        send.putExtra("key", "unknown");
        send.putExtra("message", "hello");

        LocalBroadcastManager.getInstance(
                ApplicationProvider.getApplicationContext()
        ).sendBroadcast(send);

        scenario.close();
    }
}