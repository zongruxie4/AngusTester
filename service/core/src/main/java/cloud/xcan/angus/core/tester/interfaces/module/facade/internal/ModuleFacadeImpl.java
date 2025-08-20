package cloud.xcan.angus.core.tester.interfaces.module.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.module.facade.internal.assembler.ModuleAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.module.facade.internal.assembler.ModuleAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.module.facade.internal.assembler.ModuleAssembler.toListVo;
import static cloud.xcan.angus.core.tester.interfaces.module.facade.internal.assembler.ModuleAssembler.toTree;

import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.module.ModuleCmd;
import cloud.xcan.angus.core.tester.application.query.module.ModuleQuery;
import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.core.tester.interfaces.module.facade.ModuleFacade;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleAddDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleFindDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.internal.assembler.ModuleAssembler;
import cloud.xcan.angus.core.tester.interfaces.module.facade.vo.ModuleTreeVo;
import cloud.xcan.angus.core.tester.interfaces.module.facade.vo.ModuleVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ModuleFacadeImpl implements ModuleFacade {

  @Resource
  private ModuleCmd moduleCmd;

  @Resource
  private ModuleQuery moduleQuery;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public List<IdKey<Long, Object>> add(ModuleAddDto dto) {
    return moduleCmd.add(dto.getProjectId(), addDtoToDomain(dto));
  }

  @Override
  public void update(List<ModuleUpdateDto> dto) {
    List<Module> modules = dto.stream()
        .map(ModuleAssembler::updateDtoToDomain).toList();
    moduleCmd.update(modules);
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return moduleCmd.importExample(projectId);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<ModuleReplaceDto> dto) {
    List<Module> modules = dto.stream()
        .map(ModuleAssembler::replaceDtoToDomain).toList();
    return moduleCmd.replace(modules);
  }

  @Override
  public void delete(Collection<Long> ids) {
    moduleCmd.delete(ids);
  }

  @Override
  public ModuleVo detail(Long id) {
    return toListVo(moduleQuery.detail(id));
  }

  @NameJoin
  @Override
  public List<ModuleVo> list(ModuleFindDto dto) {
    List<Module> modules = moduleQuery.find(getSpecification(dto),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return modules.stream().map(ModuleAssembler::toListVo).toList();
  }

  @Override
  public List<ModuleTreeVo> tree(ModuleFindDto dto) {
    List<Module> modules = moduleQuery.find(getSpecification(dto),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    joinSupplier.execute(() -> modules);
    return toTree(modules);
  }

}
