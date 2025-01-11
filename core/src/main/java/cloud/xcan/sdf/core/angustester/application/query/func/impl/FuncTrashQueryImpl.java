package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncTrashQuery;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrashRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import javax.annotation.Resource;

@Biz
public class FuncTrashQueryImpl implements FuncTrashQuery {

  @Resource
  private FuncTrashRepo funcTrashRepo;

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? funcTrashRepo.count()
            : funcTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public FuncTrash findMyTrashForBiz(Long id, String biz) {
    FuncTrash trashDb = funcTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "FuncTrash"));
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