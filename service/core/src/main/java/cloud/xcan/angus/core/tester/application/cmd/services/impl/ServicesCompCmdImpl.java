package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCompCmd;
import cloud.xcan.angus.core.tester.application.converter.ServicesCompConverter;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompRepo;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompType;
import cloud.xcan.angus.l2cache.spring.RedisCaffeineCacheManager;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.models.Components;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of service component command operations.
 * 
 * <p>This class provides comprehensive functionality for managing service components,
 * including OpenAPI schemas, components, and related metadata.</p>
 * 
 * <p>It handles component CRUD operations with proper permission validation,
 * cache management, and activity logging for audit purposes.</p>
 * 
 * <p>The implementation uses cache eviction strategies to ensure data consistency
 * and provides batch operations for better performance.</p>
 */
@Biz
public class ServicesCompCmdImpl extends CommCmd<ServicesComp, Long> implements ServicesCompCmd {

  @Resource
  private ServicesCompRepo serviceCompRepo;
  @Resource
  private ServicesCompQuery servicesCompQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ServicesCompCmd servicesCompCmd;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Replaces or creates a service component with caching support.
   * 
   * <p>This method handles both creation and updates of service components.
   * It performs permission validation and automatically manages cache eviction
   * to ensure data consistency.</p>
   * 
   * <p>The method logs component update activities for audit tracking.</p>
   * 
   * @param serviceId the ID of the service
   * @param type the type of component
   * @param key the component key
   * @param component the component content
   * @return the ID key of the created or updated component
   * @throws IllegalArgumentException if validation fails
   */
  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Long serviceId, ServicesCompType type, String key,
      String component) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Check if component already exists
        ServicesComp compDb = serviceCompRepo.findByServiceIdAndTypeAndKey(serviceId, type, key);
        ServicesComp comp = ServicesCompConverter.toProjectComp(serviceId, type, key, component);
        
        if (Objects.isNull(compDb)) {
          // Create new component
          insert0(comp);
        } else {
          // Update existing component
          ServicesCompConverter.updateComp(compDb, comp);
          serviceCompRepo.save(compDb);
        }
        
        // Log component update activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_UPDATED, key));
        return new IdKey<>(Objects.isNull(compDb) ? comp.getId() : compDb.getId(), key);
      }
    }.execute();
  }

  /**
   * Deletes service components by type and optional keys.
   * 
   * <p>This method supports both selective deletion by keys and bulk deletion
   * by type. It automatically manages cache eviction and logs deletion activities.</p>
   * 
   * @param serviceId the ID of the service
   * @param type the type of components to delete
   * @param keys optional set of specific keys to delete, null for all components of the type
   * @throws IllegalArgumentException if validation fails
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteByType(Long serviceId, ServicesCompType type, @Nullable Set<String> keys) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        if (isEmpty(keys)) {
          // Delete all components of the specified type
          serviceCompRepo.deleteByServiceIdAndType(serviceId, type.getValue());
        } else {
          // Delete specific components by keys
          serviceCompRepo.deleteByServiceIdAndTypeAndKey(serviceId, type.getValue(), keys);
        }
        
        // Log component deletion activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_DELETED,
            isEmpty(keys) ? type + ":all" : String.join(",", keys)));
        return null;
      }
    }.execute();
  }

  /**
   * Deletes service components by their references.
   * 
   * <p>This method deletes components based on their reference identifiers,
   * typically used when components are no longer referenced in the schema.</p>
   * 
   * @param serviceId the ID of the service
   * @param refs set of component references to delete
   * @throws IllegalArgumentException if validation fails
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteByRef(Long serviceId, Set<String> refs) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        // Delete components by references
        serviceCompRepo.deleteByServiceIdAndRefIn(serviceId, refs);
        
        // Log component deletion activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_DELETED,
            String.join(",", refs)));
        return null;
      }
    }.execute();
  }

  /**
   * Deletes all components for a service.
   * 
   * <p>This method performs a complete cleanup of all components associated
   * with the specified service.</p>
   * 
   * @param serviceId the ID of the service
   * @throws IllegalArgumentException if validation fails
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteAll(Long serviceId) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        // Delete all components for the service
        serviceCompRepo.deleteByServiceId(serviceId);
        
        // Log component deletion activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_DELETED, "all"));
        return null;
      }
    }.execute();
  }

  /**
   * Replaces service components using OpenAPI Components specification.
   * 
   * <p>This method synchronizes service components with OpenAPI Components,
   * supporting different strategies for handling duplicates and optional
   * cleanup of unreferenced components.</p>
   * 
   * <p>The method performs intelligent synchronization by comparing existing
   * components with the new OpenAPI specification and applying the specified
   * strategy for handling conflicts.</p>
   * 
   * @param serviceId the ID of the service
   * @param components the OpenAPI Components specification
   * @param strategyWhenDuplicated strategy for handling duplicate components
   * @param deleteWhenNotExisted whether to delete components not in the new specification
   */
  @Override
  public void replaceByOpenApi(Long serviceId, Components components,
      StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted) {
    // Convert OpenAPI components to service components
    Map<String, ServicesComp> openApiCompsMap = ServicesCompConverter
        .toProjectComp(serviceId, components);
    
    // Get existing components from database
    Map<String, ServicesComp> compsDbMap = servicesCompQuery.findByServiceId(serviceId)
        .stream().collect(Collectors.toMap(ServicesComp::getRef, x -> x));

    // Process new components
    if (isNotEmpty(openApiCompsMap)) {
      // Find and insert new components
      Collection<ServicesComp> newComps = openApiCompsMap.keySet().stream()
          .filter(x -> !compsDbMap.containsKey(x))
          .collect(Collectors.toMap(x -> x, openApiCompsMap::get)).values();
      if (isNotEmpty(newComps)) {
        servicesCompCmd.batchInsert0(serviceId, newComps);
      }

      // Handle component updates based on strategy
      if (StrategyWhenDuplicated.COVER.equals(strategyWhenDuplicated)) {
        Map<String, ServicesComp> updatedCompsDbMap = compsDbMap.keySet().stream()
            .filter(x -> openApiCompsMap.containsKey(x)
                && compsDbMap.get(x).getSchemaHash() != openApiCompsMap.get(x).getSchemaHash())
            .collect(Collectors.toMap(x -> x, compsDbMap::get));
        if (isNotEmpty(updatedCompsDbMap)) {
          for (String uniqueKey : updatedCompsDbMap.keySet()) {
            ServicesCompConverter.openApiToUpdateComp(updatedCompsDbMap.get(uniqueKey),
                openApiCompsMap.get(uniqueKey));
          }
          servicesCompCmd.batchUpdate0(serviceId, updatedCompsDbMap.values());
        }
      }

      // Handle component deletion if requested
      if (deleteWhenNotExisted) {
        Collection<ServicesComp> deletedCompsInDb = compsDbMap.keySet().stream()
            .filter(x -> !openApiCompsMap.containsKey(x))
            .collect(Collectors.toMap(x -> x, compsDbMap::get)).values();
        if (isNotEmpty(deletedCompsInDb)) {
          deleteByServiceIdAndRefIn(serviceId, deletedCompsInDb.stream().map(ServicesComp::getRef)
              .collect(Collectors.toSet()));
        }
      }
    } else {
      // If no components in OpenAPI spec, optionally delete all existing components
      if (deleteWhenNotExisted) {
        deleteByServiceIdAndRefIn(serviceId, null);
      }
    }
  }

  /**
   * Batch inserts new service components.
   * 
   * <p>This method provides efficient bulk insertion of components with
   * automatic cache eviction for data consistency.</p>
   * 
   * @param serviceId the ID of the service
   * @param newComps collection of new components to insert
   */
  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public void batchInsert0(Long serviceId, Collection<ServicesComp> newComps) {
    batchInsert0(newComps);
  }

  /**
   * Batch updates existing service components.
   * 
   * <p>This method provides efficient bulk updates of components with
   * automatic cache eviction for data consistency.</p>
   * 
   * @param serviceId the ID of the service
   * @param updatedComps collection of components to update
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public void batchUpdate0(Long serviceId, Collection<ServicesComp> updatedComps) {
    batchUpdate0(updatedComps);
  }

  /**
   * Deletes service components by references with cache management.
   * 
   * <p>This method deletes components based on their references and
   * automatically manages cache eviction for consistency.</p>
   * 
   * @param serviceId the ID of the service
   * @param refs collection of component references to delete, null for all components
   */
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public void deleteByServiceIdAndRefIn(Long serviceId, Collection<String> refs) {
    if (isNotEmpty(refs)) {
      serviceCompRepo.deleteByServiceIdAndRefIn(serviceId, refs);
    } else {
      serviceCompRepo.deleteByServiceId(serviceId);
    }
  }

  /**
   * Deletes all components for multiple services with cache cleanup.
   * 
   * <p>This method performs bulk deletion of components across multiple services
   * and ensures proper cache eviction for all affected services.</p>
   * 
   * @param serviceIds list of service IDs whose components should be deleted
   */
  @Override
  public void deleteByServiceIdIn(List<Long> serviceIds) {
    serviceCompRepo.deleteByServiceIdIn(serviceIds);
    
    // Evict cache for all affected services
    ((RedisCaffeineCacheManager) cacheManager).evict("servicesComps", serviceIds.stream()
        .map(id -> "serviceId_" + id).collect(Collectors.toList()));
  }

  /**
   * Clones components from one service to another.
   * 
   * <p>This method copies all components from the source service to the target service,
   * typically used during service cloning operations.</p>
   * 
   * @param clonedServiceId the ID of the source service to copy from
   * @param serviceId the ID of the target service to copy to
   */
  @Override
  public void clone(Long clonedServiceId, Long serviceId) {
    List<ServicesComp> comps = servicesCompQuery.findByServiceId(clonedServiceId);
    if (isNotEmpty(comps)) {
      List<ServicesComp> newComps = comps.stream()
          .map(x -> ServicesCompConverter.toClonedProjectComp(x, serviceId))
          .collect(Collectors.toList());
      batchInsert0(newComps);
    }
  }

  /**
   * Returns the repository instance for this command.
   * 
   * @return the service component repository
   */
  @Override
  protected BaseRepository<ServicesComp, Long> getRepository() {
    return serviceCompRepo;
  }
}
