package cloud.xcan.sdf.core.angustester.application.cmd.data.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.DatasetTargetConverter.toDomain;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ADD_ASSOC_TARGET;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETE_ASSOC_TARGET;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.emptyList;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.data.DatasetTargetCmd;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetTargetQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetRepo;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTarget;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class DatasetTargetCmdImpl extends CommCmd<DatasetTarget, Long> implements
    DatasetTargetCmd {

  @Resource
  private DatasetRepo datasetRepo;

  @Resource
  private DatasetTargetRepo datasetTargetRepo;

  @Resource
  private DatasetTargetQuery datasetTargetQuery;

  @Resource
  private VariableTargetQuery variableTargetQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long targetId, String targetType, Set<Long> datasetIds,
      boolean saveActivity) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      final CombinedTargetType type = CombinedTargetType.valueOf(targetType);
      Long projectId;

      @Override
      protected void checkParams() {
        // Check the only one project is allowed
        List<Long> projectIds = datasetRepo.findProjectIdByIdIn(datasetIds);
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only one project variable is allowed to be added");
        projectId = projectIds.get(0);
        // Check the modify permission
        variableTargetQuery.checkTargetPermission(targetId, targetType);
        // Check the target quota
        List<Long> datasetIdsDb = datasetTargetRepo.findByDatasetIdByTargetIdAndType(
            targetId, targetType);
        datasetIds.removeAll(datasetIdsDb);
        if (isNotEmpty(datasetIds)) {
          datasetTargetQuery.checkTargetQuota(datasetIdsDb.size(), targetId, type);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        if (isEmpty(datasetIds)) {
          return emptyList();
        }

        // Save associations
        List<IdKey<Long, Object>> idKeys = batchInsert(
            toDomain(projectId, type, targetId, datasetIds));

        // Add activities
        if (saveActivity) {
          List<Dataset> datasetsDb = datasetRepo.findAllById(datasetIds);
          activityCmd.batchAdd(toActivities(type.isApi() ? API : SCENARIO, datasetsDb,
              ADD_ASSOC_TARGET));
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long targetId, String targetType, Set<Long> datasetIds,
      boolean saveActivity) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // Check the modify permission
        variableTargetQuery.checkTargetPermission(targetId, targetType);
      }

      @Override
      protected Void process() {
        CombinedTargetType type = CombinedTargetType.valueOf(targetType);
        // Add activities
        if (saveActivity) {
          List<Dataset> datasetsDb = datasetRepo.findAllById(datasetIds);
          activityCmd.batchAdd(toActivities(type.isApi() ? API : SCENARIO, datasetsDb,
              DELETE_ASSOC_TARGET));
        }

        // Delete associations
        datasetTargetRepo.deleteByTargetIdAndTypeAndDatasetIdIn(targetId, targetType, datasetIds);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<DatasetTarget, Long> getRepository() {
    return this.datasetTargetRepo;
  }

}
