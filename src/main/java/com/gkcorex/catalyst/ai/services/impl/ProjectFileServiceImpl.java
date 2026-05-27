package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.project.FileContentResponse;
import com.gkcorex.catalyst.ai.dtos.project.FileNode;
import com.gkcorex.catalyst.ai.entities.Project;
import com.gkcorex.catalyst.ai.entities.ProjectFile;
import com.gkcorex.catalyst.ai.exceptions.ResourceNotFoundException;
import com.gkcorex.catalyst.ai.mappers.ProjectFileMapper;
import com.gkcorex.catalyst.ai.repositories.ProjectFileRepository;
import com.gkcorex.catalyst.ai.repositories.ProjectRepository;
import com.gkcorex.catalyst.ai.services.ProjectFileService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectFileServiceImpl implements ProjectFileService {

    ProjectRepository projectRepository;

    ProjectFileRepository projectFileRepository;

    MinioClient minioClient;

    ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private String projectBucket;

  @Override
  public List<FileNode> getFileTree(Long userId, Long projectId) {
        List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);
        return projectFileMapper.mapProjectFilesToFileNodes(projectFileList);
  }

  @Override
  public FileContentResponse getFileContent(Long userId, Long projectId, String path) {
    return null;
  }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
        log.info("Saving file: {}", filePath);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));
        String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;

        String objectKey = projectId + "/" + cleanPath;

        try{
            byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);
            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType(determineContentType(filePath))
                    .build());

            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey)
                            .createdAt(Instant.now())
                            .build());

            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file);
            log.info("Saved file {}", objectKey);
        } catch (Exception e){
            log.error("failed to save file: {}", objectKey);
            throw  new RuntimeException("Failed to save file", e);
        }
    }

    private String determineContentType(String path){
      String type = URLConnection.guessContentTypeFromName(path);
      if(type!=null) return type;
      if(path.endsWith(".js") || path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx")) return "text/javascript";
      if(path.endsWith(".json")) return "application/json";
      if(path.endsWith(".css")) return "text/css";
      return "text/plain";
    }
}
