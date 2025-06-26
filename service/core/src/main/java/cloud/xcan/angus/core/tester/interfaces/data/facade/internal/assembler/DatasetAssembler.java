package cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.data.ParameterizationDataSource;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTarget;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetExportVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
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
        .setDataSource(ParameterizationDataSource.of(dataset.getExtraction()))
        .setParameters(dataset.getParameters())
        .setExtraction(dataset.getExtraction())
        .setTenantId(dataset.getTenantId())
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
        .setDescription(dataset.getDescription())
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
