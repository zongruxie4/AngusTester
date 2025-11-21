package cloud.xcan.angus.core.tester.interfaces.project.facade;

import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.TagVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface TagFacade {

  List<IdKey<Long, Object>> add(TagAddDto dto);

  void update(List<TagUpdateDto> dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  TagVo detail(Long id);

  PageResult<TagVo> list(TagFindDto findDto);

}
