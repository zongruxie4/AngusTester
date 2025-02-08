package cloud.xcan.sdf.core.angustester.interfaces.module.facade;

import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo.ModuleTreeVo;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo.ModuleVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface ModuleFacade {

  List<IdKey<Long, Object>> add(ModuleAddDto dto);

  List<IdKey<Long, Object>> replace(List<ModuleReplaceDto> dto);

  void update(List<ModuleUpdateDto> dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  ModuleVo detail(Long id);

  List<ModuleVo> list(ModuleFindDto findDto);

  List<ModuleVo> search(ModuleSearchDto dto);

  List<ModuleTreeVo> tree(ModuleFindDto dto);

  List<ModuleTreeVo> treeSearch(ModuleSearchDto dto);

}
