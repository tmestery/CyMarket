package onetomany.WebSocketAdminNot;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;   // ✅ NEW

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import onetomany.Group.Group;
import onetomany.Group.GroupRepository;
import onetomany.Users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller      // this is needed for this to be an endpoint to springboot
@ServerEndpoint(value = "/chat/{id}/{username}")  // this is Websocket url
public class chatNot {

    private static MessageRepository msgRepo;
    private static GroupRepository groupRepository;

    @Autowired
    public void setMessageRepository(MessageRepository repo) {
        msgRepo = repo;
    }

    @Autowired
    public void setGruopRepository(GroupRepository groupRepo) {
        groupRepository = groupRepo;
    }

    // Store all socket session and their corresponding username.
    // 🔴 CHANGED: use ConcurrentHashMap instead of Hashtable (safer)
    private static Map<Session, String> sessionUsernameMap = new ConcurrentHashMap<>();
    private static Map<String, Session> usernameSessionMap = new ConcurrentHashMap<>();
    private static Map<Session, Integer> sessionGroupIdMap = new ConcurrentHashMap<>();

    // 🔴 CHANGED: use correct class in logger
    private final Logger logger = LoggerFactory.getLogger(chatNot.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("id") int id)
            throws IOException {

        logger.info("Entered into Open");

        Group group = groupRepository.findById(id);
        if (group == null) {
            logger.error("Group with ID {} not found", id);
            return;
        }

        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        sessionGroupIdMap.put(session, id); // Store groupId for the session

        // Send chat history to the newly connected user
        sendMessageToPArticularUser(username, getChatHistory(id));

        // broadcast that new user joined
        String message = "User:" + username + " has Joined the Chat";
        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

        logger.info("Entered into Message: Got Message:" + message);
        String username = sessionUsernameMap.get(session);
        Integer groupId = sessionGroupIdMap.get(session);

        if (username == null || groupId == null) {
            logger.warn("Session {} has no username or groupId stored", session.getId());
            return;
        }

        // Broadcast message to the same group
        broadcastToGroup(username + ": " + message, groupId);

        // Saving chat history to repository
        msgRepo.save(new Message(username, message, groupId));
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Entered into Close");

        String username = sessionUsernameMap.remove(session);
        if (username != null) {
            usernameSessionMap.remove(username);
        }

        // ✅ NEW: also remove from sessionGroupIdMap
        sessionGroupIdMap.remove(session);

        if (username != null) {
            String message = username + " disconnected";
            broadcast(message);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("Entered into Error", throwable);

        // ✅ NEW: clean up here as well (same as onClose)
        cleanupSession(session);
    }

    // ✅ NEW: helper to clean up all maps for a session
    private void cleanupSession(Session session) {
        if (session == null) return;

        String username = sessionUsernameMap.remove(session);
        if (username != null) {
            usernameSessionMap.remove(username);
        }
        sessionGroupIdMap.remove(session);

        logger.info("Cleaned up session {}", session.getId());
    }

    private void sendMessageToPArticularUser(String username, String message) {
        try {
            Session session = usernameSessionMap.get(username);
            if (session == null) {
                logger.warn("No session for username {} when sending private message", username);
                return;
            }

            // ✅ NEW: check open
            if (!session.isOpen()) {
                logger.warn("Session for username {} is closed; cleaning up", username);
                cleanupSession(session);
                return;
            }

            session.getBasicRemote().sendText(message);

        } catch (IOException e) {
            logger.info("Exception sending private message: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {   // ✅ NEW
            logger.warn("Tried to send to a closed session for username {}", username, e);
        }
    }

    private void broadcast(String message) {
        // iterate over a snapshot to avoid concurrent modification issues
        sessionUsernameMap.forEach((session, username) -> {
            if (session == null) return;

            // ✅ NEW: skip closed sessions
            if (!session.isOpen()) {
                logger.warn("Skipping closed session {}", session.getId());
                cleanupSession(session);
                return;
            }

            try {
                session.getBasicRemote().sendText(message);
            } catch (IllegalStateException e) {   // ✅ NEW
                logger.warn("IllegalStateException when broadcasting to {}. Cleaning up session {}", username, session.getId(), e);
                cleanupSession(session);
            } catch (IOException e) {
                logger.info("IOException when broadcasting to " + username + ": " + e.getMessage());
                e.printStackTrace();
                cleanupSession(session);  // optional, but usually you want to drop broken sessions
            }
        });
    }

    // Gets the Chat history from the repository
    private String getChatHistory(int id) {
        List<Message> messages = msgRepo.findByGroupID(id);

        StringBuilder sb = new StringBuilder();
        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                sb.append(message.getUserName()).append(": ")
                  .append(message.getContent()).append("\n");
            }
        }
        return sb.toString();
    }

    private void broadcastToGroup(String message, int groupId) {
        // iterate over snapshot of the map to avoid concurrent modification
        sessionGroupIdMap.forEach((session, group) -> {
            if (session == null) return;

            if (group != groupId) {
                return;
            }

            // ✅ NEW: check open
            if (!session.isOpen()) {
                logger.warn("Skipping closed session {} in group {}", session.getId(), groupId);
                cleanupSession(session);
                return;
            }

            try {
                session.getBasicRemote().sendText(message);
            } catch (IllegalStateException e) {   // ✅ NEW
                logger.warn("IllegalStateException broadcasting to group {} on session {}. Cleaning up", groupId, session.getId(), e);
                cleanupSession(session);
            } catch (IOException e) {
                logger.error("I/O Error broadcasting message to group {}", groupId, e);
                cleanupSession(session);  // drop broken session
            }
        });
    }
}
