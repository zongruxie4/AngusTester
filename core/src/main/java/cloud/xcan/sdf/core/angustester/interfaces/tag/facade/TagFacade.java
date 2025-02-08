package cloud.xcan.sdf.core.angustester.interfaces.tag.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.vo.TagVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface TagFacade {

  List<IdKey<Long, Object>> add(TagAddDto dto);

  void update(List<TagUpdateDto> dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  TagVo detail(Long id);

  PageResult<TagVo> list(TagFindDto findDto);

  PageResult<TagVo> search(TagSearchDto dto);

}
