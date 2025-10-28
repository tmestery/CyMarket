package onetomany.Group;

import onetomany.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Vivek Bengre
 *
 */

public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findById(int id);
    Group findByName(String name);




}
