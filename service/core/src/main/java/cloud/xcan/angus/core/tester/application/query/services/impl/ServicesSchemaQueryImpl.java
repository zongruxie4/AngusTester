package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.toSchemaApis;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_IMPORT_APIS_PLUGIN_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_IMPORT_APIS_PLUGIN_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_TEXT_TRANSLATE_PLUGIN;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_NO_TEXT_TRANSLATE_PLUGIN_CODE;
import static cloud.xcan.angus.extension.angustester.api.utils.OpenApiParser.checkAndParseOpenApi;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.UrlEnvVariableChecker.containsEnvVariable;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;
import static io.swagger.v3.oas.models.extension.OpenAPIUtils.getExtensionLong;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.converter.ServicesCompConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.core.tester.domain.services.schema.ServiceServer;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchemaRepo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.extension.angustester.api.ApisParser;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationServiceProvider;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.GzipUtils;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.extension.OpenAPIUtils;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;

/**
 * <p>
 * Implementation of ServicesSchemaQuery for services schema management and query operations.
 * </p>
 * <p>
 * Provides methods for OpenAPI schema handling, server management, component resolution, and API parsing.
 * </p>
 */
@Biz
@Slf4j
public class ServicesSchemaQueryImpl implements ServicesSchemaQuery {

  @Resource
  private ServicesSchemaRepo servicesSchemaRepo;
  @Resource
  private ServicesCompQuery servicesCompQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;

  /**
   * <p>
   * Get detailed information of a service schema with caching.
   * </p>
   * <p>
   * Retrieves service schema details with Spring cache support for performance optimization.
   * Requires view permission for the service.
   * </p>
   * @param serviceId Service ID
   * @return Service schema entity
   */
  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema detail(Long serviceId) {
    return new BizTemplate<ServicesSchema>() {
      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected ServicesSchema process() {
        return servicesSchemaRepo.findByServiceId(serviceId)
            .orElseThrow(() -> ResourceNotFound.of(serviceId, "ServicesSchema"));
      }
    }.execute();
  }

  /**
   * <p>
   * Get OpenAPI specification in specified format with optional compression.
   * </p>
   * <p>
   * Generates OpenAPI specification for selected APIs with optional gzip compression.
   * Supports JSON and YAML output formats.
   * </p>
   * @param serviceId Service ID
   * @param apisIds Set of API IDs to include
   * @param format Output format (JSON or YAML)
   * @param gzipCompression Whether to apply gzip compression
   * @param onlyApisComponents Whether to include only API-related components
   * @return OpenAPI specification as string
   */
  @Override
  public String openapiDetail(Long serviceId, Set<Long> apisIds, SchemaFormat format,
      boolean gzipCompression, boolean onlyApisComponents) {
    return new BizTemplate<String>() {

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected String process() {
        OpenAPI openApi = openapiDetail0(serviceId, apisIds, onlyApisComponents);

        // Format output
        String result = SchemaFormat.json.equals(format) ? Json31.pretty(openApi)
            : Yaml31.pretty(openApi);

        if (gzipCompression) {
          try {
            result = GzipUtils.compress(result);
          } catch (IOException e) {
            throw SysException.of("Gzip OpenAPI exception, cause: " + e.getMessage());
          }
        }
        return result;
      }
    }.execute();
  }

