package cloud.xcan.sdf.core.angustester.interfaces.services.facade;

import cloud.xcan.sdf.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.test.ServicesTestTaskGenerateDto;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.services.ApisTestCount;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public interface ServicesTestFacade {

  void testEnabled(Long serviceId, Set<TestType> testTypes, Boolean enabled);

  ApisTestCount countServiceTestApis(Long serviceId, OrgAndDateFilterDto dto);

  ApisTestCount countProjectTestApis(Long projectId, OrgAndDateFilterDto dto);

  void scriptGenerate(Long serviceId, Set<ApisTestScriptGenerateDto> dtos);

  void scriptDelete(Long serviceId, Set<TestType> testTypes);

  void testTaskGenerate(Long serviceId, @Nullable Long taskSprintId, Set<ServicesTestTaskGenerateDto> dto);

  void testTaskRestart(Long serviceId);

  void testTaskReopen(Long serviceId);

  void testTaskDelete(Long serviceId, Set<TestType> testTypes);

  void testExecAdd(Long servicesId, Set<TestType> testTypes, List<Server> servers);

  void testSmokeExecAdd(Long servicesId, List<Server> servers);

  void testSecurityExecAdd(Long servicesId, List<Server> servers);

  ExecApisResultInfo testServiceResult(Long serviceId);

  ExecApisResultInfo testProjectResult(Long projectId);
}
