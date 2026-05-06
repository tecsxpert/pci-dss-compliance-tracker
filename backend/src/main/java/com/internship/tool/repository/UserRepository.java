package com.internship.tool.repository;

import com.internship.tool.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Existing filtering
    List<User> findByNameContainingIgnoreCase(String name);

    // ✅ Pagination filtering
    Page<User> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    // ✅ Advanced filtering
    List<User> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(
            String name,
            String email
    );

    // ✅ Pagination + advanced filtering
    Page<User> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );

    // ✅ JPQL Query
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))
        """)
    List<User> searchUsersJPQL(@Param("name") String name);

    // ✅ 🔥 Native SQL Query
    @Query(
        value = """
            SELECT * FROM users
            WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
            """,
        nativeQuery = true
    )
    List<User> searchUsersNative(@Param("name") String name);
}