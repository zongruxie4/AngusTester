package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioConverter.countCreationScenario;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioConverter.toScenarioDetailSummary;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCE_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ScenarioModification;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.ScenarioModificationCode;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserFullName;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.converter.ScenarioConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioListRepo;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioSearchRepo;
import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollowRepo;
import cloud.xcan.angus.core.tester.domain.scenario.summary.ScenarioDetailSummary;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of ScenarioQuery for scenario management and query operations.
 * </p>
 * <p>
 * Provides methods for scenario CRUD operations, execution tracking, statistics, and notification handling.
 * </p>
 */
@Biz
@SummaryQueryRegister(name = "Scenario", table = "scenario",
    groupByColumns = {"created_date", "auth", "target"})
public class ScenarioQueryImpl implements ScenarioQuery {

  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;
  @Resource
  private ScenarioListRepo scenarioListRepo;
  @Resource
  private ScenarioSearchRepo scenarioSearchRepo;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private ScenarioFavouriteRepo scenarioFavoriteRepo;
  @Resource
  private ScenarioFollowRepo scenarioFollowRepo;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private UserManager userManager;
  @Resource
  private ExecQuery execQuery;

  /**
   * <p>
   * Get detailed information of a scenario including script content and user interaction states.
   * </p>
   * <p>
   * Checks view permissions, sets favorite and follow states for user actions, and loads associated script information.
   * </p>
   * @param id Scenario ID
   * @return Scenario with complete information
   */
  @Override
  public Scenario detail(Long id) {
    return new BizTemplate<Scenario>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the to have view permission
        scenarioAuthQuery.checkViewAuth(PrincipalContext.getUserId(), id);

        // Check and find
        scenarioDb = checkAndFind(id);
      }

