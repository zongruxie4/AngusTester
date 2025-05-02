package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisDeleteDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisSyncDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStartDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStopDto;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import java.util.List;

public interface MockServiceManageFacade {

  List<StartVo> start(MockServiceStartDto dto);

  List<StopVo> stop(MockServiceStopDto dto);

  List<StatusVo> status(MockServiceStatusDto dto);

  SimpleCommandResult syncApis(MockServiceApisSyncDto dto);

  SimpleCommandResult deleteApis(MockServiceApisDeleteDto dto);
}
