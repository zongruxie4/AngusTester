package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTrashQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrashRepo;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API trash query operations for deleted API management.
 *
 * <p>This class provides comprehensive functionality for querying and managing
 * deleted APIs in trash, including trash listing, pagination, search, and restoration.</p>
 *
 * <p>It handles deleted API lifecycle management, permission validation,
 * and user-specific trash access with proper security controls.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>API trash listing and count queries with pagination</li>
 *   <li>Full-text search capabilities for trash content</li>
 *   <li>Project member permission validation</li>
 *   <li>User-specific trash access control</li>
 *   <li>User information enrichment for creators and deleters</li>
 *   <li>Business operation permission validation (back/clear)</li>
 *   <li>Admin privilege handling for trash operations</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
public class ApisTrashQueryImpl implements ApisTrashQuery {

  @Resource
  private ApisTrashRepo apisTrashRepo;
  @Resource
  private ApisTrashSearchRepo apisTrashSearchRepo;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private UserManager userManager;

  /**
   * Counts deleted APIs in trash with optional project filtering.
   *
   * <p>This method returns the total number of deleted APIs in trash,
   * optionally filtered by project ID.</p>
   *
   * @param projectId the project ID to filter count by (can be null for all projects)
   * @return the total number of deleted APIs in trash
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        // Count trash items with or without project filtering
        return isNull(projectId) ? apisTrashRepo.count()
            : apisTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  /**
   * Lists deleted APIs in trash with pagination, filtering, and optional full-text search.
   *
   * <p>This method retrieves deleted APIs from trash based on specification criteria
   * with support for pagination and optional full-text search capabilities.</p>
   *
   * <p>The method validates project member permissions and enriches trash data
   * with user information for enhanced display.</p>
   *
   * @param spec           the specification for filtering trash items
   * @param pageable       the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the full-text search match fields
   * @return a page of deleted APIs in trash with enriched data
   */
  @Override
  public Page<ApisTrash> list(GenericSpecification<ApisTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisTrash>>() {
      @Override
      protected void checkParams() {
        // Verify user has project member permissions
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisTrash> process() {
        // Execute search with full-text or standard search
        Page<ApisTrash> page = fullTextSearch
            ? apisTrashSearchRepo.find(spec.getCriteria(), pageable, ApisTrash.class, match)
            : apisTrashRepo.findAll(spec, pageable);

        if (!page.isEmpty()) {
          // Enrich trash data with creator and deleter user information
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
   * Finds trash item for business operations with permission validation.
   *
   * <p>This method retrieves a trash item and validates user permissions
   * for specific business operations (BACK or CLEAR).</p>
   *
   * <p>The method enforces that only the user who deleted the item or
   * admin users can perform business operations on trash items.</p>
   *
   * @param id  the trash item ID to find
   * @param biz the business operation type (BACK or CLEAR)
   * @return the trash item if found and authorized
   * @throws ResourceNotFound if the trash item is not found
   * @throws BizException     if user lacks permission for the business operation
   */
  @Override
  public ApisTrash findMyTrashForBiz(Long id, String biz) {
    // Retrieve trash item or throw not found exception
    ApisTrash trashDb = apisTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ApisTrash"));

    // Validate user permissions for business operations
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
