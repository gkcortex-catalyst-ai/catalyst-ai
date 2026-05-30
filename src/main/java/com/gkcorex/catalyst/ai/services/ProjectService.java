package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.project.ProjectRequest;
import com.gkcorex.catalyst.ai.dtos.project.ProjectResponse;
import com.gkcorex.catalyst.ai.dtos.project.ProjectSummaryResponse;
import java.util.List;

public interface ProjectService {

  List<ProjectSummaryResponse> getUserProjects();

  ProjectSummaryResponse getUserProject(Long projectId);

  ProjectResponse createProject(ProjectRequest projectRequest);

  ProjectResponse updateProject(Long projectId, ProjectRequest projectRequest);

  void softDelete(Long projectId);
}
