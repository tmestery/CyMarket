package onetomany.WebSocketAdminNot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<onetomany.WebSocketAdminNot.Message, Long>{
        Message getMessagesByGroupID(int id);

        List<Message> findByGroupID(int id);
}
