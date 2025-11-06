package onetomany.Notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
@Controller
@ServerEndpoint("/ws/notifications/{username}")
public class NotificationWebSocket {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocket.class);

    private static final Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static final Map<String, Session> usernameSessionMap = new Hashtable<>();

    private static NotificationService notificationServiceStatic;
    
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        notificationServiceStatic = notificationService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        if (username == null || username.isBlank()) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Username required"));
            return;
        }
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        log.info("[Notifications] OPEN {} -> {}", session.getId(), username);
        session.getBasicRemote().sendText("CONNECTED: notifications for " + username);
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        String u = sessionUsernameMap.get(session);
        log.debug("[Notifications] MSG from {}: {}", u, msg);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String username = sessionUsernameMap.remove(session);
        if (username != null) {
            usernameSessionMap.remove(username);
            log.info("[Notifications] CLOSE {} ({})", username, reason);
        } else {
            log.info("[Notifications] CLOSE (unknown) ({})", reason);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String username = (session != null ? sessionUsernameMap.get(session) : null);
        log.error("[Notifications] ERROR for {}: {}", username, error.toString(), error);
    }

    public static void sendToUser(String username, NotificationDTO dto) {
        Session s = usernameSessionMap.get(username);
        if (s == null || !s.isOpen()) {
            log.info("[Notifications] {} not connected; skipping push", username);
            return;
        }
        try {
            String json = new ObjectMapper().writeValueAsString(dto);
            s.getBasicRemote().sendText("NOTIFICATION:" + json);
            log.info("[Notifications] pushed to {}", username);
        } catch (Exception e) {
            log.error("[Notifications] push failed to {}", username, e);
        }
    }

    public static boolean isUserConnected(String username) {
        Session s = usernameSessionMap.get(username);
        return s != null && s.isOpen();
    }

    public static int getConnectedUsersCount() {
        return sessionUsernameMap.size();
    }
}

