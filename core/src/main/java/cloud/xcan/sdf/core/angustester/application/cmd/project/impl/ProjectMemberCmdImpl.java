package cloud.xcan.sdf.core.angustester.application.cmd.project.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectMemberCmd;
import cloud.xcan.sdf.core.angustester.application.converter.ProjectMemberConverter;
import cloud.xcan.sdf.core.angustester.domain.project.member.ProjectMember;
import cloud.xcan.sdf.core.angustester.domain.project.member.ProjectMemberRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import javax.annotation.Resource;

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
