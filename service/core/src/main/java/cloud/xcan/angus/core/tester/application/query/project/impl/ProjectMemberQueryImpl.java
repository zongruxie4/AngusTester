package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_MEMBER_USER;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_MEMBER_USER_CODE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.member.ProjectMember;
import cloud.xcan.angus.core.tester.domain.project.member.ProjectMemberRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Implementation of ProjectMemberQuery for project member management and validation.
 * </p>
 * <p>
 * Provides methods for finding project members, checking membership permissions, and setting member information.
 * </p>
 */
@Biz
public class ProjectMemberQueryImpl implements ProjectMemberQuery {

  @Resource
  private ProjectMemberRepo projectMemberRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Find all user IDs who are members of the specified project.
   * </p>
   * @param projectId Project ID
   * @return Set of user IDs
   */
  @Override
  public Set<Long> findMemberUserIds(Long projectId) {
    List<Long> orgIds = projectMemberRepo.findMemberIdsByProjectId(projectId);
    return userManager.findUserIdsByOrgIds(orgIds);
  }

  /**
   * <p>
   * Get member IDs based on creator object type and project ID.
   * </p>
   * <p>
   * If creator object type and ID are provided, returns users associated with that creator object.
   * Otherwise, returns all project member user IDs.
   * </p>
   * @param creatorObjectType Type of creator object
   * @param creatorObjectId Creator object ID
   * @param projectId Project ID
   * @return Set of member IDs
   */
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

  /**
   * <p>
   * Check if the current user is a member of the project specified in the search criteria.
   * </p>
   * @param criteria Search criteria containing project ID
   */
  @Override
  public void checkMember(Set<SearchCriteria> criteria) {
    String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
    ProtocolAssert.assertNotEmpty(projectId, "Project id is required");
    checkMember(getUserId(), Long.valueOf(projectId));
  }

  /**
   * <p>
   * Check if a user is a member of the specified project.
   * </p>
   * <p>
   * Admins bypass this check. For regular users, validates that the user's organization IDs
   * overlap with the project's member organization IDs.
   * </p>
   * @param userId User ID to check
   * @param projectId Project ID
   */
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

  /**
   * <p>
   * Set member information for a project by grouping members by organization target type.
   * </p>
   * @param project Project to set members for
   */
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

  /**
   * <p>
   * Set member information for a list of projects.
   * </p>
   * @param projects List of projects to set members for
   */
  @Override
  public void setMembers(List<Project> projects) {
    if (isNotEmpty(projects)) {
      for (Project project : projects) {
        setMembers(project);
      }
    }
  }

}