  /**
   * <p>
   * Get OpenAPI specification object for a service.
   * </p>
   * <p>
   * Assembles complete OpenAPI specification by merging service schema, API paths, and components.
   * Handles server merging, security requirements, and component filtering.
   * </p>
   * @param serviceId Service ID
   * @param apisIds Set of API IDs to include
   * @param onlyApisComponents Whether to include only API-related components
   * @return OpenAPI specification object
   */
  @Override
  public OpenAPI openapiDetail0(Long serviceId, Set<Long> apisIds, boolean onlyApisComponents) {
    return new BizTemplate<OpenAPI>() {

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected OpenAPI process() {
        OpenAPI openApi = new OpenAPI();

        // Merge apis and project component servers
        ServicesSchema schemaDb = servicesSchemaQuery.checkAndFind(serviceId);
        List<Apis> apis = apisQuery.findByServiceAndIds(serviceId, apisIds);

        Apis singleApi = nonNull(apis) && apis.size() == 1 ? apis.get(0) : null;
        List<Server> servers = nonNull(singleApi)
            ? List.of(singleApi.getCurrentServer()) : mergeServers(apis, schemaDb.getServers());
        List<SecurityRequirement> securityRequirements = nonNull(singleApi)
            ? singleApi.getSecurity() : schemaDb.getSecurity();
        Info info = schemaDb.getInfo();
        if (nonNull(singleApi)) {
          info.setTitle(singleApi.getSummary());
          info.setDescription(singleApi.getDescription());
        }

        // Assemble OpenAPI common schema
        openApi.openapi(schemaDb.getOpenapi())
            .info(schemaDb.getInfo())
            .externalDocs(schemaDb.getExternalDocs())
            .servers(servers)
            .security(securityRequirements)
            // .tags(schemaDb.getTags())
            .extensions(schemaDb.getExtensions())
            .specVersion(schemaDb.getSpecVersion())
            .addExtension(ExtensionKey.PROJECT_ID_KEY, serviceId);

        // Assemble OpenAPI Paths schema
        if (isNotEmpty(apis)) {
          Paths paths = ApisConverter.toPaths(apis.stream()
              .collect(Collectors.groupingBy(Apis::getEndpoint)));
          openApi.paths(paths);
        }

        // Assemble OpenAPI tags
        if (isNotEmpty(apisIds) && isNotEmpty(schemaDb.getTags())) {
          // Only find the reference tags used by the API
          Set<String> apisTags = apis.stream().map(Apis::getTags)
              .filter(ObjectUtils::isNotEmpty).flatMap(Collection::stream)
              .collect(Collectors.toSet());
          openApi.setTags(schemaDb.getTags().stream()
              .filter(x -> apisTags.contains(x.getName())).collect(Collectors.toList()));
        } else {
          openApi.setTags(schemaDb.getTags());
        }

        // Assemble OpenAPI components schema
        List<ServicesComp> compsDb = nonNull(singleApi) || onlyApisComponents
            ? findProjectApisComps(serviceId, apis)
            : servicesCompQuery.findByServiceId(serviceId);
        if (isNotEmpty(compsDb)) {
          Components components = ServicesCompConverter.toOpenApiComp(
              compsDb.stream().collect(Collectors.groupingBy(ServicesComp::getType)));
          openApi.components(components);
        }

        return openApi;
      }
    }.execute();
  }

  /**
   * <p>
   * Get server list for all services in a project.
   * </p>
   * <p>
   * Retrieves all servers from all services in a project and associates them with their service IDs.
   * </p>
   * @param projectId Project ID
   * @return List of service servers
   */
  @Override
  public List<ServiceServer> serverListByProject(Long projectId) {
    return new BizTemplate<List<ServiceServer>>() {

      @Override
      protected List<ServiceServer> process() {
        List<Long> serviceIds = servicesSchemaRepo.findServiceIdByProjectId(projectId);
        if (isEmpty(serviceIds)) {
          return emptyList();
        }
        List<ServiceServer> servers = new ArrayList<>();
        for (Long serviceId : serviceIds) {
          List<Server> servers0 = findServersByServiceId(serviceId);
          for (Server server : servers0) {
            servers.add(new ServiceServer().setServiceId(serviceId).setServer(server));
          }
        }
        return servers;
      }
    }.execute();
  }

  /**
   * <p>
   * Get detailed information of a specific server.
   * </p>
   * @param serviceId Service ID
   * @param serverId Server ID
   * @return Server entity
   */
  @Override
  public Server serverDetail(Long serviceId, Long serverId) {
    return new BizTemplate<Server>() {

      @Override
      protected Server process() {
        ServicesSchema schema = servicesSchemaQuery.findByServiceId(serviceId);
        Server server = null;
        if (Objects.nonNull(schema) && isNotEmpty(schema.getServers())) {
          server = schema.getServers().stream()
              .filter(x -> Objects.equals(serverId, getExtensionLong(x.getExtensions(), ID_KEY)))
              .findFirst().orElse(null);
        }
        if (nonNull(server)) {
          return server;
        }
        throw ResourceNotFound.of(serverId, "Server");
      }
    }.execute();
  }

  /**
   * <p>
   * Merge servers from APIs and service schema.
   * </p>
   * <p>
   * Combines servers from individual APIs with servers from the service schema,
   * removing duplicates based on URL.
   * </p>
   * @param apis List of APIs
   * @param servers List of servers from service schema
   * @return Merged list of unique servers
   */
  private List<Server> mergeServers(List<Apis> apis, List<Server> servers) {
    Map<String, Server> serversMap = new HashMap<>();
    if (isNotEmpty(servers)) {
      for (Server server : servers) {
        if (isNotEmpty(server.getUrl())) {
          serversMap.put(server.getUrl(), server);
        }
      }
    }
    if (isNotEmpty(apis)) {
      for (Apis api : apis) {
        if (nonNull(api.getCurrentServer())
            && isNotEmpty(api.getCurrentServer().getUrl())) {
          serversMap.put(api.getCurrentServer().getUrl(), api.getCurrentServer());
        }
      }
    }
    return new ArrayList<>(serversMap.values());
  }

