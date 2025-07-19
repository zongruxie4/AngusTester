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
import cloud.xcan.angus.core.biz.Biz;
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

@Biz
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

  @Override
  public MockService associationMockService(Long id) {
    return new BizTemplate<MockService>() {


      @Override
      protected MockService process() {
        return mockServiceQuery.detailByProjectId(id);
      }
    }.execute();
  }

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

  @NameJoin
  public static ServicesSummary getServicesSummary(Services services) {
    return toServicesSummary(services);
  }

  @Override
  public Services find0(Long id) {
    return servicesRepo.find0ById(id).orElse(null);
  }

  @Override
  public List<Services> find0ByIds(Collection<Long> ids) {
    return servicesRepo.findAll0ByIdIn(ids);
  }

  @Override
  public List<Long> hasApisServiceIds(Collection<Long> serviceIds) {
    return apisRepo.findServiceIdByServiceIdIn(serviceIds);
  }

  @Override
  public Services checkAndFind(Long id) {
    return servicesRepo.findById(id).orElseThrow(
        () -> ResourceNotFound.of(id, "Services"));
  }

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

  @Override
  public Boolean isAuthCtrl(Long id) {
    Boolean auth = servicesRepo.findAuthById(id);
    return auth == null || auth;
  }

  @Override
  public void check(Long id) {
    if (!servicesRepo.existsById(id)) {
      throw ResourceNotFound.of(id, "Services");
    }
  }

  @Override
  public void checkQuota(long incr) {
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterServices,
        null, servicesRepo.count() + incr);
  }

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

  @Override
  public void checkNameExists(long projectId, String name) {
    // Include logic deleted project
    Long nameAndCreatedByExist = servicesRepo.countAll0ByNameAndAndProjectId(name, projectId);
    assertResourceExisted(nameAndCreatedByExist < 1, SERVICE_NAME_REPEATED_T, new Object[]{name});
  }

  @Override
  public void checkPublishStatus(Services serviceDb) {
    assertTrue(isNull(serviceDb.getStatus())
            || !serviceDb.getStatus().isReleased(), SERVICE_PUBLISHED_CANNOT_MODIFY_CODE,
        SERVICE_PUBLISHED_CANNOT_MODIFY_T, new Object[]{serviceDb.getName()});
  }

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
