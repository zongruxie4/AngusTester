package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal.assembler;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.AnalysisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo.AnalysisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo.AnalysisListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class AnalysisAssembler {

  public static Analysis toAddDomain(AnalysisAddDto dto) {
    return new Analysis().setProjectId(dto.getProjectId())
        .setResource(dto.getResource()).setTemplate(dto.getTemplate())
        .setName(dto.getName()).setDescription(dto.getDescription())
        .setObject(dto.getObject()).setPlanId(dto.getPlanId())
        .setOrgType(dto.isAnalysisOrgTargetMissing() ? AuthObjectType.USER : dto.getOrgType())
        .setOrgId(dto.isAnalysisOrgTargetMissing() ? getUserId() : dto.getOrgId())
        .setContainsUserAnalysis(nullSafe(dto.getContainsUserAnalysis(), true))
        .setContainsDataDetail(nullSafe(dto.getContainsDataDetail(), true))
        .setTimeRange(dto.getTimeRange()).setStartTime(dto.getStartTime())
        .setEndTime(dto.getEndTime())
        .setDatasource(dto.getDatasource()).setFilterCriteria(dto.getFilterCriteria());
  }

  public static Analysis toUpdateDomain(AnalysisUpdateDto dto) {
    return new Analysis().setId(dto.getId())
        .setName(dto.getName()).setDescription(dto.getDescription())
        .setObject(dto.getObject()).setPlanId(dto.getPlanId())
        .setOrgType(dto.getOrgType()).setOrgId(dto.getOrgId())
        .setContainsUserAnalysis(nullSafe(dto.getContainsUserAnalysis(), true))
        .setContainsDataDetail(nullSafe(dto.getContainsDataDetail(), true))
        .setTimeRange(dto.getTimeRange()).setStartTime(dto.getStartTime())
        .setEndTime(dto.getEndTime())
        .setDatasource(dto.getDatasource()).setFilterCriteria(dto.getFilterCriteria());
  }

  public static Analysis toReplaceDomain(AnalysisReplaceDto dto) {
    return new Analysis().setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setResource(isNull(dto.getId()) ? dto.getResource() : null)
        .setTemplate(isNull(dto.getId()) ? dto.getTemplate() : null)
        .setName(dto.getName()).setDescription(dto.getDescription())
        .setObject(dto.getObject()).setPlanId(dto.getPlanId())
        .setOrgType(dto.isAnalysisOrgTargetMissing() ? AuthObjectType.USER : dto.getOrgType())
        .setOrgId(dto.isAnalysisOrgTargetMissing() ? getUserId() : dto.getOrgId())
        .setContainsUserAnalysis(nullSafe(dto.getContainsUserAnalysis(), true))
        .setContainsDataDetail(nullSafe(dto.getContainsDataDetail(), true))
        .setTimeRange(dto.getTimeRange()).setStartTime(dto.getStartTime())
        .setEndTime(dto.getEndTime())
        .setDatasource(dto.getDatasource()).setFilterCriteria(dto.getFilterCriteria());
  }

  public static AnalysisDetailVo toDetail(Analysis analysis) {
    return new AnalysisDetailVo().setId(analysis.getId())
        .setProjectId(analysis.getProjectId())
        .setResource(analysis.getResource())
        .setTemplate(analysis.getTemplate())
        .setName(analysis.getName()).setDescription(analysis.getDescription())
        .setObject(analysis.getObject()).setPlanId(analysis.getPlanId())
        .setOrgType(analysis.getOrgType()).setOrgId(analysis.getOrgId())
        .setContainsUserAnalysis(analysis.getContainsUserAnalysis())
        .setContainsDataDetail(analysis.getContainsDataDetail())
        .setTimeRange(analysis.getTimeRange()).setStartTime(analysis.getStartTime())
        .setEndTime(analysis.getEndTime())
        .setDatasource(analysis.getDatasource()).setFilterCriteria(analysis.getFilterCriteria())
        .setData(analysis.getDataObj())
        .setCreatedBy(analysis.getCreatedBy()).setCreatedDate(analysis.getCreatedDate())
        .setLastModifiedBy(analysis.getLastModifiedBy())
        .setLastModifiedDate(analysis.getLastModifiedDate());
  }

  public static AnalysisListVo toListVo(Analysis analysis) {
    return new AnalysisListVo().setId(analysis.getId())
        .setProjectId(analysis.getProjectId())
        .setResource(analysis.getResource())
        .setTemplate(analysis.getTemplate())
        .setName(analysis.getName()).setDescription(analysis.getDescription())
        .setObject(analysis.getObject()).setPlanId(analysis.getPlanId())
        .setOrgType(analysis.getOrgType()).setOrgId(analysis.getOrgId())
        .setContainsUserAnalysis(analysis.getContainsUserAnalysis())
        .setContainsDataDetail(analysis.getContainsDataDetail())
        .setTimeRange(analysis.getTimeRange()).setStartTime(analysis.getStartTime())
        .setEndTime(analysis.getEndTime())
        .setDatasource(analysis.getDatasource()).setFilterCriteria(analysis.getFilterCriteria())
        .setCreatedBy(analysis.getCreatedBy()).setCreatedDate(analysis.getCreatedDate())
        .setLastModifiedBy(analysis.getLastModifiedBy())
        .setLastModifiedDate(analysis.getLastModifiedDate());
  }

  public static GenericSpecification<Analysis> getSpecification(AnalysisFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "createdBy", "createdDate", "lastModifiedBy", "category",
            "template")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(AnalysisSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "createdBy", "createdDate", "lastModifiedBy", "category",
            "template")
        .matchSearchFields("name", "description")
        .build();
  }

}
