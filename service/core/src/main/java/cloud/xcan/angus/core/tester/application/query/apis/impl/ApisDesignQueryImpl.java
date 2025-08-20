package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_SERVICE_DESIGN_EXISTED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.apis.ApisDesignQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesign;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API design query operations for design management.
 *
 * <p>This class provides comprehensive functionality for querying and managing
 * API designs, including design details, pagination, search, and validation.</p>
 *
 * <p>It handles design lifecycle management, service synchronization,
 * OpenAPI content management, and comprehensive data enrichment.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>API design detail and info queries with pagination</li>
 *   <li>Full-text search capabilities for design content</li>
 *   <li>Service synchronization and OpenAPI content management</li>
 *   <li>Design source validation and permission checking</li>
 *   <li>Service name enrichment and association</li>
 *   <li>Project member permission validation</li>
 *   <li>Latest content retrieval and formatting</li>
 * </ul></p>
 */
@Biz
public class ApisDesignQueryImpl implements ApisDesignQuery {

  @Resource
  private ApisDesignRepo apisDesignRepo;
  @Resource
  private ApisDesignInfoRepo apisDesignInfoRepo;
  @Resource
  private ApisDesignInfoSearchRepo apisDesignInfoSearchRepo;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed API design information with comprehensive enrichment.
   *
   * <p>This method fetches complete API design details with extensive enrichment
   * including latest OpenAPI content, service information, and permission validation.</p>
   *
   * <p>The method validates user permissions based on design source type
   * and handles service synchronization for latest content retrieval.</p>
   *
   * @param id the API design ID to retrieve details for
   * @return the detailed API design information with all enrichments
   * @throws ResourceNotFound if the design is not found
   */
  @Override
  public ApisDesign detail(Long id) {
    return new BizTemplate<ApisDesign>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Verify design exists and retrieve design info
        designDb = checkAndFind(id);
        // Verify user has appropriate view permissions based on design source
        if (designDb.getDesignSource().isSynchronousService()) {
          servicesAuthQuery.checkViewAuth(getUserId(), designDb.getDesignSourceId());
        } else {
          // Verify user has project member permissions
          projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
        }
      }

      @Override
      protected ApisDesign process() {
        // Fetch and set the latest design content information
        if (designDb.hasLatestContent()) {
          // Retrieve latest OpenAPI content from service schema
          OpenAPI openapi = servicesSchemaQuery.openapiDetail0(designDb.getDesignSourceId(),
              null, false);
          designDb.setLatestOpenapi(Json31.pretty(openapi));
        } else if (isNotEmpty(designDb.getOpenapi())) {
          // Use existing OpenAPI content if available
          designDb.setLatestOpenapi(designDb.getOpenapi());
        }

        // Enrich design with service name information
        setServicesName(designDb);
        return designDb;
      }
    }.execute();
  }

  /**
   * Lists API designs with pagination, filtering, and optional full-text search.
   *
   * <p>This method retrieves API designs based on specification criteria with support
   * for pagination and optional full-text search capabilities.</p>
   *
   * <p>The method automatically enriches design data with service names
   * and user information for enhanced display.</p>
   *
   * @param spec the specification for filtering API designs
   * @param pageable the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match the full-text search match fields
   * @return a page of API designs with enriched data
   */
  @Override
  public Page<ApisDesignInfo> list(GenericSpecification<ApisDesignInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisDesignInfo>>() {
      @Override
      protected void checkParams() {
        // Verify user has project member permissions
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisDesignInfo> process() {
        // Execute search with full-text or standard search
        Page<ApisDesignInfo> page = fullTextSearch
            ? apisDesignInfoSearchRepo.find(spec.getCriteria(), pageable,
            ApisDesignInfo.class, match)
            : apisDesignInfoRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          // Enrich designs with service names and user information
          setServicesName(page.getContent());
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<ApisDesignInfo> findByIds(HashSet<Long> ids) {
    return apisDesignInfoRepo.findAllById(ids);
  }

  @Override
  public ApisDesign checkAndFind(Long id) {
    return apisDesignRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApisDesign"));
  }

  @Override
  public ApisDesignInfo checkAndFindInfo(Long id) {
    return apisDesignInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApisDesign"));
  }

  /**
   * Checks if a service already has an associated API design.
   *
   * <p>This method validates that a service does not already have an API design,
   * preventing duplicate design creation for the same service.</p>
   *
   * @param serviceId the service ID to check for existing designs
   * @throws ResourceExisted if a design already exists for the service
   */
  @Override
  public void checkServiceExisted(Long serviceId) {
    ApisDesignInfo design = apisDesignInfoRepo.findByDesignSourceId(serviceId);
    if (nonNull(design)) {
      assertResourceExisted(design, APIS_SERVICE_DESIGN_EXISTED_T, new Object[]{design.getName()});
    }
  }

  /**
   * Sets service name for a single API design.
   *
   * <p>This method enriches an API design with its associated service name
   * for enhanced display and identification.</p>
   *
   * @param design the API design to enrich with service name
   */
  @Override
  public void setServicesName(ApisDesign design) {
    if (nonNull(design.getDesignSourceId())) {
      Services services = servicesQuery.find0(design.getDesignSourceId());
      if (nonNull(services)) {
        design.setDesignSourceName(services.getName());
      }
    }
  }

  /**
   * Sets service names for multiple API designs in batch.
   *
   * <p>This method enriches multiple API designs with their associated service names
   * for enhanced display and identification, using efficient batch processing.</p>
   *
   * @param designs the list of API designs to enrich with service names
   */
  @Override
  public void setServicesName(List<ApisDesignInfo> designs) {
    Set<Long> servicesIds = designs.stream().map(ApisDesignInfo::getDesignSourceId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    if (!servicesIds.isEmpty()) {
      Map<Long, Services> servicesMap = servicesQuery.find0ByIds(servicesIds).stream()
          .collect(Collectors.toMap(Services::getId, x -> x));
      for (ApisDesignInfo design : designs) {
        design.setDesignSourceName(servicesMap.getOrDefault(design.getDesignSourceId(),
            new Services()).getName());
      }
    }
  }
}
