package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO_MONITOR;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioMonitorConverter.setReplaceInfo;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.lengthSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioMonitorCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioMonitorHistoryCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryRepo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorRepo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.core.tester.domain.setting.NoticeSetting;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Command implementation for scenario monitoring management operations.
 * <p>
 * Provides comprehensive CRUD operations for scenario monitors including creation, modification,
 * execution, and lifecycle management.
 * <p>
 * Implements business logic validation, permission checks, activity logging, and transaction
 * management for all monitoring operations.
 * <p>
 * Supports scheduled execution, failure notification, history tracking, and comprehensive activity
 * monitoring.
 */
@Service
public class ScenarioMonitorCmdImpl extends CommCmd<ScenarioMonitor, Long>
    implements ScenarioMonitorCmd {

  @Resource
  private ScenarioMonitorRepo scenarioMonitorRepo;
  @Resource
  private ScenarioMonitorHistoryRepo scenarioMonitorHistoryRepo;
  @Resource
  private ScenarioMonitorHistoryCmd scenarioMonitorHistoryCmd;
  @Resource
  private ScenarioMonitorQuery scenarioMonitorQuery;
  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a new scenario monitor to the system.
   * <p>
   * Performs comprehensive validation including scenario existence, user permissions, monitor name
   * uniqueness, and notification settings.
   * <p>
   * Initializes monitor with pending status and calculates next execution date.
   * <p>
   * Logs creation activity and establishes proper monitoring setup.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> add(ScenarioMonitor monitor) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Ensure scenario ID is provided
        assertNotNull(monitor.getScenarioId(), "scenarioId is required");

        // Ensure the scenario exists in database
        scenarioDb = scenarioQuery.checkAndFind(monitor.getScenarioId());

        // Check user permission to view the scenario
        scenarioAuthQuery.checkViewAuth(getUserId(), scenarioDb.getId());

        // Validate monitor name is unique within the project
        scenarioMonitorQuery.checkExits(monitor.getProjectId(), monitor.getName());

        // Validate notification settings if enabled
        NoticeSetting setting = monitor.getNoticeSetting();
        assertTrue(!setting.getEnabled()
                || nonNull(setting.getOrgType()) && nonNull(setting.getOrgs()),
            "Notice org info is required");
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Initialize monitor with project and execution settings
        monitor.setProjectId(scenarioDb.getProjectId());
        monitor.setStatus(ScenarioMonitorStatus.PENDING);
        monitor.setScriptId(scenarioDb.getScriptId());
        monitor.setNextExecDate(monitor.getTimeSetting().getNextDate(LocalDateTime.now()));
        IdKey<Long, Object> idKey = insert(monitor);

        // Log monitor creation activity for audit
        activityCmd.add(toActivity(SCENARIO_MONITOR, monitor, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Updates an existing scenario monitor in the system.
   * <p>
   * Validates monitor existence and name uniqueness before updating monitor details.
   * <p>
   * Recalculates next execution date if time settings are modified.
   * <p>
   * Logs update activity and maintains monitoring schedule integrity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void update(ScenarioMonitor monitor) {
    new BizTemplate<Void>() {
      ScenarioMonitor monitorDb;

      @Override
      protected void checkParams() {
        // Ensure the monitor exists in database
        monitorDb = scenarioMonitorQuery.checkAndFind(monitor.getId());

        // Validate monitor name is unique if changed
        if (isNotEmpty(monitor.getName()) && !monitorDb.getName().equals(monitor.getName())) {
          scenarioMonitorQuery.checkExits(monitorDb.getProjectId(), monitor.getName());
        }
      }

      @Override
      protected Void process() {
        // Recalculate next execution date if time settings changed
        if (nonNull(monitor.getTimeSetting())) {
          monitor.setNextExecDate(monitor.getTimeSetting()
              .getNextDate(nullSafe(monitorDb.getLastMonitorDate(), LocalDateTime.now())));
        }

        // Update monitor information in database
        scenarioMonitorRepo.save(copyPropertiesIgnoreNull(monitor, monitorDb));

        // Log monitor update activity for audit
        activityCmd.add(toActivity(SCENARIO_MONITOR, monitorDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> replace(ScenarioMonitor monitor) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ScenarioMonitor monitorDb;

      @Override
      protected void checkParams() {
        if (nonNull(monitor.getId())) {
          // Check the monitor exists
          monitorDb = scenarioMonitorQuery.checkAndFind(monitor.getId());
          // Check the monitor name exists
          if (isNotEmpty(monitor.getName()) && !monitorDb.getName().equals(monitor.getName())) {
            scenarioMonitorQuery.checkExits(monitorDb.getProjectId(), monitor.getName());
          }
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(monitor.getId())) {
          return add(monitor);
        }

        setReplaceInfo(monitorDb, monitor);
        scenarioMonitorRepo.save(monitorDb);

        activityCmd.add(toActivity(SCENARIO_MONITOR, monitorDb, ActivityType.UPDATED));

        return new IdKey<Long, Object>().setId(monitorDb.getId()).setKey(monitorDb.getName());
      }
    }.execute();
  }

  /**
   * Executes a scenario monitor immediately and records the results.
   * <p>
   * Validates monitor existence and executes the monitoring scenario.
   * <p>
   * Updates monitor status, failure messages, and next execution date.
   * <p>
   * Sends failure notifications and maintains execution history.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void runNow(Long id) {
    new BizTemplate<Void>() {
      ScenarioMonitor monitorDb;

      @Override
      protected void checkParams() {
        // Ensure the monitor exists in database
        monitorDb = scenarioMonitorQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        try {
          // Set execution context for system-initiated runs
          if (!isUserAction()) {
            commonQuery.setInnerPrincipal(monitorDb.getTenantId(), monitorDb.getCreatedBy());
          }

          // Execute the monitoring scenario and record results
          LocalDateTime now = LocalDateTime.now();
          ScenarioMonitorHistory history = scenarioMonitorHistoryCmd.run(monitorDb);

          // Update monitor status and execution information
          monitorDb.setStatus(history.getStatus())
              .setFailureMessage(lengthSafe(history.getFailureMessage(), 400))
              .setLastMonitorHistoryId(history.getId())
              .setLastMonitorDate(history.getCreatedDate());

          // Calculate next execution date for recurring monitors
          if (nonNull(monitorDb.getTimeSetting()) && !monitorDb.getTimeSetting().isOnetime()) {
            monitorDb.setNextExecDate(monitorDb.getTimeSetting().getNextDate(now));
          }
          scenarioMonitorRepo.save(monitorDb);

          // Send failure notification if execution failed
          if (monitorDb.getStatus().isFailure()) {
            scenarioMonitorQuery.assembleAndSendScenarioMonitorFailureNoticeEvent(monitorDb);
          }
        } finally {
          // Clean up execution context for system-initiated runs
          if (!isUserAction()) {
            PrincipalContext.remove();
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Deletes scenario monitors and their associated history records.
   * <p>
   * Validates monitor existence and removes all related monitoring data.
   * <p>
   * Cleans up monitoring history and logs deletion activities.
   * <p>
   * This operation is irreversible and removes all monitoring records.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<ScenarioMonitor> monitorDb;

      @Override
      protected void checkParams() {
        // Ensure all monitors exist in database
        monitorDb = scenarioMonitorQuery.checkAndFind(ids);
      }

      @Override
      protected Void process() {
        // Delete all monitoring history records first
        scenarioMonitorHistoryRepo.deleteByMonitorIdIn(ids);

        // Delete all monitors
        scenarioMonitorRepo.deleteByIdIn(ids);

        // Log deletion activities for audit
        activityCmd.addAll(toActivities(SCENARIO_MONITOR, monitorDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ScenarioMonitor, Long> getRepository() {
    return scenarioMonitorRepo;
  }

}
