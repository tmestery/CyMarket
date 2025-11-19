package onetomany.Notifications;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/notifications/{username}")
@Component
public class NotificationWebSocket {

    // Store session mappings
    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(NotificationWebSocket.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        logger.info("Notification WebSocket opened for User: " + username);

        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        // Handle incoming messages from client if necessary
        logger.info("Received message from " + sessionUsernameMap.get(session) + ": " + message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Notification WebSocket closed");

        String username = sessionUsernameMap.get(session);
        if (username != null) {
            sessionUsernameMap.remove(session);
            usernameSessionMap.remove(username);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Notification WebSocket error");
        throwable.printStackTrace();
    }

    // Utility method to send a notification to a specific user
    public void sendNotificationToUser(String username, String message) {
        try {
            Session session = usernameSessionMap.get(username);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("Error broadcasting message", e);
            }
        });
    }
}

