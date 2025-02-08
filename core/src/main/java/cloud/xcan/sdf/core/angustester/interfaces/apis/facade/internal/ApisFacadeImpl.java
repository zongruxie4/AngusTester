package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisSearch;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisMoveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.mock.ApisAssocMockApisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisInfoListVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.mock.ApisAssocMockApiVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisScopeDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesApisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesApisInfoListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApisFacadeImpl implements ApisFacade {

  @Resource
  private ApisCmd apisCmd;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisSearch apisSearch;

  @Override
  public List<IdKey<Long, Object>> archive(List<ApisArchiveDto> dtos) {
    List<Apis> apis = dtos.stream().map(ApisAssembler::archiveDtoToDomain)
        .collect(Collectors.toList());
    return apisCmd.archive(apis);
  }

  @Override
  public void update(List<ApisUpdateDto> dto) {
    List<Apis> apis = dto.stream().map(ApisAssembler::updateDtoToDomain)
        .collect(Collectors.toList());
    apisCmd.update(apis, true);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<ApisReplaceDto> dto) {
    List<Apis> apis = dto.stream().map(ApisAssembler::replaceDtoToDomain)
        .collect(Collectors.toList());
    return apisCmd.replace(apis);
  }

  @Override
  public void rename(Long id, String name) {
    apisCmd.rename(id, name);
  }

  @Override
  public void move(ApisMoveDto dto) {
    apisCmd.move(dto.getApiIds(), dto.getTargetServiceId());
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return apisCmd.clone(id);
  }

  @Override
  public void delete(Collection<Long> ids) {
    apisCmd.delete(ids, true);
  }

  @Override
  public void statusUpdate(Long id, ApiStatus status) {
    apisCmd.statusUpdate(id, status);
  }

  @Override
  public void serverReplace(Long id, Server server) {
    apisCmd.serverReplace(id, server);
  }

  @Override
  public void serverReplaceAll(Long id, List<Server> servers) {
    apisCmd.serverReplaceAll(id, servers);
  }

  @Override
  public void serverDelete(Long id, Set<String> urls) {
    apisCmd.serverDelete(id, urls);
  }

  @Override
  public void addParameters(Long serviceId, ServiceApisScopeDto dto,
      List<Parameter> parameters) {
    apisCmd.addParameters(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), parameters);
  }

  @Override
  public void updateParameters(Long serviceId, ServiceApisScopeDto dto,
      List<Parameter> parameters) {
    apisCmd.updateParameters(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), parameters);
  }

  @Override
  public void deleteParameters(Long serviceId, ServiceApisScopeDto dto,
      List<String> names) {
    apisCmd.deleteParameters(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), names);
  }

  @Override
  public void enableParameters(Long serviceId, ServiceApisScopeDto dto,
      List<String> names,
      Boolean enabled) {
    apisCmd.enableParameters(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), names, enabled);
  }

  @Override
  public void updateAuth(Long serviceId, ServiceApisScopeDto dto,
      SecurityScheme authentication) {
    apisCmd.updateAuth(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), authentication);
  }

  @Override
  public void updateServer(Long serviceId, ServiceApisScopeDto dto, Server server) {
    apisCmd.updateServer(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), server);
  }

  @Override
  public void addVariableReference(Long serviceId, ServiceApisScopeDto dto,
      List<String> variableNames) {
    apisCmd.addVariableReference(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), variableNames);
  }

  @Override
  public void deleteVariableReference(Long serviceId, ServiceApisScopeDto dto,
      List<String> variableNames) {
    apisCmd.deleteVariableReference(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), variableNames);
  }

  @Override
  public void addDatasetReference(Long serviceId, ServiceApisScopeDto dto,
      List<String> datasetNames) {
    apisCmd.addDatasetReference(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), datasetNames);
  }

  @Override
  public void deleteDatasetReference(Long serviceId, ServiceApisScopeDto dto,
      List<String> datasetNames) {
    apisCmd.deleteDatasetReference(serviceId, dto.getScope(), dto.getMatchEndpointRegex(),
        dto.getMatchMethod(), dto.getSelectedApisIds(), dto.getFilterTags(), datasetNames);
  }

  @Override
  public List<Server> serverList(Long id) {
    return apisQuery.serverList(id);
  }

  @NameJoin
  @Override
  public ApisDetailVo detail(Long id, Boolean resolveRefFlag) {
    return ApisAssembler.toDetailVo(apisQuery.detail(id, resolveRefFlag));
  }

  @NameJoin
  @Override
  public ApisAssocMockApiVo assocMockApis(Long id) {
    MockApis mockApis = apisQuery.findMockApis(id);
    return isNull(mockApis) ? null : ApisAssembler.toAssocMockApis(mockApis);
  }

  @Override
  public IdKey<Long, Object> assocMockApisAdd(Long id, ApisAssocMockApisAddDto dto) {
    return apisCmd.assocMockApisAdd(id, dto.getMockServiceId(), dto.getSummary());
  }

  @Override
  public String openapiDetail(Long id, ApisSchemaOpenApiDto dto) {
    return apisQuery.openapiDetail(id, dto.getFormat(), dto.getGzipCompression(), false);
  }

  @Override
  public void check(Long id) {
    apisQuery.check(id);
  }

  @NameJoin
  @Override
  public List<ApisDetailVo> listDetail(HashSet<Long> ids, Boolean resolveRefFlag) {
    return apisQuery.listDetail(ids, resolveRefFlag).stream()
        .map(ApisAssembler::toDetailVo).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public PageResult<ServicesApisInfoListVo> listApis(Long serviceId, ServiceApisFindDto dto) {
    Page<ApisBasicInfo> apisPage = apisQuery.findByServiceId(serviceId,
        ApisAssembler.getSpecification(dto), dto.tranPage(), ApisBasicInfo.class);
    return buildVoPageResult(apisPage, ApisAssembler::toProjectApisPageVo);
  }

  @NameJoin
  @Override
  public PageResult<ServicesApisInfoListVo> searchApis(Long serviceId, ServicesApisSearchDto dto) {
    Page<ApisBasicInfo> apisPage = apisSearch.searchByServiceId(serviceId,
        ApisAssembler.getSearchCriteria(dto), dto.tranPage(), getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(apisPage, ApisAssembler::toProjectApisPageVo);
  }

  @NameJoin
  @Override
  public PageResult<ApisInfoListVo> list(ApisInfoFindDto dto) {
    Page<ApisBasicInfo> apisPage = apisQuery.list(ApisAssembler.getSpecification(dto),
        dto.tranPage(), ApisBasicInfo.class);
    return buildVoPageResult(apisPage, ApisAssembler::toApisPageVo);
  }

  @NameJoin
  @Override
  public PageResult<ApisInfoListVo> search(ApisInfoSearchDto dto) {
    Page<ApisBasicInfo> apisPage = apisSearch.search(ApisAssembler.getSearchCriteria(dto),
        dto.tranPage(), getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(apisPage, ApisAssembler::toApisPageVo);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      Long id, ApisExportDto dto, HttpServletResponse response) {
    return apisCmd.export(id, dto.getFormat(), response);
  }

}
