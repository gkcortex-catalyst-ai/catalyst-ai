package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.entities.Project;
import com.gkcorex.catalyst.ai.entities.ProjectFile;
import com.gkcorex.catalyst.ai.exceptions.ResourceNotFoundException;
import com.gkcorex.catalyst.ai.repositories.ProjectFileRepository;
import com.gkcorex.catalyst.ai.repositories.ProjectRepository;
import com.gkcorex.catalyst.ai.services.ProjectTemplateService;
import io.minio.*;
import io.minio.messages.Item;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectTemplateServiceImpl implements ProjectTemplateService {

  MinioClient minioClient;
  ProjectFileRepository projectFileRepository;
  ProjectRepository projectRepository;

  static String TEMPLATE_BUCKET = "starter-projects";
  static String TARGET_BUCKET = "projects";
  static String TEMPLATE_NAME = "react-vite-tailwind-daisyui-starter";

  @Override
  public void initializeProjectFromTemplate(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found", projectId.toString()));

    try {
      Iterable<Result<Item>> results =
          minioClient.listObjects(
              ListObjectsArgs.builder()
                  .bucket(TEMPLATE_BUCKET)
                  .prefix(TEMPLATE_NAME + "/")
                  .recursive(true)
                  .build());

      List<ProjectFile> filesToSave = new ArrayList<>();

      for (Result<Item> result : results) {
        Item item = result.get();
        String sourceKey = item.objectName();

        String cleanPath = sourceKey.replaceFirst(TEMPLATE_NAME + "/", "");
        String destKey = projectId + "/" + cleanPath;

        minioClient.copyObject(
            CopyObjectArgs.builder()
                .bucket(TARGET_BUCKET)
                .object(destKey)
                .source(CopySource.builder().bucket(TEMPLATE_BUCKET).object(sourceKey).build())
                .build());

        ProjectFile projectFile =
            ProjectFile.builder()
                .project(project)
                .path(cleanPath)
                .minioObjectKey(destKey)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        filesToSave.add(projectFile);
      }

      projectFileRepository.saveAll(filesToSave);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize project from template", e);
    }
  }
}
