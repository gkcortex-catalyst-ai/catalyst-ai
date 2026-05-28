package com.gkcorex.catalyst.ai.repositories;

import com.gkcorex.catalyst.ai.entities.ProjectFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
  Optional<ProjectFile> findByProjectIdAndPath(Long projectId, String cleanPath);

  List<ProjectFile> findByProjectId(Long projectId);
}
