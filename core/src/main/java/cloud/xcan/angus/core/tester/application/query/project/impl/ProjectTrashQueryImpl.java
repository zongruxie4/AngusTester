package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.core.tester.application.query.project.ProjectTrashQuery;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;

@Biz
public class ProjectTrashQueryImpl implements ProjectTrashQuery {

  @Resource
  private ProjectTrashRepo projectTrashRepo;

  @Override
  public Long count() {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return projectTrashRepo.count();
      }
    }.execute();
  }

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
