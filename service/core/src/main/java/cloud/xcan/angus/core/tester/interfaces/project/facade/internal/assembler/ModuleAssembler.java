package cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.project.module.Module;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ModuleAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ModuleFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ModuleReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ModuleUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.ModuleTreeVo;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.ModuleVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.TreeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModuleAssembler {

  public static List<Module> addDtoToDomain(ModuleAddDto dto) {
    return dto.getNames().stream()
        .map(x -> new Module().setProjectId(dto.getProjectId())
            .setPid(isNull(dto.getPid()) || dto.getPid() < 1 ? DEFAULT_ROOT_PID : dto.getPid())
            .setSequence(nullSafe(dto.getSequence(), (int) (currentTimeMillis() / 1000)))
            .setName(x)).toList();
  }

  public static Module updateDtoToDomain(ModuleUpdateDto dto) {
    return new Module().setId(dto.getId())
        .setPid(dto.getPid())
        .setSequence(dto.getSequence())
        .setName(dto.getName());
  }

  public static Module replaceDtoToDomain(ModuleReplaceDto dto) {
    return new Module().setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setPid(isNull(dto.getPid()) || dto.getPid() < 1 ? DEFAULT_ROOT_PID : dto.getPid())
        .setSequence(nullSafe(dto.getSequence(), (int) (currentTimeMillis() / 1000)))
        .setName(dto.getName());
  }

  public static ModuleVo toListVo(Module module) {
    return new ModuleVo()
        .setId(module.getId())
        .setName(module.getName())
        .setProjectId(module.getProjectId())
        .setPid(module.getPid())
        .setSequence(module.getSequence())
        .setHasEditPermission(module.getHasEditPermission())
        .setCreatedBy(module.getCreatedBy())
        .setCreatedDate(module.getCreatedDate());
  }

  public static GenericSpecification<Module> getSpecification(ModuleFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        //.subTableFields("shelfClassification")
        .matchSearchFields("name")
        .orderByFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static List<ModuleTreeVo> toTree(List<Module> modules) {
    if (isEmpty(modules)) {
      return null;
    }
    List<ModuleTreeVo> vos = new ArrayList<>();
    for (Module appFunc : modules) {
      vos.add(toAppFuncTreeVo(appFunc));
    }
    // Use database sort
    return TreeUtils.toTree(vos, true);
  }

  public static ModuleTreeVo toAppFuncTreeVo(Module module) {
    return isNull(module) ? null : new ModuleTreeVo()
        .setProjectId(module.getProjectId())
        .setId(module.getId())
        .setName(module.getName())
        .setPid(module.getPid())
        .setSequence(module.getSequence())
        .setHasEditPermission(module.getHasEditPermission())
        .setHit(module.getHit())
        .setCreatedBy(module.getCreatedBy())
        .setCreator(module.getCreator());
  }
}
