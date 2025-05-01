package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncPlanCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanSearch;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncPlanFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.plan.FuncPlanDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncPlanFacadeImpl implements FuncPlanFacade {

  @Resource
  private FuncPlanCmd funcPlanCmd;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncPlanSearch funcPlanSearch;

  @Override
  public IdKey<Long, Object> add(FuncPlanAddDto dto) {
    return funcPlanCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(FuncPlanUpdateDto dto) {
    funcPlanCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(FuncPlanReplaceDto dto) {
    return funcPlanCmd.replace(replaceDtoToDomain(dto));
  }

  @Override
  public void start(Long id) {
    funcPlanCmd.start(id);
  }

  @Override
  public void end(Long id) {
    funcPlanCmd.end(id);
  }

  @Override
  public void block(Long id) {
    funcPlanCmd.block(id);
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return funcPlanCmd.clone(id);
  }

  @Override
  public void resultReset(HashSet<Long> ids) {
    funcPlanCmd.resultReset(ids);
  }

  @Override
  public void reviewReset(HashSet<Long> ids) {
    funcPlanCmd.reviewReset(ids);
  }

  @Override
  public void delete(Long id) {
    funcPlanCmd.delete(id);
  }

  @NameJoin
  @Override
  public FuncPlanDetailVo detail(Long id) {
    FuncPlan funcPlan = funcPlanQuery.detail(id);
    return toDetailVo(funcPlan);
  }

  @NameJoin
  @Override
  public PageResult<FuncPlanDetailVo> list(FuncPlanFindDto dto) {
    Page<FuncPlan> page = funcPlanQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, FuncPlanAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncPlanDetailVo> search(FuncPlanSearchDto dto) {
    Page<FuncPlan> page = funcPlanSearch.search(getSearchCriteria(dto),
        dto.tranPage(), FuncPlan.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncPlanAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public List<FuncCaseListVo> notReviewed(Long planId, Long moduleId, Long reviewId) {
    List<FuncCaseInfo> caseInfos = funcPlanQuery.notReviewed(planId, moduleId, reviewId);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<FuncCaseListVo> notEstablishedBaseline(Long planId, Long moduleId, Long baselineId) {
    List<FuncCaseInfo> caseInfos = funcPlanQuery.notEstablishedBaseline(
        planId, moduleId, baselineId);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
  }
}
