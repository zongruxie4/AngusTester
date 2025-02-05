package cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTarget;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetExportVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class DatasetAssembler {

  public static Dataset addDtoToDomain(DatasetAddDto dto) {
    return new Dataset()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setExtracted(nonNull(dto.getExtraction()))
        .setParameters(dto.getParameters())
        .setExtraction(dto.getExtraction());
  }

  public static Dataset updateDtoToDomain(DatasetUpdateDto dto) {
    return new Dataset()
        .setId(dto.getId())
        //.setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setExtracted(nonNull(dto.getExtraction()) ? true : null)
        .setParameters(dto.getParameters())
        .setExtraction(dto.getExtraction());
  }

  public static Dataset replaceDtoToDomain(DatasetReplaceDto dto) {
    return new Dataset()
        .setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setExtracted(nonNull(dto.getExtraction()))
        .setParameters(dto.getParameters())
        .setExtraction(dto.getExtraction());
  }

  public static DatasetDetailVo toDetailVo(Dataset dataset) {
    return new DatasetDetailVo()
        .setId(dataset.getId())
        .setProjectId(dataset.getProjectId())
        .setName(dataset.getName())
        .setDescription(dataset.getDescription())
        .setExtracted(dataset.getExtracted())
        .setParameters(dataset.getParameters())
        .setExtraction(dataset.getExtraction())
        .setCreatedBy(dataset.getCreatedBy())
        .setCreatedDate(dataset.getCreatedDate())
        .setLastModifiedBy(dataset.getLastModifiedBy())
        .setLastModifiedDate(dataset.getLastModifiedDate());
  }

  public static DatasetTargetVo toTargetVo(DatasetTarget target) {
    return new DatasetTargetVo()
        .setTargetId(target.getTargetId())
        .setTargetName(target.getTargetName())
        .setTargetType(target.getTargetType())
        .setCreatedBy(target.getCreatedBy())
        .setCreatedDate(target.getCreatedDate());
  }

  public static DatasetExportVo toExportVo(Dataset dataset) {
    return new DatasetExportVo()
        .setName(dataset.getName())
        .setExtracted(dataset.getExtracted())
        .setParameters(dataset.getParameters())
        .setExtraction(dataset.getExtraction());
  }

  public static GenericSpecification<Dataset> getSpecification(DatasetFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "lastModifiedDate")
        .orderByFields("id", "name", "createdDate", "createdBy", "lastModifiedBy",
            "lastModifiedDate")
        .inAndNotFields("id", "name")
        .matchSearchFields("name")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(DatasetSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "lastModifiedDate")
        .orderByFields("id", "name", "createdDate", "createdBy", "lastModifiedBy",
            "lastModifiedDate")
        .inAndNotFields("id", "name")
        .matchSearchFields("name")
        .build();
  }

}
