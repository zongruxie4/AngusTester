package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectMemberCmd;
import cloud.xcan.angus.core.tester.application.converter.ProjectMemberConverter;
import cloud.xcan.angus.core.tester.domain.project.member.ProjectMember;
import cloud.xcan.angus.core.tester.domain.project.member.ProjectMemberRepo;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Command implementation for project member management.
 * <p>
 * Provides methods for adding and replacing project members.
 * <p>
 * Ensures batch operations and repository access.
 */
@Biz
public class ProjectMemberCmdImpl extends CommCmd<ProjectMember, Long> implements ProjectMemberCmd {

  @Resource
  private ProjectMemberRepo projectMemberRepo;

  /**
   * Add a batch of project members for a project.
   * <p>
   * Batch inserts project members for the specified project and member types.
   */
  @Override
  public void add0(Long projectId,
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds) {
    if (isNotEmpty(memberTypeIds)) {
      batchInsert(ProjectMemberConverter.toDomains(projectId, memberTypeIds));
    }
  }

  /**
   * Replace project members for a project.
   * <p>
   * Deletes existing members and batch inserts new members for the specified project and member types.
   */
  @Override
  public void replace0(Long projectId,
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds) {
    if (isNotEmpty(memberTypeIds)) {
      projectMemberRepo.deleteByProjectId(projectId);
      add0(projectId, memberTypeIds);
    }
  }

  /**
   * Get the repository for project members.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<ProjectMember, Long> getRepository() {
    return projectMemberRepo;
  }
}
