package com.gkcorex.catalyst.ai.dtos.project;

import com.gkcorex.catalyst.ai.enums.ProjectRole;
import java.time.Instant;

public record ProjectSummaryResponse(
    Long id, String name, Instant createdAt, Instant updatedAt, ProjectRole role) {}
