package onetomany.adminUser;


import org.springframework.data.jpa.repository.JpaRepository;

public interface adminUserRepository  extends JpaRepository<adminUser, Long> {


    adminUser findById(int id);

    adminUser findByEmailId(String emailId);


}