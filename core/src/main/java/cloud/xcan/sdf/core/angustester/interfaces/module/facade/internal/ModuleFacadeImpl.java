package cloud.xcan.sdf.core.angustester.interfaces.module.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;

import cloud.xcan.sdf.core.angustester.application.cmd.module.ModuleCmd;
import cloud.xcan.sdf.core.angustester.application.query.module.ModuleQuery;
import cloud.xcan.sdf.core.angustester.application.query.module.ModuleSearch;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.ModuleFacade;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.internal.assembler.ModuleAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo.ModuleTreeVo;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo.ModuleVo;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ModuleFacadeImpl implements ModuleFacade {

  @Resource
  private ModuleCmd moduleCmd;

  @Resource
  private ModuleQuery moduleQuery;

  @Resource
  private ModuleSearch moduleSearch;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public List<IdKey<Long, Object>> add(ModuleAddDto dto) {
    return moduleCmd.add(dto.getProjectId(), ModuleAssembler.addDtoToDomain(dto));
  }

  @Override
  public void update(List<ModuleUpdateDto> dto) {
    List<Module> modules = dto.stream()
        .map(ModuleAssembler::updateDtoToDomain).collect(Collectors.toList());
    moduleCmd.update(modules);
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return moduleCmd.importExample(projectId);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<ModuleReplaceDto> dto) {
    List<Module> modules = dto.stream()
        .map(ModuleAssembler::replaceDtoToDomain).collect(Collectors.toList());
    return moduleCmd.replace(modules);
  }

  @Override
  public void delete(Collection<Long> ids) {
    moduleCmd.delete(ids);
  }

  @Override
  public ModuleVo detail(Long id) {
    Module module = moduleQuery.detail(id);
    return ModuleAssembler.toListVo(module);
  }

  @NameJoin
  @Override
  public List<ModuleVo> list(ModuleFindDto dto) {
    List<Module> modules = moduleQuery.find(ModuleAssembler.getSpecification(dto));
    return modules.stream().map(ModuleAssembler::toListVo).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<ModuleVo> search(ModuleSearchDto dto) {
    List<Module> modules = moduleSearch.search(ModuleAssembler.getSearchCriteria(dto),
        Module.class, getMatchSearchFields(dto.getClass()));
    return modules.stream().map(ModuleAssembler::toListVo).collect(Collectors.toList());
  }

  @Override
  public List<ModuleTreeVo> tree(ModuleFindDto dto) {
    List<Module> modules = moduleQuery.find(ModuleAssembler.getSpecification(dto));
    joinSupplier.execute(() -> modules);
    return ModuleAssembler.toTree(modules);
  }

  @Override
  public List<ModuleTreeVo> treeSearch(ModuleSearchDto dto) {
    List<Module> modules = moduleSearch.search(ModuleAssembler.getSearchCriteria(dto),
        Module.class, getMatchSearchFields(dto.getClass()));
    joinSupplier.execute(() -> modules);
    return ModuleAssembler.toTree(modules);
  }

}
