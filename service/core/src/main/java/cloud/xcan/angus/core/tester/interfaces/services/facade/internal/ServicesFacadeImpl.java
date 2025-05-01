package cloud.xcan.angus.core.tester.interfaces.services.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler.toVo;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSearch;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesExportDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesImportDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesSearchDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAssembler;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
    return servicesCmd.add(addDtoToDomain(dto), true);
  }

  @Override
  public void rename(Long id, String name) {
    servicesCmd.rename(id, name.trim());
  }

  @Override
  public void statusUpdate(Long id, ApiStatus status) {
    servicesCmd.statusUpdate(id, status);
  }

  @Override
  public void clone(Long id) {
    servicesCmd.clone(id);
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
  public ServicesDetailVo detail(Long id, Boolean joinSchema) {
    ServicesDetailVo vo = toDetailVo(servicesQuery.detail(id, joinSchema));
    Map<Long, Long> projectMockIdsMap = mockServiceQuery.findProjectMockIdsMap(Set.of(id));
    vo.setMockServiceId(projectMockIdsMap.get(id));
    List<Long> hasApisProjectIds = servicesQuery.hasApisServiceIds(Set.of(id));
    vo.setHasApis(hasApisProjectIds.contains(id));
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
    GenericSpecification<Services> spec = getSpecification(dto);
    Page<Services> page = servicesQuery.list(spec, dto.tranPage());
    return joinAssocStatus(page, spec.getCriteria());
  }

  @Override
  public PageResult<ServiceVo> search(ServicesSearchDto dto) {
    Set<SearchCriteria> criteria = getSearchCriteria(dto);
    // Set authorization conditions when you are not an administrator or only query yourself
    Page<Services> page = servicesSearch.search(criteria, dto.tranPage(),
        getMatchSearchFields(dto.getClass()));
    return joinAssocStatus(page, criteria);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      ServicesExportDto dto, HttpServletResponse response) {
    File file = servicesCmd.exportProject(dto.getExportScope(), dto.getServiceIds(),
        dto.getApiIds(), dto.getFormat(), dto.isOnlyApisComponents());
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM, file);
  }

  private PageResult<ServiceVo> joinAssocStatus(Page<Services> page,
      Set<SearchCriteria> criteria) {
    if (isEmpty(page.getContent())) {
      // TODO PrincipalContext.addExtension("queryAllEmpty", queryAll);
      return PageResult.empty();
    }

    Set<Long> projectIds = page.stream().map(Services::getId).collect(Collectors.toSet());

    String queryHasApis = findFirstValueAndRemove(criteria, "queryHasApis");
    if (nonNull(queryHasApis) && Boolean.parseBoolean(queryHasApis)) {
      List<Long> hasApisProjectIds = servicesQuery.hasApisServiceIds(projectIds);
      for (Services project : page) {
        project.setHasApis(hasApisProjectIds.contains(project.getId()));
      }
    }

    String queryHasMockService = findFirstValueAndRemove(criteria, "queryHasMockService");
    if (nonNull(queryHasMockService) && Boolean.parseBoolean(queryHasMockService)) {
      Map<Long, Long> projectMockIdsMap = mockServiceQuery.findProjectMockIdsMap(projectIds);
      for (Services project : page) {
        project.setMockServiceId(projectMockIdsMap.get(project.getId()));
      }
    }
    return PageResult.of(page.getTotalElements(), toVo(page.getContent()));
  }

}
