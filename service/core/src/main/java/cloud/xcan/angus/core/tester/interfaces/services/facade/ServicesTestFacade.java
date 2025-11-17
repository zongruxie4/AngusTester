package cloud.xcan.angus.core.tester.interfaces.services.facade;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.test.ServicesTestTaskGenerateDto;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public interface ServicesTestFacade {

  void testEnabled(Long serviceId, Set<TestType> testTypes, Boolean enabled);

  ApisTestCount countServiceTestApis(Long serviceId, OrgAndDateFilterDto dto);

  ApisTestCount countProjectTestApis(Long projectId, OrgAndDateFilterDto dto);

  void scriptGenerate(Long serviceId, Set<ApisTestScriptGenerateDto> dto);

  void scriptDelete(Long serviceId, Set<TestType> testTypes);

  void testExecAdd(Long servicesId, Set<TestType> testTypes, List<Server> servers);

  void testSmokeExecAdd(Long servicesId, List<Server> servers);

  void testSecurityExecAdd(Long servicesId, List<Server> servers);

  ExecApisResultInfo testServiceResult(Long serviceId);

  ExecApisResultInfo testProjectResult(Long projectId);
}
