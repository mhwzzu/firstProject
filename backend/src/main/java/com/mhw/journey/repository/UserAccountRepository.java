package com.mhw.journey.repository;
import com.mhw.journey.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> { Optional<UserAccount> findByEmail(String email); }
