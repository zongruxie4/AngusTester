package cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectSearchDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class ProjectAssembler {

  public static Project addDtoToDomain(ProjectAddDto dto) {
    return new Project()
        .setType(nullSafe(dto.getType(), ProjectType.DEFAULT))
        .setName(dto.getName())
        .setAvatar(dto.getAvatar())
        .setDescription(dto.getDescription())
        .setOwnerId(dto.getOwnerId())
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setDeleted(false)
        .setMemberTypeIds(dto.getMemberTypeIds())
        .setImportExample(dto.isImportExample());
  }

  public static Project updateDtoToDomain(ProjectUpdateDto dto) {
    return new Project()
        .setId(dto.getId())
        .setName(dto.getName())
        .setAvatar(dto.getAvatar())
        .setDescription(dto.getDescription())
        .setOwnerId(dto.getOwnerId())
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setDeleted(false)
        .setMemberTypeIds(dto.getMemberTypeIds());
  }

  public static Project replaceDtoToDomain(ProjectReplaceDto dto) {
    return new Project()
        .setId(dto.getId())
        .setType(isNull(dto.getId()) ? nullSafe(dto.getType(), ProjectType.DEFAULT) : null)
        .setName(dto.getName())
        .setAvatar(dto.getAvatar())
        .setDescription(dto.getDescription())
        .setImportExample(isNull(dto.getId()) && dto.isImportExample())
        .setOwnerId(dto.getOwnerId())
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setDeleted(false)
        .setMemberTypeIds(dto.getMemberTypeIds());
  }

  public static ProjectDetailVo toDetailVo(Project project) {
    return new ProjectDetailVo()
        .setId(project.getId())
        .setName(project.getName())
        .setType(project.getType())
        .setAvatar(project.getAvatar())
        .setDescription(project.getDescription())
        .setOwnerId(project.getOwnerId())
        .setStartDate(project.getStartDate())
        .setDeadlineDate(project.getDeadlineDate())
        .setMembers(project.getMembers())
        .setCreatedBy(project.getCreatedBy())
        .setCreatedDate(project.getCreatedDate())
        .setLastModifiedBy(project.getLastModifiedBy())
        .setLastModifiedDate(project.getLastModifiedDate());
  }

  public static GenericSpecification<Project> getSpecification(ProjectFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id")
        .orderByFields("id", "name", "createdBy", "createdDate")
        .matchSearchFields("name")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ProjectSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id")
        .orderByFields("id", "name", "createdBy", "createdDate")
        .matchSearchFields("name", "description")
        .build();
  }

}




