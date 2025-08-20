package cloud.xcan.angus.core.tester.interfaces.mock;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceManageFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisDeleteDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisSyncDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStartDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStopDto;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mock Service Manage", description = "Mock Service Status Management - Control interface for mock service state management with agent-based orchestration")
@Validated
@RequestMapping("/api/v1/mock/service")
@RestController
public class MockServiceManageRest {

  @Resource
  private MockServiceManageFacade mockServiceMangeFacade;

  @Operation(summary = "Start mock service via agent",
      description = "Initiate mock service startup through agent with status monitoring and health checks",
      operationId = "mock:service:manage:start")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service startup initiated successfully")
  })
  @PostMapping(value = "/manage/start")
  public ApiLocaleResult<List<StartVo>> start(@Valid @RequestBody MockServiceStartDto dto) {
    return ApiLocaleResult.success(mockServiceMangeFacade.start(dto));
  }

  @Operation(summary = "Stop mock service via agent",
      description = "Initiate mock service shutdown through agent with graceful termination and cleanup procedures",
      operationId = "mock:service:manage:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service shutdown initiated successfully")
  })
  @PostMapping(value = "/manage/stop")
  public ApiLocaleResult<List<StopVo>> stop(@Valid @RequestBody MockServiceStopDto dto) {
    return ApiLocaleResult.success(mockServiceMangeFacade.stop(dto));
  }

  @Operation(summary = "Query mock service status via agent",
      description = "Retrieve mock service status through agent with real-time health monitoring",
      operationId = "mock:service:manage:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service status retrieved successfully")
  })
  @PostMapping(value = "/manage/status")
  public ApiLocaleResult<List<StatusVo>> status(@Valid @RequestBody MockServiceStatusDto dto) {
    return ApiLocaleResult.success(mockServiceMangeFacade.status(dto));
  }

  @Operation(summary = "Synchronize mock service APIs via agent",
      description = "Deploy mock service API configurations through agent with synchronization and validation",
      operationId = "mock:service:apis:manage:sync")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service APIs synchronized successfully")
  })
  @PostMapping(value = "/apis/manage/sync")
  public ApiLocaleResult<SimpleCommandResult> syncApis(@Valid @RequestBody MockServiceApisSyncDto dto) {
    return ApiLocaleResult.success(mockServiceMangeFacade.syncApis(dto));
  }

  @Operation(summary = "Delete mock service APIs via agent",
      description = "Remove mock service API configurations through agent with cleanup and validation",
      operationId = "mock:service:apis:manage:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service APIs deleted successfully")
  })
  @PostMapping(value = "/apis/manage/delete")
  public ApiLocaleResult<SimpleCommandResult> deleteApis(@Valid @RequestBody MockServiceApisDeleteDto dto) {
    return ApiLocaleResult.success(mockServiceMangeFacade.deleteApis(dto));
  }
}
