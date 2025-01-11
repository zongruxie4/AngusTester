package cloud.xcan.sdf.core.angustester.application.query.script;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.model.script.ScriptSource;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScriptQuery {

  Script detail(Long id);

  Script info(Long id);

  List<ScriptInfo> infos(Set<Long> ids);

  Script angusDetail(Long id);

  ScriptCount countStatistics(Set<SearchCriteria> criteria);

  ScriptResourcesCreationCount creationStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  Page<ScriptInfo> find(GenericSpecification<ScriptInfo> spec, Pageable pageable);

  Page<ScriptInfo> infoList(GenericSpecification<ScriptInfo> spec, Pageable pageable);

  void safeScenarioQuery(Set<SearchCriteria> criterias);

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
