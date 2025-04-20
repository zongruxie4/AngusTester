package cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.data.ParameterizationDataSource;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTarget;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableExportVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableTargetVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class VariableAssembler {

  public static Variable addDtoToDomain(VariableAddDto dto) {
    return new Variable()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setExtracted(nonNull(dto.getExtraction()))
        .setValue(dto.getValue())
        .setPasswordValue(dto.getPasswordValue())
        .setExtraction(dto.getExtraction());
  }

  public static Variable updateDtoToDomain(VariableUpdateDto dto) {
    return new Variable()
        .setId(dto.getId())
        //.setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setExtracted(nonNull(dto.getExtraction()) ? true : null)
        .setValue(dto.getValue())
        .setPasswordValue(dto.getPasswordValue())
        .setExtraction(dto.getExtraction());
  }

  public static Variable replaceDtoToDomain(VariableReplaceDto dto) {
    return new Variable()
        .setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setExtracted(nonNull(dto.getExtraction()))
        .setValue(dto.getValue())
        .setPasswordValue(dto.getPasswordValue())
        .setExtraction(dto.getExtraction());
  }

  public static VariableExportVo toExportVo(Variable variable) {
    return new VariableExportVo()
        .setName(variable.getName())
        .setExtracted(variable.getExtracted())
        .setValue(variable.getValue())
        .setDescription(variable.getDescription())
        .setPasswordValue(variable.getPasswordValue())
        .setExtraction(variable.getExtraction());
  }

  public static VariableDetailVo toDetailVo(Variable variable) {
    return new VariableDetailVo()
        .setId(variable.getId())
        .setProjectId(variable.getProjectId())
        .setName(variable.getName())
        .setDescription(variable.getDescription())
        .setExtracted(variable.getExtracted())
        .setDataSource(ParameterizationDataSource.of(variable.getExtraction()))
        .setValue(variable.getValue())
        .setPasswordValue(variable.getPasswordValue())
        .setExtraction(variable.getExtraction())
        .setCreatedBy(variable.getCreatedBy())
        .setCreatedDate(variable.getCreatedDate())
        .setLastModifiedBy(variable.getLastModifiedBy())
        .setLastModifiedDate(variable.getLastModifiedDate());
  }

  public static VariableTargetVo toTargetVo(VariableTarget target) {
    return new VariableTargetVo()
        .setTargetId(target.getTargetId())
        .setTargetName(target.getTargetName())
        .setTargetType(target.getTargetType())
        .setCreatedBy(target.getCreatedBy())
        .setCreatedDate(target.getCreatedDate());
  }

  public static GenericSpecification<Variable> getSpecification(VariableFindDto dto) {
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

  public static Set<SearchCriteria> getSearchCriteria(VariableSearchDto dto) {
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
