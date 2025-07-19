package cloud.xcan.angus.core.tester.application.query.scenario.impl;

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
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTrashQuery;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrashRepo;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ScenarioTrashQueryImpl implements ScenarioTrashQuery {

  @Resource
  private ScenarioTrashRepo scenarioTrashRepo;

  @Resource
  private ScenarioTrashSearchRepo scenarioTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? scenarioTrashRepo.count()
            : scenarioTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public Page<ScenarioTrash> list(GenericSpecification<ScenarioTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ScenarioTrash>>() {

      @Override
      protected Page<ScenarioTrash> process() {
        Page<ScenarioTrash> page = fullTextSearch
            ? scenarioTrashSearchRepo.find(spec.getCriteria(), pageable, ScenarioTrash.class, match)
            : scenarioTrashRepo.findAll(spec, pageable);
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
  public ScenarioTrash findMyTrashForBiz(Long id, String biz) {
    return new BizTemplate<ScenarioTrash>() {

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
