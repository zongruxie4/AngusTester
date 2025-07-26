package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_API_CASE_NUM;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_OVER_LIMIT_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_OVER_LIMIT_T;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseRepo;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.infra.util.RefResolver;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of API case query operations for test case management.
 * 
 * <p>This class provides comprehensive functionality for querying and managing
 * API test cases, including case details, pagination, search, and validation.</p>
 * 
 * <p>It handles case lifecycle management, name validation, quota checking,
 * and comprehensive data enrichment for test case operations.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>API case detail and info queries with pagination</li>
 *   <li>Full-text search capabilities for case content</li>
 *   <li>Case name validation and uniqueness checking</li>
 *   <li>Case quota management and limit enforcement</li>
 *   <li>Reference resolution and authentication setup</li>
 *   <li>Case type validation and service association</li>
 *   <li>User information enrichment</li>
 *   <li>Safe clone name generation</li>
 * </ul></p>
 */
@Slf4j
@Biz
public class ApisCaseQueryImpl implements ApisCaseQuery {

  @Resource
  private ApisCaseRepo apisCaseRepo;
  @Resource
  private ApisCaseInfoRepo apisCaseInfoRepo;
  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;
  @Resource
  private ApisCaseInfoSearchRepo apisCaseInfoSearchRepo;
  @Resource
  private ServicesCompQuery servicesCompQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed API case information with comprehensive enrichment.
   * 
   * <p>This method fetches complete API case details with extensive enrichment
   * including API and service information, user details, and tag associations.</p>
   * 
   * <p>The method handles deleted API detection and user information
   * enrichment for enhanced display.</p>
   * 
   * @param id the API case ID to retrieve details for
   * @return the detailed API case information with all enrichments
   * @throws ResourceNotFound if the case is not found
   */
  @Override
  public ApisCase detail(Long id) {
    return new BizTemplate<ApisCase>() {

      @Override
      protected ApisCase process() {
        // Retrieve API case by ID or throw not found exception
        ApisCase apisCase = apisCaseRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "ApisCase"));

        // Prepare case list for enrichment operations
        List<ApisCase> cases = List.of(apisCase);

        // Set API and service information including deletion status
        setApisAndServiceInfo(cases);

        // Enrich with user name and avatar information
        userManager.setUserNameAndAvatar(cases, "createdBy");
        return apisCase;
      }
    }.execute();
  }

  /**
   * Lists API cases with pagination, filtering, and optional full-text search.
   * 
   * <p>This method retrieves API cases based on specification criteria with support
   * for pagination and optional full-text search capabilities.</p>
   * 
   * <p>The method automatically filters out deleted APIs and enriches case data
   * with API information and user details.</p>
   * 
   * @param spec the specification for filtering API cases
   * @param pageable the pagination and sorting parameters
   * @param fullTextSearch whether to use full-text search
   * @param match the full-text search match fields
   * @return a page of API cases with enriched data
   */
  @Override
  public Page<ApisCaseInfo> list(GenericSpecification<ApisCaseInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisCaseInfo>>() {

      @Override
      protected Page<ApisCaseInfo> process() {
        // Add filter to exclude cases from deleted APIs
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(equal("apisDeleted", false));

        // Execute search with full-text or standard search
        Page<ApisCaseInfo> page = fullTextSearch
            ? apisCaseInfoSearchRepo.find(criteria, pageable, ApisCaseInfo.class, match)
            : apisCaseInfoRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          // Set API deletion status and names for enhanced display
          setInfoApisNameAndDeleted(page.getContent());
          // Enrich with user name and avatar information
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<ApisCase> findByApisId(Long apisId) {
    return apisCaseRepo.findByApisId(apisId);
  }

  @Override
  public List<ApisCase> findByServicesIdAndType(Long servicesId, ApisCaseType caseType) {
    return apisCaseRepo.findByServicesIdAndType(servicesId, caseType);
  }

  @Override
  public Long countByServiceId(Long servicesId) {
    return apisCaseRepo.countByServicesId(servicesId);
  }

  /**
   * Checks for duplicate case names within an API.
   * 
   * <p>This method validates that case names are unique within a specific API,
   * preventing naming conflicts and ensuring data integrity.</p>
   * 
   * @param apisDb the API to check case names for
   * @param cases the list of cases to validate
   * @throws ResourceExisted if duplicate case names are found
   */
  @Override
  public void checkCaseNameExists(Apis apisDb, List<ApisCase> cases) {
    List<String> existedNames = apisCaseRepo.findNamesByNameInAndApisId(
        cases.stream().map(ApisCase::getName).collect(Collectors.toSet()), apisDb.getId());
    if (!existedNames.isEmpty()) {
      throw ResourceExisted.of(APIS_CASE_NAME_REPEATED_T, new Object[]{existedNames.get(0)});
    }
  }

  /**
   * Checks for duplicate case names during update operations with safe validation.
   * 
   * <p>This method validates case name uniqueness during update operations,
   * excluding the current case from the duplicate check to allow self-updates.</p>
   * 
   * @param apisDb the API to check case names for
   * @param cases the list of cases to validate for updates
   * @throws ResourceExisted if duplicate case names are found during update
   */
  @Override
  public void checkAndSafeUpdateNameExists(Apis apisDb, List<ApisCase> cases) {
    Set<String> updateNames = cases.stream().map(ApisCase::getName).collect(Collectors.toSet());
    if (isNotEmpty(updateNames)) {
      Map<String, ApisCaseInfo> caseInfosMap = apisCaseInfoRepo.findAllByNameInAndApisId(
              updateNames, apisDb.getId()).stream()
          .collect(Collectors.toMap(ApisCaseInfo::getName, x -> x));
      if (isEmpty(caseInfosMap)) {
        return;
      }
      for (ApisCase case0 : cases) {
        if (caseInfosMap.containsKey(case0.getName())
            && !caseInfosMap.get(case0.getName()).getId().equals(case0.getId())) {
          throw ResourceExisted.of(APIS_CASE_NAME_REPEATED_T, new Object[]{case0.getName()});
        }
      }
    }
  }

  @Override
  public ApisCase checkAndFind(Long id) {
    return apisCaseRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ApisCase"));
  }

  @Override
  public ApisCaseInfo checkAndFindInfo(Long id) {
    return apisCaseInfoRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ApisCase"));
  }

  @Override
  public List<ApisCase> checkAndFind(Collection<Long> ids) {
    List<ApisCase> cases = apisCaseRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(cases), ids.iterator().next(), "ApisCase");
    if (ids.size() != cases.size()) {
      for (ApisCase case0 : cases) {
        assertResourceNotFound(ids.contains(case0.getId()), case0.getId(), "ApisCase");
      }
    }
    return cases;
  }

  @Override
  public List<ApisCaseInfo> checkAndFindInfo(Collection<Long> ids) {
    List<ApisCaseInfo> cases = apisCaseInfoRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(cases), ids.iterator().next(), "ApisCase");
    if (ids.size() != cases.size()) {
      for (ApisCaseInfo case0 : cases) {
        assertResourceNotFound(ids.contains(case0.getId()), case0.getId(), "ApisCase");
      }
    }
    return cases;
  }

  @Override
  public void checkExistedCaseType(Long apisId, List<ApisCase> cases) {
    if (isNotEmpty(cases)) {
      for (ApisCase aCase : cases) {
        assertResourceExisted(!aCase.getType().isUnique()
                || !apisCaseInfoRepo.existsByApisIdAndType(apisId, aCase.getType()),
            String.format("Apis[%s] %s type case is Existed", apisId, aCase.getType().getValue()));
      }
    }
  }

  @Override
  public void checkUpdateNameExists(Long planId, String name, Long id) {
    assertResourceExisted(isEmpty(name)
            || apisCaseRepo.countByApisIdAndNameAndIdNot(planId, name, id) < 1,
        APIS_CASE_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * Checks if adding cases would exceed the API case quota limit.
   * 
   * <p>This method validates that adding the specified number of cases
   * would not exceed the maximum allowed cases per API.</p>
   * 
   * @param incr the number of cases to be added
   * @param apisId the API ID to check quota for
   * @throws QuotaException if adding cases would exceed the quota limit
   */
  @Override
  public void checkCaseQuota(int incr, Long apisId) {
    int count = apisCaseRepo.countByApisId(apisId);
    if (count + incr > MAX_API_CASE_NUM) {
      throw QuotaException.of(APIS_CASE_OVER_LIMIT_CODE, APIS_CASE_OVER_LIMIT_T,
          new Object[]{MAX_API_CASE_NUM});
    }
  }

  /**
   * Sets API deletion status and names for case information objects.
   * 
   * <p>This method enriches case information with API deletion status and names,
   * providing enhanced display information for cases.</p>
   * 
   * @param cases the list of case information objects to enrich
   */
  @Override
  public void setInfoApisNameAndDeleted(List<ApisCaseInfo> cases) {
    Set<Long> apisIds = cases.stream().map(ApisCaseInfo::getApisId).collect(Collectors.toSet());
    if (isNotEmpty(apisIds)) {
      List<ApisBaseInfo> existedApis = apisBaseInfoRepo.findAllById(apisIds);
      if (isNotEmpty(existedApis)) {
        Map<Long, ApisBaseInfo> apisMap = existedApis.stream()
            .collect(Collectors.toMap(ApisBaseInfo::getId, x -> x));
        Set<Long> existedIds = apisMap.keySet();
        // Set API name and deletion status for each case
        cases.forEach(c -> {
          if (nonNull(c.getApisId())) {
            if (existedIds.contains(c.getApisId())) {
              c.setApisDeleted(false);
              c.setApisSummary(apisMap.get(c.getApisId()).getSummary());
            } else {
              c.setApisDeleted(true);
            }
          }
        });
      }
    }
  }

  /**
   * Sets API and service information for case objects.
   * 
   * <p>This method enriches case objects with API deletion status, names,
   * and service information for comprehensive display.</p>
   * 
   * @param cases the list of case objects to enrich
   */
  @Override
  public void setApisAndServiceInfo(List<ApisCase> cases) {
    Set<Long> apisIds = cases.stream().map(ApisCase::getApisId).collect(Collectors.toSet());
    if (isNotEmpty(apisIds)) {
      List<ApisBaseInfo> existedApis = apisBaseInfoRepo.findAllById(apisIds);
      if (isNotEmpty(existedApis)) {
        Map<Long, ApisBaseInfo> apisMap = existedApis.stream()
            .collect(Collectors.toMap(ApisBaseInfo::getId, x -> x));
        Set<Long> existedIds = apisMap.keySet();
        // Set API name, deletion status, and service information for each case
        cases.forEach(c -> {
          if (nonNull(c.getApisId())) {
            if (existedIds.contains(c.getApisId())) {
              c.setApisDeleted(false);
              c.setApisSummary(apisMap.get(c.getApisId()).getSummary());
              c.setApisServiceId(apisMap.get(c.getApisId()).getServiceId());
            } else {
              c.setApisDeleted(true);
            }
          }
        });
        // Note: Project name info is handled by @NameJoin annotation
      }
    }
  }

  @NotNull
  @Override
  public Map<String, String> findCaseAllRef(ApisCase caseDb) {
    Map<String, String> allRefModels = new HashMap<>();
    try {
      Set<String> refs = RefResolver.findPropertyValues(Json31.pretty(caseDb), "$ref");
      if (isNotEmpty(refs)) {
        Map<String, String> compModelMap = servicesCompQuery.findByServiceId(caseDb.getServicesId())
            .stream().collect(Collectors.toMap(ServicesComp::getRef, ServicesComp::getModel));
        ApisConverter.findAllRef0(allRefModels, refs, compModelMap);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return allRefModels;
  }

  @Override
  public void setAndGetRefAuthentication(ApisCase case0) {
    if (case0.isAuthSchemaRef()) {
      ServicesComp comp = servicesCompQuery.detailByRef(case0.getServicesId(),
          case0.getAuthentication().get$ref());
      if (isNull(comp)) {
        log.warn("ServicesComp `{}` not found", case0.getAuthentication().get$ref());
        return;
      }
      SecurityScheme auth = comp.toComponent(SecurityScheme.class);
      case0.setRefAuthentication(auth);
    }
  }

  @Override
  public void setSafeCloneName(ApisCase apisCase) {
    String saltName = randomAlphanumeric(3);
    String clonedName = apisCaseRepo.existsByApisIdAndName(
        apisCase.getApisId(), apisCase.getName() + "-Copy")
        ? apisCase.getName() + "-Copy." + saltName : apisCase.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH_X4 ? clonedName.substring(0,
        MAX_NAME_LENGTH_X4 - 3) + saltName : clonedName;
    apisCase.setName(clonedName);
  }

}
