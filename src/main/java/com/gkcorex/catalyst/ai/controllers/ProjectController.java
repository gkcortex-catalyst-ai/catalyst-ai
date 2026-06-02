package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.dtos.deploy.DeployResponse;
import com.gkcorex.catalyst.ai.dtos.project.ProjectRequest;
import com.gkcorex.catalyst.ai.dtos.project.ProjectResponse;
import com.gkcorex.catalyst.ai.dtos.project.ProjectSummaryResponse;
import com.gkcorex.catalyst.ai.services.DeploymentService;
import com.gkcorex.catalyst.ai.services.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// @RequestMapping("/api/projects")
@RequestMapping("/api/v1/workspace/projects")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectController {

  ProjectService projectService;

  DeploymentService deploymentService;

  @GetMapping("/{projectId}")
  public ResponseEntity<ProjectSummaryResponse> getProject(@PathVariable Long projectId) {
    return ResponseEntity.ok(projectService.getUserProject(projectId));
  }

  @GetMapping
  public ResponseEntity<List<ProjectSummaryResponse>> getAllProjects() {
    return ResponseEntity.ok(projectService.getUserProjects());
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> createProject(
      @RequestBody @Valid ProjectRequest projectRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.createProject(projectRequest));
  }

  @PatchMapping("/{projectId}")
  public ResponseEntity<ProjectResponse> updateProject(
      @PathVariable Long projectId, @RequestBody @Valid ProjectRequest projectRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.updateProject(projectId, projectRequest));
  }

  @DeleteMapping("/{projectId}")
  public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
    projectService.softDelete(projectId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/deploy")
    public ResponseEntity<DeployResponse> deployProject(@PathVariable Long id){
      return ResponseEntity.ok(deploymentService.deploy(id));
  }
}