package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_DELETE_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_DELETE_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_EDIT_MODULE_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_EDIT_MODULE_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_EDIT_TAG_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_EDIT_TAG_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_MODIFY_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.PROJECT_NOT_MODIFY_PERMISSION_MODIFY;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.emptyList;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectListRepo;
import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import cloud.xcan.angus.core.tester.domain.project.ProjectSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of ProjectQuery for project management and query operations.
 * </p>
 * <p>
 * Provides methods for project listing, detail retrieval, permission checking, and quota validation.
 * </p>
 */
@Biz
public class ProjectQueryImpl implements ProjectQuery {

  @Resource
  private ProjectRepo projectRepo;
  @Resource
  private ProjectListRepo projectListRepo;
  @Resource
  private ProjectSearchRepo projectSearchRepo;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Get projects that the user has joined, optionally filtered by name.
   * </p>
   * @param userId User ID
   * @param name Optional project name filter
   * @return List of projects
   */
  @Override
  public List<Project> userJoined(Long userId, @Nullable String name) {
    return new BizTemplate<List<Project>>() {

      @Override
      protected List<Project> process() {
        List<Long> orgIds = userManager.getValidOrgAndUserIds(userId);
        List<Project> projects = projectRepo.findMemberProjects(orgIds);
        return isEmpty(name) ? projects : projects.stream().filter(x -> x.getName().contains(name))
            .toList();
      }
    }.execute();
  }

  /**
   * <p>
   * Get user base information for all members of a project.
   * </p>
   * @param id Project ID
   * @return List of user base information
   */
  @Override
  public List<UserBase> userMember(Long id) {
    return new BizTemplate<List<UserBase>>() {

      @Override
      protected List<UserBase> process() {
        Set<Long> memberUserIds = projectMemberQuery.findMemberUserIds(id);
        return isEmpty(memberUserIds) ? emptyList() : userManager.findUserBases(memberUserIds);
      }
    }.execute();
  }

  /**
   * <p>
   * Get detailed project information including members and owner details.
   * </p>
   * @param id Project ID
   * @return Project with complete information
   */
  @Override
  public Project detail(Long id) {
    return new BizTemplate<Project>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        projectDb = checkAndFind(id);
      }

