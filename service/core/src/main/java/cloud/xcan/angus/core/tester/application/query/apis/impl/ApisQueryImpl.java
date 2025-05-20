package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
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
import cloud.xcan.angus.core.biz.ProtocolAssert;
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
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
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
import cloud.xcan.angus.core.tester.infra.util.RefResolver;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.ObjectUtils;
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

  @Override
  public Apis detail(Long id, Boolean resolveRef) {
    return new BizTemplate<Apis>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check the to have view permission
        apisAuthQuery.checkViewAuth(getUserId(), id);

        // Check the apis exists
        apisDb = checkAndFind(id);
      }

      @Override
      protected Apis process() {
        // If the apis has been deleted, determine whether it is deleted by the current person
        BizAssert.assertTrue(!apisDb.getDeleted() || apisDb.getDeletedBy().equals(getUserId()),
            TRASH_NO_VIEW_PERMISSION_CODE, TRASH_NO_VIEW_PERMISSION);

        List<Apis> list = List.of(apisDb);
        if (isUserAction()) {
          // Set favourite state
          setFavourite(list);
          // Set follow state
          setFollow(list);
        }
        // Set associated mock apis
        setAssocMockApis(list);
        // Set available servers
        setAndGetAvailableServers(apisDb);
        // Set tag schemas
        List<Tag> tagSchemas = servicesSchemaQuery.checkAndFind(apisDb.getServiceId()).getTags();
        setTagSchemas(apisDb, tagSchemas);

        // Set OpenAPI ref models
        if (Objects.equals(resolveRef, true)) {
          setOpenApiPathRefModels(apisDb);
        }
        return apisDb;
      }
    }.execute();
  }

  @Override
  public MockApis findMockApis(Long id) {
    return new BizTemplate<MockApis>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = checkAndFindBaseInfo(id);

        // Check the to have view permission
        apisAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected MockApis process() {
        MockApis mockApis = mockApisRepo.findByAssocApisId(id);
        if (isNull(mockApis)) {
          return null;
        }

        if (!Objects.equals(apisDb.getEndpoint(), mockApis.getEndpoint()) ||
            !Objects.equals(apisDb.getMethod(), mockApis.getMethod())) {
          mockApis.setInconsistentOperation(true)
              .setEndpoint(apisDb.getEndpoint()).setMethod(apisDb.getMethod());
        } else {
          mockApis.setInconsistentOperation(false);
        }
        mockApis.setMockServiceName(mockApisRepo.findMockServiceNameById(mockApis.getId()));

        // Return apis parent mock service information
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
        // Set OpenAPI ref models
        if (isNotEmpty(apisDb) && Objects.equals(resolveRef, true)) {
          // Warn:: Services ID must belong to the same service
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

  @Override
  public Page<ApisBasicInfo> findByServiceId(Long serviceId,
      GenericSpecification<ApisBasicInfo> spec, PageRequest pageable, Class<ApisBasicInfo> clz) {
    return new BizTemplate<Page<ApisBasicInfo>>() {
      @Override
      protected void checkParams() {
        // Check the view permissions
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected Page<ApisBasicInfo> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(equal("serviceId", serviceId));
        criteria.add(equal("deleted", false));
        criteria.add(equal("serviceDeleted", false));

        return list0(criteria, pageable, clz);
      }
    }.execute();
  }

  @Override
  public Page<ApisBasicInfo> list(GenericSpecification<ApisBasicInfo> spec, PageRequest pageable,
      Class<ApisBasicInfo> clz) {
    return new BizTemplate<Page<ApisBasicInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisBasicInfo> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(equal("deleted", false));
        criteria.add(equal("serviceDeleted", false));

        return list0(criteria, pageable, clz);
      }
    }.execute();
  }

  private Page<ApisBasicInfo> list0(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<ApisBasicInfo> clz) {
    // Set authorization conditions when you are not an administrator or only query yourself
    commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
    Page<ApisBasicInfo> page = apisInfoListRepo.find(criteria, pageable, clz,
        ApisConverter::objectArrToApis, null);

    if (page.hasContent()) {
      if (isUserAction()) {
        // Set favourite state
        setFavourite(page.getContent());
        // Set follow state
        setFollow(page.getContent());
      }
      // Set associated mock apis
      setInfoAssocMockApis(page.getContent());
      // Set user name and avatar
      userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
          "avatar");
    }
    return page;
  }

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
        return setAndGetAvailableServers(apisDb);
      }
    }.execute();
  }

  @Override
  public ApisResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisResourcesCreationCount>() {

      @Override
      protected ApisResourcesCreationCount process() {
        final ApisResourcesCreationCount result = new ApisResourcesCreationCount();

        // Find all when condition is null, else find by condition
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);

        // Count services
        Set<SearchCriteria> serviceFilters = merge(commonFilters, equal("deleted", false));
        List<Services> services = servicesRepo.findAllByFilters(serviceFilters);
        countCreationService(result, services);

        // Count apis
        Set<SearchCriteria> apisFilters = merge(commonFilters, equal("deleted", false),
            equal("serviceDeleted", false));
        List<ApisBaseInfo> apis = apisBaseInfoRepo.findAllByFilters(apisFilters);
        countCreationApis(result, apis);

        // Count unarchived apis
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
   * Query all data including deleted
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

  @Override
  public Apis findDeRefById(Long id) {
    Apis apis = apisRepo.findById(id).orElse(null);
    if (nonNull(apis)) {
      setOpenApiPathRefModels(apis);
    }
    return apis;
  }

  @Override
  public Map<Long, SimpleActivityResource> getCaseSimpleActivityResourceMap(
      Collection<Long> caseIds) {
    return funcCaseInfoRepo.findAllById(caseIds).stream()
        .map(x -> new SimpleActivityResource()
            .setId(x.getId()).setName(x.getName()).setParentId(x.getProjectId()))
        .peek(x -> x.setTargetType(CombinedTargetType.FUNC_CASE))
        .collect(Collectors.toMap(SimpleActivityResource::getId, x -> x));
  }

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
          ).collect(Collectors.toList());
    }
    assertResourceNotFound(serviceApisDb, "Not matched apis found", new Object[]{});
    return serviceApisDb;
  }

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

  @Override
  public void checkAddServiceApisExisted(Collection<Apis> apis) {
    Map<Long, List<Apis>> serviceApisMap = apis.stream()
        .collect(Collectors.groupingBy(Apis::getServiceId));
    for (Long serviceId : serviceApisMap.keySet()) {
      // Note:: Uri cannot be a null value, must be safe to ""
      List<ApisBaseInfo> servicesApisDb = apisBaseInfoRepo.findAllByServiceIdAndEndpointIn(
          serviceId, serviceApisMap.get(serviceId).stream().map(Apis::getEndpoint)
              .collect(Collectors.toList()));
      if (ObjectUtils.isNotEmpty(servicesApisDb)) {
        for (ApisBaseInfo apiDb : servicesApisDb) {
          for (Apis api : serviceApisMap.get(serviceId)) {
            ProtocolAssert.assertResourceExisted(
                !Objects.equals(api.getMethod(), apiDb.getMethod()) ||
                    !Objects.equals(api.getEndpoint(), apiDb.getEndpoint()), APIS_OPERATION_EXISTED,
                new Object[]{api.getMethod() + " " + emptySafe(api.getEndpoint(), "\"\"")});
          }
        }
      }
    }
  }

  @Override
  public void checkServiceApisOperationNotExisted(Collection<Apis> apis, Collection<Apis> apisDb,
      Long serviceId, boolean replace) {
    // Modifying the apis may not modify the method and uri, means method and uri is nullable.
    List<Apis> updateApis = apis.stream().filter(x -> nonNull(x.getMethod())
        || nonNull(x.getEndpoint())).collect(Collectors.toList());
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

    // Fix:: Uri cannot be a null value, must be safe to ""
    List<ApisBaseInfo> servicesApisDb = apisBaseInfoRepo.findAllByServiceIdAndEndpointIn(
        serviceId, apis.stream().map(Apis::getEndpoint).collect(Collectors.toList()));
    if (isNotEmpty(servicesApisDb)) {
      for (ApisBaseInfo apiDb : servicesApisDb) {
        for (Apis api : apis) {
          ProtocolAssert.assertResourceExisted(
              !Objects.equals(api.getMethod(), apiDb.getMethod())
                  || !Objects.equals(api.getEndpoint(), apiDb.getEndpoint())
                  || Objects.equals(api.getId(), apiDb.getId()), APIS_OPERATION_EXISTED,
              new Object[]{api.getMethod() + " " + emptySafe(api.getEndpoint(), "\"\"")});
        }
      }
    }
  }

  /**
   * Check that only a maximum of apis can be added to a service
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

  @Override
  public void checkReleasedStatus(ApisBasicInfo apisInfoDb) {
    BizAssert.assertTrue(isNull(apisInfoDb.getStatus())
            || !apisInfoDb.getStatus().isReleased(), APIS_PUBLISHED_CANNOT_MODIFY_CODE,
        APIS_PUBLISHED_CANNOT_MODIFY_T, new Object[]{apisInfoDb.getName()});
  }

  @Override
  public void checkReleasedStatus(Apis apisDb) {
    BizAssert.assertTrue(isNull(apisDb.getStatus())
            || !apisDb.getStatus().isReleased(), APIS_PUBLISHED_CANNOT_MODIFY_CODE,
        APIS_PUBLISHED_CANNOT_MODIFY_T, new Object[]{apisDb.getName()});
  }

  @Override
  public void checkReleasedStatus(Collection<Apis> apisDbs) {
    for (Apis apiDb : apisDbs) {
      // Check the released api are not allowed to be modified
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

  @Override
  public List<Server> setAndGetAvailableServers(Apis apis) {
    List<Server> servers = new ArrayList<>();
    // Assemble api servers
    assembleApiServers(apis, servers);
    // Assemble parent service and service config servers
    assembleParentServiceServers(servers, apis.getId());
    // Assemble mock service server
    assembleMockServiceServers(servers, apis.getId());
    apis.setAvailableServers(servers);
    return servers;
  }

  @Override
  public void setAvailableServers(Apis apis, List<Server> parentServers) {
    List<Server> servers = new ArrayList<>();
    // Assemble api servers
    assembleApiServers(apis, servers);
    // Assemble parent service and service config servers
    // assembleParentProjectServers(servers, apis.getId());
    if (ObjectUtils.isNotEmpty(parentServers)) {
      servers.addAll(parentServers);
    }
    apis.setAvailableServers(servers);
  }

  @Override
  public void setAndGetRefAuthentication(Apis apis) {
    if (apis.isAuthSchemaRef()) {
      ServicesComp comp = servicesCompQuery.detailByRef(apis.getServiceId(),
          apis.getAuthentication().get$ref());
      if (isNull(comp)){
        log.warn("ServicesComp `{}` not found", apis.getAuthentication().get$ref());
        return;
      }
      SecurityScheme auth = comp.toComponent(SecurityScheme.class);
      apis.setRefAuthentication(auth);
    }
  }

  @Override
  public void setTagSchemas(Apis apisDb, List<Tag> tagSchemas) {
    if (isNotEmpty(apisDb.getTags()) && isNotEmpty(tagSchemas)) {
      apisDb.setTagSchemas(tagSchemas.stream().filter(x -> apisDb.getTags()
          .contains(x.getName())).collect(Collectors.toMap(Tag::getName, x -> x)));
    }
  }

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

  @Override
  public void assembleAndSendModifyNoticeEvent(List<ApisBasicInfo> apisDb,
      List<Activity> activities) {
    Map<Long, Activity> apisActivityMap = activities.stream()
        .collect(Collectors.toMap(Activity::getTargetId, x -> x));
    for (ApisBasicInfo apiDb : apisDb) {
      assembleAndSendModifyNoticeEvent(apiDb, apisActivityMap.get(apiDb.getId()));
    }
  }

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

  private Map<Long, MockApisAssocP> findApisMockIdsMap(Set<Long> apisIds) {
    if (isEmpty(apisIds)) {
      return Collections.emptyMap();
    }
    return mockApisRepo.findApisMockIdsMap(apisIds).stream().collect(
        Collectors.toMap(MockApisAssocP::getAssocApisId, x -> x));
  }

  private void assembleParentServiceServers(List<Server> servers, Long apiId) {
    Set<Long> parentIds = findParentIds(Collections.singletonList(apiId));
    if (isNotEmpty(parentIds)) {
      List<Server> parentServers = servicesSchemaQuery.findServersByServiceIds(parentIds);
      if (isNotEmpty(parentServers)) {
        for (Server server : parentServers) {
          if (isNotEmpty(server.getUrl())) {
            server.addExtension(SERVER_SOURCE_KEY, PARENT_SERVERS.getValue());
          }
        }
        servers.addAll(parentServers);
      }
    }
  }

  private void assembleMockServiceServers(List<Server> servers, Long apiId) {
    MockServiceInfo serviceInfo = mockServiceInfoRepo.findByApisId(apiId);
    if (nonNull(serviceInfo)) {
      if (isNotEmpty(serviceInfo.getServiceDomainUrl())) {
        Server server = new Server();
        server.setUrl(serviceInfo.getServiceDomainUrl());
        server.addExtension(SERVER_SOURCE_KEY, MOCK_SERVICE.getValue());
        servers.add(server);
      }
      if (isNotEmpty(serviceInfo.getServiceHostUrl())) {
        Server server = new Server();
        server.setUrl(serviceInfo.getServiceHostUrl());
        server.addExtension(SERVER_SOURCE_KEY, MOCK_SERVICE.getValue());
        servers.add(server);
      }
    }
  }

  private void assembleApiServers(Apis apis, List<Server> servers) {
    // Assemble current request server
    if (nonNull(apis.getCurrentServer()) && isNotEmpty(apis.getCurrentServer().getUrl())) {
      apis.getCurrentServer().addExtension(SERVER_SOURCE_KEY, CURRENT_REQUEST.getValue());
      servers.add(apis.getCurrentServer());
    }
    // Assemble apis config servers
    if (isNotEmpty(apis.getServers())) {
      for (Server server : apis.getServers()) {
        if (isNotEmpty(server.getUrl())) {
          server.addExtension(SERVER_SOURCE_KEY, API_SERVERS.getValue());
        }
      }
      servers.addAll(apis.getServers());
    }
  }

  @Override
  public void setOpenApiPathRefModels(Apis apisDb) {
    Map<String, String> allRefModels = findApisAllRef(apisDb);
    apisDb.setResolvedRefModels(allRefModels);
  }

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

  //@NameJoin
  public static List<ApisInfoSummary> getApisInfoSummary(List<ApisBasicInfo> apis) {
    return isEmpty(apis) ? null
        : apis.stream().map(ApisConverter::toApisInfoSummary).collect(Collectors.toList());
  }

  @NameJoin
  public static ApisDetailSummary getApisDetailSummary(Apis apis) {
    return toApisDetailSummary(apis);
  }


}
