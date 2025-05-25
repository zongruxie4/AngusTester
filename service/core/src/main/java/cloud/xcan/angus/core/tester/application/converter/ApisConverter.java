package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.in;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.convert;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.isLastMonth;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.isLastWeek;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.ApisTargetType;
import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.apis.summary.ApisDetailSummary;
import cloud.xcan.angus.core.tester.domain.apis.summary.ApisInfoSummary;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.infra.util.RefResolver;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.core.utils.GsonUtils;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.http.HttpMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author XiaoLong Liu
 */
public class ApisConverter {

  public static Apis toCloneApis(Apis apisDb, Services serviceDb) {
    return new Apis().setProjectId(apisDb.getProjectId())
        .setServiceId(apisDb.getServiceId())
        .setSource(apisDb.getSource())
        .setImportSource(apisDb.getImportSource())
        .setProtocol(apisDb.getProtocol())
        .setMethod(apisDb.getMethod())
        .setEndpoint(apisDb.getEndpoint())
        /////// OpenAPI document start
        .setTags(apisDb.getTags())
        .setSummary(apisDb.getSummary())
        .setDescription(apisDb.getDescription())
        .setOperationId(apisDb.getOperationId())
        .setParameters(apisDb.getParameters())
        .setRequestBody(apisDb.getRequestBody())
        .setResponses(apisDb.getResponses())
        .setDeprecated(apisDb.getDeprecated())
        .setSecurity(apisDb.getSecurity())
        .setCurrentServer(apisDb.getCurrentServer())
        .setServers(apisDb.getServers())
        .setExtensions(apisDb.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(apisDb.getAuthentication())
        .setAssertions(apisDb.getAssertions())
        .setOwnerId(apisDb.getOwnerId())
        .setStatus(ApiStatus.UNKNOWN)
        .setAuth(apisDb.getAuth())
        .setServiceAuth(serviceDb.getAuth())
        .setSecurity(apisDb.getSecurity())
        .setTestFunc(apisDb.getTestFunc())
        .setTestFuncPassed(apisDb.getTestFuncPassed())
        .setTestFuncFailureMessage(apisDb.getTestFuncFailureMessage())
        .setTestPerf(apisDb.getTestPerf())
        .setTestPerfPassed(apisDb.getTestPerfPassed())
        .setTestPerfFailureMessage(apisDb.getTestPerfFailureMessage())
        .setTestStability(apisDb.getTestStability())
        .setTestStabilityPassed(apisDb.getTestStabilityPassed())
        .setTestStabilityFailureMessage(apisDb.getTestStabilityFailureMessage())
        .setFollow(apisDb.getFollow())
        .setServiceDeleted(false)
        .setDeleted(false);
  }

  public static void retainReplaceField(List<Apis> updateApis, List<Apis> updateApisDbs) {
    for (Apis api : updateApis) {
      for (Apis apisDb : updateApisDbs) {
        if (apisDb.getId().equals(api.getId())) {
          api.setProjectId(apisDb.getProjectId())
              .setServiceId(apisDb.getServiceId())
              .setSource(apisDb.getSource())
              .setImportSource(apisDb.getImportSource())
              .setStatus(apisDb.getStatus())
              .setAuth(apisDb.getAuth())
              .setServiceAuth(apisDb.getServiceAuth())
              //.setSecurity(apisDb.getSecurity())
              .setDatasetActionOnEOF(
                  nullSafe(api.getDatasetActionOnEOF(), apisDb.getDatasetActionOnEOF()))
              .setDatasetSharingMode(
                  nullSafe(api.getDatasetSharingMode(), apisDb.getDatasetSharingMode()))
              .setTestFunc(apisDb.getTestFunc())
              .setTestFuncPassed(apisDb.getTestFuncPassed())
              .setTestFuncFailureMessage(apisDb.getTestFuncFailureMessage())
              .setTestPerf(apisDb.getTestPerf())
              .setTestPerfPassed(apisDb.getTestPerfPassed())
              .setTestPerfFailureMessage(apisDb.getTestPerfFailureMessage())
              .setTestStability(apisDb.getTestStability())
              .setTestStabilityPassed(apisDb.getTestStabilityPassed())
              .setTestStabilityFailureMessage(apisDb.getTestStabilityFailureMessage())
              .setSyncName(apisDb.getSyncName())
              .setExtSearchMerge(apisDb.getExtSearchMerge())
              .setServiceDeleted(apisDb.getServiceDeleted())
              .setDeleted(apisDb.getDeleted())
              .setDeletedBy(apisDb.getDeletedBy())
              .setDeletedDate(apisDb.getDeletedDate());
        }
      }
    }
  }

  public static List<Apis> cloneApis(UidGenerator uidGenerator,
      List<Apis> apisDb, Services project) {
    return apisDb.stream().map(a -> {
      Apis cloneApis = new Apis();
      CoreUtils.copyProperties(a, cloneApis);
      cloneApis.setServiceId(project.getId())
          .setServiceName(project.getName())
          .setId(uidGenerator.getUID())
          .setServiceDeleted(false)
          .setDeleted(false)
          .setStatus(ApiStatus.UNKNOWN);
      return cloneApis;
    }).collect(Collectors.toList());
  }

  public static ApisTrash toApisTrash(ApisBaseInfo apisDb) {
    return new ApisTrash()
        .setProjectId(apisDb.getProjectId())
        .setServicesId(apisDb.getServiceId())
        .setTargetType(ApisTargetType.API)
        .setTargetId(apisDb.getId())
        .setTargetName(apisDb.getName())
        .setCreatedBy(apisDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static ApisUnarchived objectArrToApisUnarchived(Object[] objects) {
    ApisUnarchived apis = new ApisUnarchived()
        .setId(convert(objects[0], Long.class))
        .setProtocol(convert(objects[1], ApisProtocol.class))
        .setSummary(convert(objects[2], String.class))
        .setEndpoint(convert(objects[3], String.class))
        .setMethod(convert(objects[4], HttpMethod.class));
    apis.setCreatedBy(convert(objects[5], Long.class));
    apis.setCreatedDate(convert(objects[6], LocalDateTime.class));
    apis.setLastModifiedBy(convert(objects[7], Long.class));
    apis.setLastModifiedDate(convert(objects[8], LocalDateTime.class));
    return apis;
  }

  public static ApisBasicInfo objectArrToApis(Object[] objects) {
    ApisBasicInfo apis = new ApisBasicInfo().setId(convert(objects[0], Long.class))
        .setSummary(convert(objects[1], String.class))
        .setOperationId(convert(objects[2], String.class))
        .setServiceId(convert(objects[3], Long.class))
        .setAuth(convert(objects[4], Boolean.class))
        .setServiceAuth(convert(objects[5], Boolean.class))
        .setSource(convert(objects[6], ApiSource.class))
        .setImportSource(convert(objects[7], ApiImportSource.class))
        .setOwnerId(convert(objects[8], Long.class));
    apis.setCreatedBy(convert(objects[9], Long.class));
    apis.setCreatedDate(convert(objects[10], LocalDateTime.class));
    apis.setLastModifiedBy(convert(objects[11], Long.class));
    apis.setLastModifiedDate(convert(objects[12], LocalDateTime.class));
    apis.setEndpoint(convert(objects[13], String.class));
    apis.setMethod(convert(objects[14], HttpMethod.class));
    apis.setStatus(convert(objects[15], ApiStatus.class));
    if (nonNull(objects[16])) {
      apis.setTags(GsonUtils.fromJson(objects[16].toString(), new TypeToken<List<String>>() {
      }.getType()));
    }
    apis.setProtocol(convert(objects[17], ApisProtocol.class));
    apis.setDeprecated(convert(objects[18], Boolean.class));
    apis.setTestFunc(convert(objects[19], Boolean.class))
        .setTestFuncPassed(convert(objects[20], Boolean.class))
        .setTestFuncFailureMessage(convert(objects[21], String.class));
    apis.setTestPerf(convert(objects[22], Boolean.class))
        .setTestPerfPassed(convert(objects[23], Boolean.class))
        .setTestPerfFailureMessage(convert(objects[24], String.class));
    apis.setTestStability(convert(objects[25], Boolean.class))
        .setTestStabilityPassed(convert(objects[26], Boolean.class))
        .setTestStabilityFailureMessage(convert(objects[27], String.class));
    return apis;
  }

  public static void assembleApiAuthInfo(Apis api, Services projectDb) {
    api.setAuth(nonNull(api.getAuth()) ? api.getAuth() : projectDb.getAuth());
    api.setServiceAuth(projectDb.getAuth());
    if (PrincipalContextUtils.isJobOrInnerApi()) {
      api.setTenantId(projectDb.getTenantId());
      api.setOwnerId(projectDb.getCreatedBy());
      api.setCreatedBy(projectDb.getCreatedBy());
    }
    if (isEmpty(api.getId())) {
      api.setId(getBean(CachedUidGenerator.class).getUID());
    }
  }

  public static void assembleUnarchivedInfo(Apis api, ApisUnarchived apisUnarchived) {
    if (isEmpty(api.getParameters()) && nonNull(apisUnarchived.getParameters())) {
      api.setParameters(apisUnarchived.getParameters());
    }
    if (isNull(api.getAuthentication()) && nonNull(apisUnarchived.getAuthentication())) {
      api.setAuthentication(apisUnarchived.getAuthentication());
    }
    if (isNull(api.getRequestBody()) && nonNull(apisUnarchived.getRequestBody())) {
      api.setRequestBody(apisUnarchived.getRequestBody());
    }
    if (isNull(api.getResponses()) && nonNull(apisUnarchived.getResponses())) {
      api.setResponses(apisUnarchived.getResponses());
    }
  }

  public static Apis toSchemaApis(Operation operation) {
    Server currentServer = isNotEmpty(operation.getServers())
        ? operation.getServers().get(0) : new Server().url("");
    URL url = currentServer.toUrl();
    Apis apis = new Apis()
        .setProtocol(isNull(url) ? ApisProtocol.http : ApisProtocol.valueOf(url.getProtocol()))
        .setMethod(HttpMethod.valueOf(operation.method.toUpperCase()))
        // Fix:: Uri cannot be a null value, must be safe to ""
        .setEndpoint(nullSafe(operation.endpoint, ""))
        .setCurrentServer(currentServer)
        .setTags(operation.getTags())
        .setSummary(operation.getSummary())
        .setDescription(operation.getDescription())
        .setExternalDocs(operation.getExternalDocs())
        .setOperationId(operation.getOperationId())
        .setParameters(operation.getParameters())
        .setRequestBody(operation.getRequestBody())
        .setResponses(nonNull(operation.getResponses()) ? operation.getResponses() : null)
        .setDeprecated(nullSafe(operation.getDeprecated(), false))
        .setSecurity(operation.getSecurity())
        .setServers(operation.getServers())
        .setExtensions(operation.getExtensions())
        .setSchemaHash(operation.hashCode())
        .setAuthentication(operation.getAuthentication());
    apis.setEndpoint(apis.getEndpoint());
    return apis;
  }

  public static Paths toPaths(Map<String, List<Apis>> apisMap) {
    Paths paths = new Paths();
    for (String path : apisMap.keySet()) {
      List<Apis> methodOperations = apisMap.get(path);
      paths.put(path, toPathItem(methodOperations));
    }
    return paths;
  }

  public static PathItem toPathItem(List<Apis> methodOperations) {
    PathItem pathItem = new PathItem();
    for (Apis apis : methodOperations) {
      HttpMethod method = apis.getMethod();
      if (HttpMethod.GET.equals(method)) {
        pathItem.setGet(toOperations(apis));
      }
      if (HttpMethod.HEAD.equals(method)) {
        pathItem.setHead(toOperations(apis));
      }
      if (HttpMethod.POST.equals(method)) {
        pathItem.setPost(toOperations(apis));
      }
      if (HttpMethod.PUT.equals(method)) {
        pathItem.setPut(toOperations(apis));
      }
      if (HttpMethod.PATCH.equals(method)) {
        pathItem.setPatch(toOperations(apis));
      }
      if (HttpMethod.DELETE.equals(method)) {
        pathItem.setDelete(toOperations(apis));
      }
      if (HttpMethod.OPTIONS.equals(method)) {
        pathItem.setOptions(toOperations(apis));
      }
      if (HttpMethod.TRACE.equals(method)) {
        pathItem.setTrace(toOperations(apis));
      }
    }
    return pathItem;
  }

  public static Operation toOperations(Apis apis) {
    ApiResponses responses = new ApiResponses();
    if (isNotEmpty(apis.getResponses())) {
      responses.putAll(apis.getResponses());
    }
    Map<String, Object> extensions = getExtensions(apis);
    return new Operation().tags(apis.getTags())
        .summary(apis.getSummary())
        .description(apis.getDescription())
        .externalDocs(apis.getExternalDocs())
        .operationId(apis.getOperationId())
        .parameters(apis.getParameters())
        .requestBody(apis.getRequestBody())
        .responses(responses)
        //.callbacks(apis.getCallbacks())
        .deprecated(apis.getDeprecated())
        .security(apis.getSecurity())
        .servers(apis.getServers())
        .extensions(extensions);
  }

  private static Map<String, Object> getExtensions(Apis apis) {
    Map<String, Object> extensions = apis.getExtensions();
    if (isNull(extensions)) {
      extensions = new HashMap<>();
    }
    if (nonNull(apis.getStatus())) {
      extensions.put(ExtensionKey.STATUS_KEY, apis.getStatus().getValue());
    }
    return extensions;
  }

  public static Apis assembleSchemaToAddApis(Apis apis, Services serviceDb,
      Components components, ApiSource apiSource, ApiImportSource importSource, String syncName) {
    // Find authentication from the headers -> @see Operation#getAuthentication()
    SecurityScheme authentication = apis.getAuthentication();
    // Find the first authentication from the component as the default
    if (isNull(authentication)) {
      if (nonNull(apis.getSecurity()) && nonNull(components)
          && nonNull(components.getSecuritySchemes())) {
        for (SecurityRequirement securityRequirement : apis.getSecurity()) {
          authentication = components.getSecuritySchemes()
              .get(securityRequirement.keySet().stream().findFirst().orElse(""));
          if (nonNull(authentication)) {
            break;
          }
        }
      }
    }
    return apis.setSource(apiSource)
        .setImportSource(importSource)
        .setProjectId(serviceDb.getProjectId())
        .setServiceId(serviceDb.getId())
        .setDeprecated(apis.getDeprecated())
        .setAuthentication(authentication)
        .setAssertions(null)
        .setOwnerId(getUserId())
        .setStatus(ApiStatus.UNKNOWN)
        .setAuth(true) // -> Fix:: Security Bug
        .setServiceAuth(serviceDb.getAuth())
        .setSecured(nonNull(apis.getSecurity()))
        .setTestFunc(true)
        .setTestPerf(true)
        .setTestStability(true)
        .setSyncName(syncName)
        .setServiceDeleted(false)
        .setDeleted(false);
  }

  public static void assembleSchemaToUpdateApis(Apis apisDb, Apis openApis) {
    // Note:: Will not modify currentServer and authentication.
    apisDb.setTags(openApis.getTags())
        .setSummary(openApis.getSummary())
        .setDescription(openApis.getDescription())
        .setExternalDocs(openApis.getExternalDocs())
        .setOperationId(openApis.getOperationId())
        .setParameters(openApis.getParameters())
        .setRequestBody(openApis.getRequestBody())
        .setResponses(openApis.getResponses())
        .setDeprecated(isNull(apisDb.getDeprecated()) ? nullSafe(openApis.getDeprecated(), false)
            : apisDb.getDeprecated())
        .setSecurity(openApis.getSecurity())
        //.setCurrentServer(null) <- NOOP
        .setServers(openApis.getServers())
        .setExtensions(openApis.getExtensions())
        .setSchemaHash(openApis.getSchemaHash());
    //.setAuthentication(null) <- NOOP
  }

  public static ApisInfoSummary toApisInfoSummary(ApisBasicInfo apis) {
    return new ApisInfoSummary().setId(apis.getId())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setProjectId(apis.getServiceId())
        //.setProjectName(apis.getServiceName())
        .setServiceId(apis.getServiceId())
        .setProtocol(apis.getProtocol())
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        /////// OpenAPI document start
        .setTags(apis.getTags())
        .setSummary(apis.getSummary())
        .setDescription(apis.getDescription())
        .setOperationId(apis.getOperationId())
        //.setParameters(apis.getParameters())
        //.setRequestBody(apis.getRequestBody())
        //.setResponses(apis.getResponses())
        .setDeprecated(apis.getDeprecated())
        //.setSecurity(apis.getSecurity())
        //.setAvailableServers(apis.getAvailableServers())
        //.setExtensions(apis.getExtensions())
        /////// OpenAPI document end
        //.setAuthentication(apis.getAuthentication())
        //.setAssertions(apis.getAssertions())
        .setOwnerId(apis.getOwnerId())
        .setStatus(apis.getStatus())
        //.setTagSchemas(apis.getTagSchemas())
        .setAuth(apis.getAuth())
        .setServiceAuth(apis.getServiceAuth())
        .setSecured(apis.getSecured())
        .setTestFunc(apis.getTestFunc())
        .setTestFuncPassed(apis.getTestFuncPassed())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerf(apis.getTestPerf())
        .setTestPerfPassed(apis.getTestPerfPassed())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStability(apis.getTestStability())
        .setTestStabilityPassed(apis.getTestStabilityPassed())
        .setTestStabilityFailureMessage(apis.getTestStabilityFailureMessage())
        //.setFavourite(apis.getFavourite())
        //.setFollow(apis.getFollow())
        //.setSyncName(apis.getSyncName())
        //.setResolvedRefModels(apis.getResolvedRefModels())
        //.setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate());
  }

  public static ApisDetailSummary toApisDetailSummary(Apis apis) {
    return new ApisDetailSummary().setId(apis.getId())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setProjectId(apis.getServiceId())
        //.setProjectName(apis.getServiceName())
        .setServiceId(apis.getServiceId())
        .setProtocol(apis.getProtocol())
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        /////// OpenAPI document start
        .setTags(apis.getTags())
        .setSummary(apis.getSummary())
        .setDescription(apis.getDescription())
        .setOperationId(apis.getOperationId())
        .setParameters(apis.getParameters())
        .setRequestBody(apis.getRequestBody())
        .setResponses(apis.getResponses())
        .setDeprecated(apis.getDeprecated())
        .setSecurity(apis.getSecurity())
        .setAvailableServers(apis.getAvailableServers())
        .setExtensions(apis.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(apis.getAuthentication())
        .setAssertions(apis.getAssertions())
        .setOwnerId(apis.getOwnerId())
        .setStatus(apis.getStatus())
        .setTagSchemas(apis.getTagSchemas())
        .setAuth(apis.getAuth())
        .setServiceAuth(apis.getServiceAuth())
        .setSecurity(apis.getSecurity())
        .setTestFunc(apis.getTestFunc())
        .setTestFuncPassed(apis.getTestFuncPassed())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerf(apis.getTestPerf())
        .setTestPerfPassed(apis.getTestPerfPassed())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStability(apis.getTestStability())
        .setTestStabilityPassed(apis.getTestStabilityPassed())
        .setTestStabilityFailureMessage(apis.getTestStabilityFailureMessage())
        .setFavourite(apis.getFavourite())
        .setFollow(apis.getFollow())
        .setSyncName(apis.getSyncName())
        .setResolvedRefModels(apis.getResolvedRefModels())
        .setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate());
  }


  public static void countCreationService(ApisResourcesCreationCount result,
      List<Services> services) {
    result.setAllService(services.size())
        .setServiceByLastWeek(services.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setServiceByLastMonth(
            services.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<ApiStatus, List<Services>> statuseMap = services.stream()
        .collect(Collectors.groupingBy(Services::getStatus));
    for (ApiStatus value : ApiStatus.values()) {
      result.getServicesByStatus().put(value, statuseMap.getOrDefault(value, emptyList()).size());
    }
  }

  public static void countCreationApis(ApisResourcesCreationCount result, List<ApisBaseInfo> apis) {
    result.setAllApis(apis.size())
        .setApisByLastWeek(apis.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setApisByLastMonth(apis.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<ApiStatus, List<ApisBaseInfo>> statuseMap = apis.stream()
        .collect(Collectors.groupingBy(ApisBaseInfo::getStatus));
    for (ApiStatus value : ApiStatus.values()) {
      result.getApisByStatus().put(value, statuseMap.getOrDefault(value, emptyList()).size());
    }
    Map<HttpMethod, List<ApisBaseInfo>> methodMap = apis.stream()
        .collect(Collectors.groupingBy(ApisBaseInfo::getMethod));
    for (HttpMethod value : HttpMethod.values()) {
      result.getApisByMethod().put(value, methodMap.getOrDefault(value, emptyList()).size());
    }
  }

  public static void countCreationUnarchivedApis(ApisResourcesCreationCount result,
      List<ApisUnarchived> unarchivedApis) {
    result.setAllUnarchivedApis(unarchivedApis.size())
        .setUnarchivedApisByLastWeek(
            unarchivedApis.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setUnarchivedApisByLastMonth(
            unarchivedApis.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
  }

  public static Set<SearchCriteria> getApisResourcesStatsFilter(Long projectId, Long serviceId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdBys) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(serviceId)) {
      filters.add(equal("serviceId", serviceId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    if (isNotEmpty(createdBys)) {
      filters.add(in("createdBy", createdBys));
    }
    filters.add(equal("deleted", false));
    filters.add(equal("serviceDeleted", false));
    return filters;
  }

  public static void findAllRef0(Map<String, String> allRefModels, Set<String> refs,
      Map<String, String> compModelMap) throws JsonProcessingException {
    for (String ref : refs) {
      if (compModelMap.containsKey(ref)) {
        allRefModels.put(ref, compModelMap.get(ref));
        Set<String> refs0 = RefResolver.findPropertyValues(compModelMap.get(ref),
            // Prevent circular references
            "$ref").stream().filter(x -> !allRefModels.containsKey(x)).collect(Collectors.toSet());
        if (isNotEmpty(refs0)) {
          findAllRef0(allRefModels, refs0, compModelMap);
        }
      }
    }
  }
}
