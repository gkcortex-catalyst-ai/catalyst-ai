package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.project.FileContentResponse;
import com.gkcorex.catalyst.ai.dtos.project.FileNode;
import java.util.List;

public interface ProjectFileService {

  List<FileNode> getFileTree(Long projectId);

  FileContentResponse getFileContent(Long projectId, String path);

  void saveFile(Long projectId, String filePath, String fileContent);
}
