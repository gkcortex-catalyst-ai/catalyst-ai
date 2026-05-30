package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.dtos.project.FileContentResponse;
import com.gkcorex.catalyst.ai.dtos.project.FileTreeResponse;
import com.gkcorex.catalyst.ai.services.ProjectFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspace/projects/{projectId}/files")
// @RequestMapping("/api/projects/{projectId}/files")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FileController {

  ProjectFileService fileService;

  @GetMapping
  public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId) {
    return ResponseEntity.ok(fileService.getFileTree(projectId));
  }

  @GetMapping("/content")
  public ResponseEntity<FileContentResponse> getFile(
      @PathVariable Long projectId, @RequestParam String path) {
    return ResponseEntity.ok(fileService.getFileContent(projectId, path));
  }
}
