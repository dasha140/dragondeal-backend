package org.example.repository;

import org.example.domain.AppUser;
import org.example.domain.enums.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {
            "walletList"
    })
    List<AppUser> findAll();

    @EntityGraph(attributePaths = {
            "walletList"
    })
    Optional<AppUser> findWithWalletsById(Long id);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<AppUser> findByRole(UserRole role);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppUser u
            set u.avatarUrl = :avatarUrl
            where u.id = :id
            """)
    int updateAvatarById(@Param("id") Long id,
                         @Param("avatarUrl") String avatarUrl);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppUser u
            set u.passwordHash = :passwordHash
            where u.id = :id
            """)
    int updatePasswordHashById(@Param("id") Long id,
                               @Param("passwordHash") String passwordHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppUser u
            set u.rating = :rating
            where u.id = :id
            """)
    int updateRatingById(@Param("id") Long id,
                         @Param("rating") BigDecimal rating);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppUser u
            set u.role = :role
            where u.id = :id
            """)
    int updateRoleById(@Param("id") Long id,
                       @Param("role") UserRole role);
}