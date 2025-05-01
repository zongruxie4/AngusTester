package cloud.xcan.angus.core.tester.interfaces.data.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.getResourceResponseEntity;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.lang.System.currentTimeMillis;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.data.DatasetCmd;
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetSearch;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetValuePreviewDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DatasetFacadeImpl implements DatasetFacade {

  @Resource
  private DatasetCmd datasetCmd;

  @Resource
  private DatasetQuery datasetQuery;

  @Resource
  private DatasetSearch datasetSearch;

  @Override
  public IdKey<Long, Object> add(DatasetAddDto dto) {
    return datasetCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(DatasetUpdateDto dto) {
    datasetCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(DatasetReplaceDto dto) {
    return datasetCmd.replace(replaceDtoToDomain(dto));
  }

  @Override
  public List<IdKey<Long, Object>> clone(HashSet<Long> ids) {
    return datasetCmd.clone(ids);
  }

  @Override
  public List<IdKey<Long, Object>> imports(DatasetImportDto dto) {
    return datasetCmd.imports(dto.getProjectId(), dto.getStrategyWhenDuplicated(),
        dto.getContent(), dto.getFile());
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return datasetCmd.importExample(projectId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    datasetCmd.delete(ids);
  }

  @NameJoin
  @Override
  public DatasetDetailVo detail(Long id) {
    return toDetailVo(datasetQuery.detail(id));
  }

  @Override
  public LinkedHashMap<String, List<String>> valuePreview(DatasetValuePreviewDto dto) {
    return datasetQuery.valuePreview(dto.getId(), dto.getName(), dto.getParameters(),
        dto.getExtraction(), dto.getRowNum());
  }

  @NameJoin
  @Override
  public PageResult<DatasetDetailVo> list(DatasetFindDto dto) {
    Page<Dataset> page = datasetQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, DatasetAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<DatasetDetailVo> search(DatasetSearchDto dto) {
    Page<Dataset> page = datasetSearch.search(getSearchCriteria(dto),
        dto.tranPage(), Dataset.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, DatasetAssembler::toDetailVo);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(DatasetExportDto dto,
      HttpServletResponse response) {
    ScriptFormat format = nullSafe(dto.getFormat(), ScriptFormat.YAML);
    List<Dataset> variables = datasetQuery.findByProjectAndIds(dto.getProjectId(), dto.getIds());
    return getResourceResponseEntity(String.format("ExportDatasets-%s", currentTimeMillis()),
        format, variables.stream().map(DatasetAssembler::toExportVo).collect(Collectors.toList()));
  }

}
