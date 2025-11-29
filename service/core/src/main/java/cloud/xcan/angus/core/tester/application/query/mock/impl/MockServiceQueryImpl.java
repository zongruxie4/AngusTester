package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX;
import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterMockServiceApis;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_ASSOC_SERVICE_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_DOMAIN_IN_USE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_DOMAIN_PORT_IN_USE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NAME_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NODE_PORT_IN_USE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_OVER_LIMIT_IN_NODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_PORT_UNAVAILABLE_IN_AGENT_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_TOP_LEVEL_DOMAIN_ERROR_T;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import cloud.xcan.angus.agent.message.CheckPortCmdParam;
import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.mockservice.StatusCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceManageCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.config.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.config.NodeQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.mock.MockResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisCount;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceAssocP;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceCount;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfoRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSearchRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSource;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceStatus;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Implementation of MockServiceQuery for managing Mock service operations and data retrieval.
 * <p>
 * This class provides comprehensive functionality for querying and managing Mock services,
 * including service creation, configuration, monitoring, and lifecycle management.
 * It handles service validation, authorization, status monitoring, and resource management.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock service detail retrieval with comprehensive data enrichment</li>
 *   <li>Service listing with search capabilities and authorization checks</li>
 *   <li>Service status monitoring and health checking</li>
 *   <li>Domain and port validation for service configuration</li>
 *   <li>Node resource management and capacity planning</li>
 *   <li>Authorization management and permission validation</li>
 *   <li>Statistics and metrics collection for service analysis</li>
 *   <li>Associated API management and quota validation</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations. It also supports summary query
 * registration for analytics and reporting purposes.
 */
@Biz
@SummaryQueryRegister(name = "MockService", table = "mock_service",
    groupByColumns = {"created_date", "source"})
public class MockServiceQueryImpl implements MockServiceQuery {

  @Resource
  private MockServiceRepo mockServiceRepo;
  @Resource
  private MockServiceInfoRepo mockServiceInfoRepo;
  @Resource
  private MockServiceSearchRepo mockServiceSearchRepo;
  @Resource
  private MockApisResponseRepo mockApisResponseRepo;
  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;
  @Resource
  private MockServiceManageCmd mockServiceManageCmd;
  @Resource
  private NodeQuery nodeQuery;
  @Resource
  private NodeInfoQuery nodeInfoQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private MockApisRepo mockApisRepo;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed information for a specific Mock service with comprehensive data enrichment.
   * <p>
   * Fetches complete service details including status information, authorization data,
   * and node information for comprehensive service management and monitoring.
   * <p>
   * The method performs authorization validation and enriches the service data with
   * current status, user permissions, and node details.
   *
   * @param id the Mock service ID to retrieve details for
   * @return MockService object with complete enriched details
   * @throws ResourceNotFound if the Mock service is not found
   */
  @Override
  public MockService detail(Long id) {
    return new BizTemplate<MockService>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Validate user authorization for viewing the Mock service
        mockServiceAuthQuery.checkViewAuth(getUserId(), id);

        // Validate that the Mock service exists and retrieve it
        serviceDb = checkAndFind(id);
      }

