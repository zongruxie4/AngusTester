package cloud.xcan.angus.core.tester.application.query.data.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API_CASE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.domain.BaseTarget;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTarget;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of variable target query operations for variable-target associations.
 *
 * <p>This class provides functionality for managing relationships between
 * variables and their target entities, including API cases and scenarios.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Variable retrieval by target associations</li>
 *   <li>Target retrieval by variable associations</li>
 *   <li>Value preview for target-associated variables</li>
 *   <li>Target quota validation for variable associations</li>
 *   <li>Support for API case to API variable inheritance</li>
 *   <li>Target name enrichment and permission validation</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
public class VariableTargetQueryImpl implements VariableTargetQuery {

  @Resource
  private VariableTargetRepo variableTargetRepo;
  @Resource
  private VariableRepo variableRepo;
  @Resource
  private VariableQuery variableQuery;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;
  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;
  @Resource
  private ApisCaseInfoRepo apisCaseInfoRepo;
  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Finds variables associated with a specific target.
   *
   * <p>This method retrieves all variables that are associated with the
   * specified target ID and type.</p>
   *
   * @param targetId the target entity ID
   * @param targetType the target entity type
   * @return list of variables associated with the target
   */
  @Override
  public List<Variable> findVariables(Long targetId, String targetType) {
    return new BizTemplate<List<Variable>>() {

      @Override
      protected List<Variable> process() {
        // Retrieve variable IDs associated with the target
        List<Long> variableIds = variableTargetRepo.findByVariableIdByTargetIdAndType(
            targetId, targetType);
        return variableIds.isEmpty() ? emptyList() : variableRepo.findAllById(variableIds);
      }
    }.execute();
  }

  /**
   * Finds variables associated with multiple targets, including API case inheritance.
   *
   * <p>This method retrieves variables for multiple targets, with special handling
   * for API cases that inherit variables from their associated APIs.</p>
   *
   * @param targetIds collection of target IDs
   * @param targetType the target entity type
   * @param caseApiMap mapping of case IDs to API IDs for inheritance
   * @return map of target IDs to their associated variables
   */
  @Override
  public Map<Long, List<Variable>> findVariables(Collection<Long> targetIds, String targetType,
      Map<Long, Long> caseApiMap) {
    return new BizTemplate<Map<Long, List<Variable>>>() {

      @Override
      protected Map<Long, List<Variable>> process() {
        Map<Long, List<Variable>> targetVariablesMap = new HashMap<>();
        List<VariableTarget> variableTargets = variableTargetRepo.findByTargetIdInAndType(
            targetIds, targetType);

        // Handle API case inheritance from associated APIs
        if (API_CASE.getValue().equals(targetType) && isNotEmpty(caseApiMap)) {
          // Retrieve variables associated with the APIs
          Map<Long, List<VariableTarget>> apisVariableTargetMap = variableTargetRepo.findByTargetIdInAndType(
                  new HashSet<>(caseApiMap.values()), CombinedTargetType.API.getValue()).stream()
              .collect(Collectors.groupingBy(VariableTarget::getTargetId));

          if (isNotEmpty(apisVariableTargetMap)) {
            // Create inherited variable targets for cases
            for (Long caseId : targetIds) {
              if (caseApiMap.containsKey(caseId)) {
                List<VariableTarget> targets = apisVariableTargetMap.get(caseApiMap.get(caseId));
                if (isNotEmpty(targets)) {
                  variableTargets.addAll(targets.stream().map(x -> {
                        VariableTarget target = new VariableTarget();
                        target.setTargetId(caseId);
                        target.setTargetType(API_CASE);
                        target.setVariableId(x.getVariableId());
                        return target;
                      }).toList());
                }
              }
            }
          }
        }

        if (variableTargets.isEmpty()) {
          return targetVariablesMap;
        }

        // Retrieve all variables and build the result map
        List<Variable> variables = variableRepo.findAllById(
            variableTargets.stream().map(VariableTarget::getVariableId)
                .collect(Collectors.toSet()));
        if (variables.isEmpty()) {
          return targetVariablesMap;
        }

        // Group variable IDs by target ID
        Map<Long, List<Long>> caseVariableIdsMap = variableTargets.stream()
            .collect(Collectors.groupingBy(VariableTarget::getTargetId,
                Collectors.mapping(VariableTarget::getVariableId, Collectors.toList())));

        // Build final result map
        for (VariableTarget vt : variableTargets) {
          List<Long> variableIds = caseVariableIdsMap.get(vt.getTargetId());
          if (isEmpty(variableIds)) {
            targetVariablesMap.put(vt.getTargetId(),
                variables.stream().filter(x -> variableIds.contains(x.getId()))
                    .toList());
          }
        }
        return targetVariablesMap;
      }
    }.execute();
  }

  /**
   * Finds targets associated with a specific variable.
   *
   * <p>This method retrieves all targets that are associated with the
   * specified variable, including target name enrichment.</p>
   *
   * @param variableId the variable ID
   * @return list of targets associated with the variable
   */
  @Override
  public List<VariableTarget> findTargets(Long variableId) {
    return new BizTemplate<List<VariableTarget>>() {

      @Override
      protected List<VariableTarget> process() {
        List<VariableTarget> targets = variableTargetRepo.findByVariableId(variableId);
        // Enrich targets with target names
        setTargetName(targets);
        return targets;
      }
    }.execute();
  }

