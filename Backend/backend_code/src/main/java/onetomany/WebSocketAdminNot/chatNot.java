package onetomany.WebSocketAdminNot;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.PathVariable;

@Controller      // this is needed for this to be an endpoint to springboot
@ServerEndpoint(value = "/chat/{id}/{username}")  // this is Websocket url
public class chatNot {

    // cannot autowire static directly (instead we do it by the below
    // method
    private static MessageRepository msgRepo;

    private static GroupRepository groupRepository;

    /*
     * Grabs the MessageRepository singleton from the Spring Application
     * Context.  This works because of the @Controller annotation on this
     * class and because the variable is declared as static.
     * There are other ways to set this. However, this approach is
     * easiest.
     */
    @Autowired
    public void setMessageRepository(MessageRepository repo) {
        msgRepo = repo;  // we are setting the static variable
    }
    @Autowired
    public void setGruopRepository(GroupRepository groupRepo) {
        groupRepository = groupRepo;  // we are setting the static variable
    }

    // Store all socket session and their corresponding username.
    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static Map<Session, Integer> sessionGroupIdMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(chatSocket.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("id") int id)
            throws IOException {

        logger.info("Entered into Open");

        Group group = groupRepository.findById(id);
        if (group == null) {
            // Group not found, handle error or send appropriate message
            logger.error("Group with ID {} not found", id);
            return;
        }

        // Store connecting user information
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        sessionGroupIdMap.put(session, id); // Store groupId for the session



        //Send chat history to the newly connected user
        sendMessageToPArticularUser(username, getChatHistory(id));

        // broadcast that new user joined
        String message = "User:" + username + " has Joined the Chat";
        broadcast(message);
    }


    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

        logger.info("Entered into Message: Got Message:" + message);
        String username = sessionUsernameMap.get(session);
        int groupId = sessionGroupIdMap.get(session); // Get groupId for the session

        // Handle messages here...

        // Broadcast message to the same group
        broadcastToGroup(username + ": " + message, groupId);

        // Saving chat history to repository
        msgRepo.save(new Message(username, message, groupId));
    }


    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Entered into Close");

        // remove the user connection information
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        // broadcase that the user disconnected
        String message = username + " disconnected";
        broadcast(message);
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        logger.info("Entered into Error");
        throwable.printStackTrace();
    }


    private void sendMessageToPArticularUser(String username, String message) {
        try {
            usernameSessionMap.get(username).getBasicRemote().sendText(message);
        }
        catch (IOException e) {
            logger.info("Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }


    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            }
            catch (IOException e) {
                logger.info("Exception: " + e.getMessage().toString());
                e.printStackTrace();
            }

        });

    }



    // Gets the Chat history from the repository
    private String getChatHistory(int id) {
        List<Message> messages = msgRepo.findByGroupID(id);

        // convert the list to a string
        StringBuilder sb = new StringBuilder();
        if(messages != null && messages.size() != 0) {
            for (Message message : messages) {
                sb.append(message.getUserName() + ": " + message.getContent() + "\n");
            }
        }
        return sb.toString();
    }
    private void broadcastToGroup(String message, int groupId) {
        sessionGroupIdMap.forEach((session, group) -> {
            if (group == groupId) { // Check if the session belongs to the same group
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    // Handle exception
                    logger.error("Error broadcasting message to group {}", groupId, e);
                }
            }
        });
    }

}
