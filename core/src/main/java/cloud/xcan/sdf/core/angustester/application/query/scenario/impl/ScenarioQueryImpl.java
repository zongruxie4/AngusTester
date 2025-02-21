package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.sdf.core.angustester.application.converter.ScenarioConverter.countCreationScenario;
import static cloud.xcan.sdf.core.angustester.application.converter.ScenarioConverter.toScenarioDetailSummary;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SCE_NAME_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.ScenarioModification;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.ScenarioModificationCode;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserFullname;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.sdf.api.angusctrl.exec.ExecDoorRemote;
import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecInfoVo;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.ScenarioConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioListRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.sdf.core.angustester.domain.scenario.favorite.ScenarioFavouriteRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollowRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.summary.ScenarioDetailSummary;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.event.EventSender;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.model.script.ScriptSource;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
@SummaryQueryRegister(name = "Scenario", table = "scenario",
    groupByColumns = {"created_date", "auth_flag", "target"})
public class ScenarioQueryImpl implements ScenarioQuery {

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private ScenarioListRepo scenarioListRepo;

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
  private ExecDoorRemote execDoorRemote;

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

  @Override
  public void check(Long id) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        if (!scenarioRepo.existsById(id)) {
          throw ResourceNotFound.of(id, "Scenario");
        }
        return null;
      }
    }.execute();
  }

  @Override
  public List<Scenario> list(Set<Long> ids) {
    return new BizTemplate<List<Scenario>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<Scenario> process() {
        return scenarioRepo.findAll0ByIdIn(ids);
      }
    }.execute();
  }

  @Override
  public Page<Scenario> find(GenericSpecification<Scenario> spec,
      PageRequest pageable, Class<Scenario> clz) {
    return new BizTemplate<Page<Scenario>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<Scenario> process() {
        Set<SearchCriteria> criteria = spec.getCriterias();
        criteria.add(SearchCriteria.equal("deletedFlag", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Scenario> page = scenarioListRepo.find(criteria, pageable, clz,
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

  @Override
  public ScenarioResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ScenarioResourcesCreationCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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

  @NameJoin
  public static ScenarioDetailSummary getScenarioDetailSummary(Scenario scenario) {
    return toScenarioDetailSummary(scenario);
  }

  @Override
  public Scenario checkAndFind(Long id) {
    return scenarioRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Scenario"));
  }

  @Override
  public Scenario checkAndFind0(Long id) {
    return scenarioRepo.find0ById(id).orElseThrow(() -> ResourceNotFound.of(id, "Scenario"));
  }

  @Override
  public Script checkAndFindScenarioScript(Long id) {
    Script script = scriptQuery.findScriptByScenarioId(id);
    ProtocolAssert.assertResourceNotFound(script, id, "ScenarioScript");
    return script;
  }

  @Override
  public ScriptInfo checkAndFindScenarioScriptInfo(Long id) {
    ScriptInfo script = scriptQuery.findScriptInfoByScenarioId(id);
    ProtocolAssert.assertResourceNotFound(script, id, "ScenarioScript");
    return script;
  }

  @Override
  public void checkQuota(int inc) {
    long count = scenarioRepo.countAll0();
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterScenario, null, count + inc);
  }

  @Override
  public Scenario find0(Long id) {
    return scenarioRepo.find0ById(id).orElse(null);
  }

  @Override
  public List<Scenario> find0ByIdIn(Collection<Long> ids) {
    return scenarioRepo.findAll0ByIdIn(ids);
  }

  @Override
  public Scenario findLeastByProjectIdAndPluginAndTypeIn(Long projectId, String plugin,
      List<String> scriptTypes) {
    return scenarioRepo.findLeastByProjectIdAndPluginAndTypeIn(projectId, plugin, scriptTypes);
  }

  @Override
  public void checkNameExists(long projectId, String name) {
    Long count = scenarioRepo.countAll0ByNameAndProjectId(name, projectId);
    assertResourceExisted(count < 1, SCE_NAME_REPEATED_T, new Object[]{name});
  }

  @Override
  public void checkUpdateNameExists(long projectId, String name, long scenarioId) {
    Long count = scenarioRepo.countAll0ByNameAndProjectIdAndIdNot(name, projectId, scenarioId);
    assertResourceExisted(count < 1, SCE_NAME_REPEATED_T, new Object[]{name});
  }

  @Override
  public Boolean isAuthCtrl(Long id) {
    Scenario scenario = scenarioRepo.findById(id).orElse(null);
    return nonNull(scenario) && scenario.getAuthFlag();
  }

  /**
   * Set whether to collect
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
        s.setFavouriteFlag(true);
      }
    });
  }

  /**
   * Set follow state
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
        s.setFollowFlag(true);
      }
    });
  }

  /**
   * Set exec info
   */
  @Override
  public void setExecInfo(List<Scenario> scenarios) {
    Set<Long> scenarioIds = scenarios.stream().map(Scenario::getId).collect(Collectors.toSet());
    List<ExecInfoVo> execInfos = execDoorRemote
        .listInfoBySource(ScriptSource.SCENARIO.getValue(), scenarioIds, false)
        .orElseContentThrow();
    if(isEmpty(execInfos)){
      return;
    }
    Map<Long, List<ExecInfoVo>> execInfoMap = execInfos.stream()
        .collect(Collectors.groupingBy(ExecInfoVo::getScriptSourceId));
    scenarios.forEach(s -> {
      ExecInfoVo execInfoVo =
          execInfoMap.containsKey(s.getId()) ? execInfoMap.get(s.getId()).stream()
              // Sort in descending order to select the last execution
              .sorted(Comparator.comparing(ExecInfoVo::getActualStartDate,
                  Comparator.nullsLast(Comparator.naturalOrder())).reversed())
              .collect(Collectors.toList()).get(0) : null;
      if (nonNull(execInfoVo)) {
        String execFailureMessage = null;
        if (execInfoVo.getStatus().isWideFailed()) {
          execFailureMessage = isNotEmpty(execInfoVo.getLastSchedulingResult())
              ? execInfoVo.getLastSchedulingResult().stream()
              .filter(x -> !x.isSuccess() && nonNull(x.getMessage())).findFirst()
              .orElse(new RunnerRunVo()).getMessage() : null;
          execFailureMessage = nullSafe(execFailureMessage,
              nullSafe(execInfoVo.getMeterMessage(), "Unknown Error"));
        }
        s.setLastExecId(execInfoVo.getId()).setLastExecStatus(execInfoVo.getStatus())
            .setLastExecFailureMessage(execFailureMessage);
      }
    });
  }

  @Override
  public void setSafeCloneName(Scenario scenario) {
    String saltName = randomAlphanumeric(3);
    String clonedName = scenarioRepo.existsByProjectIdAndName(
        scenario.getProjectId(), scenario.getName() + "-Copy")
        ? scenario.getName() + "-Copy." + saltName : scenario.getName() + "-Copy";
    clonedName = clonedName.length() > DEFAULT_NAME_LENGTH ? clonedName.substring(0,
        DEFAULT_NAME_LENGTH - 3) + saltName : clonedName;
    scenario.setName(clonedName);
  }

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
    if (ObjectUtils.isNotEmpty(receiveObjectIds)) {
      String message = message(ScenarioModification, new Object[]{getUserFullname(),
              scenarioDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(ScenarioModificationCode, message,
          SCENARIO.getValue(), scenarioDb.getId().toString(), scenarioDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }


}
