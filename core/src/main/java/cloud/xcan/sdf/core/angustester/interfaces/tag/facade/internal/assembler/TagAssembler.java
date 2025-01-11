package cloud.xcan.sdf.core.angustester.interfaces.tag.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto.TagUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.tag.facade.vo.TagVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
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

  public static Set<SearchCriteria> getSearchCriteria(TagSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name")
        .orderByFields("id", "createdDate")
        .build();
  }

}
