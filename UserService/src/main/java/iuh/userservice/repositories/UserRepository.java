package iuh.userservice.repositories;

import iuh.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u WHERE u.phoneNumber = :phoneNumber")
    int existsUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.avatar = :avatarUrl WHERE u.userId = :userId")
    int updateAvatar(@Param("avatarUrl") String avatarUrl, @Param("userId") String userId);
    @Query("SELECT u.email FROM User u WHERE u.userId = :userId")
    String getEmailUserById(@Param("userId") String userId);

}
