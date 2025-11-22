package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.EvaluationAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.EvaluationAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.EvaluationAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.EvaluationAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.project.TestEvaluationCmd;
import cloud.xcan.angus.core.tester.application.query.project.TestEvaluationQuery;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.interfaces.project.facade.TestEvaluationFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.EvaluationAssembler;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.EvaluationDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TestEvaluationFacadeImpl implements TestEvaluationFacade {

  @Resource
  private TestEvaluationCmd evaluationCmd;

  @Resource
  private TestEvaluationQuery evaluationQuery;

  @Override
  public IdKey<Long, Object> add(EvaluationAddDto dto) {
    return evaluationCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(EvaluationUpdateDto dto) {
    evaluationCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public void generateResult(Long id) {
    evaluationCmd.generateResult(id);
  }

  @Override
  public void delete(Long id) {
    evaluationCmd.delete(id);
  }

  @NameJoin
  @Override
  public EvaluationDetailVo detail(Long id) {
    return toDetailVo(evaluationQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<EvaluationDetailVo> list(EvaluationFindDto dto) {
    Page<TestEvaluation> page = evaluationQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, EvaluationAssembler::toDetailVo);
  }
}

