package cloud.xcan.angus.core.tester.interfaces.data.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasourceAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasourceAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasourceAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasourceAssembler.testDtoToDatasource;
import static cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasourceAssembler.toDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.data.DatasourceCmd;
import cloud.xcan.angus.core.tester.application.query.data.DatasourceQuery;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasourceFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceTestDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasourceAssembler;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceTestVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class DatasourceFacadeImpl implements DatasourceFacade {

  @Resource
  private DatasourceCmd datasourceCmd;

  @Resource
  private DatasourceQuery datasourceQuery;

  @Override
  public IdKey<Long, Object> add(DatasourceAddDto dto) {
    return datasourceCmd.add(addDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(DatasourceReplaceDto dto) {
    return datasourceCmd.replace(replaceDtoToDomain(dto));
  }

  @Override
  public DatasourceTestVo testById(Long id) {
    Datasource mockService = datasourceCmd.testById(id);
    return new DatasourceTestVo().setConnSuccess(mockService.getConnSuccess())
        .setConnFailureMessage(mockService.getConnFailureMessage());
  }

  @Override
  public DatasourceTestVo testByConfig(DatasourceTestDto dto) {
    Datasource mockService = datasourceCmd.testByConfig(testDtoToDatasource(dto));
    return new DatasourceTestVo().setConnSuccess(mockService.getConnSuccess())
        .setConnFailureMessage(mockService.getConnFailureMessage());
  }

  @Override
  public void delete(Long id) {
    datasourceCmd.delete(id);
  }

  @NameJoin
  @Override
  public DatasourceDetailVo detail(Long id) {
    Datasource datasource = datasourceQuery.detail(id);
    return toDetailVo(datasource);
  }

  @NameJoin
  @Override
  public PageResult<DatasourceVo> list(DatasourceFindDto dto) {
    Page<Datasource> page = datasourceQuery.find(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, DatasourceAssembler::toVo);
  }

}
