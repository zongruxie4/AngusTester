package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ServicesSchemaConverter.toClonedSchema;
import static cloud.xcan.angus.core.tester.application.converter.ServicesSchemaConverter.toInitProjectSchema;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_DOC_CHANGE_REMINDER;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;
import static io.swagger.v3.oas.models.extension.OpenAPIUtils.getExtensionLong;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.spring.env.EnvHelper;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCompCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.converter.ServicesSchemaConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchemaRepo;
import cloud.xcan.angus.core.tester.infra.util.OpenAPITranslator;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationServiceProvider;
import cloud.xcan.angus.l2cache.spring.RedisCaffeineCacheManager;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.utils.GzipUtils;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.OpenAPIUtils;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of service schema command operations for OpenAPI schema management.
 * 
 * <p>This class provides comprehensive functionality for managing service schemas,
 * including OpenAPI document parsing, schema updates, component management,
 * and synchronization with external API documentation.</p>
 * 
 * <p>It handles the complete lifecycle of schema management from initialization
 * to deletion, including security requirements, servers, tags, and extensions.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>OpenAPI document parsing and validation</li>
 *   <li>Schema component management (security, servers, tags)</li>
 *   <li>API synchronization and import/export</li>
 *   <li>Translation services integration</li>
 *   <li>Cache management for performance</li>
 * </ul></p>
 */
