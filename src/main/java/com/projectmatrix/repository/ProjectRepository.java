package com.projectmatrix.repository;

import com.projectmatrix.entity.Project;
import com.projectmatrix.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByUsersContaining(User user, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE :user MEMBER OF p.users OR p.createdBy = :user")
    List<Project> findByUserInvolved(@Param("user") User user);

    List<Project> findByCreatedBy(User user);
}