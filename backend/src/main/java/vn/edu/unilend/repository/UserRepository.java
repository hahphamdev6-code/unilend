package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.unilend.entity.User;
import vn.edu.unilend.entity.enums.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserCode(String userCode);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserCodeOrEmail(String userCode, String email);

    boolean existsByUserCode(String userCode);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
