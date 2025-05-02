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
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ProjectQueryImpl implements ProjectQuery {

  @Resource
  private ProjectRepo projectRepo;

  @Resource
  private ProjectListRepo projectListRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  @Override
  public List<Project> userJoined(Long userId, @Nullable String name) {
    return new BizTemplate<List<Project>>() {

      @Override
      protected List<Project> process() {
        List<Long> orgIds = userManager.getValidOrgAndUserIds(userId);
        List<Project> projects = projectRepo.findMemberProjects(orgIds);
        return isEmpty(name) ? projects : projects.stream().filter(x -> x.getName().contains(name))
            .collect(Collectors.toList());
      }
    }.execute();
  }

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

  @Override
  public Page<Project> find(GenericSpecification<Project> spec, Pageable pageable) {
    return new BizTemplate<Page<Project>>() {

      @Override
      protected Page<Project> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Project> projectPage = projectListRepo.find(criteria, pageable, Project.class, null);
        if (projectPage.hasContent()) {
          projectMemberQuery.setMembers(projectPage.getContent());
        }
        return projectPage;
      }
    }.execute();
  }

  @Override
  public boolean isAgile(Long id){
    return projectRepo.countAgileById(id) > 0;
  }

  @Override
  public Project checkAndFind(Long id) {
    return projectRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Project"));
  }

  @Override
  public List<Project> find0ById(Set<Long> ids) {
    return projectRepo.findAllByIdIn(ids);
  }

  @Override
  public void checkQuota(int inc) {
    long count = projectRepo.countAll0();
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterProject, null, count + inc);
  }

  @Override
  public void checkAddNameExists(String name) {
    long count = projectRepo.countAll0ByName(name);
    if (count > 0) {
      throw ResourceExisted.of(PROJECT_NAME_REPEATED_T, new Object[]{name});
    }
  }

  @Override
  public void checkUpdateNameExists(Long id, String name) {
    long count = projectRepo.countAll0ByNameAndIdNot(name, id);
    if (count > 0) {
      throw ResourceExisted.of(PROJECT_NAME_REPEATED_T, new Object[]{name});
    }
  }

  @Override
  public void checkModifyPermission(Project projectDb) {
    BizAssert.assertTrue(isAdmin() || getUserId().equals(projectDb.getCreatedBy())
            || getUserId().equals(projectDb.getOwnerId()),
        PROJECT_NOT_MODIFY_PERMISSION_CODE, PROJECT_NOT_MODIFY_PERMISSION_MODIFY);
  }

  @Override
  public void checkDeletePermission(Project projectDb) {
    BizAssert.assertTrue(isAdmin() || getUserId().equals(projectDb.getCreatedBy())
            || getUserId().equals(projectDb.getOwnerId()),
        PROJECT_NOT_DELETE_PERMISSION_CODE, PROJECT_NOT_DELETE_PERMISSION);
  }

  @Override
  public void checkEditTagPermission(Project projectDb) {
    BizAssert.assertTrue(hasEditPermission(projectDb),
        PROJECT_NOT_EDIT_TAG_PERMISSION_CODE, PROJECT_NOT_EDIT_TAG_PERMISSION);
  }

  @Override
  public void checkEditModulePermission(Project projectDb) {
    BizAssert.assertTrue(hasEditPermission(projectDb),
        PROJECT_NOT_EDIT_MODULE_PERMISSION_CODE, PROJECT_NOT_EDIT_MODULE_PERMISSION);
  }

  @Override
  public boolean hasEditPermission(Long projectId) {
    Project projectDb = checkAndFind(projectId);
    return hasEditPermission(projectDb);
  }

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
