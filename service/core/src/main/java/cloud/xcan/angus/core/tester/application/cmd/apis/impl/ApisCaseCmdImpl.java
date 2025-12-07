package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API_CASE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisCaseConverter.setReplaceInfo;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.CLONE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.NAME_UPDATED;
import static cloud.xcan.angus.core.utils.CoreUtils.batchCopyPropertiesIgnoreNull;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisCaseConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of ApisCaseCmd for API test case management and command operations.
 * </p>
 * <p>
 * Provides comprehensive test case management services including adding, updating, replacing,
 * renaming, enabling/disabling, cloning, deleting, and syncing API cases. Ensures permission
 * checks, name uniqueness, quota validation, script synchronization, and activity logging.
 * </p>
 */
@Biz
public class ApisCaseCmdImpl extends CommCmd<ApisCase, Long> implements ApisCaseCmd {

  @Resource
  private ApisCaseRepo apisCaseRepo;
  @Resource
  private ApisCaseInfoRepo apisCaseInfoRepo;
  @Resource
  private ApisCaseQuery apisCaseQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ScriptCmd scriptCmd;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * <p>
   * Add a batch of API cases for a single API.
   * </p>
   * <p>
   * Validates API existence, permission, case type uniqueness, name duplication, and quota. Inserts
   * cases, synchronizes with scripts, and logs creation activities.
   * </p>
   *
   * @param cases List of API cases to add
   * @return List of ID keys for created cases
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<ApisCase> cases) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Validate all cases belong to the same API (single API constraint)
        Set<Long> apiIds = cases.stream().map(ApisCase::getApisId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(apiIds.size() == 1,
            "Only batch adding cases with one apis is allowed");
        apisDb = apisQuery.checkAndFind(apiIds.iterator().next());

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisDb.getId());

        // Ensure case types are unique within the API
        apisCaseQuery.checkExistedCaseType(apisDb.getId(), cases);

        // Validate case names are not duplicated
        apisCaseQuery.checkCaseNameExists(apisDb, cases);

        // Check quota limits for case creation
        apisCaseQuery.checkCaseQuota(cases.size(), apisDb.getId());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Set API information for all cases
        ApisCaseConverter.setApisInfo(cases, apisDb);

        // Insert all cases and get their IDs
        List<IdKey<Long, Object>> idKeys = batchInsert(cases, "name");

        // Synchronize test cases to script for execution
        scriptCmd.syncApisCaseToScript(apisDb, cases);

        // Log creation activities for all cases
        activityCmd.addAll(toActivities(API_CASE, cases, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Add cases for a specific API without synchronizing script content.
   * <p>
   * Only inserts cases and logs creation activities.
   */
  @Override
  public void add(Long apisId, List<ApisCase> cases) {
    if (isEmpty(cases)) {
      return;
    }

    batchInsert(cases, "name");

    activityCmd.addAll(toActivities(API_CASE, cases, ActivityType.CREATED));
  }

  /**
   * <p>
   * Update a batch of API cases.
   * </p>
   * <p>
   * Validates case existence, API, permission, and name duplication. Updates cases, synchronizes
   * with scripts, and logs update activities.
   * </p>
   *
   * @param cases List of API cases to update
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<ApisCase> cases) {
    new BizTemplate<Void>() {
      List<ApisCase> updatedCasesDb;
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Validate all cases exist and retrieve current records
        updatedCasesDb = apisCaseQuery.checkAndFind(
            cases.stream().map(ApisCase::getId).toList());

        // Ensure all cases belong to the same API (single API constraint)
        Set<Long> apiIds = updatedCasesDb.stream().map(ApisCase::getApisId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(apiIds.size() == 1,
            "Only batch adding cases with one apis is allowed");
        apisDb = apisQuery.checkAndFind(apiIds.iterator().next());

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisDb.getId());

        // Validate case names are not duplicated (safe update check)
        apisCaseQuery.checkAndSafeUpdateNameExists(apisDb, cases);
      }

      @Override
      protected Void process() {
        // Update cases with new properties (ignore null values)
        batchUpdate0(batchCopyPropertiesIgnoreNull(cases, updatedCasesDb));

        // Synchronize updated cases to script for execution
        scriptCmd.syncApisCaseToScript(apisDb, updatedCasesDb);

        // Log update activities for all cases
        activityCmd.addAll(toActivities(API_CASE, updatedCasesDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replace (add or update) a batch of API cases.
   * <p>
   * Handles both new and existing cases, validates all constraints, and logs activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(List<ApisCase> cases) {
    new BizTemplate<Void>() {
      List<ApisCase> updatedCases;
      List<ApisCase> updatedCasesDb;
      Apis apisDb;

      @Override
      protected void checkParams() {
        updatedCases = cases.stream().filter(x -> nonNull(x.getId())).toList();
        if (isNotEmpty(updatedCases)) {
          // Check the cases exists
          List<Long> ids = updatedCases.stream().map(ApisCase::getId).toList();
          updatedCasesDb = apisCaseQuery.checkAndFind(ids);

          // Check the apis exists
          Set<Long> apiIds = updatedCasesDb.stream().map(ApisCase::getApisId)
              .collect(Collectors.toSet());
          ProtocolAssert.assertTrue(apiIds.size() == 1,
              "Only batch adding cases with one apis is allowed");
          apisDb = apisQuery.checkAndFind(apiIds.iterator().next());

          // Check the test permission
          apisAuthQuery.checkTestAuth(getUserId(), apisDb.getId());

          // Check the name exists
          apisCaseQuery.checkAndSafeUpdateNameExists(apisDb, cases);

          // Check the names duplicate
          apisCaseQuery.checkAndSafeUpdateNameExists(apisDb, updatedCases);
        }
      }

      @Override
      protected Void process() {
        List<ApisCase> addCases = cases.stream().filter(x -> isNull(x.getId()))
            .toList();
        if (isNotEmpty(addCases)) {
          // Save cases
          ApisCaseConverter.setApisInfo(addCases, apisDb);
          add(addCases);
        }

        if (isNotEmpty(updatedCases)) {
          // Update cases
          Map<Long, ApisCase> updatedCasesMap = updatedCases.stream()
              .collect(Collectors.toMap(ApisCase::getId, x -> x));
          apisCaseRepo.batchUpdate(updatedCasesDb.stream()
              .map(x -> setReplaceInfo(x, apisDb, updatedCasesMap.get(x.getId())))
              .toList());

          // Synchronize testing cases to script
          scriptCmd.syncApisCaseToScript(apisDb, updatedCasesDb);

          activityCmd.addAll(toActivities(API_CASE, updatedCasesDb, ActivityType.UPDATED));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Rename an API case.
   * <p>
   * Validates name uniqueness and permission, updates the case, synchronizes with scripts, and logs
   * the activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      ApisCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(name, "Name is required");

        // Check and find case
        caseDb = apisCaseQuery.checkAndFind(id);

        // Check the test permission
        apisAuthQuery.checkTestAuth(getUserId(), caseDb.getApisId());

        if (!caseDb.getName().equals(name)) {
          // Check the update name exist
          apisCaseQuery.checkUpdateNameExists(caseDb.getApisId(), name, id);
        }
      }

      @Override
      protected Void process() {
        if (!name.equals(caseDb.getName())) {
          caseDb.setName(name);
          apisCaseRepo.save(caseDb);

          // Synchronize testing cases to script
          scriptCmd.renameCaseToScript(caseDb.getApisId(), caseDb.getId(), name);

          activityCmd.add(toActivity(API_CASE, caseDb, NAME_UPDATED, name));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Enable or disable a batch of API cases.
   * <p>
   * Validates permission, updates status, synchronizes with scripts, and logs the activity.
   */
  @Override
  public void enabled(Set<Long> ids, Boolean enabled) {
    new BizTemplate<Void>() {
      List<ApisCase> casesDb = null;

      @Override
      protected void checkParams() {
        // Check and find case
        casesDb = apisCaseQuery.checkAndFind(ids);

        // Check the apis exists
        Set<Long> apiIds = casesDb.stream().map(ApisCase::getApisId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(apiIds.size() == 1,
            "Only batch enabled or disabled cases with one apis is allowed");

        // Check the test permission
        apisAuthQuery.checkTestAuth(getUserId(), apiIds.iterator().next());
      }

      @Override
      protected Void process() {
        for (ApisCase aCase : casesDb) {
          aCase.setEnabled(enabled);
        }
        apisCaseRepo.batchUpdate(casesDb);

        // Synchronize testing cases to script
        scriptCmd.enableCaseToScript(casesDb.get(0).getApisId(), ids, enabled);

        activityCmd.addAll(toActivities(API_CASE, casesDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Synchronize all cases of an API to script.
   * <p>
   * Validates permission and synchronizes cases if present.
   */
  @Override
  public void syncToScript(Long apisId) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = apisQuery.checkAndFind(apisId);
        // Check the test permission
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        List<ApisCase> casesDb = apisCaseQuery.findByApisId(apisId);
        if (isNotEmpty(casesDb)) {
          scriptCmd.syncApisCaseToScript(apisDb, casesDb);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Clone a batch of API cases.
   * <p>
   * Validates permission, clones cases, synchronizes with scripts, and logs clone activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> clone(Set<Long> ids) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<ApisCase> casesDb;
      Set<Long> apiIds;
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = apisCaseQuery.checkAndFind(ids);
        // Check the apis exists
        apiIds = casesDb.stream().map(ApisCase::getApisId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(apiIds.size() == 1,
            "Only batch clone cases with one apis is allowed");
        // Check the apis exists
        apisDb = apisQuery.checkAndFind(casesDb.get(0).getApisId());

        // Check the test permission
        apisAuthQuery.checkTestAuth(getUserId(), apisDb.getId());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<ApisCase> clonedCases = new ArrayList<>();
        for (ApisCase caseDb : casesDb) {
          ApisCase clonedCase = ApisCaseConverter.toCloneCase(caseDb);
          apisCaseQuery.setSafeCloneName(clonedCase);
          clonedCases.add(clonedCase);
        }

        List<IdKey<Long, Object>> idKeys = batchInsert(clonedCases, "name");

        // Synchronize testing cases to script
        scriptCmd.syncApisCaseToScript(apisDb, clonedCases);

        // Add clone activities
        activityCmd.addAll(toActivities(API_CASE, clonedCases, CLONE,
            casesDb.stream().map(s -> new Object[]{s.getName()}).toList()));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Delete a batch of API cases.
   * <p>
   * Validates permission, updates delete status, synchronizes with scripts, and logs delete
   * activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<ApisCaseInfo> casesDb;
      Set<Long> apiIds;

      @Override
      protected void checkParams() {
        // Published api are not allowed to be modified
        casesDb = apisCaseInfoRepo.findAll0ByIdIn(ids);

        // Check the apis exists
        apiIds = casesDb.stream().map(ApisCaseInfo::getApisId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(apiIds.size() == 1,
            "Only batch clone cases with one apis is allowed");

        if (isNotEmpty(casesDb)) {
          // Check the test permission
          apisAuthQuery.batchCheckPermission(casesDb.stream().map(ApisCaseInfo::getApisId)
              .collect(Collectors.toSet()), ApiPermission.TEST);
        }
      }

      @Override
      protected Void process() {
        if (isEmpty(casesDb)) {
          return null;
        }

        // Update delete status
        apisCaseRepo.deleteByIdIn(ids);

        // Delete case in script
        scriptCmd.deleteCaseInScript(apiIds.iterator().next(), ids);

        // Add delete activity
        activityCmd.addAll(toActivities(API_CASE, casesDb, ActivityType.DELETED,
            activityParams(casesDb)));
        return null;
      }
    }.execute();
  }

  /**
   * Delete all cases by API IDs.
   * <p>
   * Physically deletes all cases for the given APIs.
   */
  @Override
  public void deleteByApisIdIn(List<Long> apiIds) {
    apisCaseRepo.deleteByApisIdIn(apiIds);
  }

  /**
   * Get the repository for ApisCase entity.
   * <p>
   *
   * @return the ApisCaseRepo instance
   */
  @Override
  protected BaseRepository<ApisCase, Long> getRepository() {
    return this.apisCaseRepo;
  }
}
