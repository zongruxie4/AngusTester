package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.MAX_FREE_CONCURRENT_TASK;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.MAX_FREE_EXEC_NUM;
import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterConcurrency;
import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterConcurrentTask;
import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterNode;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_ALREADY_IN_RUNNING_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_ALREADY_IN_STOPPED_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NO_OP_PERMISSION;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NO_OP_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_TRIAL_CONCURRENT_TASK_OVER_LIMIT_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_TRIAL_TASK_OVER_LIMIT_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NODE_IS_NOT_APP_ROLE_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NODE_IS_NOT_EXEC_ROLE_T;
import static cloud.xcan.angus.core.utils.AngusUtils.toServer;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isJobOrDoorApi;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.model.AngusConstant.SAMPLE_TOTAL_NAME;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.parser.AngusParser.YAML_MAPPER;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.setting.SettingKey;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.angus.api.commonlink.setting.tenant.event.TesterEvent;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.SettingManager;
import cloud.xcan.angus.api.manager.SettingTenantManager;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.pojo.node.NodeInfo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.SneakyThrow0;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.entity.projection.IdAndName;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleExtcQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoListRepo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoRepo;
import cloud.xcan.angus.core.tester.domain.exec.ExecRepo;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.pipeline.PipelineBuilder;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
@SummaryQueryRegister(name = "Exec", table = "exec",
    groupByColumns = {"created_date", "script_type", "status",}
)
public class ExecQueryImpl implements ExecQuery {

  @Resource
  private ExecRepo execRepo;

  @Resource
  private ExecInfoRepo execInfoRepo;

  @Resource
  private ExecSampleExtcQuery execSampleExtcQuery;

  @Resource
  private ExecInfoListRepo execInfoListRepo;

  @Resource
  private ExecSampleQuery execSampleQuery;

  @Resource
  private SettingManager settingManager;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private SettingTenantManager settingTenantManager;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Override
  public Exec detail(Long id) {
    return new BizTemplate<Exec>() {

      @Override
      protected Exec process() {
        Exec exec = checkAndFind(id);

        // For sharding invoke by innerapi
        if (isJobOrDoorApi()) {
          PrincipalContext.get().setOptTenantId(exec.getTenantId());
        }

        setParsedScriptContent(exec);

        setExecNodeInfo(exec);

        setSampleContent(exec, id);

        List<Exec> execs = List.of(exec);
        setExecScriptName(execs);

        setExecCurrentOperationPermission(execs);

        execSampleQuery.setExecLatestTotalMergeSample(execs);
        return exec;
      }
    }.execute();
  }

  @Override
  public List<ExecInfo> listInfo(Set<Long> ids, Boolean joinSampleSummary) {
    return new BizTemplate<List<ExecInfo>>() {

      @Override
      protected List<ExecInfo> process() {
        List<ExecInfo> execs = findInfo(ids);

        // For sharding invoke by innerapi
        setSampleSummary(execs, joinSampleSummary);
        return execs;
      }
    }.execute();
  }

  @Override
  public List<ExecInfo> listInfoBySource(ScriptSource resourceType, Set<Long> resourceIds,
      Boolean joinSampleSummary) {
    return new BizTemplate<List<ExecInfo>>() {

      @Override
      protected List<ExecInfo> process() {
        List<ExecInfo> execs = findInfoBySource(resourceType, resourceIds);

        setSampleSummary(execs, joinSampleSummary);
        return execs;
      }
    }.execute();
  }

  @Override
  public String script(Long id) {
    return new BizTemplate<String>() {
      Exec execDb;

      @Override
      protected void checkParams() {
        execDb = checkAndFind(id);
      }

      @Override
      protected String process() {
        return execDb.getScript();
      }
    }.execute();
  }

