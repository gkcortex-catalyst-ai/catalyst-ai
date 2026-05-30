package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.project.FileContentResponse;
import com.gkcorex.catalyst.ai.dtos.project.FileTreeResponse;

public interface ProjectFileService {
  FileTreeResponse getFileTree(Long projectId);

  FileContentResponse getFileContent(Long projectId, String path);

  void saveFile(Long projectId, String filePath, String fileContent);
}
