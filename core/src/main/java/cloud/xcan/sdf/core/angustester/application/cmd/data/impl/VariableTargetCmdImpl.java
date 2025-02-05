package cloud.xcan.sdf.core.angustester.application.cmd.data.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.VariableTargetConverter.toDomain;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ADD_ASSOC_TARGET;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETE_ASSOC_TARGET;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.data.VariableTargetCmd;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableRepo;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTarget;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTargetRepo;
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
public class VariableTargetCmdImpl extends CommCmd<VariableTarget, Long> implements
    VariableTargetCmd {

  @Resource
  private VariableRepo variableRepo;

  @Resource
  private VariableTargetRepo variableTargetRepo;

  @Resource
  private VariableTargetQuery variableTargetQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long targetId, String targetType, Set<Long> variableIds,
      boolean saveActivity) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      final CombinedTargetType type = CombinedTargetType.valueOf(targetType);
      Long projectId;

      @Override
      protected void checkParams() {
        // Check the support ref variable resources
        ProtocolAssert.assertTrue(type.supportRefVariableResource(),
            format("Does not support referencing variable resource targetType: %s", targetType));
        // Check the only one project is allowed
        List<Long> projectIds = variableRepo.findProjectIdByIdIn(variableIds);
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only one project variable is allowed to be added");
        projectId = projectIds.get(0);
        // Check the modify permission
        variableTargetQuery.checkTargetPermission(targetId, targetType);
        // Check the target quota
        List<Long> variableIdsDb = variableTargetRepo.findByVariableIdByTargetIdAndType(
            targetId, targetType);
        variableIds.removeAll(variableIdsDb);
        if (isNotEmpty(variableIds)) {
          variableTargetQuery.checkTargetQuota(variableIdsDb.size(), targetId, type);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        if (isEmpty(variableIds)) {
          return emptyList();
        }

        // Save associations
        List<VariableTarget> variableTargets = toDomain(projectId, type, targetId, variableIds);
        List<IdKey<Long, Object>> idKeys = batchInsert(variableTargets);

        // Add activities
        if (saveActivity) {
          List<Variable> variablesDb = variableRepo.findAllById(variableIds);
          activityCmd.batchAdd(toActivities(type, variablesDb, ADD_ASSOC_TARGET));
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long targetId, String targetType, Set<Long> variableIds,
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
          List<Variable> variablesDb = variableRepo.findAllById(variableIds);
          activityCmd.batchAdd(toActivities(type.isApi() ? API : SCENARIO, variablesDb,
              DELETE_ASSOC_TARGET));
        }

        // Delete associations
        variableTargetRepo.deleteByTargetIdAndTypeAndVariableIdIn(targetId, targetType,
            variableIds);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<VariableTarget, Long> getRepository() {
    return this.variableTargetRepo;
  }

}
