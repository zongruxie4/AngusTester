package cloud.xcan.sdf.core.angustester.application.cmd.apis.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisUnarchivedCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisUnarchivedQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaolong.liu
 */
@Slf4j
@Biz
public class ApisUnarchivedCmdImpl extends CommCmd<ApisUnarchived, Long> implements
    ApisUnarchivedCmd {

  @Resource
  private ApisUnarchivedRepo apisUnarchivedRepo;

  @Resource
  private ApisUnarchivedQuery apisUnarchivedQuery;

  /**
   * Add unarchived http or websocket apis. `Related projects and api owner are not supported.`
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<ApisUnarchived> apis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        return batchInsert(apis, "summary");
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<ApisUnarchived> apis) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        apisUnarchivedQuery.checkUpdateApiPermission(apis);
      }

      @Override
      protected Void process() {
        batchUpdateOrNotFound0(apis);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      ApisUnarchived apisUnarchived;

      @Override
      protected void checkParams() {
        apisUnarchived = apisUnarchivedQuery.checkAndFind(id);
        apisUnarchivedQuery.checkUpdateApiPermission(Collections.singletonList(apisUnarchived));
      }

      @Override
      protected Void process() {
        apisUnarchivedRepo.updateApisUnarchivedSummary(id, name);
        return null;
      }
    }.execute();
  }

  /**
   * Delete my unarchive apis.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id, boolean deleteAllFlag) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        if (deleteAllFlag) {
          apisUnarchivedRepo.deleteAllByCreatedBy(getUserId());
        } else {
          apisUnarchivedRepo.deleteByCreatedByAndId(getUserId(), id);
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ApisUnarchived, Long> getRepository() {
    return this.apisUnarchivedRepo;
  }
}
