package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.entity.projection.IdAndName;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ExecQuery {

  Exec detail(Long id);

  List<ExecInfo> listInfo(Set<Long> ids, Boolean joinSampleSummary);

  List<ExecInfo> listInfoBySource(ScriptSource resourceType, Set<Long> resourceIds,
      Boolean joinSampleSummary);

  String script(Long id);

  List<Server> findServers(Long id);

  Page<ExecInfo> list(GenericSpecification<ExecInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  List<ExecInfo> findByNodeId(Long id);

  Exec findByScript(Long scriptId, ScriptType scriptType, ScriptSource scriptSource);

  ExecInfo findInfo(Long id);

  List<ExecInfo> findInfoBySource(ScriptSource resourceType, Set<Long> resourceIds);

  List<ExecInfo> findInfo(Collection<Long> execIds);

  Map<Long, IdAndName> findInfoMap(Collection<Long> execIds);

  Map<String, List<NoticeType>> findTenantEventNoticeTypes(Long tenantId);

  Exec checkAndFind(Long id);

  List<Exec> checkAndFind(Collection<Long> ids);

  ExecInfo checkAndFindInfo(Long id);

  List<ExecInfo> checkAndFindInfo(Collection<Long> ids);

  void checkNotRunning(Exec exec);

  void checkNotStopped(ExecInfo exec);

  void checkNodeValid(Configuration configuration, boolean trial);

  void checkPermission(Exec exec);

  void checkPermissionInfo(ExecInfo exec);

  void checkAddQuota(long incr, boolean trial);

  void checkThreadAndNodesQuota(Boolean trial, Configuration configuration);

  void checkConcurrentTaskQuota(long incr, boolean trial);

  void checkConcurrentTaskQuota0(long incr, boolean trial);

  boolean isAdminUser();

  LinkedHashMap<String, List<String>> getPipelineTargetMappings(Long execId);

  boolean hasPermission(Exec exec);

  boolean hasPermissionInfo(ExecInfo exec);

  void setExecScriptName(List<Exec> execs);

  void setExecInfoScriptName(List<ExecInfo> execs);

  void setExecNodeInfo(Exec exec);

  void setSampleContent(Exec exec, Long id);

  void setExecCurrentOperationPermission(List<Exec> execs);

  void setExecInfoCurrentOperationPermission(List<ExecInfo> execs);

  void setSampleSummary(List<ExecInfo> execs, Boolean joinSampleSummary);

  void setParsedScriptContent(Exec exec);

}
