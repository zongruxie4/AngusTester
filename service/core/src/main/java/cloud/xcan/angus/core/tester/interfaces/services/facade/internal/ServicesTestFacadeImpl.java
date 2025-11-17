package cloud.xcan.angus.core.tester.interfaces.services.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisTestAssembler.generateToScript;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesTestCmd;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesTestFacade;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.dto.OrgAndDateFilterDto;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ServicesTestFacadeImpl implements ServicesTestFacade {

  @Resource
  private ServicesTestCmd servicesTestCmd;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ExecResultFacade execResultFacade;

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
  public void scriptGenerate(Long serviceId, Set<ApisTestScriptGenerateDto> dto) {
    servicesTestCmd.scriptGenerate(serviceId, generateToScript(dto));
  }

  @Override
  public void scriptDelete(Long serviceId, Set<TestType> testTypes) {
    servicesTestCmd.scriptDelete(serviceId, testTypes);
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
    return execResultFacade.serviceApisResult(serviceId, new OrgAndDateFilterDto());
  }

  @Override
  public ExecApisResultInfo testProjectResult(Long projectId) {
    return execResultFacade.projectApisResult(projectId, new OrgAndDateFilterDto());
  }

}