  /**
   * <p>
   * Find servers for multiple services.
   * </p>
   * @param serviceIds Collection of service IDs
   * @return List of servers from all services
   */
  @Override
  public List<Server> findServersByServiceIds(Collection<Long> serviceIds) {
    List<Server> servers = new ArrayList<>();
    for (Long serviceId : serviceIds) {
      ServicesSchema schema = servicesSchemaQuery.findByServiceId(serviceId);
      if (Objects.nonNull(schema)) {
        servers.addAll(schema.getServers());
      }
    }
    return servers;
  }

  /**
   * <p>
   * Find servers for a specific service.
   * </p>
   * @param serviceId Service ID
   * @return List of servers for the service
   */
  @Override
  public List<Server> findServersByServiceId(Long serviceId) {
    List<Server> servers = new ArrayList<>();
    ServicesSchema schema = servicesSchemaQuery.findByServiceId(serviceId);
    if (Objects.nonNull(schema) && isNotEmpty(schema.getServers())) {
      servers.addAll(schema.getServers());
    }
    return servers;
  }

  /**
   * <p>
   * Find service schema by service ID with caching.
   * </p>
   * @param serviceId Service ID
   * @return Service schema entity or null if not found
   */
  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema findByServiceId(Long serviceId) {
    return servicesSchemaRepo.findByServiceId(serviceId).orElse(null);
  }

  /**
   * <p>
   * Check and find service schema by service ID with caching.
   * </p>
   * @param serviceId Service ID
   * @return Service schema entity
   */
  @Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema checkAndFind(Long serviceId) {
    return servicesSchemaRepo.findByServiceId(serviceId)
        .orElseThrow(() -> ResourceNotFound.of(serviceId, "ServicesSchema"));
  }

  /**
   * <p>
   * Check and find service schema by service ID without caching.
   * </p>
   * @param serviceId Service ID
   * @return Service schema entity
   */
  //@Cacheable(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Override
  public ServicesSchema checkAndFind0(Long serviceId) {
    return servicesSchemaRepo.findByServiceId(serviceId)
        .orElseThrow(() -> ResourceNotFound.of(serviceId, "ServicesSchema"));
  }

  /**
   * <p>
   * Check if a service schema exists.
   * </p>
   * @param serviceId Service ID
   */
  @Override
  public void checkSchemaExisted(Long serviceId) {
    assertResourceNotFound(servicesSchemaRepo.existsByServiceId(serviceId),
        serviceId, "OpenAPI Schema");
  }

  /**
   * <p>
   * Validate server configuration.
   * </p>
   * <p>
   * Ensures server URLs are not empty and that environment variables have corresponding variable definitions.
   * </p>
   * @param servers List of servers to validate
   */
  @Override
  public void checkValidServer(List<Server> servers) {
    if (isNotEmpty(servers)) {
      for (Server server : servers) {
        assertTrue(isNotEmpty(server.getUrl()), "Server url should not be empty");
        assertTrue(!containsEnvVariable(server.getUrl())
            || isNotEmpty(server.getVariables()), "Server variables is missing");
      }
    }
  }

  /**
   * <p>
   * Check and get API parser for a specific import source.
   * </p>
   * <p>
   * Dynamically finds and returns the appropriate API parser based on the import source.
   * Uses Spring context to discover available parsers.
   * </p>
   * @param importSource API import source
   * @return API parser for the specified source
   */
  @Override
  public ApisParser checkAndGetApisParser(ApiImportSource importSource) {
    ApisParser targetApisParser = null;
    Map<String, ApisParser> providerMap = null;
    try {
      //Fix:: Dependency injection On linux
      providerMap = SpringContextHolder.getCtx().getBeansOfType(ApisParser.class);
    } catch (Exception e) {
      // NOOP
    }
    if (nonNull(providerMap) && isNotEmpty(providerMap.values())) {
      for (ApisParser parser : providerMap.values()) {
        if (nonNull(parser.getSource()) && parser.getSource().equals(importSource)) {
          targetApisParser = parser;
          break;
        }
      }
    }
    if (isNull(targetApisParser)) {
      // No apis parse plugin available
      throw SysException.of(SERVICE_NO_IMPORT_APIS_PLUGIN_CODE,
          SERVICE_NO_IMPORT_APIS_PLUGIN_T, new Object[]{importSource});
    }
    return targetApisParser;
  }

