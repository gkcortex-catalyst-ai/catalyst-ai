package com.gkcorex.catalyst.ai.mappers;

import com.gkcorex.catalyst.ai.dtos.project.ProjectResponse;
import com.gkcorex.catalyst.ai.dtos.project.ProjectSummaryResponse;
import com.gkcorex.catalyst.ai.entities.Project;
import com.gkcorex.catalyst.ai.enums.ProjectRole;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

  ProjectResponse mapEntityToResponse(Project project);

  List<ProjectSummaryResponse> mapEntitiesToSummaryResponses(List<Project> projects);

  ProjectSummaryResponse mapProjectWithRoleToSummaryResponse(Project project, ProjectRole role);
}
