package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.deploy.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}
