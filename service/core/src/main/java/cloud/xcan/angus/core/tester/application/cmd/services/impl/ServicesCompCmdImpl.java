package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
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
import cloud.xcan.angus.spec.utils.ObjectUtils;
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
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected IdKey<Long, Object> process() {
        ServicesComp compDb = serviceCompRepo.findByServiceIdAndTypeAndKey(serviceId, type, key);
        ServicesComp comp = ServicesCompConverter.toProjectComp(serviceId, type, key, component);
        if (Objects.isNull(compDb)) {
          insert0(comp);
        } else {
          ServicesCompConverter.updateComp(compDb, comp);
          serviceCompRepo.save(compDb);
        }
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_UPDATED, key));
        return new IdKey<>(Objects.isNull(compDb) ? comp.getId() : compDb.getId(), key);
      }
    }.execute();
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteByType(Long serviceId, ServicesCompType type, @Nullable Set<String> keys) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        if (ObjectUtils.isEmpty(keys)) {
          serviceCompRepo.deleteByServiceIdAndType(serviceId, type.getValue());
        } else {
          serviceCompRepo.deleteByServiceIdAndTypeAndKey(serviceId, type.getValue(), keys);
        }
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_DELETED,
            ObjectUtils.isEmpty(keys) ? type + ":all" : String.join(",", keys)));
        return null;
      }
    }.execute();
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteByRef(Long serviceId, Set<String> refs) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        serviceCompRepo.deleteByServiceIdAndRefIn(serviceId, refs);
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_DELETED,
            String.join(",", refs)));
        return null;
      }
    }.execute();
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteAll(Long serviceId) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        serviceCompRepo.deleteByServiceId(serviceId);
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.SCHEMA_COMP_DELETED, "all"));
        return null;
      }
    }.execute();
  }

  @Override
  public void replaceByOpenApi(Long serviceId, Components components,
      StrategyWhenDuplicated strategyWhenDuplicated, boolean deleteWhenNotExisted) {
    Map<String, ServicesComp> openApiCompsMap = ServicesCompConverter
        .toProjectComp(serviceId, components);
    Map<String, ServicesComp> compsDbMap = servicesCompQuery.findByServiceId(serviceId)
        .stream().collect(Collectors.toMap(ServicesComp::getRef, x -> x));

    // Find new components
    if (isNotEmpty(openApiCompsMap)) {
      Collection<ServicesComp> newComps = openApiCompsMap.keySet().stream()
          .filter(x -> !compsDbMap.containsKey(x))
          .collect(Collectors.toMap(x -> x, openApiCompsMap::get)).values();
      if (isNotEmpty(newComps)) {
        servicesCompCmd.batchInsert0(serviceId, newComps);
      }

      // Find update apis
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

      // Find delete apis in database
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
      if (deleteWhenNotExisted) {
        deleteByServiceIdAndRefIn(serviceId, null);
      }
    }
  }

  @DoInFuture("Add quota restrictions")
  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public void batchInsert0(Long serviceId, Collection<ServicesComp> newComps) {
    batchInsert0(newComps);
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public void batchUpdate0(Long serviceId, Collection<ServicesComp> updatedComps) {
    batchUpdate0(updatedComps);
  }

  @CacheEvict(key = "'servicesId_' + #serviceId", value = "servicesComps")
  @Override
  public void deleteByServiceIdAndRefIn(Long serviceId, Collection<String> refs) {
    if (ObjectUtils.isNotEmpty(refs)) {
      serviceCompRepo.deleteByServiceIdAndRefIn(serviceId, refs);
    } else {
      serviceCompRepo.deleteByServiceId(serviceId);
    }
  }

  @Override
  public void deleteByServiceIdIn(List<Long> serviceIds) {
    serviceCompRepo.deleteByServiceIdIn(serviceIds);
    ((RedisCaffeineCacheManager) cacheManager).evict("servicesComps", serviceIds.stream()
        .map(id -> "serviceId_" + id).collect(Collectors.toList()));
  }

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

  @Override
  protected BaseRepository<ServicesComp, Long> getRepository() {
    return serviceCompRepo;
  }
}
