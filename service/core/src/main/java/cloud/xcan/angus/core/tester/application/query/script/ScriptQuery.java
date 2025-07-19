package cloud.xcan.angus.core.tester.application.query.script;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScriptQuery {

  Script detail(Long id);

  Script findById(Long id);

  List<ScriptInfo> infos(Set<Long> ids);

  Script angusDetail(Long id);

  ScriptCount countStatistics(Set<SearchCriteria> criteria);

  ScriptResourcesCreationCount creationStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  Page<ScriptInfo> list(GenericSpecification<ScriptInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  Page<ScriptInfo> infoList(GenericSpecification<ScriptInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  void safeScenarioQuery(Set<SearchCriteria> criteria);

  List<ScriptInfo> findInfoBySource(ScriptSource source, Long sourceTargetId);

  List<Script> findBySource(ScriptSource source, Long sourceTargetId);

  Set<Long> findIdsBySource(ScriptSource source, Collection<Long> sourceTargetIds);

  Set<Long> findIdsBySourceAndTypeIn(ScriptSource source, Collection<Long> sourceTargetIds,
      Collection<ScriptType> testTypes);

  Script findScriptByScenarioId(Long scenarioId);

  ScriptInfo findScriptInfoByScenarioId(Long scenarioId);

  Script findBySourceAndScriptType(ScriptSource source, Long sourceTargetId,
      ScriptType scriptType);

  Script checkAndFind(Long id);

  ScriptInfo checkAndFindInfo(Long id);

  List<ScriptInfo> checkAndFindInfos(Collection<Long> ids);

  Map<Long, ScriptInfo> getScriptInfoMap(Set<Long> scriptIds);

  AngusScript checkAndParse(String content, boolean validation);

  String checkAndSerialize(AngusScript script, boolean validation);

  void checkAngusScript(AngusScript script);

  void checkQuota(int incr);

  void checkScriptLength(String script);

  void checkSourceResourceExist(Script script);

  void checkSourceAddScriptExist(Script script);

  void checkSourceUpdateScriptExist(Script script);

  boolean isAuthCtrl(Long scriptId);

  void setScriptTag(Script script);

  void setScriptInfoTag(List<ScriptInfo> scriptInfos);

  void setScriptSourceName(Script script);

  void setScriptSourceName(List<ScriptInfo> scriptInfos);

}
