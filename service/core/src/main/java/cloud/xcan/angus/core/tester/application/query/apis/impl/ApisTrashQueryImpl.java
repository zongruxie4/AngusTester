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

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? apisTrashRepo.count()
            : apisTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public Page<ApisTrash> list(GenericSpecification<ApisTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisTrash>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisTrash> process() {
        Page<ApisTrash> page = fullTextSearch
            ? apisTrashSearchRepo.find(spec.getCriteria(), pageable, ApisTrash.class, match)
            : apisTrashRepo.findAll(spec, pageable);

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

  @Override
  public ApisTrash findMyTrashForBiz(Long id, String biz) {
    ApisTrash trashDb = apisTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ApisTrash"));
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
