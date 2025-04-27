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

@Biz
public class ProjectMemberCmdImpl extends CommCmd<ProjectMember, Long> implements ProjectMemberCmd {

  @Resource
  private ProjectMemberRepo projectMemberRepo;

  @Override
  public void add0(Long projectId,
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds) {
    if (isNotEmpty(memberTypeIds)) {
      batchInsert(ProjectMemberConverter.toDomains(projectId, memberTypeIds));
    }
  }

  @Override
  public void replace0(Long projectId,
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds) {
    if (isNotEmpty(memberTypeIds)) {
      projectMemberRepo.deleteByProjectId(projectId);
      add0(projectId, memberTypeIds);
    }
  }

  @Override
  protected BaseRepository<ProjectMember, Long> getRepository() {
    return projectMemberRepo;
  }
}
