package cloud.xcan.sdf.core.angustester.application.query.project.impl;

import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.PROJECT_NOT_MEMBER_USER;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.PROJECT_NOT_MEMBER_USER_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.member.ProjectMember;
import cloud.xcan.sdf.core.angustester.domain.project.member.ProjectMemberRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

@Biz
public class ProjectMemberQueryImpl implements ProjectMemberQuery {

  @Resource
  private ProjectMemberRepo projectMemberRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Set<Long> findMemberUserIds(Long projectId) {
    List<Long> orgIds = projectMemberRepo.findMemberIdsByProjectId(projectId);
    return userManager.findUserIdsByOrgIds(orgIds);
  }

  @Override
  public Set<Long> getMemberIds(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId) {
    Set<Long> createdBys;
    if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
      createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
    } else {
      createdBys = findMemberUserIds(projectId);
    }
    return createdBys;
  }

  @Override
  public void checkMember(Set<SearchCriteria> criteria) {
    String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
    ProtocolAssert.assertNotEmpty(projectId, "Project id is required");
    checkMember(getUserId(), Long.valueOf(projectId));
  }

  @Override
  public void checkMember(Long userId, Long projectId) {
    if (!isAdmin()) {
      List<Long> userOrgIds = userManager.getValidOrgAndUserIds(userId);
      List<Long> memberOrgIds = projectMemberRepo.findMemberIdsByProjectId(projectId);
      int memberNum = memberOrgIds.size();
      memberOrgIds.removeAll(userOrgIds);
      BizAssert.assertTrue(memberNum != memberOrgIds.size(),
          PROJECT_NOT_MEMBER_USER_CODE, PROJECT_NOT_MEMBER_USER);
    }
  }

  @Override
  public void setMembers(Project project) {
    List<ProjectMember> members = projectMemberRepo.findByProjectId(project.getId());
    LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> members0 = new LinkedHashMap<>();
    if (isNotEmpty(members)) {
      for (ProjectMember member : members) {
        OrgTargetType key = member.getMemberType();
        Long id = member.getMemberId();
        if (!members0.containsKey(key)) {
          members0.put(key, new LinkedHashSet<>());
        }
        members0.get(key).add(id);
      }
      project.setMembers(commonQuery.findOrgs(members0));
    }
  }

  @Override
  public void setMembers(List<Project> projects) {
    if (isNotEmpty(projects)) {
      for (Project project : projects) {
        setMembers(project);
      }
    }
  }

}
