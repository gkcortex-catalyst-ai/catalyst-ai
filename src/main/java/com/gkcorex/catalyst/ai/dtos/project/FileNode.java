package com.gkcorex.catalyst.ai.dtos.project;

public record FileNode(String path) {

  @Override
  public String toString() {
    return path;
  }
}