  /**
   * Generates value preview for variables associated with a target.
   *
   * <p>This method retrieves variables associated with a target and generates
   * a preview of their values for validation and testing purposes.</p>
   *
   * @param targetId the target entity ID
   * @param targetType the target entity type
   * @return map of variable names to preview values
   */
  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return new BizTemplate<Map<String, String>>() {

      @Override
      protected Map<String, String> process() {
        // Retrieve variable IDs associated with the target
        List<Long> variableIds = variableTargetRepo.findByVariableIdByTargetIdAndType(
            targetId, targetType);
        return isEmpty(variableIds) ? emptyMap() : variableQuery.valuePreview(variableIds);
      }
    }.execute();
  }

  /**
   * Finds variable IDs associated with a service.
   *
   * <p>This method discovers variables associated with APIs and API cases
   * that belong to the specified service.</p>
   *
   * @param serviceId the service ID
   * @return set of variable IDs associated with the service
   */
  @Override
  public Set<Long> findVariableIdByServiceId(Long serviceId) {
    Set<Long> targetIds = new HashSet<>();
    // Collect target IDs from APIs and API cases in the service
    targetIds.addAll(apisBaseInfoRepo.findIdByServiceId(serviceId));
    targetIds.addAll(apisCaseInfoRepo.findIdByServiceId(serviceId));
    return isEmpty(targetIds) ? emptySet()
        : variableTargetRepo.findByVariableIdByTargetId(targetIds);
  }

  /**
   * Validates target permission for variable operations.
   *
   * <p>This method checks if the current user has modification permissions
   * for the specified target based on its type.</p>
   *
   * @param targetId the target ID
   * @param targetType the target type
   * @throws BizException if the user lacks modification permissions
   */
  @Override
  public void checkTargetPermission(Long targetId, String targetType) {
    CombinedTargetType type = CombinedTargetType.valueOf(targetType);
    if (type.isApi()) {
      apisAuthQuery.checkModifyAuth(getUserId(), targetId);
    } else if (type.isScenario()) {
      scenarioAuthQuery.checkModifyAuth(getUserId(), targetId);
    }
  }

  /**
   * Validates target quota for variable associations.
   *
   * <p>This method checks if the target has sufficient quota to create
   * additional variable associations.</p>
   *
   * @param inc the increment amount to check
   * @param targetId the target ID
   * @param targetType the target type
   * @throws QuotaException if the quota limit would be exceeded
   */
  @Override
  public void checkTargetQuota(int inc, Long targetId, CombinedTargetType targetType) {
    long count = variableTargetRepo.countByTargetIdAndTargetType(targetId, targetType);
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterTargetVariable,
        Collections.singleton(targetId), count + inc);
  }

  /**
   * Enriches target names for a list of base targets.
   *
   * <p>This method retrieves and sets target names for various target types
   * including APIs, API cases, and scenarios.</p>
   *
   * @param targets list of base targets to enrich with names
   */
  @Override
  public void setTargetName(List<? extends BaseTarget> targets) {
    if (isEmpty(targets)) {
      return;
    }

    // Enrich API target names
    Set<Long> apisIds = targets.stream().filter(x -> x.getTargetType().isApi())
        .map(BaseTarget::getTargetId).collect(Collectors.toSet());
    if (isNotEmpty(apisIds)) {
      Map<Long, ApisBaseInfo> apisMap = apisBaseInfoRepo.findByIdIn(apisIds).stream()
          .collect(Collectors.toMap(ApisBaseInfo::getId, x -> x));
      for (BaseTarget target : targets) {
        if (apisMap.containsKey(target.getTargetId())) {
          target.setTargetName(apisMap.get(target.getTargetId()).getName());
        }
      }
    }

    // Enrich API case target names
    Set<Long> apisCaseIds = targets.stream().filter(x -> x.getTargetType().isApiCase())
        .map(BaseTarget::getTargetId).collect(Collectors.toSet());
    if (isNotEmpty(apisCaseIds)) {
      Map<Long, ApisCaseInfo> caseMap = apisCaseInfoRepo.findAll0ByIdIn(apisCaseIds).stream()
          .collect(Collectors.toMap(ApisCaseInfo::getId, x -> x));
      for (BaseTarget target : targets) {
        if (caseMap.containsKey(target.getTargetId())) {
          target.setTargetName(caseMap.get(target.getTargetId()).getName());
        }
      }
    }

    // Enrich scenario target names
    Set<Long> sceIds = targets.stream().filter(x -> x.getTargetType().isScenario())
        .map(BaseTarget::getTargetId).collect(Collectors.toSet());
    if (isNotEmpty(sceIds)) {
      Map<Long, Scenario> sceMap = scenarioRepo.findByIdIn(sceIds).stream()
          .collect(Collectors.toMap(Scenario::getId, x -> x));
      for (BaseTarget target : targets) {
        if (sceMap.containsKey(target.getTargetId())) {
          target.setTargetName(sceMap.get(target.getTargetId()).getName());
        }
      }
    }
  }

}
