package cloud.xcan.angus.core.tester.interfaces.tag.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.tag.facade.internal.assembler.TagAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.tag.facade.internal.assembler.TagAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.tag.facade.internal.assembler.TagAssembler.toListVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.tag.TagCmd;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagSearch;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.TagFacade;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagAddDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagFindDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagSearchDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.dto.TagUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.internal.assembler.TagAssembler;
import cloud.xcan.angus.core.tester.interfaces.tag.facade.vo.TagVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return tagCmd.importExample(projectId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    tagCmd.delete(ids);
  }

  @Override
  public TagVo detail(Long id) {
    return toListVo(tagQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<TagVo> list(TagFindDto dto) {
    Page<Tag> page = tagQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TagAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<TagVo> search(TagSearchDto dto) {
    Page<Tag> page = tagSearch.search(getSearchCriteria(dto),
        dto.tranPage(), Tag.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TagAssembler::toListVo);
  }

}
