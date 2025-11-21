package cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.project.tag.Tag;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.TagUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.TagVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class TagAssembler {

  public static Tag updateDtoToDomain(TagUpdateDto dto) {
    return new Tag().setId(dto.getId()).setName(dto.getName());
  }

  public static TagVo toListVo(Tag tag) {
    return new TagVo()
        .setId(tag.getId())
        .setName(tag.getName())
        .setProjectId(tag.getProjectId())
        .setHasEditPermission(tag.getHasEditPermission())
        .setCreatedBy(tag.getCreatedBy())
        .setCreatedDate(tag.getCreatedDate());
  }

  public static GenericSpecification<Tag> getSpecification(TagFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        //.subTableFields("shelfClassification")
        .matchSearchFields("name")
        .orderByFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
