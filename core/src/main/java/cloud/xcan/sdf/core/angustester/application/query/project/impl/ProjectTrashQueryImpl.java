package cloud.xcan.sdf.core.angustester.application.query.project.impl;

import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectTrashQuery;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrashRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import javax.annotation.Resource;

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