      @Override
      protected Project process() {
        projectMemberQuery.setMembers(projectDb);

        // Set user name and avatar
        userManager.setUserNameAndAvatar(List.of(projectDb), "ownerId", "ownerName", "ownerAvatar");
        return projectDb;
      }
    }.execute();
  }

  /**
   * <p>
   * List projects with optional full-text search and authorization filtering.
   * </p>
   * <p>
   * Only returns non-deleted projects. Applies authorization conditions for non-admin users.
   * </p>
   * @param spec Project search specification
   * @param pageable Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match Full-text search keywords
   * @return Page of projects
   */
  @Override
  public Page<Project> list(GenericSpecification<Project> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Project>>() {

      @Override
      protected Page<Project> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Project> page = fullTextSearch
            ? projectSearchRepo.find(criteria, pageable, Project.class, match)
            : projectListRepo.find(criteria, pageable, Project.class, null);
        if (page.hasContent()) {
          projectMemberQuery.setMembers(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Check if a project is an agile project.
   * </p>
   * @param id Project ID
   * @return true if the project is agile, false otherwise
   */
  @Override
  public boolean isAgile(Long id) {
    return projectRepo.countAgileById(id) > 0;
  }

  /**
   * <p>
   * Check and find a project by ID, throw exception if not found.
   * </p>
   * @param id Project ID
   * @return Project entity
   */
  @Override
  public Project checkAndFind(Long id) {
    return projectRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Project"));
  }

  /**
   * <p>
   * Find projects by a set of IDs.
   * </p>
   * @param ids Set of project IDs
   * @return List of projects
   */
  @Override
  public List<Project> find0ById(Set<Long> ids) {
    return projectRepo.findAllByIdIn(ids);
  }

  /**
   * <p>
   * Check if the project quota is exceeded after increment.
   * </p>
   * @param inc Number of projects to add
   */
  @Override
  public void checkQuota(int inc) {
    long count = projectRepo.countAll0();
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterProject, null, count + inc);
  }

  /**
   * <p>
   * Check if a project name already exists when adding a new project.
   * </p>
   * @param name Project name
   */
  @Override
  public void checkAddNameExists(String name) {
    long count = projectRepo.countAll0ByName(name);
    if (count > 0) {
      throw ResourceExisted.of(PROJECT_NAME_REPEATED_T, new Object[]{name});
    }
  }

  /**
   * <p>
   * Check if a project name already exists when updating a project, excluding the current project.
   * </p>
   * @param id Project ID
   * @param name Project name
   */
  @Override
  public void checkUpdateNameExists(Long id, String name) {
    long count = projectRepo.countAll0ByNameAndIdNot(name, id);
    if (count > 0) {
      throw ResourceExisted.of(PROJECT_NAME_REPEATED_T, new Object[]{name});
    }
  }

  /**
   * <p>
   * Check if the current user has permission to modify the project.
   * </p>
   * <p>
   * Admins, project creators, and project owners have modification permissions.
   * </p>
   * @param projectDb Project entity
   */
  @Override
  public void checkModifyPermission(Project projectDb) {
    BizAssert.assertTrue(isAdmin() || getUserId().equals(projectDb.getCreatedBy())
            || getUserId().equals(projectDb.getOwnerId()),
        PROJECT_NOT_MODIFY_PERMISSION_CODE, PROJECT_NOT_MODIFY_PERMISSION_MODIFY);
  }

  /**
   * <p>
   * Check if the current user has permission to delete the project.
   * </p>
   * <p>
   * Admins, project creators, and project owners have deletion permissions.
   * </p>
   * @param projectDb Project entity
   */
  @Override
  public void checkDeletePermission(Project projectDb) {
    BizAssert.assertTrue(isAdmin() || getUserId().equals(projectDb.getCreatedBy())
            || getUserId().equals(projectDb.getOwnerId()),
        PROJECT_NOT_DELETE_PERMISSION_CODE, PROJECT_NOT_DELETE_PERMISSION);
  }

  /**
   * <p>
   * Check if the current user has permission to edit project tags.
   * </p>
   * @param projectDb Project entity
   */
  @Override
  public void checkEditTagPermission(Project projectDb) {
    BizAssert.assertTrue(hasEditPermission(projectDb),
        PROJECT_NOT_EDIT_TAG_PERMISSION_CODE, PROJECT_NOT_EDIT_TAG_PERMISSION);
  }

  /**
   * <p>
   * Check if the current user has permission to edit project modules.
   * </p>
   * @param projectDb Project entity
   */
  @Override
  public void checkEditModulePermission(Project projectDb) {
    BizAssert.assertTrue(hasEditPermission(projectDb),
        PROJECT_NOT_EDIT_MODULE_PERMISSION_CODE, PROJECT_NOT_EDIT_MODULE_PERMISSION);
  }

  /**
   * <p>
   * Check if the current user has edit permission for a project by ID.
   * </p>
   * @param projectId Project ID
   * @return true if the user has edit permission, false otherwise
   */
  @Override
  public boolean hasEditPermission(Long projectId) {
    Project projectDb = checkAndFind(projectId);
    return hasEditPermission(projectDb);
  }

  /**
   * <p>
   * Check if the current user has edit permission for a project.
   * </p>
   * <p>
   * Admins, project creators, project owners, and project members have edit permissions.
   * </p>
   * @param projectDb Project entity
   * @return true if the user has edit permission, false otherwise
   */
  @Override
  public boolean hasEditPermission(Project projectDb) {
    Long currentUserId = getUserId();
    if (currentUserId.equals(projectDb.getCreatedBy())
        || currentUserId.equals(projectDb.getOwnerId()) || isAdmin()) {
      return true;
    }
    Set<Long> memberUserIds = projectMemberQuery.findMemberUserIds(projectDb.getProjectId());
    return memberUserIds.contains(currentUserId);
  }

}
