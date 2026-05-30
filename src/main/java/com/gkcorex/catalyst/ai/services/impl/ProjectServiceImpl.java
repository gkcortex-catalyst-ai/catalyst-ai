package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.project.ProjectRequest;
import com.gkcorex.catalyst.ai.dtos.project.ProjectResponse;
import com.gkcorex.catalyst.ai.dtos.project.ProjectSummaryResponse;
import com.gkcorex.catalyst.ai.entities.Project;
import com.gkcorex.catalyst.ai.entities.ProjectMember;
import com.gkcorex.catalyst.ai.entities.ProjectMemberId;
import com.gkcorex.catalyst.ai.entities.User;
import com.gkcorex.catalyst.ai.enums.ProjectRole;
import com.gkcorex.catalyst.ai.exceptions.BadRequestException;
import com.gkcorex.catalyst.ai.exceptions.ResourceNotFoundException;
import com.gkcorex.catalyst.ai.mappers.ProjectMapper;
import com.gkcorex.catalyst.ai.repositories.ProjectMemberRepository;
import com.gkcorex.catalyst.ai.repositories.ProjectRepository;
import com.gkcorex.catalyst.ai.repositories.UserRepository;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.ProjectService;
import com.gkcorex.catalyst.ai.services.ProjectTemplateService;
import com.gkcorex.catalyst.ai.services.SubscriptionService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

  ProjectRepository projectRepository;

  UserRepository userRepository;

  ProjectMemberRepository projectMemberRepository;

  ProjectMapper projectMapper;

  JwtAuthUtil jwtAuthUtil;

  SubscriptionService subscriptionService;

  ProjectTemplateService projectTemplateService;

  @Override
  public List<ProjectSummaryResponse> getUserProjects() {
    Long userId = jwtAuthUtil.getCurrentUserId();
    var projectsWithRoles = projectRepository.findAllAccessibleByUser(userId);
    return projectsWithRoles.stream()
        .map(p -> projectMapper.mapProjectWithRoleToSummaryResponse(p.getProject(), p.getRole()))
        .toList();
  }

  @Override
  @PreAuthorize("@security.canViewProject(#projectId)")
  public ProjectSummaryResponse getUserProject(Long projectId) {
    Long userId = jwtAuthUtil.getCurrentUserId();

    var projectWithRole =
        projectRepository
            .findAccessibleProjectByIdWithRole(userId, projectId)
            .orElseThrow(() -> new BadRequestException("Project not found"));

    return projectMapper.mapProjectWithRoleToSummaryResponse(
        projectWithRole.getProject(), projectWithRole.getRole());
  }

  @Override
  public ProjectResponse createProject(ProjectRequest projectRequest) {
    if (!subscriptionService.canCreateNewProject()) {
      throw new BadRequestException("User cannot create a new project with current plan.");
    }
    Long userId = jwtAuthUtil.getCurrentUserId();
    User owner =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found", userId.toString()));
    Project project = Project.builder().name(projectRequest.name()).isPublic(true).build();
    project = projectRepository.save(project);

    ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), owner.getId());
    ProjectMember projectMember =
        ProjectMember.builder()
            .id(projectMemberId)
            .projectRole(ProjectRole.OWNER)
            .project(project)
            .user(owner)
            .invitedAt(Instant.now())
            .acceptedAt(Instant.now())
            .build();
    projectMemberRepository.save(projectMember);

    projectTemplateService.initializeProjectFromTemplate(project.getId());

    return projectMapper.mapEntityToResponse(project);
  }

  @Override
  @PreAuthorize("@security.canEditProject(#projectId)")
  public ProjectResponse updateProject(Long projectId, ProjectRequest projectRequest) {
    Long userId = jwtAuthUtil.getCurrentUserId();
    Project project = getAccessibleProjectById(userId, projectId);
    project.setName(projectRequest.name());
    project = projectRepository.save(project);
    return projectMapper.mapEntityToResponse(project);
  }

  @Override
  @PreAuthorize("@security.canDeleteProject(#projectId)")
  public void softDelete(Long projectId) {
    Long userId = jwtAuthUtil.getCurrentUserId();
    Project project = getAccessibleProjectById(userId, projectId);
    project.setDeletedAt(Instant.now());
    projectRepository.save(project);
  }

  //    INTERNAL FUNCTIONS

  private Project getAccessibleProjectById(Long userId, Long projectId) {
    return projectRepository
        .findAccessibleProjectById(userId, projectId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Project not found", projectId.toString()));
  }
}
