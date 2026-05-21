package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.dtos.project.FileContentResponse;
import com.gkcorex.catalyst.ai.dtos.project.FileNode;
import com.gkcorex.catalyst.ai.services.ProjectFileService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/files")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FileController {

  ProjectFileService fileService;

  @GetMapping
  public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId) {
    Long userId = 1L;
    return ResponseEntity.ok(fileService.getFileTree(userId, projectId));
  }

  @GetMapping("/{*path}")
  public ResponseEntity<FileContentResponse> getFile(
      @PathVariable Long projectId, @PathVariable String path) {
    Long userId = 1L;
    return ResponseEntity.ok(fileService.getFileContent(userId, projectId, path));
  }
}
