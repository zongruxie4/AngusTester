package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.pojo.CorsData;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSource;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceFileImportDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceServicesAssocDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceInfoVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceListVo;
import cloud.xcan.angus.model.remoting.MockServiceSetting;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Objects;
import java.util.Set;

public class MockServiceAssembler {

  public static MockService addDtoToDomain(MockServiceAddDto dto) {
    return new MockService()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setSource(MockServiceSource.CREATED)
        .setImportSource(null)
        .setAuth(true)
        .setNodeId(dto.getNodeId())
        .setServicePort(dto.getServicePort())
        .setServiceDomain(dto.getServiceDomain())
        .setApisSecurity(dto.getApisSecurity())
        .setApisCors(nullSafe(dto.getApisCors(), new CorsData().setEnabled(true)))
        .setSetting(nullSafe(dto.getSetting(), new MockServiceSetting()));
  }

  public static MockService updateDtoToDomain(MockServiceUpdateDto dto) {
    return new MockService()
        .setId(dto.getId())
        //.setProjectId(dto.getProjectId())
        .setName(dto.getName())
        //.setSource(MockServiceSource.CREATED)
        //.setImportSource(null)
        //.setAuth(true)
        //.setNodeId(dto.getNodeId())
        //.setServicePort(dto.getServicePort())
        .setServiceDomain(
            isCloudServiceEdition() /* Cloud service edition cannot modify domain name */
                ? null : dto.getServiceDomain())
        .setApisSecurity(dto.getApisSecurity())
        .setApisCors(dto.getApisCors())
        .setSetting(dto.getSetting());
  }

  public static MockService replaceDtoToDomain(MockServiceReplaceDto dto) {
    MockService mockService = new MockService()
        .setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setName(dto.getName())
        //.setSource(MockServiceSource.CREATED)
        //.setImportSource(null)
        //.setAuth(true)
        //.setNodeId(dto.getNodeId())
        //.setServicePort(dto.getServicePort())
        .setServiceDomain(nonNull(dto.getId())
            && isCloudServiceEdition() /* Cloud service edition cannot modify domain name */
            ? null : dto.getServiceDomain())
        .setApisSecurity(dto.getApisSecurity())
        .setApisCors(nullSafe(dto.getApisCors(), new CorsData().setEnabled(true)))
        .setSetting(nullSafe(dto.getSetting(), new MockServiceSetting()));
    if (isNull(dto.getId())) { // Add
      mockService.setSource(MockServiceSource.CREATED)
          .setImportSource(null)
          .setAuth(true)
          .setNodeId(dto.getNodeId())
          .setServicePort(dto.getServicePort());
    }
    return mockService;
  }

  public static MockService fileImportDtoToDomain(MockServiceFileImportDto dto) {
    return new MockService()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setSource(MockServiceSource.FILE_IMPORT)
        .setImportSource(dto.getImportType())
        .setImportFile(dto.getFile())
        .setImportText(dto.getText())
        .setAuth(true)
        .setNodeId(dto.getNodeId())
        .setServicePort(dto.getServicePort())
        .setServiceDomain(dto.getServiceDomain())
        .setApisSecurity(dto.getApisSecurity())
        .setApisCors(nullSafe(dto.getApisCors(), new CorsData().setEnabled(false)))
        .setSetting(nullSafe(dto.getSetting(), new MockServiceSetting()));
  }

  public static MockService servicesAssocDtoToDomain(MockServiceServicesAssocDto dto) {
    return new MockService()
        .setName(dto.getName())
        .setSource(MockServiceSource.ASSOC_SERVICE)
        //.setImportSource(dto.getImportType())
        .setAssocServiceId(dto.getServiceId())
        .setAuth(true)
        .setNodeId(dto.getNodeId())
        .setServicePort(dto.getServicePort())
        .setServiceDomain(dto.getServiceDomain())
        .setApisSecurity(dto.getApisSecurity())
        .setApisCors(nullSafe(dto.getApisCors(), new CorsData().setEnabled(false)))
        .setSetting(nullSafe(dto.getSetting(), new MockServiceSetting()))
        .setApiIds(dto.getApiIds());
  }

  public static MockServiceInfoVo toInfoVo(MockService service) {
    return new MockServiceInfoVo().setId(service.getId())
        .setName(service.getName())
        .setSource(service.getSource())
        .setStatus(service.getServiceStatus())
        .setNodeId(service.getNodeId())
        .setServiceDomainUrl(service.getServiceDomainUrl())
        .setServiceHostUrl(service.getServiceHostUrl())
        .setAuth(service.getAuth())
        //.setApisSecurity(service.getApisSecurity())
        //.setApisCors(service.getApisCors())
        //.setSetting(service.getSetting())
        .setCreatedBy(service.getCreatedBy())
        .setCreatedDate(service.getCreatedDate())
        .setLastModifiedBy(service.getLastModifiedBy())
        .setLastModifiedDate(service.getLastModifiedDate());
  }

  public static MockServiceDetailVo toDetailVo(MockService service) {
    return new MockServiceDetailVo().setId(service.getId())
        .setProjectId(service.getProjectId())
        .setName(service.getName())
        .setSource(service.getSource())
        .setStatus(service.getServiceStatus())
        .setNodeId(service.getNodeId())
        .setNodeIp(service.getNodeIp())
        .setNodePublicIp(service.getNodePublicIp())
        .setServicePort(service.getServicePort())
        .setServiceDomainUrl(service.getServiceDomainUrl())
        .setServiceHostUrl(service.getServiceHostUrl())
        .setAuth(service.getAuth())
        .setCurrentAuths(service.getCurrentAuths())
        .setApisSecurity(service.getApisSecurity())
        .setApisCors(service.getApisCors())
        .setSetting(service.getSetting())
        .setTenantId(service.getTenantId())
        .setCreatedBy(service.getCreatedBy())
        .setCreatedDate(service.getCreatedDate())
        .setLastModifiedBy(service.getLastModifiedBy())
        .setLastModifiedDate(service.getLastModifiedDate());
  }

  public static MockServiceListVo toServiceListVo(MockServiceInfo service) {
    return new MockServiceListVo().setId(service.getId())
        .setProjectId(service.getProjectId())
        .setName(service.getName())
        .setSource(service.getSource())
        .setStatus(service.getServiceStatus())
        .setNodeId(service.getNodeId())
        .setNodeIp(service.getNodeIp())
        .setNodePublicIp(service.getNodePublicIp())
        .setServicePort(service.getServicePort())
        .setServiceDomainUrl(service.getServiceDomainUrl())
        .setServiceHostUrl(service.getServiceHostUrl())
        .setAuth(service.getAuth())
        .setCurrentAuths(service.getCurrentAuths())
        .setAssocServices(Objects.nonNull(service.getAssocServiceId()))
        .setTenantId(service.getTenantId())
        .setCreatedBy(service.getCreatedBy())
        .setCreatedDate(service.getCreatedDate())
        .setLastModifiedBy(service.getLastModifiedBy())
        .setLastModifiedDate(service.getLastModifiedDate());
  }

  public static GenericSpecification<MockServiceInfo> getSpecification(MockServiceFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .matchSearchFields("name", "extSearchMerge")
        .build();
    return new GenericSpecification<>(filters);
  }

}
