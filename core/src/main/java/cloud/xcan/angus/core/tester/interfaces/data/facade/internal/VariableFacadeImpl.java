package cloud.xcan.angus.core.tester.interfaces.data.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.getResourceResponseEntity;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.lang.System.currentTimeMillis;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.data.VariableCmd;
import cloud.xcan.angus.core.tester.application.query.data.VariableQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableSearch;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.core.tester.interfaces.data.facade.VariableFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableValuePreviewDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class VariableFacadeImpl implements VariableFacade {

  @Resource
  private VariableCmd variableCmd;

  @Resource
  private VariableQuery variableQuery;

  @Resource
  private VariableSearch variableSearch;

  @Override
  public IdKey<Long, Object> add(VariableAddDto dto) {
    return variableCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(VariableUpdateDto dto) {
    variableCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(VariableReplaceDto dto) {
    return variableCmd.replace(replaceDtoToDomain(dto));
  }

  @Override
  public List<IdKey<Long, Object>> clone(HashSet<Long> ids) {
    return variableCmd.clone(ids);
  }

  @Override
  public List<IdKey<Long, Object>> imports(VariableImportDto dto) {
    return variableCmd.imports(dto.getProjectId(), dto.getStrategyWhenDuplicated(),
        dto.getContent(), dto.getFile());
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return variableCmd.importExample(projectId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    variableCmd.delete(ids);
  }

  @NameJoin
  @Override
  public VariableDetailVo detail(Long id) {
    Variable variable = variableQuery.detail(id);
    return VariableAssembler.toDetailVo(variable);
  }

  @Override
  public String valuePreview(VariableValuePreviewDto dto) {
    return variableQuery.valuePreview(dto.getId(), dto.getName(), dto.getValue(),
        dto.getExtraction());
  }

  @NameJoin
  @Override
  public PageResult<VariableDetailVo> list(VariableFindDto dto) {
    Page<Variable> page = variableQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, VariableAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<VariableDetailVo> search(VariableSearchDto dto) {
    Page<Variable> page = variableSearch.search(getSearchCriteria(dto),
        dto.tranPage(), Variable.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, VariableAssembler::toDetailVo);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(VariableExportDto dto,
      HttpServletResponse response) {
    ScriptFormat format = nullSafe(dto.getFormat(), ScriptFormat.YAML);
    List<Variable> variables = variableQuery.findByProjectAndIds(dto.getProjectId(), dto.getIds());
    return getResourceResponseEntity(String.format("ExportVariables-%s", currentTimeMillis()),
        format, variables.stream().map(VariableAssembler::toExportVo).collect(Collectors.toList()));
  }
}
