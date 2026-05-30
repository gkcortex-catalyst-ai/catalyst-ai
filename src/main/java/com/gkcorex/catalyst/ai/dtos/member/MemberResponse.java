package com.gkcorex.catalyst.ai.dtos.member;

import com.gkcorex.catalyst.ai.enums.ProjectRole;
import java.time.Instant;

public record MemberResponse(
    Long userId, String name, String username, ProjectRole role, Instant invitedAt) {}
