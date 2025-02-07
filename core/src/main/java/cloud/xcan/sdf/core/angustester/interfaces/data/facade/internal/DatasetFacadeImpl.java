package cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.getResourceResponseEntity;
import static cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler.addDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler.getSearchCriteria;
import static cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler.getSpecification;
import static cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler.replaceDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler.updateDtoToDomain;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.lang.System.currentTimeMillis;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.data.DatasetCmd;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetSearch;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptFormat;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DatasetFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetValuePreviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
  public List<IdKey<Long, Object>> exampleImport(Long projectId) {
    return datasetCmd.exampleImport(projectId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    datasetCmd.delete(ids);
  }

  @NameJoin
  @Override
  public DatasetDetailVo detail(Long id) {
    Dataset dataset = datasetQuery.detail(id);
    return DatasetAssembler.toDetailVo(dataset);
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
