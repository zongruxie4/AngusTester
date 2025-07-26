package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.ProjectTrashQuery;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashRepo;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of ProjectTrashQuery for project trash management and query operations.
 * </p>
 * <p>
 * Provides methods for listing deleted projects, counting trash items, and validating trash operation permissions.
 * </p>
 */
@Biz
public class ProjectTrashQueryImpl implements ProjectTrashQuery {

  @Resource
  private ProjectTrashRepo projectTrashRepo;
  @Resource
  private ProjectTrashSearchRepo projectTrashSearchRepo;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Count the total number of projects in trash.
   * </p>
   * @return Number of projects in trash
   */
  @Override
  public Long count() {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return projectTrashRepo.count();
      }
    }.execute();
  }

  /**
   * <p>
   * List projects in trash with optional full-text search.
   * </p>
   * <p>
   * Sets user name and avatar information for both created by and deleted by users.
   * </p>
   * @param spec Project trash search specification
   * @param pageable Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match Full-text search keywords
   * @return Page of project trash items
   */
  @Override
  public Page<ProjectTrash> list(GenericSpecification<ProjectTrash> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ProjectTrash>>() {

      @Override
      protected Page<ProjectTrash> process() {
        Page<ProjectTrash> page = fullTextSearch
            ? projectTrashSearchRepo.find(spec.getCriteria(), pageable, ProjectTrash.class, match)
            : projectTrashRepo.findAll(spec, pageable);

        if (!page.isEmpty()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
              "createdByAvatar");
          userManager.setUserNameAndAvatar(page.getContent(), "deletedBy", "deletedByName",
              "deletedByAvatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Find a project trash item for business operations with permission validation.
   * </p>
   * <p>
   * Admins can perform any operation. Regular users can only perform operations on items they deleted.
   * </p>
   * @param id Project trash ID
   * @param biz Business operation type ("BACK" or "CLEAR")
   * @return Project trash entity
   */
  @Override
  public ProjectTrash findMyTrashForBiz(Long id, String biz) {
    ProjectTrash trashDb = projectTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ProjectTrash"));
    if (!isAdmin() && ObjectUtils.notEqual(trashDb.getDeletedBy(), getUserId())) {
      if ("BACK".equals(biz)) {
        throw BizException.of(TRASH_NO_BACK_PERMISSION_CODE, TRASH_NO_BACK_PERMISSION);
      } else if ("CLEAR".equals(biz)) {
        throw BizException.of(TRASH_NO_CLEAR_PERMISSION_CODE, TRASH_NO_CLEAR_PERMISSION);
      }
    }
    return trashDb;
  }
}
