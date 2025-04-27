package cloud.xcan.angus.core.tester.application.cmd.script;

import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.List;

public interface ScriptCmd {

  IdKey<Long, Object> add(Script script, boolean saveActivity);

  IdKey<Long, Object> add(Script script, AngusScript angusScript, boolean validateScript);

  IdKey<Long, Object> addByScenario(Script script, AngusScript angusScript);

  IdKey<Long, Object> addByAngus(Long projectId, AngusScript script, boolean validateScript);

  void update(Script script);

  void update(Script script, AngusScript angusScript, boolean validateScript);

  void update0(Script scriptDb, AngusScript angusScript, boolean replaceTag,
      boolean validateScript);

  void update0(Script scriptDb, AngusScript angusScript);

  IdKey<Long, Object> replace(Script script);

  void angusReplace(Long id, AngusScript script, boolean validateScript);

  IdKey<Long, Object> clone(Long id);

  IdKey<Long, Object> cloneByScenario(Long id, Long newId);

  IdKey<Long, Object> imports(Script script);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void deleteBySource(ScriptSource source, Collection<Long> sourceTargetIds);

  void delete(Collection<Long> ids);

  void deleteBySource(ScriptSource source, Collection<Long> sourceTargetIds,
      Collection<ScriptType> testTypes);

  long syncApisCaseToScript(Apis apisDb, List<ApisCase> cases);

  long syncServiceCaseToScript(Services serviceDb, ScriptSource source, List<ApisCase> cases,
      List<Server> servers);

  void renameCaseToScript(Long apisId, Long caseId, /*String oldName, */String newName);

  void deleteCaseInScript(Long apisId, Collection<Long> caseIds);

  void enableCaseToScript(Long apisId, Collection<Long> caseIds, boolean enabled);
}




