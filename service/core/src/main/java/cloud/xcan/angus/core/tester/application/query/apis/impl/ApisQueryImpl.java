package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.countCreationApis;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.countCreationService;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.countCreationUnarchivedApis;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.toApisDetailSummary;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_OPERATION_EXISTED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_PUBLISHED_CANNOT_MODIFY_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_PUBLISHED_CANNOT_MODIFY_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TRASH_NO_VIEW_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TRASH_NO_VIEW_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ApisModification;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ApisModificationCode;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.merge;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserFullName;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.RegexpUtils.matches;
import static io.swagger.v3.oas.models.extension.ApiServerSource.API_SERVERS;
import static io.swagger.v3.oas.models.extension.ApiServerSource.CURRENT_REQUEST;
import static io.swagger.v3.oas.models.extension.ApiServerSource.MOCK_SERVICE;
import static io.swagger.v3.oas.models.extension.ApiServerSource.PARENT_SERVERS;
import static io.swagger.v3.oas.models.extension.ExtensionKey.SERVER_SOURCE_KEY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.SimpleActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisInfoListRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavourite;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollow;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowRepo;
import cloud.xcan.angus.core.tester.domain.apis.summary.ApisDetailSummary;
import cloud.xcan.angus.core.tester.domain.apis.summary.ApisInfoSummary;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisAssocP;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfoRepo;
import cloud.xcan.angus.core.tester.domain.services.ServiceApisScope;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.infra.util.RefResolver;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API query operations for comprehensive API management and reporting.
 *
 * <p>This class provides extensive functionality for querying and retrieving
 * API data, including detailed information, pagination, search, and various enrichment
 * operations.</p>
 *
 * <p>It handles API lifecycle management, mock service integration, server
 * configuration, reference resolution, and comprehensive data enrichment.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>API detail and basic info queries with pagination</li>
 *   <li>Full-text search capabilities for API content</li>
 *   <li>OpenAPI schema generation and reference resolution</li>
 *   <li>Mock service integration and server management</li>
 *   <li>API creation statistics and quota management</li>
 *   <li>Favourite and follow status management</li>
 *   <li>Activity resource mapping and notification events</li>
 *   <li>Comprehensive validation and status checking</li>
 *   <li>Summary query registration for reporting</li>
 * </ul></p>
 */
@Slf4j
@Biz
@SummaryQueryRegister(name = "Apis", table = "apis",
    groupByColumns = {"created_date", "source", "method", "protocol_type", "status", "auth",
        "publish"}
)
public class ApisQueryImpl implements ApisQuery {

