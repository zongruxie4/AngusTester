package cloud.xcan.sdf.core.angustester.application.query.scenario;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioQuery {

  Scenario detail(Long id);

  void check(Long id);

  List<Scenario> list(Set<Long> ids);

  Page<Scenario> find(GenericSpecification<Scenario> spec, PageRequest pageable,
      Class<Scenario> clz);

  ScenarioResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd);

  Scenario checkAndFind(Long id);

  Scenario checkAndFind0(Long id);

  Script checkAndFindScenarioScript(Long id);

  ScriptInfo checkAndFindScenarioScriptInfo(Long id);

  void checkQuota(int i);

  Scenario find0(Long id);

  List<Scenario> find0ByIdIn(Collection<Long> scenarioIds);

  Scenario findLeastByProjectIdAndPluginAndTypeIn(Long projectId, String plugin,
      List<String> scriptTypes);

  void checkNameExists(long projectId, String name);

  void checkUpdateNameExists(long projectId, String name, long scenarioId);

  Boolean isAuthCtrl(Long id);

  void setFavourite(List<Scenario> scenarios);

  void setFollow(List<Scenario> scenarios);

  void setExecInfo(List<Scenario> scenarios);

  void setSafeCloneName(Scenario scenario);

  void assembleAndSendModifyNoticeEvent(Scenario scenarioDb, Activity activity);

}


