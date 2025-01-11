package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisTestAssembler.generateToScript;
import static cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler.ServicesTestAssembler.toTestTaskTestings;

import cloud.xcan.sdf.api.angusctrl.exec.ExecResultRemote;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.sdf.model.services.ApisTestCount;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesTestCmd;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.test.ServicesTestTaskGenerateDto;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ServicesTestFacadeImpl implements ServicesTestFacade {

  @Resource
  private ServicesTestCmd servicesTestCmd;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ExecResultRemote execResultRemote;

  @Override
  public void testEnabled(Long serviceId, Set<TestType> testTypes, Boolean enabled) {
    servicesTestCmd.testEnabled(serviceId, testTypes, enabled);
  }

  @Override
  public ApisTestCount countServiceTestApis(Long serviceId, OrgAndDateFilterDto dto) {
    return servicesQuery.countServiceTestApis(serviceId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public ApisTestCount countProjectTestApis(Long projectId, OrgAndDateFilterDto dto) {
    return servicesQuery.countProjectTestApis(projectId, dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getCreatedDateStart(), dto.getCreatedDateEnd());
  }

  @Override
  public void scriptGenerate(Long serviceId, Set<ApisTestScriptGenerateDto> dtos) {
    servicesTestCmd.scriptGenerate(serviceId, generateToScript(dtos));
  }

  @Override
  public void scriptDelete(Long serviceId, Set<TestType> testTypes) {
    servicesTestCmd.scriptDelete(serviceId, testTypes);
  }

  @Override
  public void testTaskGenerate(Long serviceId, @Nullable Long taskSprintId,
      Set<ServicesTestTaskGenerateDto> dtos) {
    servicesTestCmd.testTaskGenerate(serviceId, taskSprintId, toTestTaskTestings(dtos));
  }

  @Override
  public void testTaskRestart(Long serviceId) {
    servicesTestCmd.retestTaskRestart(serviceId, true);
  }

  @Override
  public void testTaskReopen(Long serviceId) {
    servicesTestCmd.retestTaskRestart(serviceId, false);
  }

  @Override
  public void testTaskDelete(Long serviceId, Set<TestType> testTypes) {
    servicesTestCmd.testTaskDelete(serviceId, testTypes);
  }

  @Override
  public void testExecAdd(Long servicesId, Set<TestType> testTypes, List<Server> servers) {
    servicesTestCmd.testExecAdd(servicesId, testTypes, servers);
  }

  @Override
  public void testSmokeExecAdd(Long servicesId, List<Server> servers) {
    servicesTestCmd.testSmokeExecAdd(servicesId, servers);
  }

  @Override
  public void testSecurityExecAdd(Long servicesId, List<Server> servers) {
    servicesTestCmd.testSecurityExecAdd(servicesId, servers);
  }

  @Override
  public ExecApisResultInfo testServiceResult(Long serviceId) {
    return execResultRemote.serviceApisResult(serviceId, null).orElseContentThrow();
  }

  @Override
  public ExecApisResultInfo testProjectResult(Long projectId) {
    return execResultRemote.projectApisResult(projectId, null).orElseContentThrow();
  }

}