      @Override
      protected MockService process() {
        // Enrich service with current status information
        setMockServiceStatus(Collections.singletonList(serviceDb));

        // Enrich service with current user permissions
        setMockServiceCurrentAuths(Collections.singletonList(serviceDb));

        // Enrich service with node information
        setNodeInfo(List.of(serviceDb));
        return serviceDb;
      }
    }.execute();
  }

  /**
   * Retrieves detailed information for a Mock service by associated project ID.
   * <p>
   * Finds and retrieves Mock service details based on the associated project,
   * performing authorization validation and data enrichment.
   * <p>
   * The method returns null if no Mock service is associated with the project.
   *
   * @param projectId the project ID to find the associated Mock service for
   * @return MockService object with enriched details, or null if not found
   */
  @Override
  public MockService detailByProjectId(Long projectId) {
    return new BizTemplate<MockService>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Find Mock service by associated project ID
        serviceDb = findByProjectId(projectId);
        if (isNull(serviceDb)) {
          return;
        }

        // Validate user authorization for viewing the Mock service
        mockServiceAuthQuery.checkViewAuth(getUserId(), serviceDb.getId());
      }

      @Override
      protected MockService process() {
        if (isNull(serviceDb)) {
          return null;
        }

        // Enrich service with current status information
        setMockServiceStatus(Collections.singletonList(serviceDb));

        // Enrich service with current user permissions
        setMockServiceCurrentAuths(Collections.singletonList(serviceDb));
        return serviceDb;
      }
    }.execute();
  }

  /**
   * Retrieves basic information for a specific Mock service with authorization validation.
   * <p>
   * Fetches service information with proper error handling for non-existent services.
   * Note: Authorization validation is currently disabled for this method.
   *
   * @param id the Mock service ID to retrieve information for
   * @return MockService object with basic information
   * @throws ResourceNotFound if the Mock service is not found
   */
  @Override
  public MockService info(Long id) {
    return new BizTemplate<MockService>() {
      @Override
      protected void checkParams() {
        // Note: Authorization validation is currently disabled for this method
        // mockServiceAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected MockService process() {
        return mockServiceRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "MockService"));
      }
    }.execute();
  }

  /**
   * Retrieves basic information for a specific Mock service without authorization validation.
   * <p>
   * Fetches service information and returns null if the service does not exist.
   * Note: Authorization validation is currently disabled for this method.
   *
   * @param id the Mock service ID to retrieve information for
   * @return MockService object with basic information, or null if not found
   */
  @Override
  public MockService info0(Long id) {
    return new BizTemplate<MockService>() {
      @Override
      protected void checkParams() {
        // Note: Authorization validation is currently disabled for this method
        // mockServiceAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected MockService process() {
        return mockServiceRepo.findById(id).orElse(null);
      }
    }.execute();
  }

  /**
   * Retrieves all associated API IDs for a specific Mock service.
   * <p>
   * Fetches the complete list of API IDs associated with the Mock service
   * for relationship management and analysis.
   * Note: Authorization validation is currently disabled for this method.
   *
   * @param id the Mock service ID to retrieve associated API IDs for
   * @return Set of API IDs associated with the Mock service
   */
  @Override
  public Set<Long> assocApisIdsList(Long id) {
    return new BizTemplate<Set<Long>>() {
      @Override
      protected void checkParams() {
        // Note: Authorization validation is currently disabled for this method
        // mockServiceAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected Set<Long> process() {
        return mockApisRepo.findAllIdsByMockServiceId(id);
      }
    }.execute();
  }

  /**
   * Validates that a Mock service exists.
   * <p>
   * Performs existence validation and throws ResourceNotFound if the service
   * is not found in the system.
   *
   * @param id the Mock service ID to validate
   * @throws ResourceNotFound if the Mock service is not found
   */
  @Override
  public void check(Long id) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        if (!mockServiceRepo.existsById(id)) {
          throw ResourceNotFound.of(id, "MockService");
        }
        return null;
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of Mock services with comprehensive filtering and enrichment.
   * <p>
   * Supports both regular search and full-text search with project member validation.
   * Provides enriched service information including status, permissions, and node details.
   * <p>
   * The method performs project member validation to ensure the current user has
   * access to the requested services.
   *
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match array of field names to include in full-text search
   * @return Page of MockServiceInfo objects with enriched service information
   */
  @Override
  public Page<MockServiceInfo> find(GenericSpecification<MockServiceInfo> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<MockServiceInfo>>() {
      @Override
      protected void checkParams() {
        // Validate project member permissions for the search criteria
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<MockServiceInfo> process() {
        // Execute search based on whether full-text search is enabled
        Page<MockServiceInfo> page = fullTextSearch
            ? mockServiceSearchRepo.find(spec.getCriteria(), pageable, MockServiceInfo.class, match)
            : mockServiceInfoRepo.findAll(spec, pageable);
        if (page.isEmpty()) {
          return page;
        }

        // Enrich services with status information
        setMockServiceInfoStatus(page.getContent());
        // Enrich services with current user permissions
        setMockServiceInfoCurrentAuths(page.getContent());
        // Enrich services with node information
        setInfoNodeInfo(page.getContent());
        return page;
      }
    }.execute();
  }

  /**
   * Retrieves all Mock services associated with a specific node.
   * <p>
   * Fetches services deployed on the specified node and enriches them with
   * current status information for comprehensive node management.
   *
   * @param nodeId the node ID to retrieve services for
   * @return List of MockServiceInfo objects with status information
   */
  @Override
  public List<MockServiceInfo> findByNodeId(Long nodeId) {
    return new BizTemplate<List<MockServiceInfo>>() {

      @Override
      protected List<MockServiceInfo> process() {
        List<MockServiceInfo> infos = mockServiceInfoRepo.findByNodeId(nodeId);

        // Enrich services with current status information
        setMockServiceInfoStatus(infos);
        return infos;
      }
    }.execute();
  }

  /**
   * Retrieves comprehensive statistics for Mock services and their APIs.
   * <p>
   * Collects detailed metrics including API counts, request statistics, and
   * performance data for service analysis and monitoring.
   * <p>
   * The method handles both service-specific and global statistics collection.
   *
   * @param mockServiceId the Mock service ID for service-specific statistics (null for global)
   * @return MockServiceCount object with comprehensive statistics
   */
  @Override
  public MockServiceCount countStatistics(Long mockServiceId) {
    return new BizTemplate<MockServiceCount>() {

      @Override
      protected MockServiceCount process() {
        MockServiceCount count = new MockServiceCount();
        MockApisCount apisCount;
        if (nonNull(mockServiceId)) {
          // Collect service-specific statistics
          count.setApisNum(mockApisRepo.countAllByMockServiceId(mockServiceId));
          apisCount = mockApisRepo.countAllNum(mockServiceId);
        } else {
          // Collect global statistics
          count.setApisNum(mockApisRepo.countAll());
          apisCount = mockApisRepo.countAllNum();
        }
        // Note: Handle null values from JPA advice to prevent primitive type mismatch
        // Fix:: JPA Null return value from advice does not match primitive return type for: MockApisCount
        // Modify primitive long to Long.
        count.setRequestNum(nullSafe(apisCount.getRequestNum(), 0L));
        count.setPushbackNum(nullSafe(apisCount.getPushbackNum(), 0L));
        count.setSimulateErrorNum(nullSafe(apisCount.getSimulateErrorNum(), 0L));
        count.setSuccessNum(nullSafe(apisCount.getSuccessNum(), 0L));
        count.setExceptionNum(nullSafe(apisCount.getExceptionNum(), 0L));
        return count;
      }
    }.execute();
  }

  /**
   * Retrieves creation statistics for Mock resources within specified criteria.
   * <p>
   * Collects comprehensive creation metrics for services, APIs, responses, and pushback
   * configurations based on project, creator, and time range filters.
   * <p>
   * The method supports flexible filtering for detailed resource analysis and reporting.
   *
   * @param projectId the project ID for filtering (null for all projects)
   * @param creatorObjectType the type of creator object for filtering
   * @param creatorObjectId the creator object ID for filtering
   * @param createdDateStart the start date for time range filtering
   * @param createdDateEnd the end date for time range filtering
   * @return MockResourcesCreationCount object with comprehensive creation statistics
   */
  @Override
  public MockResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<MockResourcesCreationCount>() {

      final MockResourcesCreationCount result = new MockResourcesCreationCount();

      Set<Long> createdBys = null;

      @Override
      protected MockResourcesCreationCount process() {
        // Determine creator filter based on object type
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        // Build comprehensive filter criteria for statistics collection
        Set<SearchCriteria> allFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);

        // Collect statistics for different resource types
        countService(result, allFilters);
        countApi(result, allFilters);
        countResponse(result, allFilters);
        allFilters.add(SearchCriteria.equal("pushback", 1));
        countPushback(result, allFilters);
        return result;
      }
    }.execute();
  }

  /**
   * Retrieves associated API IDs that the user has permission to access.
   * <p>
   * Finds APIs within the specified project that are associated with the Mock service
   * and validates user permissions for both the Mock service and the project.
   * <p>
   * The method respects quota limits and authorization controls for secure API access.
   *
   * @param id the Mock service ID to find associated APIs for
   * @param angusProjectId the Angus project ID for API filtering
   * @return Set of API IDs that are accessible and associated with the Mock service
   */
  @Override
  public Set<Long> assocApisList(Long id, Long angusProjectId) {
    return new BizTemplate<Set<Long>>() {
      @Override
      protected void checkParams() {
        // Validate that the Mock service exists
        check(id);

        // Validate user authorization for viewing the Mock service
        mockServiceAuthQuery.checkViewAuth(getUserId(), id);

        // Validate that the Angus project exists
        servicesQuery.check(angusProjectId);

        // Validate user authorization for viewing the Angus project
        servicesAuthQuery.checkViewAuth(getUserId(), angusProjectId);
      }

      @Override
      protected Set<Long> process() {
        // Query project APIs with quota limit consideration
        long maxApisNum = commonQuery.findTenantQuota(AngusTesterMockServiceApis).getQuota();
        Page<ApisBasicInfo> apisExist = apisQuery.find0(angusProjectId,
            PageRequest.of(0, (int) maxApisNum, Sort.by(Direction.DESC, "id")),
            ApisBasicInfo.class);
        if (!apisExist.hasContent()) {
          return null;
        }

        // Query APIs associated with the Mock service from external services
        Set<Long> mockApisIds = mockApisRepo.findAllByMockServiceIdAndSource(id,
                MockServiceSource.ASSOC_SERVICE).stream()
            .map(MockApis::getAssocApisId)
            .filter(Objects::nonNull).collect(toSet());

        // Return intersection of project APIs and Mock service associated APIs
        return apisExist.getContent().stream().map(ApisBasicInfo::getId)
            .filter(mockApisIds::contains).collect(toSet());
      }
    }.execute();
  }

  /**
   * Finds a Mock service by its associated project ID.
   * <p>
   * Retrieves the Mock service that is associated with the specified project,
   * returning null if no association exists.
   *
   * @param projectId the project ID to find the associated Mock service for
   * @return MockService object if found, null otherwise
   */
  @Override
  public MockService findByProjectId(Long projectId) {
    return mockServiceRepo.findByAssocServiceId(projectId).orElse(null);
  }

  /**
   * Validates that a Mock service exists and retrieves it.
   * <p>
   * Performs existence validation and throws ResourceNotFound if the service is not found.
   * Used as a helper method for other operations that require service validation.
   *
   * @param id the Mock service ID to validate and retrieve
   * @return MockService object if found
   * @throws ResourceNotFound if the Mock service is not found
   */
  @Override
  public MockService checkAndFind(Long id) {
    return mockServiceRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "MockService"));
  }

  /**
   * Validates that a Mock service info exists and retrieves it.
   * <p>
   * Performs existence validation and throws ResourceNotFound if the service info is not found.
   * Used as a helper method for other operations that require service info validation.
   *
   * @param id the Mock service ID to validate and retrieve info for
   * @return MockServiceInfo object if found
   * @throws ResourceNotFound if the Mock service info is not found
   */
  @Override
  public MockServiceInfo checkAndFindInfo(Long id) {
    return mockServiceInfoRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "MockService"));
  }

  /**
   * Validates that multiple Mock services exist and retrieves them.
   * <p>
   * Performs batch existence validation and throws ResourceNotFound if any service is not found.
   * Used for bulk operations that require multiple service validations.
   *
   * @param ids set of Mock service IDs to validate and retrieve
   * @return List of MockService objects for all found services
   * @throws ResourceNotFound if any Mock service is not found
   */
  @Override
  public List<MockService> checkAndFind(Set<Long> ids) {
    List<MockService> mockService = mockServiceRepo.findAllById(ids);
    Set<Long> serviceIds = mockService.stream().map(MockService::getId).collect(toSet());
    ids.removeAll(serviceIds);
    assertResourceNotFound(isEmpty(ids), ids, "MockService");
    return mockService;
  }

  /**
   * Validates that multiple Mock service infos exist and retrieves them.
   * <p>
   * Performs batch existence validation and throws ResourceNotFound if any service info is not found.
   * Used for bulk operations that require multiple service info validations.
   *
   * @param ids set of Mock service IDs to validate and retrieve info for
   * @return List of MockServiceInfo objects for all found services
   * @throws ResourceNotFound if any Mock service info is not found
   */
  @Override
  public List<MockServiceInfo> checkAndFindInfo(Set<Long> ids) {
    List<MockServiceInfo> mockService = mockServiceInfoRepo.findAllById(ids);
    Set<Long> serviceIds = mockService.stream().map(MockServiceInfo::getId).collect(toSet());
    ids.removeAll(serviceIds);
    assertResourceNotFound(isEmpty(ids), ids, "MockService");
    return mockService;
  }

  /**
   * Validates that a Mock service name does not already exist.
   * <p>
   * Checks for name uniqueness to prevent duplicate service names during creation operations.
   *
   * @param name the service name to validate
   * @throws ResourceExisted if the service name already exists
   */
  @Override
  public void checkNameExists(String name) {
    long count = mockServiceRepo.countByName(name);
    assertResourceExisted(count < 1, MOCK_SERVICE_NAME_EXISTED_T, new Object[]{name});
  }

  /**
   * Validates that a node has sufficient capacity for additional Mock services.
   * <p>
   * Checks the current service count against the node's memory capacity,
   * limiting services to approximately 1GB of memory per service while ensuring
   * at least 2 services can be created on any node.
   *
   * @param nodeId the node ID to check capacity for
   * @param existedNodeNum the current number of services on the node
   * @throws ProtocolException if the node capacity limit would be exceeded
   */
  @Override
  public void checkNodeServiceNum(long nodeId, long existedNodeNum) {
    if (existedNodeNum >= 2) {
      NodeInfo nodeInfo = nodeInfoQuery.detail(nodeId, false);
      if (nonNull(nodeInfo.getInfo())
          /* Allow detailVo.getInfo().getMemTotal() / 1024 / 1024 / 1024 + 1 nodes */
          && nodeInfo.getInfo().getMemTotal() / 1024 / 1024 / 1024 < existedNodeNum) {
        throw ProtocolException.of(MOCK_SERVICE_OVER_LIMIT_IN_NODE);
      }
    }
  }

  /**
   * Validates that a Mock service's domain and port configuration is valid and available.
   * <p>
   * Performs comprehensive validation including cloud domain requirements, domain uniqueness,
   * and port availability checks for secure service configuration.
   *
   * @param service the Mock service to validate domain and port for
   * @throws ProtocolException if domain or port validation fails
   */
  @Override
  public void checkValidDomainAndPort(MockService service) {
    String serviceDomain = service.getServiceDomain();
    if (isNotEmpty(serviceDomain)) {
      if (isCloudServiceEdition()) {
        // Validate that cloud service domain ends with the required suffix
        assertTrue(serviceDomain.endsWith(MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX),
            MOCK_SERVICE_TOP_LEVEL_DOMAIN_ERROR_T, new Object[]{MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX});
      }
      // Validate that domain is not used by other nodes (excluding current node)
      boolean existedDomain = mockServiceRepo.existsByServiceDomainAndNodeIdNot(
          serviceDomain, service.getNodeId());
      assertTrue(!existedDomain, MOCK_SERVICE_DOMAIN_IN_USE_T,
          new Object[]{service.getNodeId()});
      // Validate that domain and port combination is not used by other services
      boolean existedDomainAndPort = mockServiceRepo.existsByServiceDomainAndServicePort(
          serviceDomain, service.getServicePort());
      assertTrue(!existedDomainAndPort, MOCK_SERVICE_DOMAIN_PORT_IN_USE_T,
          new Object[]{serviceDomain, String.valueOf(service.getServicePort())});
    }
  }

  /**
   * Validates that a domain update is valid and available.
   * <p>
   * Performs domain validation for update operations, ensuring the new domain
   * meets cloud service requirements and is not already in use.
   *
   * @param serviceDomain the new service domain to validate
   * @param nodeId the node ID to exclude from domain uniqueness check
   * @throws ProtocolException if domain validation fails
   */
  @Override
  public void checkUpdateDomain(String serviceDomain, Long nodeId) {
    if (isNotEmpty(serviceDomain)) {
      if (isCloudServiceEdition()) {
        // Validate that cloud service domain ends with the required suffix
        assertTrue(serviceDomain.endsWith(MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX),
            MOCK_SERVICE_TOP_LEVEL_DOMAIN_ERROR_T, new Object[]{MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX});
      }
      // Validate that domain is not used by other nodes (excluding current node)
      boolean existedDomain = mockServiceRepo.existsByServiceDomainAndNodeIdNot(
          serviceDomain, nodeId);
      assertTrue(!existedDomain, MOCK_SERVICE_DOMAIN_IN_USE_T,
          new Object[]{nodeId});
    }
  }

  /**
   * Validates that a node and port combination is available for service deployment.
   * <p>
   * Checks both database records and agent availability to ensure the port
   * is not already in use and is available for service deployment.
   *
   * @param nodeDb the node to check port availability for
   * @param port the port to validate
   * @throws ProtocolException if port is unavailable or already in use
   */
  @Override
  public void checkNodeAndPortAvailable(Node nodeDb, int port) {
    // Validate that port is not used by other services on the same node
    boolean existedNodePort = mockServiceRepo.existsByNodeIdAndServicePort(nodeDb.getId(), port);
    assertTrue(!existedNodePort, MOCK_SERVICE_NODE_PORT_IN_USE_T,
        new Object[]{nodeDb.getId(), port});

    // Validate port availability through node agent
    CheckPortVo result = checkPort(nodeDb.getId(), nodeDb.getIp(), port);
    if (!result.isSuccess()) {
      throw ProtocolException.of(MOCK_SERVICE_PORT_UNAVAILABLE_IN_AGENT_T,
          new Object[]{port, nodeDb.getId()}, new RuntimeException(result.getMessage()));
    }
  }

  /**
   * Validates that a project is not already associated with another Mock service.
   * <p>
   * Ensures that projects can only be associated with one Mock service to prevent
   * conflicts and maintain data integrity.
   *
   * @param projectDb the project to validate association for
   * @throws ResourceExisted if the project is already associated with another Mock service
   */
  @Override
  public void checkAssocProjectExists(Services projectDb) {
    assertTrue(!mockServiceRepo.existsByAssocServiceId(projectDb.getId()),
        MOCK_SERVICE_ASSOC_SERVICE_EXISTED_T, new Object[]{projectDb.getName()});
  }

  @Override
  public void setNodeInfo(List<MockService> services) {
    if (isNotEmpty(services)) {
      Map<Long, Node> nodeMap = nodeQuery.findNodeMap(services.stream()
          .map(MockService::getNodeId).toList());
      for (MockService service : services) {
        Node node = nodeMap.get(service.getNodeId());
        if (nonNull(node)) {
          service.setNodeIp(node.getIp());
          service.setNodePublicIp(node.getPublicIp());
        } else {
          service.setNodeIp("localhost");
          service.setNodePublicIp("localhost");
        }
      }
    }
  }

  @Override
  public void setInfoNodeInfo(List<MockServiceInfo> services) {
    if (isNotEmpty(services)) {
      Map<Long, Node> nodeMap = nodeQuery.findNodeMap(services.stream()
          .map(MockServiceInfo::getNodeId).toList());
      for (MockServiceInfo service : services) {
        Node node = nodeMap.get(service.getNodeId());
        if (nonNull(node)) {
          service.setNodeIp(node.getIp());
          service.setNodePublicIp(node.getPublicIp());
        } else {
          service.setNodeIp("localhost");
          service.setNodePublicIp("localhost");
        }
      }
    }
  }

  @Override
  public void setMockServiceCurrentAuths(List<MockService> mockServices) {
    if (isNotEmpty(mockServices)) {
      List<MockService> doAuthServices = new ArrayList<>();
      for (MockService service : mockServices) {
        if (!service.getAuth() || commonQuery.isAdminUser()) {
          service.setCurrentAuths(new HashSet<>(MockServicePermission.ALL));
        } else {
          doAuthServices.add(service);
        }
      }

      if (isNotEmpty(doAuthServices)) {
        Map<Long, List<MockServiceAuth>> authServiceMap = mockServiceAuthQuery.findAuth(
                getUserId(), mockServices.stream().map(MockService::getId).toList())
            .stream().collect(Collectors.groupingBy(MockServiceAuth::getMockServiceId));
        for (MockService doAuthService : doAuthServices) {
          if (authServiceMap.containsKey(doAuthService.getId())) {
            doAuthService.setCurrentAuths(authServiceMap.get(doAuthService.getId()).stream()
                .map(MockServiceAuth::getAuths).flatMap(Collection::stream)
                .collect(toSet()));
          }
        }
      }
    }
  }

  @Override
  public void setMockServiceInfoCurrentAuths(List<MockServiceInfo> mockServices) {
    if (isNotEmpty(mockServices)) {
      List<MockServiceInfo> doAuthServices = new ArrayList<>();
      for (MockServiceInfo service : mockServices) {
        if (!service.getAuth() || commonQuery.isAdminUser()) {
          service.setCurrentAuths(new HashSet<>(MockServicePermission.ALL));
        } else {
          doAuthServices.add(service);
        }
      }

      if (isNotEmpty(doAuthServices)) {
        Map<Long, List<MockServiceAuth>> authServiceMap = mockServiceAuthQuery.findAuth(
                getUserId(), mockServices.stream().map(MockServiceInfo::getId)
                    .toList())
            .stream().collect(Collectors.groupingBy(MockServiceAuth::getMockServiceId));
        for (MockServiceInfo doAuthService : doAuthServices) {
          if (authServiceMap.containsKey(doAuthService.getId())) {
            doAuthService.setCurrentAuths(authServiceMap.get(doAuthService.getId()).stream()
                .map(MockServiceAuth::getAuths).flatMap(Collection::stream)
                .collect(toSet()));
          }
        }
      }
    }
  }

  @Override
  public void setMockServiceStatus(List<MockService> mockServices) {
    if (isNotEmpty(mockServices)) {
      Map<Long, MockServiceStatus> serviceStatusMap = getMockServiceStatus(mockServices);
      for (MockService service : mockServices) {
        service.setServiceStatus(
            serviceStatusMap.getOrDefault(service.getId(), MockServiceStatus.NOT_STARTED));
      }
    }
  }

  @Override
  public void setMockServiceInfoStatus(List<MockServiceInfo> mockServices) {
    if (isNotEmpty(mockServices)) {
      Map<Long, MockServiceStatus> serviceStatusMap = getMockServiceInfoStatus(mockServices);
      for (MockServiceInfo service : mockServices) {
        service.setServiceStatus(serviceStatusMap.getOrDefault(service.getId(),
            MockServiceStatus.NOT_STARTED));
      }
    }
  }

  @Override
  public Map<Long, MockServiceStatus> getMockServiceStatus(List<MockService> services) {
    MockServiceStatusDto dto = new MockServiceStatusDto()
        .setBroadcast(true).setCmdParams(services.stream().map(x -> new StatusCmdParam()
                .setDeviceId(x.getNodeId()).setServiceId(x.getId())
                .setServerIp(x.getNodeIp()).setServerPort(x.getServicePort()))
            .toList());
    List<StatusVo> result = mockServiceManageCmd.status(dto);
    Map<Long, MockServiceStatus> serviceStatusMap = new HashMap<>(services.size());
    if (isNotEmpty(result)) {
      Map<Long, List<StatusVo>> serviceStatusVoMap = result.stream()
          .collect(Collectors.groupingBy(StatusVo::getServiceId));
      for (MockService service : services) {
        if (serviceStatusVoMap.containsKey(service.getId())
            && serviceStatusVoMap.get(service.getId()).stream().anyMatch(StatusVo::isSuccess)) {
          serviceStatusMap.put(service.getId(), MockServiceStatus.RUNNING);
        } else {
          serviceStatusMap.put(service.getId(), MockServiceStatus.NOT_STARTED);
        }
      }
    }
    return serviceStatusMap;
  }

  @Override
  public Map<Long, MockServiceStatus> getMockServiceInfoStatus(List<MockServiceInfo> services) {
    MockServiceStatusDto dto = new MockServiceStatusDto()
        .setBroadcast(true).setCmdParams(
            services.stream().map(x -> new StatusCmdParam()
                    .setDeviceId(x.getNodeId()).setServiceId(x.getId())
                    .setServerIp(x.getNodeIp()).setServerPort(x.getServicePort()))
                .toList());
    List<StatusVo> result = mockServiceManageCmd.status(dto);
    Map<Long, MockServiceStatus> serviceStatusMap = new HashMap<>(services.size());
    if (isNotEmpty(result)) {
      Map<Long, List<StatusVo>> serviceStatusVoMap = result.stream()
          .collect(Collectors.groupingBy(StatusVo::getServiceId));
      for (MockServiceInfo service : services) {
        if (serviceStatusVoMap.containsKey(service.getId())
            && serviceStatusVoMap.get(service.getId()).stream().anyMatch(StatusVo::isSuccess)) {
          serviceStatusMap.put(service.getId(), MockServiceStatus.RUNNING);
        } else {
          serviceStatusMap.put(service.getId(), MockServiceStatus.NOT_STARTED);
        }
      }
    }
    return serviceStatusMap;
  }

  @Override
  public Map<Long, Long> findProjectMockIdsMap(Set<Long> projectIds) {
    if (isEmpty(projectIds)) {
      return Collections.emptyMap();
    }
    return mockServiceRepo.findServiceMockIdsMap(projectIds).stream().collect(Collectors.toMap(
        MockServiceAssocP::getAssocServiceId, MockServiceAssocP::getId));
  }

  @Override
  public Boolean isAuthCtrl(Long id) {
    Boolean auth = mockServiceRepo.findAuthById(id);
    return auth == null || auth;
  }

  private CheckPortVo checkPort(Long nodeId, String nodeIp, int port) {
    List<CheckPortVo> result = nodeInfoQuery.checkPort(
        new NodeAgentCheckPortDto().setBroadcast(true)
            .setCmdParams(List.of(new CheckPortCmdParam().setDeviceId(nodeId)
                .setServerIp(nodeIp).setServerPort(port))));
    return result.get(0);
  }

  private void countService(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllService(mockServiceRepo.countAllByFilters(allFilters));
  }

  private void countApi(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllApi(mockApisRepo.countAllByFilters(allFilters));
  }

  private void countResponse(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllResponse(mockApisResponseRepo.countAllByFilters(allFilters));
  }

  private void countPushback(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllPushback(mockApisResponseRepo.countAllByFilters(allFilters));
  }
}
