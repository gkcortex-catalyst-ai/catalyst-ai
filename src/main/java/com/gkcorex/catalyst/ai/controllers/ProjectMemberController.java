package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.dtos.member.InviteMemberRequest;
import com.gkcorex.catalyst.ai.dtos.member.MemberResponse;
import com.gkcorex.catalyst.ai.dtos.member.UpdateMemberRoleRequest;
import com.gkcorex.catalyst.ai.services.ProjectMemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// @RequestMapping("/api/projects/{projectId}/members")
@RequestMapping("/api/v1/workspace/projects/{projectId}/members")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectMemberController {

  ProjectMemberService projectMemberService;

  @GetMapping
  public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable Long projectId) {
    return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
  }

  @PostMapping
  public ResponseEntity<MemberResponse> inviteMember(
      @PathVariable Long projectId, @RequestBody @Valid InviteMemberRequest inviteMemberRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectMemberService.inviteMember(projectId, inviteMemberRequest));
  }

  @PatchMapping("/{memberId}")
  public ResponseEntity<MemberResponse> updateMemberRole(
      @PathVariable Long projectId,
      @PathVariable Long memberId,
      @RequestBody @Valid UpdateMemberRoleRequest updateMemberRoleRequest) {
    return ResponseEntity.ok(
        projectMemberService.updateMemberRole(projectId, memberId, updateMemberRoleRequest));
  }

  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteProjectMember(
      @PathVariable Long projectId, @PathVariable Long memberId) {
    projectMemberService.deleteMember(projectId, memberId);
    return ResponseEntity.noContent().build();
  }
}
