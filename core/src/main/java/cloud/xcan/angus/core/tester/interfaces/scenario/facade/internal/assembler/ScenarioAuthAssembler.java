package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthCurrent;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthDeptDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthGroupDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthUserDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class ScenarioAuthAssembler {

  public static ScenarioAuth addDtoToDomain(Long id, ScenarioAuthAddDto dto) {
    Set<ScenarioPermission> permissions = new HashSet<>();
    permissions.add(ScenarioPermission.VIEW);
    if (ObjectUtils.isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new ScenarioAuth().setScenarioId(id).setCreator(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static ScenarioAuth replaceDtoToDomain(Long id, ScenarioAuthReplaceDto dto) {
    dto.getPermissions().add(ScenarioPermission.VIEW);
    return new ScenarioAuth().setId(id).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static ScenarioAuthVo toDetailVo(ScenarioAuth scenarioAuth) {
    ScenarioAuthVo authVo;
    switch (scenarioAuth.getAuthObjectType()) {
      case USER:
        authVo = new ScenarioAuthUserDetailVo();
        break;
      case GROUP:
        authVo = new ScenarioAuthGroupDetailVo();
        break;
      case DEPT:
        authVo = new ScenarioAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(scenarioAuth.getAuthObjectType().getMessage());
    }
    authVo.setId(scenarioAuth.getId());
    authVo.setPermissions(scenarioAuth.getAuths());
    authVo.setAuthObjectType(scenarioAuth.getAuthObjectType());
    authVo.setAuthObjectId(scenarioAuth.getAuthObjectId());
    authVo.setCreator(scenarioAuth.getCreator());
    authVo.setScenarioId(scenarioAuth.getScenarioId());
    return authVo;
  }

  public static ScenarioAuthCurrentVo toAuthCurrentVo(ScenarioAuthCurrent authCurrent) {
    return new ScenarioAuthCurrentVo()
        .setScenarioAuth(authCurrent.isScenarioAuth())
        .setPermissions(authCurrent.getPermissions());
  }

  public static Specification<ScenarioAuth> getSpecification(ScenarioAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "scenarioId", "authObjectId", "createdDate")
        .orderByFields("id", "scenarioId", "authObjectId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
