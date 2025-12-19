package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.core.biz.BizAssert.assertTrue;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.ServicesConverter.toServicesSummary;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_APIS_NOT_BELONG_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_PUBLISHED_CANNOT_MODIFY_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_PUBLISHED_CANNOT_MODIFY_T;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.LongKeyCountSummary;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesListRepo;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.services.ServicesSearchRepo;
import cloud.xcan.angus.core.tester.domain.services.summary.ServicesSummary;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Implementation of ServicesQuery for services management and query operations.
 * </p>
 * <p>
 * Provides methods for services CRUD operations, API association management, test statistics, and
 * quota validation.
 * </p>
 */
@Service
@SummaryQueryRegister(name = "Services", table = "services",
    groupByColumns = {"created_date", "source", "auth", "status", "type"})
public class ServicesQueryImpl implements ServicesQuery {

  @Resource
  private ServicesRepo servicesRepo;
  @Resource
  private ServicesListRepo servicesListRepo;
  @Resource
  private ServicesSearchRepo servicesSearchRepo;
  @Resource
  private ApisTestQuery apisTestQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ApisRepo apisRepo;
  @Resource
  private ApisCaseQuery apisCaseQuery;
  @Resource
  private MockServiceQuery mockServiceQuery;
  @Resource
  private CommonQuery commonQuery;

