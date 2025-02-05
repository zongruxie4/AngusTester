package cloud.xcan.sdf.core.angustester.application.query.data.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API_CASE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.BaseTarget;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableRepo;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTarget;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

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

  @Override
  public List<Variable> findVariables(Long targetId, String targetType) {
    return new BizTemplate<List<Variable>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<Variable> process() {
        List<Long> variableIds = variableTargetRepo.findByVariableIdByTargetIdAndType(
            targetId, targetType);
        return variableIds.isEmpty() ? emptyList() : variableRepo.findAllById(variableIds);
      }
    }.execute();
  }

  @Override
  public Map<Long, List<Variable>> findVariables(Collection<Long> targetIds, String targetType,
      Map<Long, Long> caseApiMap) {
    return new BizTemplate<Map<Long, List<Variable>>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Map<Long, List<Variable>> process() {
        Map<Long, List<Variable>> targetVariablesMap = new HashMap<>();
        List<VariableTarget> variableTargets = variableTargetRepo.findByTargetIdInAndType(
            targetIds, targetType);
        // Set apis variable to cases
        if (API_CASE.getValue().equals(targetType) && isNotEmpty(caseApiMap)) {
          Map<Long, List<VariableTarget>> apisVariableTargetMap = variableTargetRepo.findByTargetIdInAndType(
                  new HashSet<>(caseApiMap.values()), CombinedTargetType.API.getValue()).stream()
              .collect(Collectors.groupingBy(VariableTarget::getTargetId));
          if (isNotEmpty(apisVariableTargetMap)) {
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
                      }).collect(Collectors.toList()));
                }
              }
            }
          }
        }
        if (variableTargets.isEmpty()) {
          return targetVariablesMap;
        }
        List<Variable> variables = variableRepo.findAllById(
            variableTargets.stream().map(VariableTarget::getVariableId)
                .collect(Collectors.toSet()));
        if (variables.isEmpty()) {
          return targetVariablesMap;
        }
        Map<Long, List<Long>> caseVariableIdsMap = variableTargets.stream()
            .collect(Collectors.groupingBy(VariableTarget::getTargetId,
                Collectors.mapping(VariableTarget::getVariableId, Collectors.toList())));
        for (VariableTarget vt : variableTargets) {
          List<Long> variableIds = caseVariableIdsMap.get(vt.getTargetId());
          if (isEmpty(variableIds)) {
            targetVariablesMap.put(vt.getTargetId(),
                variables.stream().filter(x -> variableIds.contains(x.getId()))
                    .collect(Collectors.toList()));
          }
        }
        return targetVariablesMap;
      }
    }.execute();
  }

  @Override
  public List<VariableTarget> findTargets(Long variableId) {
    return new BizTemplate<List<VariableTarget>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<VariableTarget> process() {
        List<VariableTarget> targets = variableTargetRepo.findByVariableId(variableId);
        setTargetName(targets);
        return targets;
      }
    }.execute();
  }

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return new BizTemplate<Map<String, String>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Map<String, String> process() {
        List<Long> variableIds = variableTargetRepo.findByVariableIdByTargetIdAndType(
            targetId, targetType);
        return isEmpty(variableIds) ? emptyMap() : variableQuery.valuePreview(variableIds);
      }
    }.execute();
  }

  @Override
  public Set<Long> findVariableIdByServiceId(Long serviceId) {
    Set<Long> targetIds = new HashSet<>();
    targetIds.addAll(apisBaseInfoRepo.findIdByServiceId(serviceId));
    targetIds.addAll(apisCaseInfoRepo.findIdByServiceId(serviceId));
    return isEmpty(targetIds) ? emptySet()
        : variableTargetRepo.findByVariableIdByTargetId(targetIds);
  }

  @Override
  public void checkTargetPermission(Long targetId, String targetType) {
    CombinedTargetType type = CombinedTargetType.valueOf(targetType);
    if (type.isApi()) {
      apisAuthQuery.checkModifyAuth(getUserId(), targetId);
    } else if (type.isScenario()) {
      scenarioAuthQuery.checkModifyAuth(getUserId(), targetId);
    }
  }

  @Override
  public void checkTargetQuota(int inc, Long targetId, CombinedTargetType targetType) {
    long count = variableTargetRepo.countByTargetIdAndTargetType(targetId, targetType);
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterTargetVariable,
        Collections.singleton(targetId), count + inc);
  }

  @Override
  public void setTargetName(List<? extends BaseTarget> targets) {
    if (isEmpty(targets)) {
      return;
    }
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