@Biz
public class ServicesSchemaCmdImpl extends CommCmd<ServicesSchema, Long> implements
    ServicesSchemaCmd {

  @Resource
  private ServicesSchemaRepo servicesSchemaRepo;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;
  @Resource
  private ServicesSchemaCmd servicesSchemaCmd;
  @Resource
  private ServicesCompCmd servicesCompCmd;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ApisCmd apisCmd;
  @Resource
  private ApisRepo apisRepo;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Replaces or updates individual schema components with caching support.
   * 
   * <p>This method allows updating specific schema components (security requirements,
   * servers, or tags) individually. It performs permission validation and
   * automatically manages cache eviction to ensure data consistency.</p>
   * 
   * <p>The method logs component update activities for audit tracking.</p>
   * 
   * @param serviceId the ID of the service
   * @param sr the security requirement to add or update
   * @param server the server configuration to add or update
   * @param tag the tag to add or update
   * @throws IllegalArgumentException if validation fails
   */
  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Long serviceId, SecurityRequirement sr, Server server, Tag tag) {
    new BizTemplate<Void>() {
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify schema exists and retrieve it
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
        // Validate server configuration if provided
        if (nonNull(server)) {
          servicesSchemaQuery.checkValidServer(List.of(server));
        }
      }

      @Override
      protected Void process() {
        Services projectDb = servicesQuery.checkAndFind(serviceId);

        // Add or update SecurityRequirement
        if (nonNull(sr)) {
          addOrUpdateSecurityRequirement(schemaDb, sr);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SR_UPDATED));
        }

        // Add or update server
        if (nonNull(server)) {
          addOrUpdateServer(schemaDb, server);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SERVER_UPDATED));
        }

        // Add or update server
        if (nonNull(tag)) {
          addAndUpdateTag(schemaDb, tag);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_TAG_UPDATED));
        }

        servicesSchemaRepo.save(schemaDb);
        return null;
      }
    }.execute();
  }

  /**
   * Updates the current server configuration for all APIs in a service.
   * 
   * <p>This method synchronizes the server configuration across all APIs
   * within a service, ensuring consistent server settings for API execution.</p>
   * 
   * <p>The method logs the server synchronization activity for audit purposes.</p>
   * 
   * @param serviceId the ID of the service
   * @param serverId the ID of the server to set as current
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void apisServerReplace(Long serviceId, Long serverId) {
    new BizTemplate<Void>() {
      Services serviceDb;
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        // Verify service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify schema exists and retrieve it
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
      }

      @Override
      protected Void process() {
        Server server = isEmpty(schemaDb.getServers())
            ? null : schemaDb.getServers().stream()
            .filter(x -> nonNull(x) && x.getExtensions().containsKey(ID_KEY)
                && Objects.equals(serverId, Long.valueOf(x.getExtensions().get(ID_KEY).toString())))
            .findFirst().orElse(null);
        if (nonNull(server)) {
          apisRepo.updateCurrentServerById(Long.valueOf(
              server.getExtensions().get(ID_KEY).toString()), server);

          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.APIS_SERVER_SYNC));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces all schema components with comprehensive caching support.
   * 
   * <p>This method allows updating all schema components at once including info,
   * external documentation, security requirements, servers, tags, and extensions.</p>
   * 
   * <p>It performs permission validation and automatically manages cache eviction
   * to ensure data consistency across all components.</p>
   * 
   * @param serviceId the ID of the service
   * @param info the OpenAPI info object
   * @param extDoc the external documentation
   * @param srs the list of security requirements
   * @param servers the list of server configurations
   * @param tags the list of tags
   * @param extensions the map of extensions
   * @throws IllegalArgumentException if validation fails
   */
  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAll(Long serviceId, Info info, ExternalDocumentation extDoc,
      List<SecurityRequirement> srs, List<Server> servers, List<Tag> tags,
      Map<String, Object> extensions) {
    new BizTemplate<Void>() {
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify schema exists and retrieve it
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
        // Validate server configurations if provided
        if (nonNull(servers)) {
          servicesSchemaQuery.checkValidServer(servers);
        }
      }

      @Override
      protected Void process() {
        Services projectDb = servicesQuery.checkAndFind(serviceId);

        if (nonNull(info)) {
          schemaDb.setInfo(info);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_INFO_UPDATED));
        }
        if (nonNull(extDoc)) {
          schemaDb.setExternalDocs(extDoc);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_EXT_DOC_UPDATED));
        }
        if (nonNull(srs)) {
          schemaDb.setSecurity(srs);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SR_UPDATED));
        }
        if (nonNull(servers)) {
          schemaDb.setServers(servers);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_SERVER_UPDATED));
        }
        if (nonNull(tags)) {
          schemaDb.setTags(tags);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_TAG_UPDATED));
        }
        if (nonNull(extensions)) {
          schemaDb.setExtensions(extensions);
          activityCmd.add(toActivity(SERVICE, projectDb, ActivityType.SCHEMA_EXT_UPDATED));
        }

        servicesSchemaRepo.save(schemaDb);
        return null;
      }
    }.execute();
  }

  /**
   * Deletes specific schema components with caching support.
   * 
   * <p>This method allows selective deletion of schema components including
   * security requirements, servers, and tags. It performs permission validation
   * and automatically manages cache eviction.</p>
   * 
   * <p>The method logs component deletion activities for audit tracking.</p>
   * 
   * @param serviceId the ID of the service
   * @param srNames the set of security requirement names to delete
   * @param serverIds the set of server IDs to delete
   * @param tagNames the set of tag names to delete
   * @throws IllegalArgumentException if validation fails
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long serviceId, Set<String> srNames, Set<Long> serverIds,
      Set<String> tagNames) {
    new BizTemplate<Void>() {
      ServicesSchema schemaDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify schema exists and retrieve it
        schemaDb = servicesSchemaQuery.checkAndFind0(serviceId);
      }

      @Override
      protected Void process() {
        Services serviceDb = servicesQuery.checkAndFind(serviceId);

        // Delete SecurityRequirement
        if (isNotEmpty(srNames)) {
          if (isEmpty(schemaDb.getSecurity())) {
            return null;
          }
          List<SecurityRequirement> srs = new ArrayList<>();
          for (SecurityRequirement sr : schemaDb.getSecurity()) {
            for (String name : sr.keySet()) {
              if (srNames.contains(name)) {
                sr.remove(name);
              }
            }
            if (!sr.isEmpty()) {
              srs.add(sr);
            }
          }
          schemaDb.setSecurity(srs);
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_SR_UPDATED));
        }

        // Delete server
        if (isNotEmpty(serverIds)) {
          if (isEmpty(schemaDb.getServers())) {
            return null;
          }
          schemaDb.setServers(schemaDb.getServers().stream()
              .filter(x -> !serverIds.contains(getExtensionLong(x.getExtensions(), ID_KEY)))
              .toList());
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_SERVER_UPDATED));
        }

        // Delete server
        if (isNotEmpty(tagNames)) {
          if (isEmpty(schemaDb.getTags())) {
            return null;
          }
          schemaDb.setTags(schemaDb.getTags().stream()
              .filter(x -> !tagNames.contains(x.getName())).toList());
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_TAG_UPDATED));
        }
        servicesSchemaRepo.save(schemaDb);
        return null;
      }
    }.execute();
  }

  /**
   * Replaces OpenAPI content with comprehensive validation and processing.
   * 
   * <p>This method handles OpenAPI content replacement including decompression,
   * validation, and parsing. It supports various import sources and strategies
   * for handling duplicate components.</p>
   * 
   * <p>Note: When an API is deleted, the front-end should prompt the user to prevent
   * accidental deletion. Additionally, if the user has not modified or saved,
   * do not submit the request to the backend interface.</p>
   * 
   * @param serviceId the ID of the service
   * @param forced whether to force the operation
   * @param gzipCompression whether the content is gzip compressed
   * @param content the OpenAPI content
   * @param strategyWhenDuplicated strategy for handling duplicates
   * @param deleteWhenNotExisted whether to delete components not in the import
   * @param apiSource the source of the API
   * @param importSource the import source type
   * @param mergeSchema whether to merge schemas
   * @param syncName the synchronization name
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public void openapiReplace(Long serviceId, Boolean forced, boolean gzipCompression,
      String content, StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted,
      ApiSource apiSource, ApiImportSource importSource, boolean mergeSchema, String syncName) {
    new BizTemplate<Void>() {
      String decompressedContent;
      OpenAPI openApi;

      @Override
      protected void checkParams() {
        // Decompress OpenAPI documentation if needed
        try {
          decompressedContent = gzipCompression ? GzipUtils.decompress(content) : content;
        } catch (IOException e) {
          assertTrue(false, "Incorrect gzip format, cause:" + e.getMessage());
        }
      }

      @Override
      protected Void process() {
        // Validate and parse OpenAPI content
        openApi = servicesSchemaQuery.checkAndGetApisParser(
                importSource.isWideOpenapi() ? ApiImportSource.OPENAPI : importSource)
            .parse(decompressedContent);

        // Update schema with parsed OpenAPI
        servicesSchemaCmd.openapiReplace(serviceId, true, openApi, strategyWhenDuplicated,
            deleteWhenNotExisted, apiSource, importSource, mergeSchema, syncName);
        return null;
      }
    }.execute();
  }

  /**
   * Replaces OpenAPI schema with comprehensive processing and synchronization.
   * 
   * <p>This method handles complete OpenAPI schema replacement including API
   * synchronization, component management, and cache invalidation. It supports
   * various strategies for handling duplicates and optional cleanup.</p>
   * 
   * <p>The method performs extensive validation and logs activities for audit purposes.</p>
   * 
   * @param serviceId the ID of the service
   * @param forced whether to force the operation
   * @param openApi the OpenAPI object
   * @param strategyWhenDuplicated strategy for handling duplicates
   * @param deleteWhenNotExisted whether to delete components not in the import
   * @param apiSource the source of the API
   * @param importSource the import source type
   * @param mergeSchema whether to merge schemas
   * @param syncName the synchronization name
   * @throws IllegalArgumentException if validation fails
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesSchema")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void openapiReplace(Long serviceId, Boolean forced,
      OpenAPI openApi, StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted,
      ApiSource apiSource, ApiImportSource importSource, boolean mergeSchema, String syncName) {
    new BizTemplate<Void>() {
      ServicesSchema serviceSchemaDb;
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify schema exists and retrieve it
        // Note: After the first successful import, the query project document is empty
        serviceSchemaDb = servicesSchemaQuery.checkAndFind(serviceId);
        // Verify service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        // Update service schema
        // Warning: Multiple files importing the same project will be overwritten by the last imported file
        servicesSchemaCmd.updateSchema(serviceId, serviceSchemaDb, openApi, mergeSchema,
            nonNull(strategyWhenDuplicated) && strategyWhenDuplicated.isCover());

        // Update APIs (Operation Object Schema)
        Map<String, Apis> apisDbMap = apisQuery.findByServiceId(serviceId).stream()
            .collect(Collectors.toMap(x -> x.getMethod().getValue().toLowerCase()
                + ":" + x.getEndpoint(), x -> x));
        // Note: Since OpenAPI 3.1, paths are not required
        if (isNotEmpty(openApi.getPaths())) {
          Map<String, Operation> operationsMap = OpenAPIUtils.flatPaths(openApi.getPaths());
          Map<String, Apis> openApisMap = isEmpty(operationsMap) ? emptyMap()
              : operationsMap.keySet().stream().collect(Collectors
                  .toMap(x -> x, x -> ApisConverter.toSchemaApis(operationsMap.get(x))));
          
          // Find APIs to update
          if (StrategyWhenDuplicated.COVER.equals(strategyWhenDuplicated)) {
            Map<String, Apis> updatedApisDbMap = apisDbMap.keySet().stream()
                .filter(x -> openApisMap.containsKey(x) &&
                    apisDbMap.get(x).getSchemaHash() != openApisMap.get(x).getSchemaHash())
                .collect(Collectors.toMap(x -> x, apisDbMap::get));
            if (isNotEmpty(updatedApisDbMap)) {
              assertTrue(forced, SERVICE_DOC_CHANGE_REMINDER);
              apisCmd.updateSyncApis(updatedApisDbMap, openApisMap);
            }
          }

          // Delete APIs not in the import if requested
          if (deleteWhenNotExisted) {
            Collection<Apis> deletedApisInDb = apisDbMap.keySet().stream()
                .filter(x -> !openApisMap.containsKey(x)
                    // Note: If synchronization, only delete the synchronization of its own data
                    && (isEmpty(syncName) || syncName.equals(apisDbMap.get(x).getSyncName())))
                .collect(Collectors.toMap(x -> x, apisDbMap::get)).values();
            if (isNotEmpty(deletedApisInDb)) {
              assertTrue(forced, SERVICE_DOC_CHANGE_REMINDER);
              // Note: The following method does not delete component references, deleted by apisCmd.delete0() when clearing Trash
              apisCmd.delete(deletedApisInDb.stream().map(Apis::getId).collect(Collectors.toSet()),
                  false);
            }
          }

          // Find new APIs to add
          Collection<Apis> newApis = openApisMap.keySet().stream()
              .filter(x -> !apisDbMap.containsKey(x))
              .collect(Collectors.toMap(x -> x, openApisMap::get)).values();
          if (isNotEmpty(newApis)) {
            apisCmd.add(newApis.stream().map(
                    x -> ApisConverter.assembleSchemaToAddApis(x, serviceDb, openApi.getComponents(),
                        apiSource, importSource, syncName))
                .toList(), serviceDb, false);
          }
        } else {
          // Delete all APIs if no paths exist and deletion is requested
          if (deleteWhenNotExisted) {
            // Note: The following method does not delete component references, deleted by apisCmd.delete0() when clearing Trash
            apisCmd.delete(apisDbMap.values().stream().map(Apis::getId).collect(Collectors.toSet()),
                false);
          }
        }

        // Update service components (Components Object Schema)
        if (isNotEmpty(openApi.getComponents())) {
          servicesCompCmd.replaceByOpenApi(serviceDb.getId(), openApi.getComponents(),
              strategyWhenDuplicated, deleteWhenNotExisted);
        }

        // Log appropriate activity based on API source
        if (isNull(apiSource) || ApiSource.EDITOR.equals(apiSource)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_OPENAPI_UPDATED));
        } else if (ApiSource.SYNC.equals(apiSource)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_OPENAPI_SYNC));
        } else if (ApiSource.IMPORT.equals(apiSource)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.IMPORT));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Translates OpenAPI documentation between different languages.
   * 
   * <p>This method uses external translation services to translate OpenAPI
   * documentation content between supported languages. It requires proper
   * API key configuration for the translation service.</p>
   * 
   * <p>The method performs complete translation and updates the schema
   * with the translated content.</p>
   * 
   * @param serviceId the ID of the service
   * @param sourceLanguage the source language for translation
   * @param targetLanguage the target language for translation
   * @throws SysException if translation service configuration fails
   */
  @Override
  public void translate(Long serviceId, SupportedLanguage sourceLanguage,
      SupportedLanguage targetLanguage) {
    new BizTemplate<Void>() {
      @Override
      protected Void process() {
        // Configure translation service API key
        System.setProperty("deepseek.api.key", EnvHelper.getString("DEEPSEEK_TRANSLATION_API_KEY"));

        // Initialize translation service
        TranslationService translationService = servicesSchemaQuery.checkAndGetTranslationService(
            TranslationServiceProvider.DeepSeek);
        try {
          translationService.loadConfig();
        } catch (Exception e) {
          throw new SysException(e.getMessage());
        }

        // Retrieve current OpenAPI and perform translation
        OpenAPI openApi = servicesSchemaQuery.openapiDetail0(serviceId, null, false);
        OpenAPITranslator translator = new OpenAPITranslator(
            translationService, sourceLanguage, targetLanguage
        );
        translator.translateOpenAPI(openApi);

        // Update schema with translated content
        servicesSchemaCmd.openapiReplace(serviceId, true, openApi, StrategyWhenDuplicated.COVER,
            false, null, null, false, null);

        // TODO: Notify the user that the translation is complete
        return null;
      }
    }.execute();
  }

  /**
   * Initializes a new service schema with default configuration.
   * 
   * @param services the service to initialize schema for
   */
  @Override
  public void init(Services services) {
    insert0(toInitProjectSchema(services, null));
  }

  /**
   * Initializes a new service schema with provided OpenAPI configuration.
   * 
   * @param services the service to initialize schema for
   * @param openAPI the OpenAPI configuration to use
   */
  @Override
  public void init(Services services, OpenAPI openAPI) {
    insert0(toInitProjectSchema(services, openAPI));
  }

  /**
   * Clones a service schema to a new service.
   * 
   * <p>This method creates a complete copy of a service schema including
   * all components, security requirements, servers, and tags.</p>
   * 
   * @param clonedServiceId the ID of the source service to clone from
   * @param serviceId the ID of the target service to clone to
   */
  @Override
  public void clone(Long clonedServiceId, Long serviceId) {
    ServicesSchema schemaDb = servicesSchemaQuery.findByServiceId(clonedServiceId);
    if (nonNull(schemaDb)) {
      insert0(toClonedSchema(schemaDb, serviceId));
    }
  }

  /**
   * Deletes schemas for multiple services with cache invalidation.
   * 
   * <p>This method performs bulk deletion of service schemas and ensures
   * proper cache invalidation to maintain data consistency.</p>
   * 
   * @param serviceIds the collection of service IDs to delete schemas for
   */
  @Override
  public void deleteByServiceIdIn(Collection<Long> serviceIds) {
    servicesSchemaRepo.deleteByServiceIdIn(serviceIds);
    ((RedisCaffeineCacheManager) cacheManager).evict("servicesSchema",
        serviceIds.stream().map(id -> "serviceId_" + id)
            .toList());
  }

  /**
   * Updates a service schema with new OpenAPI content.
   * 
   * <p>This method updates the service schema with new OpenAPI content,
   * supporting both merge and cover strategies for handling conflicts.</p>
   * 
   * <p>The method logs the schema update activity for audit purposes.</p>
   * 
   * @param serviceId the ID of the service
   * @param serviceSchemaDb the current service schema
   * @param openApi the new OpenAPI content
   * @param mergeSchema whether to merge schemas
   * @param cover whether to cover existing content
   */
  @Override
  public void updateSchema(Long serviceId, ServicesSchema serviceSchemaDb, OpenAPI openApi,
      boolean mergeSchema, boolean cover) {
    ServicesSchemaConverter.updateSchema(serviceSchemaDb, openApi, mergeSchema, cover);
    servicesSchemaRepo.save(serviceSchemaDb);
    activityCmd.add(toActivity(SERVICE, serviceSchemaDb, ActivityType.SCHEMA_UPDATED));
  }

  /**
   * Adds or updates a security requirement in the schema.
   * 
   * <p>This method handles security requirement management by either updating
   * existing requirements or adding new ones. It only uses one-dimensional
   * security requirements for simplicity.</p>
   * 
   * @param schemaDb the schema to update
   * @param sr the security requirement to add or update
   */
  private void addOrUpdateSecurityRequirement(ServicesSchema schemaDb, SecurityRequirement sr) {
    List<SecurityRequirement> securityDb = schemaDb.getSecurity();
    if (isNotEmpty(securityDb)) {
      boolean update = false;
      // Only use one-dimensional element for simplicity
      for (String name : securityDb.get(0).keySet()) {
        if (sr.containsKey(name)) {
          securityDb.get(0).put(name, sr.get(name));
          update = true;
          break;
        }
      }
      if (!update) {
        securityDb.get(0).putAll(sr);
      }
    } else {
      schemaDb.setSecurity(List.of(sr));
    }
  }

  /**
   * Adds or updates a server configuration in the schema.
   * 
   * <p>This method handles server configuration management by either updating
   * existing servers or adding new ones. It automatically generates unique IDs
   * for new servers if not provided.</p>
   * 
   * @param schemaDb the schema to update
   * @param server the server configuration to add or update
   */
  private void addOrUpdateServer(ServicesSchema schemaDb, Server server) {
    boolean update = false;
    Long serverId = getExtensionLong(server.getExtensions(), ID_KEY);
    List<Server> serversDb = schemaDb.getServers();
    if (isNotEmpty(serversDb)) {
      for (Server serverDb : serversDb) {
        Long serverDbId = getExtensionLong(serverDb.getExtensions(), ID_KEY);
        if (nonNull(serverId) && nonNull(serverDbId) && serverId.equals(serverDbId)) {
          serverDb.description(server.getDescription())
              .url(server.getUrl())
              .variables(server.getVariables())
              .extensions(server.getExtensions());
          update = true;
          break;
        }
      }
      if (!update) {
        if (isNull(serverId)) {
          server.addExtension(ID_KEY, getCachedUidGenerator().getUID());
        }
        schemaDb.getServers().add(server);
      }
    } else {
      if (isNull(serverId)) {
        server.addExtension(ID_KEY, getCachedUidGenerator().getUID());
      }
      schemaDb.setServers(List.of(server));
    }
  }

  /**
   * Adds or updates a tag in the schema.
   * 
   * <p>This method handles tag management by either updating existing tags
   * or adding new ones based on tag name matching.</p>
   * 
   * @param schemaDb the schema to update
   * @param tag the tag to add or update
   */
  private void addAndUpdateTag(ServicesSchema schemaDb, Tag tag) {
    boolean update = false;
    List<Tag> tagsDb = schemaDb.getTags();
    if (isNotEmpty(tagsDb)) {
      for (Tag tagDb : tagsDb) {
        if (tagDb.getName().equals(tag.getName())) {
          tagDb.description(tag.getDescription())
              .externalDocs(tag.getExternalDocs())
              .extensions(tag.getExtensions());
          update = true;
          break;
        }
      }
      if (!update) {
        schemaDb.getTags().add(tag);
      }
    } else {
      schemaDb.setTags(List.of(tag));
    }
  }

  /**
   * Returns the repository instance for this command.
   * 
   * @return the services schema repository
   */
  @Override
  protected BaseRepository<ServicesSchema, Long> getRepository() {
    return this.servicesSchemaRepo;
  }

}
