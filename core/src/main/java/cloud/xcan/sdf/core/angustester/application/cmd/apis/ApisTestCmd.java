package cloud.xcan.sdf.core.angustester.application.cmd.apis;


import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public interface ApisTestCmd {

  void testEnabled(Long apisId, Set<TestType> testTypes, boolean enabled);

  void scriptGenerate(Long apisId, List<Script> scripts);

  void scriptGenerate0(Apis apisDb, Map<String, Server> serverMap, List<Script> scripts);

  void scriptDelete(Long apisId, Set<TestType> testTypes);

  void scriptDelete0(Long apisId, Set<TestType> testTypes);

  void testTaskGenerate(Long apisId, Long taskSprintId, List<Task> tasks,
      boolean ignoreApisPermission);

  void testTaskRetest(Long apisId, Boolean restartFlag);

  void testTaskDelete(List<Long> apisIds, Set<TestType> testTypes);

  void testExecAdd(Long apisId, Set<TestType> testTypes, @Nullable List<Server> servers);

  void testExecAdd(HashSet<Long> apisIds, HashSet<TestType> testTypes,
      @Nullable List<Server> servers);

  void testExecAdd0(Apis apisDb, Set<TestType> testTypes, List<Server> servers);

  void testCaseExecAdd(Long apisId, LinkedHashSet<Long> caseIds);

}
