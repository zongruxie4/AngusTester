package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioTrashQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrashRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import javax.annotation.Resource;

@Biz
public class ScenarioTrashQueryImpl implements ScenarioTrashQuery {

  @Resource
  private ScenarioTrashRepo scenarioTrashRepo;

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? scenarioTrashRepo.count()
            : scenarioTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public ScenarioTrash findMyTrashForBiz(Long id, String biz) {
    return new BizTemplate<ScenarioTrash>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ScenarioTrash process() {
        ScenarioTrash trashDb = scenarioTrashRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "ScenarioTrash"));
        if (!isAdmin() && ObjectUtils.notEqual(trashDb.getDeletedBy(), getUserId())) {
          if ("BACK".equals(biz)) {
            throw BizException.of(TRASH_NO_BACK_PERMISSION_CODE, TRASH_NO_BACK_PERMISSION);
          } else if ("CLEAR".equals(biz)) {
            throw BizException.of(TRASH_NO_CLEAR_PERMISSION_CODE, TRASH_NO_CLEAR_PERMISSION);
          }
        }
        return trashDb;
      }
    }.execute();
  }


}
