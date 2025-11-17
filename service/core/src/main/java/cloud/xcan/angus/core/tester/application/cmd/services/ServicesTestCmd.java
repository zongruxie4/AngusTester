package cloud.xcan.angus.core.tester.application.cmd.services;

import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.services.testing.TestTaskSetting;
import cloud.xcan.angus.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;

public interface ServicesTestCmd {

  void testEnabled(Long serviceId, Set<TestType> testTypes, Boolean enabled);

  void scriptGenerate(Long serviceId, List<Script> scripts);

  void scriptDelete(Long serviceId, Set<TestType> testTypes);

  void testExecAdd(Long servicesId, Set<TestType> testTypes, List<Server> servers);

  void testSmokeExecAdd(Long servicesId, List<Server> servers);

  void testSecurityExecAdd(Long servicesId, List<Server> servers);
}