      @Override
      protected Scenario process() {
        if (isUserAction()) {
          // Set favourite state
          setFavourite(List.of(scenarioDb));
          // Set follow state
          setFollow(List.of(scenarioDb));
        }

        // Find the script of scenario
        Script script = checkAndFindScenarioScript(id);
        if (nonNull(script) && isNotEmpty(script.getContent())) {
          scenarioDb.setAngusScript(scriptQuery.checkAndParse(script.getContent(), false));
          scenarioDb.setScriptId(script.getId());
          scenarioDb.setScriptName(script.getName());
        }
        return scenarioDb;
      }
    }.execute();
  }

  /**
   * <p>
   * Check if a scenario exists by ID.
   * </p>
   * @param id Scenario ID
   */
  @Override
  public void check(Long id) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        if (!scenarioRepo.existsById(id)) {
          throw ResourceNotFound.of(id, "Scenario");
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Find scenarios by a set of IDs.
   * </p>
   * @param ids Set of scenario IDs
   * @return List of scenarios
   */
  @Override
  public List<Scenario> findByIds(Set<Long> ids) {
    return new BizTemplate<List<Scenario>>() {

      @Override
      protected List<Scenario> process() {
        return scenarioRepo.findAll0ByIdIn(ids);
      }
    }.execute();
  }

  /**
   * <p>
   * List scenarios with optional full-text search and comprehensive data assembly.
   * </p>
   * <p>
   * Checks project member permissions, applies authorization filters, and assembles execution information,
   * user interaction states, and user details for all scenarios in the result.
   * </p>
   * @param spec Scenario search specification
   * @param pageable Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match Full-text search keywords
   * @return Page of scenarios
   */
  @Override
  public Page<Scenario> list(GenericSpecification<Scenario> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Scenario>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<Scenario> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Scenario> page = fullTextSearch
            ? scenarioSearchRepo.find(criteria, pageable, Scenario.class,
            ScenarioConverter::objectArrToScenario, match)
            : scenarioListRepo.find(criteria, pageable, Scenario.class,
                ScenarioConverter::objectArrToScenario, null);

        if (page.hasContent()) {
          if (isUserAction()) {
            // Set favourite state
            setFavourite(page.getContent());
            // Set follow state
            setFollow(page.getContent());
          }
          // Set exec infos
          setExecInfo(page.getContent());
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Get scenario creation statistics for a project within a specified time range.
   * </p>
   * <p>
   * Supports filtering by creator object type and provides comprehensive creation count statistics.
   * </p>
   * @param projectId Project ID
   * @param creatorObjectType Type of creator object
   * @param creatorObjectId Creator object ID
   * @param createdDateStart Start date for filtering
   * @param createdDateEnd End date for filtering
   * @return Scenario creation statistics
   */
  @Override
  public ScenarioResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ScenarioResourcesCreationCount>() {

      @Override
      protected ScenarioResourcesCreationCount process() {
        final ScenarioResourcesCreationCount result = new ScenarioResourcesCreationCount();

        // Find all when condition is null, else find by condition
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        Set<SearchCriteria> commonDeletedFilters = getCommonDeletedResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);
        List<Scenario> scenarios = scenarioRepo.findAllByFilters(commonDeletedFilters);

        // Number of statistical scenario
        countCreationScenario(result, scenarios);
        return result;
      }
    }.execute();
  }

  /**
   * <p>
   * Convert a scenario to a detail summary format.
   * </p>
   * @param scenario Scenario entity
   * @return Scenario detail summary
   */
  @NameJoin
  public static ScenarioDetailSummary getScenarioDetailSummary(Scenario scenario) {
    return toScenarioDetailSummary(scenario);
  }

  /**
   * <p>
   * Check and find a scenario by ID.
   * </p>
   * @param id Scenario ID
   * @return Scenario entity
   */
  @Override
  public Scenario checkAndFind(Long id) {
    return scenarioRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Scenario"));
  }

  /**
   * <p>
   * Check and find a scenario by ID using optimized query.
   * </p>
   * @param id Scenario ID
   * @return Scenario entity
   */
  @Override
  public Scenario checkAndFind0(Long id) {
    return scenarioRepo.find0ById(id).orElseThrow(() -> ResourceNotFound.of(id, "Scenario"));
  }

  /**
   * <p>
   * Check and find the script associated with a scenario.
   * </p>
   * @param id Scenario ID
   * @return Script entity
   */
  @Override
  public Script checkAndFindScenarioScript(Long id) {
    Script script = scriptQuery.findScriptByScenarioId(id);
    assertResourceNotFound(script, id, "ScenarioScript");
    return script;
  }

  /**
   * <p>
   * Check and find the script information associated with a scenario.
   * </p>
   * @param id Scenario ID
   * @return Script information entity
   */
  @Override
  public ScriptInfo checkAndFindScenarioScriptInfo(Long id) {
    ScriptInfo script = scriptQuery.findScriptInfoByScenarioId(id);
    assertResourceNotFound(script, id, "ScenarioScript");
    return script;
  }

  /**
   * <p>
   * Check if the scenario quota is exceeded after increment.
   * </p>
   * @param inc Number of scenarios to add
   */
  @Override
  public void checkQuota(int inc) {
    long count = scenarioRepo.countAll0();
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterScenario, null, count + inc);
  }

  /**
   * <p>
   * Find a scenario by ID using optimized query, returns null if not found.
   * </p>
   * @param id Scenario ID
   * @return Scenario entity or null
   */
  @Override
  public Scenario find0(Long id) {
    return scenarioRepo.find0ById(id).orElse(null);
  }

  /**
   * <p>
   * Find scenarios by a collection of IDs using optimized query.
   * </p>
   * @param ids Collection of scenario IDs
   * @return List of scenarios
   */
  @Override
  public List<Scenario> find0ByIdIn(Collection<Long> ids) {
    return scenarioRepo.findAll0ByIdIn(ids);
  }

  /**
   * <p>
   * Find the least recently used scenario by project, plugin, and script types.
   * </p>
   * @param projectId Project ID
   * @param plugin Plugin name
   * @param scriptTypes List of script types
   * @return Scenario entity
   */
  @Override
  public Scenario findLeastByProjectIdAndPluginAndTypeIn(Long projectId, String plugin,
      List<String> scriptTypes) {
    return scenarioRepo.findLeastByProjectIdAndPluginAndTypeIn(projectId, plugin, scriptTypes);
  }

  /**
   * <p>
   * Check if a scenario name already exists in the project when adding a new scenario.
   * </p>
   * @param projectId Project ID
   * @param name Scenario name
   */
  @Override
  public void checkNameExists(long projectId, String name) {
    Long count = scenarioRepo.countAll0ByNameAndProjectId(name, projectId);
    assertResourceExisted(count < 1, SCE_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * <p>
   * Check if a scenario name already exists when updating a scenario, excluding the current scenario.
   * </p>
   * @param projectId Project ID
   * @param name Scenario name
   * @param scenarioId Current scenario ID
   */
  @Override
  public void checkUpdateNameExists(long projectId, String name, long scenarioId) {
    Long count = scenarioRepo.countAll0ByNameAndProjectIdAndIdNot(name, projectId, scenarioId);
    assertResourceExisted(count < 1, SCE_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * <p>
   * Check if authorization control is enabled for a scenario.
   * </p>
   * @param id Scenario ID
   * @return true if authorization control is enabled, false otherwise
   */
  @Override
  public Boolean isAuthCtrl(Long id) {
    Scenario scenario = scenarioRepo.findById(id).orElse(null);
    return nonNull(scenario) && scenario.getAuth();
  }

  /**
   * <p>
   * Set favorite state for a list of scenarios.
   * </p>
   * <p>
   * Batch retrieves favorite information to avoid N+1 query problems.
   * </p>
   * @param scenarios List of scenarios to update
   */
  @Override
  public void setFavourite(List<Scenario> scenarios) {
    Set<Long> scenarioIds = scenarios.stream().map(Scenario::getId).collect(Collectors.toSet());
    List<ScenarioFavourite> favourites = scenarioFavoriteRepo
        .findAllByScenarioIdInAndCreatedBy(scenarioIds, getUserId());
    Set<Long> favouritesIds = favourites.stream().map(ScenarioFavourite::getScenarioId)
        .collect(Collectors.toSet());
    scenarios.forEach(s -> {
      if (favouritesIds.contains(s.getId())) {
        s.setFavourite(true);
      }
    });
  }

  /**
   * <p>
   * Set follow state for a list of scenarios.
   * </p>
   * <p>
   * Batch retrieves follow information to avoid N+1 query problems.
   * </p>
   * @param scenarios List of scenarios to update
   */
  @Override
  public void setFollow(List<Scenario> scenarios) {
    Set<Long> scenarioIds = scenarios.stream().map(Scenario::getId).collect(Collectors.toSet());
    List<ScenarioFollow> follows = scenarioFollowRepo
        .findAllByScenarioIdInAndCreatedBy(scenarioIds, getUserId());
    Set<Long> followApiIds = follows.stream().map(ScenarioFollow::getScenarioId)
        .collect(Collectors.toSet());
    scenarios.forEach(s -> {
      if (followApiIds.contains(s.getId())) {
        s.setFollow(true);
      }
    });
  }

  /**
   * <p>
   * Set execution information for a list of scenarios.
   * </p>
   * <p>
   * Retrieves the latest execution information for each scenario and sets failure messages for failed executions.
   * </p>
   * @param scenarios List of scenarios to update
   */
  @Override
  public void setExecInfo(List<Scenario> scenarios) {
    Set<Long> scenarioIds = scenarios.stream().map(Scenario::getId).collect(Collectors.toSet());
    List<ExecInfo> execInfos = execQuery
        .listInfoBySource(ScriptSource.SCENARIO, scenarioIds, false);
    if (isEmpty(execInfos)) {
      return;
    }
    Map<Long, List<ExecInfo>> execInfoMap = execInfos.stream()
        .collect(Collectors.groupingBy(ExecInfo::getScriptSourceId));
    scenarios.forEach(s -> {
      ExecInfo execInfo =
          execInfoMap.containsKey(s.getId()) ? execInfoMap.get(s.getId()).stream()
              // Sort in descending order to select the last execution
              .sorted(Comparator.comparing(ExecInfo::getActualStartDate,
                  Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList().get(0)
              : null;
      if (nonNull(execInfo)) {
        String execFailureMessage = null;
        if (execInfo.getStatus().isWideFailed()) {
          execFailureMessage = isNotEmpty(execInfo.getLastSchedulingResult())
              ? execInfo.getLastSchedulingResult().stream()
              .filter(x -> !x.isSuccess() && nonNull(x.getMessage())).findFirst()
              .orElse(new RunnerRunVo()).getMessage() : null;
          execFailureMessage = nullSafe(execFailureMessage,
              nullSafe(execInfo.getMeterMessage(), "Unknown Error"));
        }
        s.setLastExecId(execInfo.getId())
            .setLastExecName(execInfo.getName())
            .setLastExecStatus(execInfo.getStatus())
            .setLastExecFailureMessage(execFailureMessage)
            .setLastExecDate(execInfo.getActualStartDate());
      }
    });
  }

  /**
   * <p>
   * Set a safe clone name for a scenario to avoid naming conflicts.
   * </p>
   * <p>
   * Generates a unique name by appending "-Copy" and random suffix if necessary, ensuring the name length is within limits.
   * </p>
   * @param scenario Scenario to set clone name for
   */
  @Override
  public void setSafeCloneName(Scenario scenario) {
    String saltName = randomAlphanumeric(3);
    String clonedName = scenarioRepo.existsByProjectIdAndName(
        scenario.getProjectId(), scenario.getName() + "-Copy")
        ? scenario.getName() + "-Copy." + saltName : scenario.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH - 3) + saltName : clonedName;
    scenario.setName(clonedName);
  }

  /**
   * <p>
   * Assemble and send modification notification events for scenario changes.
   * </p>
   * <p>
   * Sends notifications to scenario creators and followers when scenarios are modified.
   * Supports multiple notification types based on tenant settings.
   * </p>
   * @param scenarioDb Scenario entity
   * @param activity Activity information
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(Scenario scenarioDb, Activity activity) {
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(scenarioDb.getTenantId())
        .get(ScenarioModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(scenarioDb.getCreatedBy());
    List<Long> followUserIds = scenarioFollowRepo.findUserIdsByScenarioId(scenarioDb.getId());
    receiveObjectIds.addAll(followUserIds);
    receiveObjectIds.remove(getUserId());
    if (isNotEmpty(receiveObjectIds)) {
      String message = message(ScenarioModification, new Object[]{
              getUserFullName(), scenarioDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(ScenarioModificationCode, message,
          SCENARIO.getValue(), scenarioDb.getId().toString(), scenarioDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }


}
