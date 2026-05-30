package com.gkcorex.catalyst.ai.mappers;

import com.gkcorex.catalyst.ai.dtos.member.MemberResponse;
import com.gkcorex.catalyst.ai.entities.ProjectMember;
import com.gkcorex.catalyst.ai.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {
  @Mapping(target = "userId", source = "id")
  @Mapping(target = "role", constant = "OWNER")
  MemberResponse mapOwnerToMemberResponse(User owner);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "name", source = "user.name")
  @Mapping(target = "role", source = "projectRole")
  MemberResponse mapProjectMemberToMemberResponse(ProjectMember projectMember);
}
