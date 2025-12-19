package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisUnarchivedCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisUnarchivedQuery;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of ApisUnarchivedCmd for unarchived API management and command operations.
 * </p>
 * <p>
 * Provides comprehensive unarchived API management services including adding, updating, renaming,
 * and deleting unarchived APIs. Handles permission checks and batch operations. Manages temporary
 * API storage for users who have unarchived APIs from external sources.
 * </p>
 */
@Slf4j
@Service
public class ApisUnarchivedCmdImpl extends CommCmd<ApisUnarchived, Long> implements
    ApisUnarchivedCmd {

  @Resource
  private ApisUnarchivedRepo apisUnarchivedRepo;
  @Resource
  private ApisUnarchivedQuery apisUnarchivedQuery;

  /**
   * <p>
   * Add unarchived APIs in batch.
   * </p>
   * <p>
   * Inserts unarchived APIs, does not support related projects and API owner.
   * </p>
   *
   * @param apis List of unarchived APIs to add
   * @return List of ID keys for created APIs
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<ApisUnarchived> apis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {

      @Override
      protected List<IdKey<Long, Object>> process() {
        for (ApisUnarchived api : apis) {
          api.setId(BIDUtils.getId(BIDKey.apisId));
        }
        // Batch insert unarchived APIs with summary validation
        return batchInsert(apis, "summary");
      }
    }.execute();
  }

  /**
   * <p>
   * Update unarchived APIs in batch.
   * </p>
   * <p>
   * Validates permission and updates unarchived APIs.
   * </p>
   *
   * @param apis List of unarchived APIs to update
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<ApisUnarchived> apis) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // Verify current user has permission to update the APIs
        apisUnarchivedQuery.checkUpdateApiPermission(apis);
      }

      @Override
      protected Void process() {
        // Batch update APIs (throw exception if not found)
        batchUpdateOrNotFound0(apis);
        return null;
      }
    }.execute();
  }

  /**
   * Rename an unarchived API.
   * <p>
   * Validates permission and updates the name.
   */
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
   * Delete unarchived APIs.
   * <p>
   * Deletes all or a single unarchived API for the current user.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id, boolean deleteAll) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        if (deleteAll) {
          apisUnarchivedRepo.deleteAllByCreatedBy(getUserId());
        } else {
          apisUnarchivedRepo.deleteByCreatedByAndId(getUserId(), id);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Get the repository for ApisUnarchived entity.
   * <p>
   *
   * @return the ApisUnarchivedRepo instance
   */
  @Override
  protected BaseRepository<ApisUnarchived, Long> getRepository() {
    return this.apisUnarchivedRepo;
  }
}
