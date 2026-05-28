package com.gkcorex.catalyst.ai.mappers;

import com.gkcorex.catalyst.ai.dtos.project.FileNode;
import com.gkcorex.catalyst.ai.entities.ProjectFile;
import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface ProjectFileMapper {
  List<FileNode> mapProjectFilesToFileNodes(List<ProjectFile> projectFileList);
}