  /**
   * <p>
   * Check and get translation service for a specific provider.
   * </p>
   * <p>
   * Dynamically finds and returns the appropriate translation service based on the provider.
   * Uses Spring context to discover available translation services.
   * </p>
   * @param provider Translation service provider
   * @return Translation service for the specified provider
   */
  @Override
  public TranslationService checkAndGetTranslationService(TranslationServiceProvider provider) {
    TranslationService targetTranslationService = null;
    Map<String, TranslationService> providerMap = null;
    try {
      //Fix:: Dependency injection On linux
      providerMap = SpringContextHolder.getCtx().getBeansOfType(TranslationService.class);
    } catch (Exception e) {
      // NOOP
    }
    if (nonNull(providerMap) && isNotEmpty(providerMap.values())) {
      for (TranslationService service : providerMap.values()) {
        if (nonNull(service.getProvider()) && service.getProvider().equals(provider)) {
          targetTranslationService = service;
          break;
        }
      }
    }
    if (isNull(targetTranslationService)) {
      // No text translate plugin available
      throw SysException.of(SERVICE_NO_TEXT_TRANSLATE_PLUGIN_CODE,
          SERVICE_NO_TEXT_TRANSLATE_PLUGIN, new Object[]{provider});
    }
    return targetTranslationService;
  }

  /**
   * <p>
   * Parse OpenAPI content and convert to API entities.
   * </p>
   * <p>
   * Imports OpenAPI format data (including Postman format) and converts operations to API entities.
   * Uses fully resolved parsing options for complete schema resolution.
   * </p>
   * @param content OpenAPI content to parse
   * @return List of parsed API entities
   */
  @Override
  public @NotNull List<Apis> parseOpenApis(String content) {
    // Import OpenAPI format data, Note: Include Postman format data.
    ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setInferSchemaType(false);
    parseOptions.setResolveFully(true);
    OpenAPI openAPI = checkAndParseOpenApi(content, null, parseOptions);
    Map<String, Operation> operationsMap = OpenAPIUtils.flatPaths(openAPI.getPaths());
    return isEmpty(operationsMap) ? new ArrayList<>()
        : operationsMap.keySet().stream().map(x -> toSchemaApis(operationsMap.get(x)))
            .collect(Collectors.toList());
  }

  /**
   * <p>
   * Find components used by specific APIs in a service.
   * </p>
   * <p>
   * Filters service components to include only those referenced by the specified APIs.
   * Includes security schemes and referenced components used by the APIs.
   * </p>
   * @param serviceId Service ID
   * @param apis List of APIs to find components for
   * @return List of components used by the APIs
   */
  private List<ServicesComp> findProjectApisComps(Long serviceId, List<Apis> apis) {
    List<ServicesComp> allComps = servicesCompQuery.findByServiceId(serviceId);
    List<ServicesComp> finalComps = new ArrayList<>();
    if (isNotEmpty(apis)) {
      // Only find the security schemes or reference components used by the API
      Set<String> apisRefComps = new HashSet<>();
      for (Apis api : apis) {
        Map<String, String> refs = apisQuery.findApisAllRef(api);
        if (!refs.isEmpty()) {
          apisRefComps.addAll(refs.keySet());
        }
      }
      if (!apisRefComps.isEmpty()) {
        // Use map to remove duplicates
        Map<String, ServicesComp> finalRefComps = new HashMap<>();
        for (ServicesComp comp : allComps) {
          if (apisRefComps.contains(comp.getRef()) && !finalRefComps.containsKey(comp.getRef())) {
            finalRefComps.put(comp.getRef(), comp);
          }
        }
        finalComps.addAll(finalRefComps.values());
      }

      Map<String, ServicesComp> securityComps = allComps.stream()
          .filter(x -> x.getType().isSecuritySchemes())
          .collect(Collectors.toMap(ServicesComp::getKey, x -> x));
      // Use map to remove duplicates
      Map<String, ServicesComp> finalSecurityComps = new HashMap<>();
      boolean hasSecurityRequirement = false;
      for (Apis api : apis) {
        if (isNotEmpty(api.getSecurity())) {
          hasSecurityRequirement = true;
          for (SecurityRequirement sr : api.getSecurity()) {
            for (Entry<String, List<String>> entry : sr.entrySet()) {
              if (securityComps.containsKey(entry.getKey())) {
                finalSecurityComps.put(entry.getKey(), securityComps.get(entry.getKey()));
              }
            }
          }
        }
      }

      if (hasSecurityRequirement && finalSecurityComps.isEmpty()) {
        log.error("The security plan and security requirement names are inconsistent or missing.");
        finalComps.addAll(securityComps.values());
      } else {
        finalComps.addAll(finalSecurityComps.values());
      }

      if (finalComps.isEmpty()) {
        return null;
      }
    }
    return finalComps;
  }
}
