package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioTrashCmd;
import cloud.xcan.angus.core.tester.application.converter.ActivityConverter;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTrashQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrashRepo;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ScenarioTrashCmdImpl extends CommCmd<ScenarioTrash, Long> implements ScenarioTrashCmd {

  @Resource
  private ScenarioTrashRepo scenarioTrashRepo;

  @Resource
  private ScenarioTrashQuery scenarioTrashQuery;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private ScenarioCmd scenarioCmd;

  @Resource
  private ActivityCmd activityCmd;

  @Override
  public void add0(List<ScenarioTrash> trashes) {
    batchInsert0(trashes);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clear(Long id) {
    new BizTemplate<Void>() {
      ScenarioTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = scenarioTrashQuery.findMyTrashForBiz(id, "CLEAR");
      }

      @Override
      protected Void process() {
        // Delete trash
        scenarioTrashRepo.deleteById(id);

        // Delete association data
        deleteAssociation(singletonList(trashDb.getTargetId()));

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clearAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<ScenarioTrash> allTrashes = getAllTrashesByProjectId(projectId);

        if (isEmpty(allTrashes)) {
          return null;
        }

        List<Long> scenarioIds = allTrashes.stream()
            .map(ScenarioTrash::getTargetId).collect(Collectors.toList());

        // Delete all trash
        scenarioTrashRepo.deleteByTargetIdIn(scenarioIds);

        // Delete association data
        deleteAssociation(scenarioIds);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void back(Long id) {
    new BizTemplate<Void>() {
      ScenarioTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = scenarioTrashQuery.findMyTrashForBiz(id, "BACK");
      }

      @Override
      protected Void process() {
        // Update undeleted scenario status
        scenarioRepo.updateToUndeletedStatusByIdIn(Collections.singleton(trashDb.getTargetId()));
        // Delete apis trash
        scenarioTrashRepo.deleteById(id);
        // Add back scenario activity
        activityCmd.add(ActivityConverter.toActivity(CombinedTargetType.SCENARIO, trashDb,
            ActivityType.BACK, trashDb.getName()));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<ScenarioTrash> allTrashes = getAllTrashesByProjectId(projectId);

        // Bach scenario
        if (ObjectUtils.isNotEmpty(allTrashes)) {
          List<Long> scenarioIds = allTrashes.stream()
              .map(ScenarioTrash::getTargetId).collect(Collectors.toList());

          // Update undeleted scenario status
          scenarioRepo.updateToUndeletedStatusByIdIn(scenarioIds);

          // Delete trash
          scenarioTrashRepo.deleteByTargetIdIn(scenarioIds);
        }
        return null;
      }
    }.execute();
  }

  private void deleteAssociation(List<Long> scenarioIds) {
    if (isNotEmpty(scenarioIds)) {
      scenarioCmd.delete0(scenarioIds);
    }
  }

  private List<ScenarioTrash> getAllTrashesByProjectId(Long projectId) {
    Long currentUserId = getUserId();
    List<ScenarioTrash> trashDbs;
    if (isNull(projectId)) {
      trashDbs = isAdmin() ? scenarioTrashRepo.findAll()
          : scenarioTrashRepo.findByCreatedBy(currentUserId);
    } else {
      trashDbs = isAdmin() ? scenarioTrashRepo.findByProjectId(projectId)
          : scenarioTrashRepo.findByProjectIdAndCreatedBy(projectId, currentUserId);
    }
    return trashDbs;
  }

  @Override
  protected BaseRepository<ScenarioTrash, Long> getRepository() {
    return this.scenarioTrashRepo;
  }
}