  /**
   * <p>
   * Get detailed information of a service including API counts and optional schema information.
   * </p>
   * <p>
   * Retrieves service details, sets API and API case counts, and optionally joins schema
   * information. Requires view permission for the service.
   * </p>
   *
   * @param id         Service ID
   * @param joinSchema Whether to include schema information
   * @return Service with detailed information
   */
  @Override
  public Services detail(Long id, Boolean joinSchema) {
    return new BizTemplate<Services>() {
      @Override
      protected void checkParams() {
        // Check the to have view permission
        servicesAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected Services process() {
        Services project = servicesRepo.findById(id).orElseThrow(
            () -> ResourceNotFound.of(id, "Services"));
        project.setApisNum(apisQuery.countByServiceId(id));
        project.setApisCaseNum(apisCaseQuery.countByServiceId(id));
        if (nonNull(joinSchema) && joinSchema) {
          PrincipalContext.addExtension("servicesSchema", servicesSchemaQuery.checkAndFind(id));
        }
        return project;
      }
    }.execute();
  }

  /**
   * <p>
   * List services with pagination and optional full-text search.
   * </p>
   * <p>
   * Retrieves paginated services with authorization filtering and sets API counts for each service.
   * Supports both regular database queries and full-text search operations.
   * </p>
   *
   * @param spec           Search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Search match parameters
   * @return Page of services
   */
  @Override
  public Page<Services> list(GenericSpecification<Services> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Services>>() {

      @Override
      protected Page<Services> process() {
        spec.getCriteria().add(SearchCriteria.equal("deleted", 0));
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());
        Page<Services> page = fullTextSearch
            ? servicesSearchRepo.find(spec.getCriteria(), pageable, Services.class, match)
            : servicesListRepo.find(spec.getCriteria(), pageable, Services.class, null);
        setApisNum(page.getContent(), spec.getCriteria());
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Get the associated mock service for a service.
   * </p>
   *
   * @param id Service ID
   * @return Associated mock service
   */
  @Override
  public MockService associationMockService(Long id) {
    return new BizTemplate<MockService>() {


      @Override
      protected MockService process() {
        return mockServiceQuery.detailByProjectId(id);
      }
    }.execute();
  }

  /**
   * <p>
   * Count API tests for a specific service within a time range.
   * </p>
   * <p>
   * Provides test statistics for APIs belonging to a service, with optional filtering by creator
   * and time range.
   * </p>
   *
   * @param serviceId         Service ID
   * @param creatorObjectType Creator object type
   * @param creatorObjectId   Creator object ID
   * @param createdDateStart  Start date for filtering
   * @param createdDateEnd    End date for filtering
   * @return API test count statistics
   */
  @Override
  public ApisTestCount countServiceTestApis(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisTestCount>() {

      @Override
      protected ApisTestCount process() {
        return apisTestQuery.countServiceTestApis(serviceId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
      }
    }.execute();
  }

  /**
   * <p>
   * Count API tests for a project within a time range.
   * </p>
   * <p>
   * Provides test statistics for all APIs in a project, with optional filtering by creator and time
   * range.
   * </p>
   *
   * @param projectId         Project ID
   * @param creatorObjectType Creator object type
   * @param creatorObjectId   Creator object ID
   * @param createdDateStart  Start date for filtering
   * @param createdDateEnd    End date for filtering
   * @return API test count statistics
   */
  @Override
  public ApisTestCount countProjectTestApis(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisTestCount>() {

      @Override
      protected ApisTestCount process() {
        return apisTestQuery.countProjectTestApis(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
      }
    }.execute();
  }

  /**
   * <p>
   * Convert a service to a summary format.
   * </p>
   * <p>
   * Used by the summary query system to convert service entities to summary format.
   * </p>
   *
   * @param services Service entity
   * @return Service summary
   */
  @NameJoin
  public static ServicesSummary getServicesSummary(Services services) {
    return toServicesSummary(services);
  }

  /**
   * <p>
   * Find a service by ID without throwing exceptions.
   * </p>
   *
   * @param id Service ID
   * @return Service entity or null if not found
   */
  @Override
  public Services find0(Long id) {
    return servicesRepo.find0ById(id).orElse(null);
  }

  /**
   * <p>
   * Find multiple services by IDs without throwing exceptions.
   * </p>
   *
   * @param ids Collection of service IDs
   * @return List of service entities
   */
  @Override
  public List<Services> find0ByIds(Collection<Long> ids) {
    return servicesRepo.findAll0ByIdIn(ids);
  }

  /**
   * <p>
   * Find service IDs that have associated APIs.
   * </p>
   *
   * @param serviceIds Collection of service IDs to check
   * @return List of service IDs that have APIs
   */
  @Override
  public List<Long> hasApisServiceIds(Collection<Long> serviceIds) {
    return apisRepo.findServiceIdByServiceIdIn(serviceIds);
  }

  /**
   * <p>
   * Check and find a service by ID.
   * </p>
   *
   * @param id Service ID
   * @return Service entity
   */
  @Override
  public Services checkAndFind(Long id) {
    return servicesRepo.findById(id).orElseThrow(
        () -> ResourceNotFound.of(id, "Services"));
  }

  /**
   * <p>
   * Check and find multiple services by IDs.
   * </p>
   * <p>
   * Validates that all specified service IDs exist. Throws ResourceNotFound if any service is
   * missing.
   * </p>
   *
   * @param serviceIds Set of service IDs
   * @return List of service entities
   */
  @Override
  public List<Services> checkAndFind(Set<Long> serviceIds) {
    List<Services> services = servicesRepo.findAllById(serviceIds);
    assertResourceNotFound(isNotEmpty(services), serviceIds.iterator().next(), "Services");
    if (serviceIds.size() != services.size()) {
      for (Services project : services) {
        assertResourceNotFound(serviceIds.contains(project.getId()), project.getId(), "Services");
      }
    }
    return services;
  }

  /**
   * <p>
   * Check if authorization control is enabled for a service.
   * </p>
   * <p>
   * Returns true if authorization is enabled or null (default enabled), false if explicitly
   * disabled.
   * </p>
   *
   * @param id Service ID
   * @return true if authorization control is enabled, false otherwise
   */
  @Override
  public Boolean isAuthCtrl(Long id) {
    Boolean auth = servicesRepo.findAuthById(id);
    return auth == null || auth;
  }

  /**
   * <p>
   * Check if a service exists by ID.
   * </p>
   *
   * @param id Service ID
   */
  @Override
  public void check(Long id) {
    if (!servicesRepo.existsById(id)) {
      throw ResourceNotFound.of(id, "Services");
    }
  }

  /**
   * <p>
   * Check tenant quota for service creation.
   * </p>
   *
   * @param incr Number of services to be added
   */
  @Override
  public void checkQuota(long incr) {
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterServices,
        null, servicesRepo.count() + incr);
  }

  /**
   * <p>
   * Check if APIs belong to a specific service.
   * </p>
   * <p>
   * Validates that all specified API IDs belong to the given service. Throws an exception if any
   * API does not belong to the service.
   * </p>
   *
   * @param apiIds    Collection of API IDs to validate
   * @param serviceId Service ID
   * @return List of validated API entities
   */
  @Override
  public List<Apis> checkApisBelongService(Collection<Long> apiIds, Long serviceId) {
    List<Apis> apisDb = apisQuery.findAllByServiceIdAndIdIn(serviceId, apiIds);
    Set<Long> apisDbIds = apisDb.stream().map(Apis::getId).collect(Collectors.toSet());
    Set<Long> reqApis = new HashSet<>(apiIds);
    reqApis.removeAll(apisDbIds);
    ProtocolAssert.assertTrue(isEmpty(reqApis), SERVICE_APIS_NOT_BELONG_T,
        new Object[]{isNotEmpty(reqApis) ? reqApis.stream().findFirst().orElse(null) : "",
            serviceId});
    return apisDb;
  }

  /**
   * <p>
   * Check if a service name already exists in a project.
   * </p>
   * <p>
   * Validates that a service with the specified name does not already exist in the project.
   * Includes logic for handling deleted services.
   * </p>
   *
   * @param projectId Project ID
   * @param name      Service name to check
   */
  @Override
  public void checkNameExists(long projectId, String name) {
    // Include logic deleted project
    Long nameAndCreatedByExist = servicesRepo.countAll0ByNameAndAndProjectId(name, projectId);
    assertResourceExisted(nameAndCreatedByExist < 1, SERVICE_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * <p>
   * Check if a service can be modified based on its publish status.
   * </p>
   * <p>
   * Prevents modification of released services. Throws an exception if the service is released.
   * </p>
   *
   * @param serviceDb Service entity to check
   */
  @Override
  public void checkPublishStatus(Services serviceDb) {
    assertTrue(isNull(serviceDb.getStatus())
            || !serviceDb.getStatus().isReleased(), SERVICE_PUBLISHED_CANNOT_MODIFY_CODE,
        SERVICE_PUBLISHED_CANNOT_MODIFY_T, new Object[]{serviceDb.getName()});
  }

  /**
   * <p>
   * Set API counts for a list of services.
   * </p>
   * <p>
   * Efficiently loads and sets API counts for multiple services to avoid N+1 query problems. Uses
   * aggregation queries to get counts grouped by service ID.
   * </p>
   *
   * @param services List of services to set API counts for
   * @param criteria Search criteria containing project ID
   */
  @Override
  public void setApisNum(List<Services> services, Set<SearchCriteria> criteria) {
    if (isNotEmpty(services)) {
      String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
      Set<SearchCriteria> filters = new HashSet<>();
      filters.add(SearchCriteria.equal("projectId", projectId));
      filters.add(SearchCriteria.equal("serviceDeleted", 0));
      filters.add(SearchCriteria.equal("deleted", 0));
      Map<Long, Long> apisNumMap = servicesRepo.countByFiltersAndGroup(ApisBasicInfo.class,
              LongKeyCountSummary.class, filters, "serviceId", "id").stream()
          .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
      for (Services service : services) {
        service.setApisNum(apisNumMap.containsKey(service.getId())
            ? apisNumMap.get(service.getId()) : 0);
      }
    }
  }

  /**
   * <p>
   * Set a safe clone name for a service.
   * </p>
   * <p>
   * Generates a unique name for service cloning by appending "-Copy" and a random salt if needed.
   * Ensures the name length does not exceed the maximum allowed length.
   * </p>
   *
   * @param service Service entity to set the clone name for
   */
  @Override
  public void setSafeCloneName(Services service) {
    String saltName = randomAlphanumeric(3);
    String clonedName = servicesRepo.existsByName(service.getName() + "-Copy")
        ? service.getName() + "-Copy." + saltName : service.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH_X2 ? clonedName.substring(0,
        MAX_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    service.setName(clonedName);
  }

}