  @Resource
  private ApisRepo apisRepo;
  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;
  @Resource
  private ApisBasicInfoRepo apisBasicInfoRepo;
  @Resource
  private ApisInfoSearchRepo apisInfoSearchRepo;
  @Resource
  private ApisFavouriteRepo favouriteRepo;
  @Resource
  private ApisFollowRepo apisFollowRepo;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ApisInfoListRepo apisInfoListRepo;
  @Resource
  private ApisUnarchivedRepo apisUnarchivedRepo;
  @Resource
  private ServicesRepo servicesRepo;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;
  @Resource
  private ServicesCompQuery servicesCompQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private MockServiceInfoRepo mockServiceInfoRepo;
  @Resource
  private MockApisRepo mockApisRepo;
  @Resource
  private UserManager userManager;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  /**
   * Retrieves detailed API information with comprehensive enrichment.
   *
   * <p>This method fetches complete API details with extensive enrichment including
   * favourite/follow status, mock service associations, available servers, tag schemas, and
   * optional reference resolution.</p>
   *
   * <p>The method validates user permissions and handles deleted API access
   * with appropriate authorization checks.</p>
   *
   * @param id         the API ID to retrieve details for
   * @param resolveRef whether to resolve OpenAPI references
   * @return the detailed API information with all enrichments
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public Apis detail(Long id, Boolean resolveRef) {
    return new BizTemplate<Apis>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Verify user has API view permissions
        apisAuthQuery.checkViewAuth(getUserId(), id);

        // Verify API exists and retrieve API info
        apisDb = checkAndFind(id);

        // Verify user can access deleted API (only if they deleted it)
        BizAssert.assertTrue(!apisDb.getDeleted() || apisDb.getDeletedBy().equals(getUserId()),
            TRASH_NO_VIEW_PERMISSION_CODE, TRASH_NO_VIEW_PERMISSION);
      }

      @Override
      protected Apis process() {
        List<Apis> list = List.of(apisDb);
        if (isUserAction()) {
          // Set favourite and follow status for current user
          setFavourite(list);
          setFollow(list);
        }
        // Set associated mock APIs for enhanced display
        setAssocMockApis(list);
        // Set available servers for API execution
        setAndGetAvailableServers(apisDb);
        // Set tag schemas from parent service
        List<Tag> tagSchemas = servicesSchemaQuery.checkAndFind(apisDb.getServiceId()).getTags();
        setTagSchemas(apisDb, tagSchemas);

        // Resolve OpenAPI references if requested
        if (Objects.equals(resolveRef, true)) {
          setOpenApiPathRefModels(apisDb);
        }
        return apisDb;
      }
    }.execute();
  }

  /**
   * Finds mock API information associated with a specific API.
   *
   * <p>This method retrieves mock API details and validates consistency
   * between the original API and mock API configurations.</p>
   *
   * <p>The method checks for operation inconsistencies and enriches
   * mock service information for display purposes.</p>
   *
   * @param id the API ID to find mock information for
   * @return the mock API information or null if not found
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public MockApis findMockApis(Long id) {
    return new BizTemplate<MockApis>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Verify API exists and retrieve API base info
        apisDb = checkAndFindBaseInfo(id);

        // Verify user has API view permissions
        apisAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected MockApis process() {
        // Retrieve mock API information for the specified API
        MockApis mockApis = mockApisRepo.findByAssocApisId(id);
        if (isNull(mockApis)) {
          return null;
        }

        // Check for operation consistency between API and mock API
        if (!Objects.equals(apisDb.getEndpoint(), mockApis.getEndpoint()) ||
            !Objects.equals(apisDb.getMethod(), mockApis.getMethod())) {
          mockApis.setInconsistentOperation(true)
              .setEndpoint(apisDb.getEndpoint()).setMethod(apisDb.getMethod());
        } else {
          mockApis.setInconsistentOperation(false);
        }
        // Set mock service name for enhanced display
        mockApis.setMockServiceName(mockApisRepo.findMockServiceNameById(mockApis.getId()));

        // Return API parent mock service information
        MockServiceInfo service = mockServiceInfoRepo.findByAssocServiceId(apisDb.getServiceId());
        if (nonNull(service)) {
          mockApis.setParentMockServiceId(service.getId())
              .setParentMockServiceName(service.getName())
              .setMockServiceHostUrl(service.getServiceHostUrl())
              .setMockServiceDomainUrl(service.getServiceDomainUrl());
        }
        return mockApis;
      }
    }.execute();
  }

  /**
   * Generates OpenAPI specification for an API with format and compression options.
   *
   * <p>This method generates OpenAPI documentation for an API with support for
   * different schema formats (JSON/YAML) and optional gzip compression.</p>
   *
   * <p>The method validates export permissions and handles schema generation
   * with proper error handling.</p>
   *
   * @param id              the API ID to generate OpenAPI specification for
   * @param format          the schema format (JSON or YAML)
   * @param gzipCompression whether to apply gzip compression
   * @param checkExport     whether to check export permissions
   * @return the OpenAPI specification as a string
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public String openapiDetail(Long id, SchemaFormat format, Boolean gzipCompression
      , boolean checkExport) {
    return new BizTemplate<String>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        if (checkExport) {
          // Check the to have export permission
          apisAuthQuery.checkExportAuth(getUserId(), id);
        }

        // NOOP: Check to have view permission -> Do in serviceSchemaQuery.openapiDetail()

        // Check the apis exists
        apisDb = checkAndFind(id);
      }

      @Override
      protected String process() {
        PrincipalContext.addExtension(id.toString(), apisDb);
        return servicesSchemaQuery.openapiDetail(apisDb.getServiceId(), Set.of(id),
            format, gzipCompression, true);
      }
    }.execute();
  }

  /**
   * Checks if an API exists by ID.
   *
   * <p>This method validates that an API with the specified ID exists
   * in the system, throwing a ResourceNotFound exception if not found.</p>
   *
   * @param id the API ID to check for existence
   * @throws ResourceNotFound if the API is not found
   */
  @Override
  public void check(Long id) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        if (!apisRepo.existsById(id)) {
          throw ResourceNotFound.of(id, "Apis");
        }
        return null;
      }
    }.execute();
  }

  /**
   * Retrieves detailed API information for multiple APIs with optional reference resolution.
   *
   * <p>This method fetches detailed information for multiple APIs, ensuring they all belong
   * to the same service and optionally resolving OpenAPI references.</p>
   *
   * <p>The method validates user permissions for all APIs and enriches data with
   * tag schemas and reference models when requested.</p>
   *
   * @param ids        the set of API IDs to retrieve details for
   * @param resolveRef whether to resolve OpenAPI references
   * @return list of detailed API information
   * @throws IllegalArgumentException if APIs belong to different services
   */
  @Override
  public List<Apis> listDetail(HashSet<Long> ids, Boolean resolveRef) {
    return new BizTemplate<List<Apis>>() {
      List<Apis> apisDb;

      @Override
      protected void checkParams() {
        apisAuthQuery.batchCheckPermission(ids, ApiPermission.VIEW);

        apisDb = apisRepo.findAllById(ids);
        assertTrue(apisDb.isEmpty()
                || apisDb.stream().map(Apis::getServiceId).collect(Collectors.toSet()).size() == 1,
            "Only allow querying one services apis");
      }

      @Override
      protected List<Apis> process() {
        // Set OpenAPI reference models if requested
        if (isNotEmpty(apisDb) && Objects.equals(resolveRef, true)) {
          // Note: All APIs must belong to the same service for consistent tag schemas
          List<Tag> tagSchemas = servicesSchemaQuery.checkAndFind(
              apisDb.get(0).getServiceId()).getTags();
          for (Apis api : apisDb) {
            setOpenApiPathRefModels(api);
            setTagSchemas(api, tagSchemas);
          }
        }
        return apisDb;
      }
    }.execute();
  }

  /**
   * Finds APIs by service ID with pagination, filtering, and optional full-text search.
   *
   * <p>This method retrieves APIs for a specific service with support for pagination,
   * filtering, and optional full-text search capabilities.</p>
   *
   * <p>The method validates service view permissions and automatically filters
   * out deleted APIs and services.</p>
   *
   * @param serviceId      the service ID to find APIs for
   * @param spec           the specification for filtering APIs
   * @param pageable       the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the full-text search match fields
   * @return a page of APIs for the specified service
   */
  @Override
  public Page<ApisBasicInfo> findByServiceId(Long serviceId,
      GenericSpecification<ApisBasicInfo> spec, PageRequest pageable, boolean fullTextSearch,
      String[] match) {
    return new BizTemplate<Page<ApisBasicInfo>>() {
      @Override
      protected void checkParams() {
        // Verify user has service view permissions
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected Page<ApisBasicInfo> process() {
        // Add service-specific and deletion filters
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(equal("serviceId", serviceId));
        criteria.add(equal("deleted", false));
        criteria.add(equal("serviceDeleted", false));

        return list0(criteria, pageable, fullTextSearch, match);
      }
    }.execute();
  }

  /**
   * Lists APIs with pagination, filtering, and optional full-text search.
   *
   * <p>This method retrieves APIs based on specification criteria with support
   * for pagination and optional full-text search capabilities.</p>
   *
   * <p>The method automatically enriches API data with mock associations
   * for enhanced display.</p>
   *
   * @param spec           the specification for filtering APIs
   * @param pageable       the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the full-text search match fields
   * @return a page of APIs with enriched data
   */
  @Override
  public Page<ApisBasicInfo> list(GenericSpecification<ApisBasicInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisBasicInfo>>() {
      @Override
      protected void checkParams() {
        // Verify user has project member permissions
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisBasicInfo> process() {
        // Add deletion filters to exclude deleted APIs and services
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(equal("deleted", false));
        criteria.add(equal("serviceDeleted", false));

        return list0(criteria, pageable, fullTextSearch, match);
      }
    }.execute();
  }

  /**
   * Internal method for listing APIs with common enrichment logic.
   *
   * <p>This method handles the core listing logic with authorization filtering,
   * search execution, and comprehensive data enrichment.</p>
   *
   * @param criteria       the search criteria for filtering APIs
   * @param pageable       the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the full-text search match fields
   * @return a page of APIs with enriched data
   */
  private Page<ApisBasicInfo> list0(Set<SearchCriteria> criteria, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    // Set authorization conditions for non-admin users or self-query scenarios
    commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
    Page<ApisBasicInfo> page = fullTextSearch
        ? apisInfoSearchRepo.find(criteria, pageable, ApisBasicInfo.class,
        ApisConverter::objectArrToApis, match)
        : apisInfoListRepo.find(criteria, pageable, ApisBasicInfo.class,
            ApisConverter::objectArrToApis, null);

    if (page.hasContent()) {
      if (isUserAction()) {
        // Set favourite and follow status for current user
        setFavourite(page.getContent());
        setFollow(page.getContent());
      }
      // Set associated mock APIs for enhanced display
      setInfoAssocMockApis(page.getContent());
      // Enrich with user name and avatar information
      userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
          "avatar");
    }
    return page;
  }

  /**
   * Gets the list of available servers for an API.
   *
   * <p>This method retrieves all available servers for an API, including
   * API-specific servers, parent service servers, and mock service servers.</p>
   *
   * @param id the API ID to get servers for
   * @return list of available servers for the API
   */
  @Override
  public List<Server> serverList(Long id) {
    return new BizTemplate<List<Server>>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        apisDb = checkAndFind(id);
      }

      @Override
      protected List<Server> process() {
        // Retrieve and set all available servers for the API
        return setAndGetAvailableServers(apisDb);
      }
    }.execute();
  }

  /**
   * Generates API creation statistics for reporting and analysis.
   *
   * <p>This method calculates comprehensive creation statistics including
   * API counts, service counts, and unarchived API counts within specified date ranges and creator
   * criteria.</p>
   *
   * <p>The method supports filtering by project, creator type, and date
   * ranges for detailed analytics.</p>
   *
   * @param projectId         the project ID to filter statistics
   * @param creatorObjectType the type of creator object
   * @param creatorObjectId   the creator object ID
   * @param createdDateStart  the start date for statistics
   * @param createdDateEnd    the end date for statistics
   * @return comprehensive API creation statistics
   */
  @Override
  public ApisResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisResourcesCreationCount>() {

      @Override
      protected ApisResourcesCreationCount process() {
        final ApisResourcesCreationCount result = new ApisResourcesCreationCount();

        // Determine creator filter based on object type and ID
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        // Build common filters for all resource types
        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);

        // Count services with deletion filter
        Set<SearchCriteria> serviceFilters = merge(commonFilters, equal("deleted", false));
        List<Services> services = servicesRepo.findAllByFilters(serviceFilters);
        countCreationService(result, services);

        // Count APIs with deletion filters for both API and service
        Set<SearchCriteria> apisFilters = merge(commonFilters, equal("deleted", false),
            equal("serviceDeleted", false));
        List<ApisBaseInfo> apis = apisBaseInfoRepo.findAllByFilters(apisFilters);
        countCreationApis(result, apis);

        // Count unarchived APIs without deletion filter
        List<ApisUnarchived> unarchivedApis = apisUnarchivedRepo.findAllByFilters(commonFilters);
        countCreationUnarchivedApis(result, unarchivedApis);
        return result;
      }
    }.execute();
  }

  @Override
  public Page<ApisBasicInfo> find0(Long serviceId, PageRequest pageable,
      Class<ApisBasicInfo> clz) {
    Set<SearchCriteria> criteria = new HashSet<>();
    criteria.add(equal("serviceId", serviceId));
    criteria.add(equal("deleted", false));
    //criteria.add(equal("serviceDeleted", false));

    // Set authorization conditions when you are not an administrator or only query yourself
    commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
    return apisInfoListRepo.find(criteria, pageable, clz, ApisConverter::objectArrToApis, null);
  }

  @Override
  public List<Apis> findByServiceId(Long serviceId) {
    return apisRepo.findByServiceId(serviceId);
  }

  @Override
  public List<Apis> findByServiceAndIds(Long serviceId, Collection<Long> ids) {
    return isEmpty(ids) ? apisRepo.findByServiceId(serviceId)
        : apisRepo.findAllByIdInAndServiceIdIn(ids, Collections.singleton(serviceId));
  }

  @Override
  public ApisBaseInfo findBase0ById(Long id) {
    return apisBaseInfoRepo.find0ById(id);
  }

  /**
   * Finds API base information by IDs, including deleted APIs.
   *
   * <p>This method retrieves API base information for the specified IDs,
   * including APIs that have been marked as deleted.</p>
   *
   * @param ids the collection of API IDs to find
   * @return list of API base information including deleted ones
   */
  @Override
  public List<ApisBaseInfo> findBase0ByIdIn(Collection<Long> ids) {
    return apisBaseInfoRepo.findAll0ByIdIn(ids);
  }

  @Override
  public ApisBaseInfo findBaseByIdIn(Long id) {
    return apisBaseInfoRepo.findById(id).orElse(null);
  }

  @Override
  public ApisBaseInfo findLeastByProjectId(Long projectId) {
    return apisBaseInfoRepo.findLeastByProjectId(projectId);
  }

  @Override
  public List<Apis> findAllByServiceIdAndIdIn(Long serviceId, Collection<Long> apiIds) {
    return apisRepo.findAllByServiceIdAndIdIn(serviceId, apiIds);
  }

  /**
   * Finds an API by ID with OpenAPI reference resolution.
   *
   * <p>This method retrieves an API and resolves its OpenAPI references
   * to provide complete schema information.</p>
   *
   * @param id the API ID to find
   * @return the API with resolved OpenAPI references or null if not found
   */
  @Override
  public Apis findDeRefById(Long id) {
    Apis apis = apisRepo.findById(id).orElse(null);
    if (nonNull(apis)) {
      setOpenApiPathRefModels(apis);
    }
    return apis;
  }

  /**
   * Creates a mapping of case IDs to simple activity resources.
   *
   * <p>This method converts functional case information into simple activity
   * resources for activity tracking and reporting purposes.</p>
   *
   * @param caseIds the collection of case IDs to map
   * @return map of case ID to simple activity resource
   */
  @Override
  public Map<Long, SimpleActivityResource> getCaseSimpleActivityResourceMap(
      Collection<Long> caseIds) {
    return funcCaseInfoRepo.findAllById(caseIds).stream()
        .map(x -> new SimpleActivityResource()
            .setId(x.getId()).setName(x.getName()).setParentId(x.getProjectId()))
        .peek(x -> x.setTargetType(CombinedTargetType.FUNC_CASE))
        .collect(Collectors.toMap(SimpleActivityResource::getId, x -> x));
  }

  /**
   * Checks if an API has authorization control enabled.
   *
   * <p>This method determines whether both the service and API have
   * authorization control enabled, requiring permission checks for access.</p>
   *
   * @param id the API ID to check authorization control for
   * @return true if authorization control is enabled, false otherwise
   */
  @Override
  public Boolean isAuthCtrl(Long id) {
    ApisBaseInfo apis = apisBaseInfoRepo.findById(id).orElse(null);
    return nonNull(apis) && (apis.getServiceAuth() && apis.getAuth());
  }

  @Override
  public Long countByServiceId(Long id) {
    return apisRepo.countByServiceId(id);
  }

  @Override
  public Apis checkAndFind(Long id) {
    return apisRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Apis"));
  }

  @Override
  public List<Apis> checkAndFind(Collection<Long> ids) {
    List<Apis> apis = apisRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(apis), ids.iterator().next(), "Apis");
    if (ids.size() != apis.size()) {
      for (Apis api : apis) {
        assertResourceNotFound(ids.contains(api.getId()), api.getId(), "Apis");
      }
    }
    return apis;
  }

  @Override
  public ApisBaseInfo checkAndFindBaseInfo(Long id) {
    return apisBaseInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Apis"));
  }

  @Override
  public ApisBasicInfo checkAndFindBasicInfo(Long id) {
    return apisBasicInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Apis"));
  }

  /**
   * Finds and validates service APIs based on modification scope and criteria.
   *
   * <p>This method retrieves APIs for a service based on the specified scope
   * (all, selected, or matched by criteria) and validates user modification permissions.</p>
   *
   * <p>The method supports filtering by endpoint regex, HTTP method, and tags
   * for match-based scoping.</p>
   *
   * @param serviceId          the service ID to find APIs for
   * @param modifyScope        the scope for API selection (all, selected, or matched)
   * @param matchEndpointRegex the endpoint regex pattern for matching
   * @param matchMethod        the HTTP method for matching
   * @param selectedApisIds    the set of selected API IDs
   * @param filterTags         the set of tags for filtering
   * @return list of APIs matching the specified criteria
   * @throws IllegalArgumentException if required parameters are missing for the scope
   */
  @Override
  public List<Apis> checkAndFindServiceApis(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags) {
    assertTrue(!modifyScope.isMatch() || isNotEmpty(matchEndpointRegex)
            || nonNull(matchMethod) || isNotEmpty(filterTags),
        "Parameter matchEndpointRegex or matchMethod or filterTags is required when the scope is a 'MATCH_APIS'.");
    assertTrue(!modifyScope.isSelected() || isNotEmpty(selectedApisIds),
        "Parameter selectedApisIds is required when the scope is a 'SELECTED_APIS'.");
    servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
    List<Apis> serviceApisDb;
    if (modifyScope.isAll()) {
      serviceApisDb = findByServiceAndIds(serviceId, null);
    } else if (modifyScope.isSelected()) {
      serviceApisDb = findByServiceAndIds(serviceId, selectedApisIds);
    } else { // MATCH_APIS_BY_REGEX
      serviceApisDb = findByServiceAndIds(serviceId, null).stream()
          .filter(x -> (isNull(matchMethod) || matchMethod.equals(x.getMethod()))
              && (isEmpty(matchEndpointRegex) || (isNotEmpty(x.getEndpoint())
              && matches(x.getEndpoint(), matchEndpointRegex)))
              && (isEmpty(filterTags) || (isNotEmpty(x.getTags())
              && x.getTags().stream().anyMatch(filterTags::contains)))
          ).toList();
    }
    assertResourceNotFound(serviceApisDb, "Not matched apis found", new Object[]{});
    return serviceApisDb;
  }

  /**
   * Validates that API owners exist in the system.
   *
   * <p>This method checks that all API owners (users) still exist in the system,
   * preventing issues after user deletion.</p>
   *
   * @param apis the collection of APIs to validate owners for
   * @throws ResourceNotFound if any API owner no longer exists
   */
  @Override
  public void checkOwnerExist(Collection<Apis> apis) {
    List<Long> ids = apis.stream().map(Apis::getOwnerId).collect(Collectors.toList());
    if (isNotEmpty(ids)) {
      // Prevent user sync failure after user deletion
      List<Long> userIds = userManager.findUserIdsByIdIn(ids);
      ids.removeAll(userIds);
      assertResourceNotFound(isEmpty(ids), userIds, "User");
    }
  }

  @Override
  public List<ApisBaseInfo> checkAndFindBaseInfos(Collection<Long> apisIds) {
    List<ApisBaseInfo> apis = apisBaseInfoRepo.findAllById(apisIds);
    assertResourceNotFound(isNotEmpty(apis), apisIds, "Apis");

    if (apisIds.size() != apis.size()) {
      for (ApisBaseInfo api : apis) {
        assertResourceNotFound(apisIds.contains(api.getId()), api.getId(), "Apis");
      }
    }
    return apis;
  }

  @Override
  public void checkExists(Long parentId, Collection<Long> apisIds) {
    List<Long> apisIdDb = apisRepo.findAllIdByServiceIdAndIdIn(parentId, apisIds);
    assertResourceNotFound(isNotEmpty(apisIdDb), apisIds, "Apis");

    if (apisIds.size() != apisIdDb.size()) {
      for (Long apiId : apisIds) {
        assertResourceNotFound(apisIdDb.contains(apiId), apiId, "Apis");
      }
    }
  }

  /**
   * Validates that APIs to be added do not already exist in their services.
   *
   * <p>This method checks for duplicate API operations (method + endpoint combinations)
   * within the same service to prevent conflicts during API creation.</p>
   *
   * @param apis the collection of APIs to validate for existence
   * @throws ResourceExisted if any API operation already exists in the service
   */
  @Override
  public void checkAddServiceApisExisted(Collection<Apis> apis) {
    Map<Long, List<Apis>> serviceApisMap = apis.stream()
        .collect(Collectors.groupingBy(Apis::getServiceId));
    for (Long serviceId : serviceApisMap.keySet()) {
      // Note: URI cannot be null, must be safely converted to empty string
      List<ApisBaseInfo> servicesApisDb = apisBaseInfoRepo.findAllByServiceIdAndEndpointIn(
          serviceId, serviceApisMap.get(serviceId).stream().map(Apis::getEndpoint)
              .toList());
      if (isNotEmpty(servicesApisDb)) {
        for (ApisBaseInfo apiDb : servicesApisDb) {
          for (Apis api : serviceApisMap.get(serviceId)) {
            assertResourceExisted(!Objects.equals(api.getMethod(), apiDb.getMethod())
                    || !Objects.equals(api.getEndpoint(), apiDb.getEndpoint()), APIS_OPERATION_EXISTED,
                new Object[]{api.getMethod() + " " + emptySafe(api.getEndpoint(), "\"\"")});
          }
        }
      }
    }
  }

  /**
   * Validates that API operations do not conflict during updates.
   *
   * <p>This method checks for duplicate API operations when updating APIs,
   * handling method and endpoint updates with proper conflict detection.</p>
   *
   * @param apis      the collection of APIs being updated
   * @param apisDb    the collection of existing APIs in the database
   * @param serviceId the service ID for the APIs
   * @param replace   whether this is a replace operation
   * @throws ResourceExisted if any API operation conflicts with existing ones
   */
  @Override
  public void checkServiceApisOperationNotExisted(Collection<Apis> apis, Collection<Apis> apisDb,
      Long serviceId, boolean replace) {
    // Handle partial updates where method and endpoint may not be modified (nullable)
    List<Apis> updateApis = apis.stream().filter(x -> nonNull(x.getMethod())
        || nonNull(x.getEndpoint())).toList();
    if (isNotEmpty(updateApis) && !replace /*Update apis*/) {
      for (Apis api : updateApis) {
        for (Apis apiDb : apisDb) {
          if (api.getId().equals(apiDb.getId())) {
            if (isNull(api.getMethod())) {
              api.setMethod(apiDb.getMethod());
            }
            if (isNull(api.getEndpoint())) {
              api.setEndpoint(apiDb.getEndpoint());
            }
            api.setServiceId(apiDb.getServiceId());
          }
        }
      }
    }

    // Note: URI cannot be null, must be safely converted to empty string
    List<ApisBaseInfo> servicesApisDb = apisBaseInfoRepo.findAllByServiceIdAndEndpointIn(
        serviceId, apis.stream().map(Apis::getEndpoint).toList());
    if (isNotEmpty(servicesApisDb)) {
      for (ApisBaseInfo apiDb : servicesApisDb) {
        for (Apis api : apis) {
          assertResourceExisted(
              !Objects.equals(api.getMethod(), apiDb.getMethod())
                  || !Objects.equals(api.getEndpoint(), apiDb.getEndpoint())
                  || Objects.equals(api.getId(), apiDb.getId()), APIS_OPERATION_EXISTED,
              new Object[]{api.getMethod() + " " + emptySafe(api.getEndpoint(), "\"\"")});
        }
      }
    }
  }

  /**
   * Validates that adding APIs would not exceed service and tenant quotas.
   *
   * <p>This method checks both service-level API quotas and tenant-level
   * API quotas to ensure limits are not exceeded.</p>
   *
   * @param apis the list of APIs to be added
   * @throws QuotaException if adding APIs would exceed quota limits
   */
  @Override
  public void checkServiceApisQuota(List<Apis> apis) {
    List<Map<String, Long>> serviceApisNums = apisRepo.countServiceApis(
        apis.stream().map(Apis::getServiceId).collect(Collectors.toSet()));
    Map<Long, Long> requestProjectApisNumMap = apis.stream()
        .collect(Collectors.groupingBy(Apis::getServiceId, Collectors.counting()));
    for (Map<String, Long> serviceApisNum : serviceApisNums) {
      Long serviceId = serviceApisNum.get("serviceId");
      Long apisNum = serviceApisNum.get("num");
      commonQuery.checkTenantQuota(QuotaResource.AngusTesterServicesApis,
          Collections.singleton(apis.get(0).getServiceId()),
          apisNum + requestProjectApisNumMap.get(serviceId));
    }
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterApis, apis.stream().map(Apis::getId)
        .collect(Collectors.toSet()), apisRepo.countByTenantId(getOptTenantId()) + apis.size());
  }

  /**
   * Validates that an API is not in released status for modification.
   *
   * <p>This method ensures that released APIs cannot be modified,
   * maintaining data integrity for published APIs.</p>
   *
   * @param apisInfoDb the API basic info to check release status for
   * @throws BizException if the API is in released status
   */
  @Override
  public void checkReleasedStatus(ApisBasicInfo apisInfoDb) {
    BizAssert.assertTrue(isNull(apisInfoDb.getStatus())
            || !apisInfoDb.getStatus().isReleased(), APIS_PUBLISHED_CANNOT_MODIFY_CODE,
        APIS_PUBLISHED_CANNOT_MODIFY_T, new Object[]{apisInfoDb.getName()});
  }

  /**
   * Validates that an API is not in released status for modification.
   *
   * <p>This method ensures that released APIs cannot be modified,
   * maintaining data integrity for published APIs.</p>
   *
   * @param apisDb the API to check release status for
   * @throws BizException if the API is in released status
   */
  @Override
  public void checkReleasedStatus(Apis apisDb) {
    BizAssert.assertTrue(isNull(apisDb.getStatus())
            || !apisDb.getStatus().isReleased(), APIS_PUBLISHED_CANNOT_MODIFY_CODE,
        APIS_PUBLISHED_CANNOT_MODIFY_T, new Object[]{apisDb.getName()});
  }

  /**
   * Validates that multiple APIs are not in released status for modification.
   *
   * <p>This method ensures that none of the APIs in the collection are in released status,
   * preventing modification of published APIs.</p>
   *
   * @param apisDbs the collection of APIs to check release status for
   * @throws BizException if any API is in released status
   */
  @Override
  public void checkReleasedStatus(Collection<Apis> apisDbs) {
    for (Apis apiDb : apisDbs) {
      // Verify that released APIs are not allowed to be modified
      checkReleasedStatus(apiDb);
    }
  }

  @Override
  public void checkApisBaseReleasedStatus(ApisBaseInfo apisBasesDb) {
    BizAssert.assertTrue(isNull(apisBasesDb.getStatus())
            || !apisBasesDb.getStatus().isReleased(), APIS_PUBLISHED_CANNOT_MODIFY_CODE,
        APIS_PUBLISHED_CANNOT_MODIFY_T, new Object[]{apisBasesDb.getName()});
  }

  @Override
  public void checkApisBaseReleasedStatus(Collection<ApisBaseInfo> apisBasesDb) {
    for (ApisBaseInfo apiDb : apisBasesDb) {
      // Check the released api are not allowed to be modified
      checkApisBaseReleasedStatus(apiDb);
    }
  }

  @Override
  public Set<Long> findParentIds(Collection<Long> apiIds) {
    return apisRepo.findAllServiceIdByIdIn(apiIds);
  }

  /**
   * Sets favourite status for a list of APIs.
   *
   * <p>This method enriches APIs with their favourite status for the current user,
   * providing personalized display information.</p>
   *
   * @param apis the list of APIs to set favourite status for
   */
  @Override
  public void setFavourite(List<? extends ResourceFavouriteAndFollow<?, ?>> apis) {
    Set<Long> apiIds = apis.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());
    List<ApisFavourite> favourites = favouriteRepo
        .findAllByApisIdInAndCreatedBy(apiIds, getUserId());
    Set<Long> favouritesApiIds = favourites.stream().map(ApisFavourite::getApisId)
        .collect(Collectors.toSet());
    apis.forEach(api -> {
      if (favouritesApiIds.contains(api.getId())) {
        api.setFavourite(true);
      }
    });
  }

  /**
   * Sets follow status for a list of APIs.
   *
   * <p>This method enriches APIs with their follow status for the current user,
   * providing personalized display information.</p>
   *
   * @param apis the list of APIs to set follow status for
   */
  @Override
  public void setFollow(List<? extends ResourceFavouriteAndFollow<?, ?>> apis) {
    Set<Long> apiIds = apis.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());
    List<ApisFollow> follows = apisFollowRepo
        .findAllByApisIdInAndCreatedBy(apiIds, getUserId());
    Set<Long> followApiIds = follows.stream().map(ApisFollow::getApisId)
        .collect(Collectors.toSet());
    apis.forEach(api -> {
      if (followApiIds.contains(api.getId())) {
        api.setFollow(true);
      }
    });
  }

  /**
   * Sets associated mock API information for basic API info objects.
   *
   * <p>This method enriches basic API information with associated mock service
   * and mock API IDs for enhanced display.</p>
   *
   * @param apis the list of basic API info objects to enrich
   */
  @Override
  public void setInfoAssocMockApis(List<ApisBasicInfo> apis) {
    Map<Long, MockApisAssocP> apisMockMap = findApisMockIdsMap(apis.stream()
        .map(ApisBasicInfo::getId).collect(Collectors.toSet()));
    if (isEmpty(apisMockMap)) {
      return;
    }
    apis.forEach(api -> {
      MockApisAssocP assoc = apisMockMap.get(api.getId());
      if (nonNull(assoc)) {
        api.setMockServiceId(assoc.getMockServiceId());
        api.setMockApisId(assoc.getMockApisId());
      }
    });
  }

  /**
   * Sets associated mock API information for full API objects.
   *
   * <p>This method enriches full API objects with associated mock service
   * and mock API IDs for enhanced display.</p>
   *
   * @param apis the list of API objects to enrich
   */
  @Override
  public void setAssocMockApis(List<Apis> apis) {
    Map<Long, MockApisAssocP> apisMockMap = findApisMockIdsMap(apis.stream()
        .map(Apis::getId).collect(Collectors.toSet()));
    if (isEmpty(apisMockMap)) {
      return;
    }
    apis.forEach(api -> {
      MockApisAssocP assoc = apisMockMap.get(api.getId());
      if (nonNull(assoc)) {
        api.setMockServiceId(assoc.getMockServiceId());
        api.setMockApisId(assoc.getMockApisId());
      }
    });
  }

  @Override
  public void setSafeCloneName(Apis apis) {
    String saltName = randomAlphanumeric(3);
    String clonedName = apisRepo.existsByServiceIdAndSummary(
        apis.getServiceId(), apis.getName() + "-Copy")
        ? apis.getName() + "-Copy." + saltName : apis.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_OPENAPI_SUMMARY_LENGTH ? clonedName.substring(0,
        MAX_OPENAPI_SUMMARY_LENGTH - 3) + saltName : clonedName;
    apis.setSummary(clonedName);
    apis.setEndpoint(apis.getEndpoint() + "-Copy." + saltName);
  }

  /**
   * Sets and retrieves all available servers for an API.
   *
   * <p>This method assembles all available servers for an API including
   * API-specific servers, parent service servers, and mock service servers.</p>
   *
   * @param apis the API to set and get servers for
   * @return list of all available servers for the API
   */
  @Override
  public List<Server> setAndGetAvailableServers(Apis apis) {
    List<Server> servers = new ArrayList<>();
    // Assemble API-specific servers
    assembleApiServers(apis, servers);
    // Assemble parent service and service configuration servers
    assembleParentServiceServers(servers, apis.getId());
    // Assemble mock service servers
    assembleMockServiceServers(servers, apis.getId());
    apis.setAvailableServers(servers);
    return servers;
  }

  /**
   * Sets available servers for an API with custom parent servers.
   *
   * <p>This method sets available servers for an API using provided parent servers
   * instead of automatically retrieving them.</p>
   *
   * @param apis          the API to set servers for
   * @param parentServers the custom parent servers to include
   */
  @Override
  public void setAvailableServers(Apis apis, List<Server> parentServers) {
    List<Server> servers = new ArrayList<>();
    // Assemble API-specific servers
    assembleApiServers(apis, servers);
    // Note: Parent project servers assembly is commented out
    // assembleParentProjectServers(servers, apis.getId());
    if (isNotEmpty(parentServers)) {
      servers.addAll(parentServers);
    }
    apis.setAvailableServers(servers);
  }

  /**
   * Sets and retrieves referenced authentication for an API.
   *
   * <p>This method resolves authentication schema references and sets
   * the resolved authentication component for the API.</p>
   *
   * @param apis the API to set referenced authentication for
   */
  @Override
  public void setAndGetRefAuthentication(Apis apis) {
    if (apis.isAuthSchemaRef()) {
      ServicesComp comp = servicesCompQuery.detailByRef(apis.getServiceId(),
          apis.getAuthentication().get$ref());
      if (isNull(comp)) {
        log.warn("ServicesComp `{}` not found", apis.getAuthentication().get$ref());
        return;
      }
      SecurityScheme auth = comp.toComponent(SecurityScheme.class);
      apis.setRefAuthentication(auth);
    }
  }

  /**
   * Sets tag schemas for an API based on available tag definitions.
   *
   * <p>This method filters and sets tag schemas that match the API's tags,
   * providing complete tag information for display and validation.</p>
   *
   * @param apisDb     the API to set tag schemas for
   * @param tagSchemas the list of available tag schemas
   */
  @Override
  public void setTagSchemas(Apis apisDb, List<Tag> tagSchemas) {
    if (isNotEmpty(apisDb.getTags()) && isNotEmpty(tagSchemas)) {
      apisDb.setTagSchemas(tagSchemas.stream().filter(x -> apisDb.getTags()
          .contains(x.getName())).collect(Collectors.toMap(Tag::getName, x -> x)));
    }
  }

  /**
   * Gets parent service servers for an API.
   *
   * <p>This method retrieves servers from the parent service of an API,
   * adding source extensions for identification.</p>
   *
   * @param apiId the API ID to get parent service servers for
   * @return list of parent service servers or null if not found
   */
  @Override
  public List<Server> getParentServiceServers(Long apiId) {
    Set<Long> parentIds = findParentIds(Collections.singletonList(apiId));
    if (isNotEmpty(parentIds)) {
      List<Server> parentServers = servicesSchemaQuery.findServersByServiceIds(parentIds);
      if (isNotEmpty(parentServers)) {
        for (Server server : parentServers) {
          if (isNotEmpty(server.getUrl())) {
            server.addExtension(SERVER_SOURCE_KEY, PARENT_SERVERS.getValue());
          }
        }
        return parentServers;
      }
    }
    return null;
  }

  /**
   * Assembles and sends modification notice events for multiple APIs.
   *
   * <p>This method processes multiple API modifications and sends appropriate
   * notification events to relevant users.</p>
   *
   * @param apisDb     the list of APIs that were modified
   * @param activities the list of activities corresponding to the modifications
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(List<ApisBasicInfo> apisDb,
      List<Activity> activities) {
    Map<Long, Activity> apisActivityMap = activities.stream()
        .collect(Collectors.toMap(Activity::getTargetId, x -> x));
    for (ApisBasicInfo apiDb : apisDb) {
      assembleAndSendModifyNoticeEvent(apiDb, apisActivityMap.get(apiDb.getId()));
    }
  }

  /**
   * Assembles and sends modification notice event for a single API.
   *
   * <p>This method creates and sends notification events for API modifications
   * to the API creator and followers.</p>
   *
   * @param apisDb   the API that was modified
   * @param activity the activity corresponding to the modification
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(ApisBasicInfo apisDb, Activity activity) {
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(apisDb.getTenantId())
        .get(ApisModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(apisDb.getCreatedBy());
    List<Long> followUserIds = apisFollowRepo.findUserIdsByApisId(apisDb.getId());
    receiveObjectIds.addAll(followUserIds);
    receiveObjectIds.remove(getUserId());
    if (isNotEmpty(receiveObjectIds)) {
      String message = message(ApisModification, new Object[]{getUserFullName(),
              apisDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(ApisModificationCode, message,
          API.getValue(), apisDb.getId().toString(), apisDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  /**
   * Finds mock API association mapping for a set of API IDs.
   *
   * <p>This method retrieves mock API associations for the specified APIs
   * and creates a mapping for efficient lookup.</p>
   *
   * @param apisIds the set of API IDs to find mock associations for
   * @return map of API ID to mock API association information
   */
  private Map<Long, MockApisAssocP> findApisMockIdsMap(Set<Long> apisIds) {
    if (isEmpty(apisIds)) {
      return Collections.emptyMap();
    }
    return mockApisRepo.findApisMockIdsMap(apisIds).stream().collect(
        Collectors.toMap(MockApisAssocP::getAssocApisId, x -> x));
  }

  /**
   * Assembles parent service servers for an API.
   *
   * <p>This method retrieves servers from the parent service and adds them
   * to the server list with source extensions for identification.</p>
   *
   * @param servers the list to add parent service servers to
   * @param apiId   the API ID to get parent service servers for
   */
  private void assembleParentServiceServers(List<Server> servers, Long apiId) {
    Set<Long> parentIds = findParentIds(Collections.singletonList(apiId));
    if (isNotEmpty(parentIds)) {
      List<Server> parentServers = servicesSchemaQuery.findServersByServiceIds(parentIds);
      if (isNotEmpty(parentServers)) {
        for (Server server : parentServers) {
          if (isNotEmpty(server.getUrl())) {
            // Add source extension to identify server origin
            server.addExtension(SERVER_SOURCE_KEY, PARENT_SERVERS.getValue());
          }
        }
        servers.addAll(parentServers);
      }
    }
  }

  /**
   * Assembles mock service servers for an API.
   *
   * <p>This method retrieves mock service information for an API and adds
   * mock service servers to the server list with source extensions.</p>
   *
   * @param servers the list to add mock service servers to
   * @param apiId   the API ID to get mock service servers for
   */
  private void assembleMockServiceServers(List<Server> servers, Long apiId) {
    MockServiceInfo serviceInfo = mockServiceInfoRepo.findByApisId(apiId);
    if (nonNull(serviceInfo)) {
      if (isNotEmpty(serviceInfo.getServiceDomainUrl())) {
        // Create mock service domain server
        Server server = new Server();
        server.setUrl(serviceInfo.getServiceDomainUrl());
        server.addExtension(SERVER_SOURCE_KEY, MOCK_SERVICE.getValue());
        servers.add(server);
      }
      if (isNotEmpty(serviceInfo.getServiceHostUrl())) {
        // Create mock service host server
        Server server = new Server();
        server.setUrl(serviceInfo.getServiceHostUrl());
        server.addExtension(SERVER_SOURCE_KEY, MOCK_SERVICE.getValue());
        servers.add(server);
      }
    }
  }

  /**
   * Assembles API-specific servers.
   *
   * <p>This method adds API-specific servers including current request server
   * and configured API servers to the server list.</p>
   *
   * @param apis    the API to get servers from
   * @param servers the list to add API servers to
   */
  private void assembleApiServers(Apis apis, List<Server> servers) {
    // Assemble current request server
    if (nonNull(apis.getCurrentServer()) && isNotEmpty(apis.getCurrentServer().getUrl())) {
      apis.getCurrentServer().addExtension(SERVER_SOURCE_KEY, CURRENT_REQUEST.getValue());
      servers.add(apis.getCurrentServer());
    }
    // Assemble API configuration servers
    if (isNotEmpty(apis.getServers())) {
      for (Server server : apis.getServers()) {
        if (isNotEmpty(server.getUrl())) {
          // Add source extension to identify server origin
          server.addExtension(SERVER_SOURCE_KEY, API_SERVERS.getValue());
        }
      }
      servers.addAll(apis.getServers());
    }
  }

  /**
   * Sets OpenAPI path reference models for an API.
   *
   * <p>This method resolves OpenAPI references and sets the resolved
   * reference models for the API.</p>
   *
   * @param apisDb the API to set reference models for
   */
  @Override
  public void setOpenApiPathRefModels(Apis apisDb) {
    Map<String, String> allRefModels = findApisAllRef(apisDb);
    apisDb.setResolvedRefModels(allRefModels);
  }

  /**
   * Finds all OpenAPI references for an API.
   *
   * <p>This method extracts all $ref properties from an API's OpenAPI specification
   * and resolves them using service component models.</p>
   *
   * @param apisDb the API to find references for
   * @return map of reference paths to resolved model content
   * @throws RuntimeException if JSON processing fails
   */
  @NotNull
  @Override
  public Map<String, String> findApisAllRef(Apis apisDb) {
    Map<String, String> allRefModels = new HashMap<>();
    try {
      Set<String> refs = RefResolver.findPropertyValues(Json31.pretty(apisDb), "$ref");
      if (isNotEmpty(refs)) {
        Map<String, String> compModelMap = servicesCompQuery.findByServiceId(apisDb.getServiceId())
            .stream().collect(Collectors.toMap(ServicesComp::getRef, ServicesComp::getModel));
        ApisConverter.findAllRef0(allRefModels, refs, compModelMap);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return allRefModels;
  }

  /**
   * Converts basic API information to summary format.
   *
   * <p>This method transforms basic API information into summary format
   * for reporting and display purposes.</p>
   *
   * @param apis the list of basic API information to convert
   * @return list of API information summaries or null if input is empty
   */
  //@NameJoin
  public static List<ApisInfoSummary> getApisInfoSummary(List<ApisBasicInfo> apis) {
    return isEmpty(apis) ? null
        : apis.stream().map(ApisConverter::toApisInfoSummary).toList();
  }

  /**
   * Converts API detail information to summary format.
   *
   * <p>This method transforms detailed API information into summary format
   * for reporting and display purposes.</p>
   *
   * @param apis the API detail information to convert
   * @return API detail summary
   */
  @NameJoin
  public static ApisDetailSummary getApisDetailSummary(Apis apis) {
    return toApisDetailSummary(apis);
  }


}
