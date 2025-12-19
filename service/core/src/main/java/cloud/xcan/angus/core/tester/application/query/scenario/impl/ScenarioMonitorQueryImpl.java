package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO_MONITOR;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioMonitorConverter.assembleScenarioMonitorCount0;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ScenarioMonitorFailed;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ScenarioMonitorFailedCode;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorHistoryQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorQuery;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorRepo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorSearchRepo;
import cloud.xcan.angus.core.tester.domain.setting.NoticeSetting;
import cloud.xcan.angus.remote.info.IdAndName;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Implementation of ScenarioMonitorQuery for scenario monitoring management and query operations.
 * </p>
 * <p>
 * Provides methods for scenario monitoring CRUD operations, failure notification handling, and
 * monitoring statistics assembly.
 * </p>
 */
@Service
public class ScenarioMonitorQueryImpl implements ScenarioMonitorQuery {

  @Resource
  private ScenarioMonitorRepo scenarioMonitorRepo;
  @Resource
  private ScenarioMonitorSearchRepo scenarioMonitorSearchRepo;
  @Resource
  private ScenarioMonitorHistoryQuery scenarioMonitorHistoryQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Get detailed information of a scenario monitor including monitoring statistics.
   * </p>
   * <p>
   * Assembles monitoring count information from history records.
   * </p>
   *
   * @param id Monitor ID
   * @return Scenario monitor with complete information
   */
  @Override
  public ScenarioMonitor detail(Long id) {
    return new BizTemplate<ScenarioMonitor>() {
      ScenarioMonitor monitorDb;

      @Override
      protected void checkParams() {
        monitorDb = checkAndFind(id);
      }

      @Override
      protected ScenarioMonitor process() {
        List<ScenarioMonitorHistoryInfo> histories =
            scenarioMonitorHistoryQuery.findInfoById(List.of(id));
        assembleScenarioMonitorCount0(monitorDb, histories);
        return monitorDb;
      }
    }.execute();
  }

  /**
   * <p>
   * List scenario monitors with optional full-text search and monitoring statistics assembly.
   * </p>
   * <p>
   * Supports both regular search and full-text search. Assembles monitoring count information for
   * all monitors in the result.
   * </p>
   *
   * @param spec           Monitor search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Full-text search keywords
   * @return Page of scenario monitors
   */
  @Override
  public Page<ScenarioMonitor> list(GenericSpecification<ScenarioMonitor> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ScenarioMonitor>>() {

      @Override
      protected Page<ScenarioMonitor> process() {
        Page<ScenarioMonitor> page = fullTextSearch
            ? scenarioMonitorSearchRepo.find(spec.getCriteria(), pageable,
            ScenarioMonitor.class, match)
            : scenarioMonitorRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          assembleScenarioMonitorCount(page);
        }
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find a scenario monitor by ID.
   * </p>
   *
   * @param id Monitor ID
   * @return Scenario monitor entity
   */
  @Override
  public ScenarioMonitor checkAndFind(Long id) {
    return scenarioMonitorRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ScenarioMonitor"));
  }

  /**
   * <p>
   * Check and find multiple scenario monitors by IDs.
   * </p>
   * <p>
   * Validates that all requested monitors exist and throws appropriate exceptions if any are
   * missing.
   * </p>
   *
   * @param ids Collection of monitor IDs
   * @return List of scenario monitors
   */
  @Override
  public List<ScenarioMonitor> checkAndFind(Collection<Long> ids) {
    List<ScenarioMonitor> monitors = scenarioMonitorRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(monitors), ids.iterator().next(), "ScenarioMonitor");
    if (ids.size() != monitors.size()) {
      for (ScenarioMonitor monitor : monitors) {
        assertResourceNotFound(ids.contains(monitor.getId()), monitor.getId(), "ScenarioMonitor");
      }
    }
    return monitors;
  }

  /**
   * <p>
   * Check if a monitor with the specified name already exists in the project.
   * </p>
   *
   * @param projectId Project ID
   * @param name      Monitor name
   */
  @Override
  public void checkExits(Long projectId, String name) {
    long count = scenarioMonitorRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(name, "ScenarioMonitor");
    }
  }

  /**
   * <p>
   * Assemble monitoring count information for a page of scenario monitors.
   * </p>
   * <p>
   * Batch retrieves monitoring history information and assembles count statistics to avoid N+1
   * query problems.
   * </p>
   *
   * @param page Page of scenario monitors
   */
  @Override
  public void assembleScenarioMonitorCount(Page<ScenarioMonitor> page) {
    Set<Long> monitorIds = page.getContent().stream().map(ScenarioMonitor::getId)
        .collect(Collectors.toSet());
    Map<Long, List<ScenarioMonitorHistoryInfo>> histories =
        scenarioMonitorHistoryQuery.findInfoById(monitorIds).stream()
            .collect(Collectors.groupingBy(ScenarioMonitorHistoryInfo::getMonitorId));
    for (ScenarioMonitor monitor : page.getContent()) {
      assembleScenarioMonitorCount0(monitor, histories.get(monitor.getId()));
    }
  }

  /**
   * <p>
   * Assemble and send failure notification events for scenario monitoring.
   * </p>
   * <p>
   * Sends notifications to monitor creators and configured organization members when monitoring
   * fails. Supports multiple notification types based on tenant settings.
   * </p>
   *
   * @param monitorDb Scenario monitor entity
   */
  @Override
  public void assembleAndSendScenarioMonitorFailureNoticeEvent(ScenarioMonitor monitorDb) {
    if (nonNull(monitorDb.getCreatedBy())) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(monitorDb.getTenantId())
          .get(ScenarioMonitorFailedCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      List<Long> receiveObjectIds = new ArrayList<>();
      receiveObjectIds.add(monitorDb.getCreatedBy());
      NoticeSetting noticeSetting = monitorDb.getNoticeSetting();
      if (nonNull(noticeSetting) && noticeSetting.getEnabled()) {
        Set<Long> orgUserIds = userManager.getUserIdByOrgTypeIn0(noticeSetting.getOrgType(),
            noticeSetting.getOrgs().stream().map(IdAndName::getId).collect(Collectors.toSet()));
        receiveObjectIds.addAll(orgUserIds);
      }
      String message = message(ScenarioMonitorFailed, new Object[]{monitorDb.getName(),
          monitorDb.getFailureMessage()}, PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(ScenarioMonitorFailedCode,
          message, SCENARIO_MONITOR.getValue(), monitorDb.getId().toString(), monitorDb.getName(),
          noticeTypes, receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }
}
