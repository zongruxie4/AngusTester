package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceManageCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceMetricsQuery;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceManageFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisDeleteDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisSyncDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStartDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStopDto;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockServiceManageFacadeImpl implements MockServiceManageFacade {

  @Resource
  private MockServiceManageCmd mockServiceManageCmd;

  @Resource
  private MockServiceMetricsQuery mockServiceMetricsQuery;

  @Override
  public List<StartVo> start(MockServiceStartDto dto) {
    return mockServiceManageCmd.start(dto);
  }

  @Override
  public List<StopVo> stop(MockServiceStopDto dto) {
    return mockServiceManageCmd.stop(dto);
  }

  @Override
  public List<StatusVo> status(MockServiceStatusDto dto) {
    // return mockServiceManageCmd.status(dto);
    return mockServiceMetricsQuery.status(dto);
  }

  @Override
  public SimpleCommandResult syncApis(MockServiceApisSyncDto dto) {
    return mockServiceManageCmd.syncApis(dto);
  }

  @Override
  public SimpleCommandResult deleteApis(MockServiceApisDeleteDto dto) {
    return mockServiceManageCmd.deleteApis(dto);
  }

}
