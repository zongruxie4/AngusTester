package cloud.xcan.sdf.core.angustester.application.query.services.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.ServicesConverter.toServicesSummary;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_APIS_NOT_BELONG_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_NAME_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_PUBLISHED_CANNOT_MODIFY_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_PUBLISHED_CANNOT_MODIFY_T;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisTestQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesListRepo;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesRepo;
import cloud.xcan.sdf.core.angustester.domain.services.summary.ServicesSummary;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.repository.LongKeyCountSummary;
import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.model.services.ApisTestCount;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
@SummaryQueryRegister(name = "Services", table = "services",
    groupByColumns = {"created_date", "source", "auth_flag", "status", "type"})
public class ServicesQueryImpl implements ServicesQuery {

  @Resource
  private ServicesRepo servicesRepo;

  @Resource
  private ServicesListRepo servicesListRepo;

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
  public Services detail(Long id, Boolean joinSchemaFlag) {
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
        if (nonNull(joinSchemaFlag) && joinSchemaFlag) {
          PrincipalContext.addExtension("servicesSchema", servicesSchemaQuery.checkAndFind(id));
        }
        return project;
      }
    }.execute();
  }

  @Override
  public Page<Services> list(GenericSpecification<Services> spec, PageRequest pageable) {
    return new BizTemplate<Page<Services>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Services> process() {
        spec.getCriterias().add(SearchCriteria.equal("deletedFlag", 0));
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriterias());
        Page<Services> page = servicesListRepo.find(spec.getCriterias(), pageable,
            Services.class, null);
        setApisNum(page.getContent(), spec.getCriterias());
        return page;
      }
    }.execute();
  }

  @Override
  public MockService associationMockService(Long id) {
    return new BizTemplate<MockService>() {

      @Override
      protected void checkParams() {
        // NOOP
      }

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
      protected void checkParams() {
        // NOOP
      }

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
      protected void checkParams() {
        // NOOP
      }

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
    ProtocolAssert.assertResourceNotFound(isNotEmpty(services), serviceIds.iterator().next(),
        "Services");
    if (serviceIds.size() != services.size()) {
      for (Services project : services) {
        ProtocolAssert.assertResourceNotFound(serviceIds.contains(project.getId()), project.getId(),
            "Services");
      }
    }
    return services;
  }

  @Override
  public Boolean isAuthCtrl(Long id) {
    Boolean authFlag = servicesRepo.findAuthFlagById(id);
    return authFlag == null || authFlag;
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
    Long nameAndCreatedByExist =
        servicesRepo.countAll0ByNameAndAndProjectId(name, projectId);
    ProtocolAssert.assertResourceExisted(nameAndCreatedByExist < 1, SERVICE_NAME_REPEATED_T,
        new Object[]{name});
  }

  @Override
  public void checkPublishStatus(Services serviceDb) {
    BizAssert.assertTrue(isNull(serviceDb.getStatus())
            || !serviceDb.getStatus().isReleased(), SERVICE_PUBLISHED_CANNOT_MODIFY_CODE,
        SERVICE_PUBLISHED_CANNOT_MODIFY_T, new Object[]{serviceDb.getName()});
  }

  @Override
  public void setApisNum(List<Services> services, Set<SearchCriteria> criteria) {
    if (isNotEmpty(services)) {
      String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
      Set<SearchCriteria> filters = new HashSet<>();
      filters.add(SearchCriteria.equal("projectId", projectId));
      filters.add(SearchCriteria.equal("serviceDeletedFlag", 0));
      filters.add(SearchCriteria.equal("deletedFlag", 0));
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
    String clonedName =
        servicesRepo.existsByName(service.getName() + "-Copy") ? service.getName() + "-Copy."
            + saltName : service.getName() + "-Copy";
    clonedName = clonedName.length() > DEFAULT_NAME_LENGTH_X2 ? clonedName.substring(0,
        DEFAULT_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    service.setName(clonedName);
  }

}
