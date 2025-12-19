package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertUnauthorized;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SHARE_EXPIRED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SHARE_TOKEN_ERROR_T;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.SettingUserAssembler.toApiProxyVo;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.StringUtils.format;

import cloud.xcan.angus.api.manager.UserManager;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.apis.ApisShareQuery;
import cloud.xcan.angus.core.tester.application.query.config.SettingUserQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareRepo;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareSearchRepo;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API share query operations for shared API management.
 *
 * <p>This class provides comprehensive functionality for querying and managing
 * shared APIs, including share details, pagination, search, and access validation.</p>
 *
 * <p>It handles shared API lifecycle management, token validation,
 * expiration checking, and user access control with proper security measures.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>API share detail and list queries with pagination</li>
 *   <li>Full-text search capabilities for share content</li>
 *   <li>Share token validation and access control</li>
 *   <li>Share expiration checking and management</li>
 *   <li>User information enrichment</li>
 *   <li>Summary query registration for reporting</li>
 *   <li>Public access token (PAT) validation</li>
 * </ul></p>
 */
@SummaryQueryRegister(name = "ApisShare", table = "apis_share",
    groupByColumns = {"created_date", "target_type"}
)
@Service
public class ApisShareQueryImpl implements ApisShareQuery {

  @Resource
  private ApisShareRepo apisShareRepo;
  @Resource
  private ApisShareSearchRepo apisShareSearchRepo;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;
  @Resource
  private SettingUserQuery settingUserQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed API share information with URL generation.
   *
   * <p>This method fetches API share details and generates the complete
   * share URL with public access token for external access.</p>
   *
   * @param id the API share ID to retrieve details for
   * @return the detailed API share information with generated URL
   */
  @Override
  public ApisShare detail(Long id) {
    return new BizTemplate<ApisShare>() {
      ApisShare shareDb;

      @Override
      protected void checkParams() {
        // Verify API share exists and retrieve share info
        shareDb = checkAndFind(id);
      }

      @Override
      protected ApisShare process() {
        // Generate complete share URL with public access token
        shareDb.setUrl(format("%s?id=%s&pat=%s", shareDb.getBaseUrl(),
            shareDb.getId(), shareDb.getPat()));
        return shareDb;
      }
    }.execute();
  }

  /**
   * Lists API shares with pagination, filtering, and optional full-text search.
   *
   * <p>This method retrieves API shares based on specification criteria with support
   * for pagination and optional full-text search capabilities.</p>
   *
   * <p>The method automatically enriches share data with user information
   * for enhanced display.</p>
   *
   * @param spec           the specification for filtering API shares
   * @param pageable       the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the full-text search match fields
   * @return a page of API shares with enriched data
   */
  @Override
  public Page<ApisShare> list(GenericSpecification<ApisShare> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisShare>>() {

      @Override
      protected Page<ApisShare> process() {
        // Execute search with full-text or standard search
        Page<ApisShare> page = fullTextSearch
            ? apisShareSearchRepo.find(spec.getCriteria(), pageable, ApisShare.class, match)
            : apisShareRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          // Enrich shares with user name and avatar information
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }

  /**
   * Views an API share with public access token validation.
   *
   * <p>This method validates the public access token and retrieves API share
   * information with OpenAPI content for external access.</p>
   *
   * <p>The method includes expiration checking and API proxy information
   * for cross-tenant access.</p>
   *
   * @param id  the API share ID to view
   * @param pat the public access token for validation
   * @return the API share information with OpenAPI content
   * @throws UnauthorizedException    if token validation fails
   * @throws IllegalArgumentException if share is expired
   */
  @Override
  public ApisShare view(Long id, String pat) {
    return new BizTemplate<ApisShare>(false) {
      ApisShare shareDb;

      @Override
      protected void checkParams() {
        // Verify API share exists and retrieve share info
        shareDb = checkAndFind(id);
        // Verify public access token authorization
        assertUnauthorized(pat.equals(shareDb.getPat()), SERVICE_SHARE_TOKEN_ERROR_T);
        // Verify share is not expired
        assertTrue(shareDb.isNotExpired(), SERVICE_SHARE_EXPIRED);
      }

      @Override
      protected ApisShare process() {
        // Generate OpenAPI content for the shared APIs
        String openapi = servicesSchemaQuery.openapiDetail(shareDb.getServicesId(),
            shareDb.getApisIds(), SchemaFormat.json, false, true);
        shareDb.setOpenapi(openapi);
        // Retrieve API proxy information for cross-tenant access
        UserApiProxy proxy = settingUserQuery.findProxyByTenantId(shareDb.getTenantId());
        shareDb.setApiProxy(toApiProxyVo(proxy));
        return shareDb;
      }
    }.execute();
  }

  @Override
  public ApisShare checkAndFind(Long id) {
    return apisShareRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApisShare"));
  }

  @Override
  public List<ApisShare> checkAndFind(Collection<Long> ids) {
    List<ApisShare> shares = apisShareRepo.findAllById(ids);
    if (ids.size() != shares.size()) {
      ids.removeAll(shares.stream().map(ApisShare::getId).collect(Collectors.toSet()));
      assertResourceNotFound(isEmpty(ids), ids, "ApisShare");
    }
    return shares;
  }
}
