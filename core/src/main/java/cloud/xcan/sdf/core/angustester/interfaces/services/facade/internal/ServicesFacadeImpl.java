package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler.ServicesAssembler.toVo;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesCmd;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSearch;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockServiceAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler.ServicesAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServicesFacadeImpl implements ServicesFacade {

  @Resource
  private ServicesCmd servicesCmd;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ServicesSearch servicesSearch;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Override
  public IdKey<Long, Object> add(ServicesAddDto dto) {
    return servicesCmd.add(ServicesAssembler.addDtoToDomain(dto));
  }

  @Override
  public void rename(Long id, String name) {
    servicesCmd.rename(id, name.trim());
  }

  @Override
  public void clone(Long id) {
    servicesCmd.clone(id);
  }

  @Override
  public void statusUpdate(Long id, ApiStatus status) {
    servicesCmd.statusUpdate(id, status);
  }

  @Override
  public IdKey<Long, Object> imports(ServicesImportDto dto) {
    return servicesCmd.imports(dto.getProjectId(), dto.getServiceId(), dto.getServiceName(),
        dto.getImportSource(), dto.getStrategyWhenDuplicated(), dto.getDeleteWhenNotExisted(),
        dto.getFile(), dto.getContent());
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return servicesCmd.importExample(projectId);
  }

  @Override
  public void delete(Long id) {
    servicesCmd.delete(id);
  }

  @Override
  @NameJoin
  public ServicesDetailVo detail(Long id, Boolean joinSchemaFlag) {
    ServicesDetailVo vo = ServicesAssembler.toDetailVo(servicesQuery.detail(id, joinSchemaFlag));
    Map<Long, Long> projectMockIdsMap = mockServiceQuery.findProjectMockIdsMap(Set.of(id));
    vo.setMockServiceId(projectMockIdsMap.get(id));
    List<Long> hasApisProjectIds = servicesQuery.hasApisServiceIds(Set.of(id));
    vo.setHasApisFlag(hasApisProjectIds.contains(id));
    return vo;
  }

  @NameJoin
  @Override
  public MockServiceDetailVo associationMockService(Long id) {
    MockService mockService = servicesQuery.associationMockService(id);
    return isNull(mockService) ? null : MockServiceAssembler.toDetailVo(mockService);
  }

  @Override
  public PageResult<ServiceVo> list(ServicesFindDto dto) {
    GenericSpecification<Services> spec = ServicesAssembler.getSpecification(dto);
    Page<Services> page = servicesQuery.list(spec, dto.tranPage());
    return joinAssocStatus(page, spec.getCriterias());
  }

  @Override
  public PageResult<ServiceVo> search(ServicesSearchDto dto) {
    Set<SearchCriteria> criteria = ServicesAssembler.getSearchCriteria(dto);
    // Set authorization conditions when you are not an administrator or only query yourself
    Page<Services> page = servicesSearch.search(criteria, dto.tranPage(),
        getMatchSearchFields(dto.getClass()));
    return joinAssocStatus(page, criteria);
  }

  @SneakyThrows
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      ServicesExportDto dto, HttpServletResponse response) {
    File file = servicesCmd.exportProject(dto.getExportScope(), dto.getServiceIds(),
        dto.getApiIds(), dto.getFormat(), dto.isOnlyApisComponents());
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
    return buildDownloadResourceResponseEntity(-1,
        APPLICATION_OCTET_STREAM, file.getName(), file.length(), resource);
  }

  private PageResult<ServiceVo> joinAssocStatus(Page<Services> page,
      Set<SearchCriteria> criteria) {
    if (isEmpty(page.getContent())) {
      // TODO PrincipalContext.addExtension("queryAllEmpty", queryAll);
      return PageResult.empty();
    }

    Set<Long> projectIds = page.stream().map(Services::getId).collect(Collectors.toSet());

    String queryHasApisFlag = findFirstValueAndRemove(criteria, "queryHasApisFlag");
    if (nonNull(queryHasApisFlag) && Boolean.parseBoolean(queryHasApisFlag)) {
      List<Long> hasApisProjectIds = servicesQuery.hasApisServiceIds(projectIds);
      for (Services project : page) {
        project.setHasApisFlag(hasApisProjectIds.contains(project.getId()));
      }
    }

    String queryHasMockServiceFlag = findFirstValueAndRemove(criteria, "queryHasMockServiceFlag");
    if (nonNull(queryHasMockServiceFlag) && Boolean.parseBoolean(queryHasMockServiceFlag)) {
      Map<Long, Long> projectMockIdsMap = mockServiceQuery.findProjectMockIdsMap(projectIds);
      for (Services project : page) {
        project.setMockServiceId(projectMockIdsMap.get(project.getId()));
      }
    }
    return PageResult.of(page.getTotalElements(), toVo(page.getContent()));
  }

}