  @Override
  public List<Server> findServers(Long id) {
    return new BizTemplate<List<Server>>() {
      Exec execDb;

      @Override
      protected void checkParams() {
        execDb = checkAndFind(id);
        assertTrue(PLUGIN_HTTP_NAME.equals(execDb.getPlugin()), "Only http plugin is supported");
      }

      @Override
      protected List<Server> process() {
        AngusScript angusScript;
        try {
          angusScript = YAML_MAPPER.readValue(execDb.getScript(), AngusScript.class);
        } catch (JsonProcessingException e) {
          throw ProtocolException.of("Execution script parsing exception: " + e.getMessage());
        }
        List<Server> servers = new ArrayList<>();
        if (nonNull(angusScript.getConfiguration()) && isNotEmpty(
            angusScript.getConfiguration().getVariables())) {
          servers.addAll(angusScript.getConfiguration().getVariables().stream()
              .filter(x -> x.isExtractable() && x.getExtraction() instanceof HttpExtraction)
              .map(x -> (HttpExtraction) x.getExtraction())
              .filter(x -> nonNull(x.getRequest().getServer())
                  && nonNull(x.getRequest().getServer().getUrl()))
              .map(x -> toServer(x.getRequest().getServer())).toList());
        }
        if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
          servers.addAll(angusScript.getTask().getPipelines().stream().filter(
                  x -> x instanceof Http && nonNull(((Http) x).getRequest())
                      && nonNull(((Http) x).getRequest().getServer())
                      && nonNull(((Http) x).getRequest().getServer().getUrl()))
              .map(x -> toServer(((Http) x).getRequest().getServer())).toList());
        }
        return servers.stream()
            .filter(cloud.xcan.angus.spec.utils.ObjectUtils.distinctByKey(Server::getUrl))
            .collect(Collectors.toList());
      }
    }.execute();
  }

  @SneakyThrow0(level = "WARN") // Check exec quota in running
  @Override
  public Page<ExecInfo> find(GenericSpecification<ExecInfo> spec, PageRequest pageable) {
    return new BizTemplate<Page<ExecInfo>>() {
      String projectId;

      @Override
      protected void checkParams() {
        projectId = findFirstValue(spec.getCriteria(), "projectId", SearchOperation.EQUAL);
        assertNotNull(projectId, "projectId must not be null");
      }

      @Override
      protected Page<ExecInfo> process() {
        Page<ExecInfo> page = execInfoListRepo.find(spec.getCriteria(), pageable,
            ExecInfo.class, null);
        if (page.hasContent()) {
          setExecInfoScriptName(page.getContent());

          setExecInfoCurrentOperationPermission(page.getContent());

          execSampleQuery.setExecInfoLatestTotalMergeSample(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<ExecInfo> findByNodeId(Long nodeId) {
    return new BizTemplate<List<ExecInfo>>() {

      @Override
      protected List<ExecInfo> process() {
        return execInfoRepo.findAllByNodeId(nodeId);
      }
    }.execute();
  }

  @Override
  public Exec findByScript(Long scriptId, ScriptType scriptType, ScriptSource scriptSource) {
    List<Exec> execsDb = execRepo.findByScriptId(scriptId);
    if (isEmpty(execsDb)) {
      return null;
    }
    return scriptSource.isApis() ? execsDb.get(0)
        : execsDb.stream().filter(x -> x.getScriptType().equals(scriptType)).findFirst()
            .orElse(null);
  }

  @Override
  public ExecInfo findInfo(Long id) {
    return execInfoRepo.findById(id).orElse(null);
  }

  @Override
  public List<ExecInfo> findInfoBySource(ScriptSource resourceType,
      Set<Long> resourceIds) {
    return execInfoRepo.findByScriptSourceAndScriptSourceIdIn(resourceType, resourceIds);
  }

  @Override
  public List<ExecInfo> findInfo(Collection<Long> execIds) {
    return execInfoRepo.findAllById(execIds);
  }

  @Override
  public Map<Long, IdAndName> findInfoMap(Collection<Long> execIds) {
    return execRepo.findInfoByIdIn(execIds).stream()
        .collect(Collectors.toMap(IdAndName::getId, x -> x));
  }

  @Override
  public Map<String, List<NoticeType>> findTenantEventNoticeTypes(Long tenantId) {
    Long finalTenantId = nullSafe(tenantId, getOptTenantId());
    String cachedSettingTenant = settingTenantManager.getCachedSetting(finalTenantId);
    SettingTenant settingTenant = settingTenantManager.parseCachedSetting(cachedSettingTenant);
    List<TesterEvent> eventData =
        isNull(settingTenant) || isEmpty(settingTenant.getTesterEventData())
            ? settingManager.setting(SettingKey.TESTER_EVENT).getTesterEvent()
            : settingTenant.getTesterEventData();
    return eventData.stream()
        .filter(x -> ObjectUtils.isNotEmpty(x.getEventCode()) && isNotEmpty(x.getNoticeTypes())).
        collect(Collectors.toMap(TesterEvent::getEventCode, TesterEvent::getNoticeTypes));
  }

  @Override
  public Exec checkAndFind(Long id) {
    return execRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Exec"));
  }

  @Override
  public List<Exec> checkAndFind(Collection<Long> ids) {
    List<Exec> execs = execRepo.findAllById(ids);
    Set<Long> execIds = execs.stream().map(Exec::getId).collect(Collectors.toSet());
    ids.removeAll(execIds);
    assertResourceNotFound(isEmpty(ids), ids, "Exec");
    return execs;
  }

  @Override
  public ExecInfo checkAndFindInfo(Long id) {
    return execInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Exec"));
  }

  @Override
  public List<ExecInfo> checkAndFindInfo(Collection<Long> ids) {
    List<ExecInfo> execs = execInfoRepo.findAllById(ids);
    Set<Long> execIds = execs.stream().map(ExecInfo::getId).collect(Collectors.toSet());
    ids.removeAll(execIds);
    assertResourceNotFound(isEmpty(ids), ids, "Exec");
    return execs;
  }

  @Override
  public void checkNotRunning(Exec exec) {
    assertTrue(!exec.getStatus().isRunning(), EXEC_ALREADY_IN_RUNNING_T,
        new Object[]{exec.getName()});
  }

  @Override
  public void checkNotStopped(ExecInfo exec) {
    assertTrue(!exec.getStatus().isWideStopped(), EXEC_ALREADY_IN_STOPPED_T,
        new Object[]{exec.getName()});
  }

  /**
   * Check available and application nodes existence and role types are consistent.
   */
  @Override
  public void checkNodeValid(Configuration configuration, boolean trial) {
    if (!configuration.hasAppNodes() && !configuration.hasAvailableNodes()) {
      return;
    }
    Set<Long> nodeIds = getAllNodeIds(configuration);
    if (isNotEmpty(nodeIds)) {
      Map<Long, Node> scriptMap = nodeInfoQuery.getNodeMap(nodeIds, trial);
      if (isNotEmpty(scriptMap)) {
        if (isNotEmpty(configuration.getNodeSelectors().getAvailableNodeIds())) {
          for (Long nodeId : configuration.getNodeSelectors().getAvailableNodeIds()) {
            assertResourceNotFound(scriptMap.containsKey(nodeId), nodeId, "Node");
            assertTrue(scriptMap.get(nodeId).getRoles().contains(NodeRole.EXECUTION),
                message(NODE_IS_NOT_EXEC_ROLE_T, new Object[]{nodeId}));
          }
        }
        if (isNotEmpty(configuration.getNodeSelectors().getAppNodeIds())) {
          for (Long nodeId : configuration.getNodeSelectors().getAppNodeIds()) {
            assertResourceNotFound(scriptMap.containsKey(nodeId), nodeId, "Node");
            assertTrue(scriptMap.get(nodeId).getRoles().contains(NodeRole.APPLICATION),
                message(NODE_IS_NOT_APP_ROLE_T, new Object[]{nodeId}));
          }
        }
      }
    }
  }

  @Override
  public void checkPermission(Exec exec) {
    if (isEmpty(exec) || isAdminUser() || !isUserAction()) {
      return;
    }
    if (-1L != exec.getCreatedBy() && !exec.getCreatedBy().equals(getUserId())) {
      throw BizException.of(EXEC_NO_OP_PERMISSION_CODE, EXEC_NO_OP_PERMISSION);
    }
  }

  @Override
  public void checkPermissionInfo(ExecInfo exec) {
    if (isEmpty(exec) || isAdminUser() || !isUserAction()) {
      return;
    }
    if (-1L != exec.getCreatedBy() && !exec.getCreatedBy().equals(getUserId())) {
      throw BizException.of(EXEC_NO_OP_PERMISSION_CODE, EXEC_NO_OP_PERMISSION);
    }
  }

  @Override
  public void checkAddQuota(long incr, boolean trial) {
    if (incr > 0) {
      if (trial) {
        long num = execRepo.countByTenantIdAndTrial(getOptTenantId(), true);
        if (num >= MAX_FREE_EXEC_NUM) {
          throw QuotaException.of(message(EXEC_TRIAL_TASK_OVER_LIMIT_T,
              new Object[]{MAX_FREE_EXEC_NUM}));
        }
      } else {
        long num = execRepo.countByTenantId(getOptTenantId());
        settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterExecution,
            null, num + incr);
      }
    }
  }

  @Override
  public void checkThreadAndNodesQuota(Boolean trial, Configuration configuration) {
    /* If trial, Front end verification, backend forcibly overwrites as security value */
    if ((isNull(trial) || !trial) && nonNull(configuration)) {
      if (nonNull(configuration.getThread())) {
        settingTenantQuotaManager.checkTenantQuota(AngusTesterConcurrency,
            null, (long) configuration.getThread().getThreads());
      }
      if (nonNull(configuration.getNodeSelectors())
          && nonNull(configuration.getNodeSelectors().getNum())) {
        settingTenantQuotaManager.checkTenantQuota(AngusTesterNode,
            null, (long) configuration.getNodeSelectors().getNum());
      }
    }
  }

  @SneakyThrow0(level = "WARN")
  @Override
  public void checkConcurrentTaskQuota(long incr, boolean trial) {
    if (incr > 0) {
      checkConcurrentTaskQuota00(incr, trial);
    }
  }

  @Override
  public void checkConcurrentTaskQuota0(long incr, boolean trial) {
    if (incr > 0) {
      checkConcurrentTaskQuota00(incr, trial);
    }
  }

  private void checkConcurrentTaskQuota00(long incr, boolean trial) {
    long num = execRepo.countByTenantIdAndStatus(getOptTenantId(), ExecStatus.RUNNING);
    if (trial) {
      if (num >= MAX_FREE_CONCURRENT_TASK) {
        throw QuotaException.of(message(EXEC_TRIAL_CONCURRENT_TASK_OVER_LIMIT_T,
            new Object[]{MAX_FREE_CONCURRENT_TASK}));
      }
    } else {
      settingTenantQuotaManager.checkTenantQuota(AngusTesterConcurrentTask, null, num + incr);
    }
  }

  @Override
  public boolean isAdminUser() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin();
  }

  @Override
  public LinkedHashMap<String, List<String>> getPipelineTargetMappings(Long execId) {
    Exec execDb = checkAndFind(execId);
    setParsedScriptContent(execDb);
    return execDb.getPipelineTargetMappings();
  }

  @Override
  public boolean hasPermission(Exec exec) {
    if (isAdminUser()) {
      return true;
    }
    return -1L == exec.getCreatedBy() || exec.getCreatedBy().equals(getUserId())
        || (nonNull(exec.getExecBy()) && exec.getExecBy().equals(getUserId()))
        || (nonNull(exec.getScriptCreatedBy()) && exec.getScriptCreatedBy().equals(getUserId()));
  }

  @Override
  public boolean hasPermissionInfo(ExecInfo exec) {
    if (isAdminUser()) {
      return true;
    }
    return -1L == exec.getCreatedBy() || exec.getCreatedBy().equals(getUserId())
        || (nonNull(exec.getExecBy()) && exec.getExecBy().equals(getUserId()))
        || (nonNull(exec.getScriptCreatedBy()) && exec.getScriptCreatedBy().equals(getUserId()));
  }

  @Override
  public void setExecScriptName(List<Exec> execs) {
    if (isNotEmpty(execs)) {
      Set<Long> scriptIds = execs.stream().map(Exec::getScriptId)
          .filter(Objects::nonNull).collect(Collectors.toSet());
      if (isNotEmpty(scriptIds)) {
        List<ScriptInfo> scripts = scriptQuery.infos(scriptIds);
        if (isNotEmpty(scripts)) {
          Map<Long, ScriptInfo> scriptMap = scripts.stream()
              .collect(Collectors.toMap(ScriptInfo::getId, x -> x));
          for (Exec exec : execs) {
            if (scriptMap.containsKey(exec.getScriptId())) {
              exec.setScriptName(scriptMap.get(exec.getScriptId()).getName());
              exec.setScriptSourceName(scriptMap.get(exec.getScriptId()).getSourceName());
            }
          }
        }
      }
    }
  }

  @Override
  public void setExecInfoScriptName(List<ExecInfo> execs) {
    if (isNotEmpty(execs)) {
      Set<Long> scriptIds = execs.stream().map(ExecInfo::getScriptId)
          .filter(Objects::nonNull).collect(Collectors.toSet());
      if (isNotEmpty(scriptIds)) {
        List<ScriptInfo> scripts = scriptQuery.infos(scriptIds);
        if (isNotEmpty(scripts)) {
          Map<Long, ScriptInfo> scriptMap = scripts.stream()
              .collect(Collectors.toMap(ScriptInfo::getId, x -> x));
          for (ExecInfo exec : execs) {
            if (scriptMap.containsKey(exec.getScriptId())) {
              exec.setScriptName(scriptMap.get(exec.getScriptId()).getName());
              exec.setScriptSourceName(scriptMap.get(exec.getScriptId()).getSourceName());
            }
          }
        }
      }
    }
  }

  @Override
  public void setExecNodeInfo(Exec exec) {
    Set<Long> nodeIds = getAllNodeIds(exec);
    if (isNotEmpty(nodeIds)) {
      Map<Long, Node> nodeMap = nodeInfoQuery.getNodeMap(nodeIds, exec.isTrial());
      if (isNotEmpty(nodeMap)) {
        if (isNotEmpty(exec.getAvailableNodeIds())) {
          Set<Long> availableNodesIds = exec.getAvailableNodeIds();
          exec.setAvailableNodes(nodeMap.entrySet().stream()
              .filter(x -> availableNodesIds.contains(x.getKey()))
              .map(x -> CoreUtils.copyProperties(x.getValue(), new NodeInfo()))
              .collect(Collectors.toList()));
        }
        Set<Long> execNodesIds = isNotEmpty(exec.getExecNodeIds()) ? exec.getExecNodeIds()
            : isEmpty(exec.getLastSchedulingResult())
                // Fix:: When scheduling fails, the execution node is empty.
                ? Collections.emptySet() : exec.getLastSchedulingResult().stream()
                .map(RunnerRunVo::getDeviceId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (isNotEmpty(execNodesIds)) {
          exec.setExecNodes(nodeMap.entrySet().stream()
              .filter(x -> execNodesIds.contains(x.getKey()))
              .map(x -> CoreUtils.copyProperties(x.getValue(), new NodeInfo()))
              .collect(Collectors.toList()));
        }
        if (isNotEmpty(exec.getAppNodeIds())) {
          Set<Long> appNodesIds = exec.getAppNodeIds();
          exec.setAppNodes(nodeMap.entrySet().stream()
              .filter(x -> appNodesIds.contains(x.getKey()))
              .map(x -> CoreUtils.copyProperties(x.getValue(), new NodeInfo()))
              .collect(Collectors.toList()));
        }
      }
    }
  }

  @Override
  public void setSampleContent(Exec exec, Long id) {
    if (exec.getScriptType().isFunctionalTesting()) {
      List<ExecSampleContent> sampleContents = execSampleExtcQuery.findIterationSampleContent(id);
      exec.setSampleContents(sampleContents);
    }
  }

  @Override
  public void setExecCurrentOperationPermission(List<Exec> execs) {
    boolean isAdmin = isAdminUser();
    for (Exec exec : execs) {
      exec.setHasOperationPermission(isAdmin || hasPermission(exec));
    }
  }

  @Override
  public void setExecInfoCurrentOperationPermission(List<ExecInfo> execs) {
    boolean isAdmin = isAdminUser();
    for (ExecInfo exec : execs) {
      exec.setHasOperationPermission(isAdmin || hasPermissionInfo(exec));
    }
  }

  @Override
  public void setSampleSummary(List<ExecInfo> execs, Boolean joinSampleSummary) {
    if (isEmpty(execs)) {
      return;
    }

    // For sharding invoke by innerapi
    if (isJobOrDoorApi()) {
      PrincipalContext.get().setOptTenantId(execs.get(0).getTenantId());
    }

    // setExecInfoScriptName(execs);
    // setExecInfoCurrentOperationPermission(execs);

    if (isNull(joinSampleSummary) || joinSampleSummary) {
      execSampleQuery.setExecInfoLatestTotalMergeSample(execs);
    }
  }

  @Override
  public void setParsedScriptContent(Exec exec) {
    try {
      AngusScript angusScript = AngusParser.YAML_MAPPER.readValue(exec.getScript(),
          AngusScript.class);
      exec.setConfiguration(angusScript.getConfiguration());
      exec.setTask(angusScript.getTask());
      if (isNotEmpty(angusScript.getTask().getPipelines())) {
        PipelineBuilder builder = PipelineBuilder.of(angusScript.getTask().getPipelines());
        // Fix: Single task pipeline name can be empty
        if (isNull(builder) || isEmpty(builder.getEnabledTargetNameMapping())) {
          LinkedHashMap<String, List<String>> pipelines = new LinkedHashMap<>();
          pipelines.put(SAMPLE_TOTAL_NAME, new ArrayList<>());
          exec.setPipelineTargetMappings(pipelines);
          return;
        } else if (builder.getEnabledTargetNameMapping().size() == 1 && isEmpty(
            builder.getEnabledTargetNameMapping().values().stream().findFirst().orElse(null))) {
          builder.getEnabledTargetNameMapping().clear();
          builder.getEnabledTargetNameMapping().put(SAMPLE_TOTAL_NAME, new ArrayList<>());
        } else {
          builder.getEnabledTargetNameMapping().put(SAMPLE_TOTAL_NAME, new ArrayList<>());
        }
        exec.setPipelineTargetMappings(builder.getEnabledTargetNameMapping());
      }
    } catch (Exception e) {
      throw SysException.of("Script content format error: " + e.getMessage());
    }
  }

  private Set<Long> getAllNodeIds(Exec exec) {
    Set<Long> nodeIds = new HashSet<>();
    if (isNotEmpty(exec.getAvailableNodeIds())) {
      nodeIds.addAll(exec.getAvailableNodeIds());
    }
    if (isNotEmpty(exec.getExecNodeIds())) {
      nodeIds.addAll(exec.getExecNodeIds());
    }
    if (isNotEmpty(exec.getAppNodeIds())) {
      nodeIds.addAll(exec.getAppNodeIds());
    }
    if (isNotEmpty(exec.getLastSchedulingResult())) {
      nodeIds.addAll(exec.getLastSchedulingResult().stream()
          .map(RunnerRunVo::getDeviceId).filter(Objects::nonNull).toList());
    }
    return nodeIds;
  }

  private Set<Long> getAllNodeIds(Configuration configuration) {
    Set<Long> nodeIds = new HashSet<>();
    if (nonNull(configuration.getNodeSelectors())) {
      if (isNotEmpty(configuration.getNodeSelectors().getAvailableNodeIds())) {
        nodeIds.addAll(configuration.getNodeSelectors().getAvailableNodeIds());
      }
      if (isNotEmpty(configuration.getNodeSelectors().getAppNodeIds())) {
        nodeIds.addAll(configuration.getNodeSelectors().getAppNodeIds());
      }
    }
    return nodeIds;
  }
}
