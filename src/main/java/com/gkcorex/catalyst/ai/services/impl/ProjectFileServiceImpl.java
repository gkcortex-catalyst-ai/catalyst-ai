package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.project.FileContentResponse;
import com.gkcorex.catalyst.ai.dtos.project.FileNode;
import com.gkcorex.catalyst.ai.services.ProjectFileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFileServiceImpl implements ProjectFileService {
  @Override
  public List<FileNode> getFileTree(Long userId, Long projectId) {
    return List.of();
  }

  @Override
  public FileContentResponse getFileContent(Long userId, Long projectId, String path) {
    return null;
  }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
        log.info("Saving file: {}", filePath);
//        save the file metadata in postgres

//        save the file content in minio
    }
}
