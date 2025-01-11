package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthDeptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthGroupDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthUserDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
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
    return new ScenarioAuth().setScenarioId(id).setCreatorFlag(false)
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
    authVo.setCreatorFlag(scenarioAuth.getCreatorFlag());
    authVo.setScenarioId(scenarioAuth.getScenarioId());
    return authVo;
  }

  public static ScenarioAuthCurrentVo toAuthCurrentVo(ScenarioAuthCurrent authCurrent) {
    return new ScenarioAuthCurrentVo()
        .setScenarioAuthFlag(authCurrent.isScenarioAuthFlag())
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
