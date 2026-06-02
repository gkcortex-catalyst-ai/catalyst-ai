package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.deploy.DeployResponse;
import com.gkcorex.catalyst.ai.services.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class KubernetesDeploymentServiceImpl implements DeploymentService {

    KubernetesClient kubernetesClient;

    static String NAMESPACE = "catalyst-ai-apps";
    static String POOL_LABEL = "status";
    static String PROJECT_LABEL = "project-id";
    static String IDLE = "idle";
    static String BUSY = "busy";

    @Override
    public DeployResponse deploy(Long projectId) {
        String domain = "project-" + projectId + ".app.catalyst-ai.com";

        Pod existingPod = findActivePod(projectId);

        if(existingPod!=null){
            return new DeployResponse("http://"+domain+":8090");
        }

        return claimAndStartNewPod(projectId, domain);
    }

    private DeployResponse claimAndStartNewPod(Long projectId, String domain) {
        return null;
    }

    private Pod findActivePod(Long projectId){
        return kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabel(PROJECT_LABEL, projectId.toString())
                .withLabel(POOL_LABEL, BUSY) // Active/Busy Pod
                .list().getItems().stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst().orElse(null);
    }
}
