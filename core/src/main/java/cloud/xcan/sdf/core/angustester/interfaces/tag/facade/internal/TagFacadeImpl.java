package cloud.xcan.sdf.core.angustester.interfaces.tag.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagCmd;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagSearch;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.TagFacade;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.internal.assembler.TagAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.vo.TagVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TagFacadeImpl implements TagFacade {

  @Resource
  private TagCmd tagCmd;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private TagSearch tagSearch;

  @Override
  public List<IdKey<Long, Object>> add(TagAddDto dto) {
    return tagCmd.add(dto.getProjectId(), dto.getNames());
  }

  @Override
  public void update(List<TagUpdateDto> dto) {
    List<Tag> appTags = dto.stream().map(TagAssembler::updateDtoToDomain)
        .collect(Collectors.toList());
    tagCmd.update(appTags);
  }

  @Override
  public void delete(Collection<Long> ids) {
    tagCmd.delete(ids);
  }

  @Override
  public TagVo detail(Long id) {
    Tag tag = tagQuery.detail(id);
    return TagAssembler.toListVo(tag);
  }

  @NameJoin
  @Override
  public PageResult<TagVo> list(TagFindDto dto) {
    Page<Tag> page = tagQuery.find(TagAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TagAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<TagVo> search(TagSearchDto dto) {
    Page<Tag> page = tagSearch.search(TagAssembler.getSearchCriteria(dto),
        dto.tranPage(), Tag.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TagAssembler::toListVo);
  }

}
