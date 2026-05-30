package com.gkcorex.catalyst.ai.repositories;

import com.gkcorex.catalyst.ai.entities.Project;
import com.gkcorex.catalyst.ai.enums.ProjectRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Query(
      """
            SELECT p as project, pm.projectRole as role
            FROM Project p
            JOIN ProjectMember pm ON pm.project.id = p.id
            WHERE pm.user.id = :userId
                AND p.deletedAt IS NULL
            ORDER BY p.updatedAt DESC
            """)
  List<ProjectWithRole> findAllAccessibleByUser(@Param("userId") Long userId);

  @Query(
      """
            SELECT p from Project p
            WHERE p.id = :projectId
            AND p.deletedAt IS NULL
            AND EXISTS (
            SELECT 1 FROM ProjectMember pm
                WHERE pm.id.userId = :userId
                AND pm.id.projectId = :projectId
            )
            """)
  Optional<Project> findAccessibleProjectById(
      @Param("userId") Long userId, @Param("projectId") Long projectId);

  @Query(
      """
                  SELECT p as project, pm.projectRole as role
                  FROM Project p
                  JOIN ProjectMember pm ON pm.project.id = p.id
                  WHERE p.id = :projectId
                    AND pm.user.id = :userId
                    AND p.deletedAt IS NULL
                  """)
  Optional<ProjectWithRole> findAccessibleProjectByIdWithRole(
      @Param("userId") Long userId, @Param("projectId") Long projectId);

  interface ProjectWithRole {
    Project getProject();

    ProjectRole getRole();
  }
}
