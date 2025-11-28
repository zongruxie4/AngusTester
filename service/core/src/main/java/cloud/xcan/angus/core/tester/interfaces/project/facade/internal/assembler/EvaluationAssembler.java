package cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.EvaluationDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class EvaluationAssembler {

  public static TestEvaluation addDtoToDomain(EvaluationAddDto dto) {
    TestEvaluation evaluation = new TestEvaluation();
    evaluation.setName(dto.getName());
    evaluation.setProjectId(dto.getProjectId());
    evaluation.setScope(dto.getScope());
    evaluation.setPurposes(dto.getPurposes());
    evaluation.setResourceId(dto.getResourceId());
    evaluation.setStartDate(dto.getStartDate());
    evaluation.setDeadlineDate(dto.getDeadlineDate());
    return evaluation;
  }

  public static TestEvaluation updateDtoToDomain(EvaluationUpdateDto dto) {
    TestEvaluation evaluation = new TestEvaluation();
    evaluation.setId(dto.getId());
    evaluation.setProjectId(dto.getProjectId());
    evaluation.setName(dto.getName());
    evaluation.setScope(dto.getScope());
    evaluation.setPurposes(dto.getPurposes());
    evaluation.setResourceId(dto.getResourceId());
    evaluation.setStartDate(dto.getStartDate());
    evaluation.setDeadlineDate(dto.getDeadlineDate());
    return evaluation;
  }

  public static EvaluationDetailVo toDetailVo(TestEvaluation evaluation) {
    EvaluationDetailVo vo = new EvaluationDetailVo();
    vo.setId(evaluation.getId());
    vo.setName(evaluation.getName());
    vo.setProjectId(evaluation.getProjectId());
    vo.setScope(evaluation.getScope());
    vo.setPurposes(evaluation.getPurposes());
    vo.setResourceId(evaluation.getResourceId());
    vo.setResourceName(evaluation.getResourceName());
    vo.setStartDate(evaluation.getStartDate());
    vo.setDeadlineDate(evaluation.getDeadlineDate());
    vo.setResult(evaluation.getResult());
    vo.setCreatedBy(evaluation.getCreatedBy());
    vo.setCreatedDate(evaluation.getCreatedDate());
    vo.setLastModifiedBy(evaluation.getLastModifiedBy());
    vo.setLastModifiedDate(evaluation.getLastModifiedDate());
    return vo;
  }

  public static GenericSpecification<TestEvaluation> getSpecification(EvaluationFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "lastModifiedDate")
        .orderByFields("id", "name", "scope", "createdBy", "createdDate")
        .matchSearchFields("name")
        .build();
    return new GenericSpecification<>(filters);
  }
}

