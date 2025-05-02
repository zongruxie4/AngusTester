package cloud.xcan.angus.core.tester.interfaces.task;

import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetail;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskTestFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TaskTest", description = "Task Associations Resource Query - Query resources and test results associated with specific task.")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskTestRest {

  @Resource
  private TaskTestFacade taskTestFacade;

  @Operation(description = "Query the associated target list of task", operationId = "task:assoc:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/{taskType}/{targetId}/test/association")
  public ApiLocaleResult<Map<ScriptType, List<TaskAssocVo>>> assocList(
      @Parameter(name = "taskType", description = "Task type, allowable values: API_TEST, SCENARIO_TEST", required = true) @PathVariable("taskType") TaskType taskType,
      @Parameter(name = "targetId", description = "Associated apis or scenario id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(taskTestFacade.assocList(taskType, targetId));
  }

  @Operation(description = "Query the test results of task", operationId = "apis:test:result:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{taskType}/{targetId}/{testType}/result")
  public ApiLocaleResult<ExecTestResultDetail> testResult(
      @Parameter(name = "taskType", description = "Task type, allowable values: API_TEST, SCENARIO_TEST", required = true) @PathVariable("taskType") TaskType taskType,
      @Parameter(name = "targetId", description = "Associated apis or scenario id", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "testType", description = "Test type", required = true) @PathVariable("testType") TestType testType) {
    return ApiLocaleResult.success(taskTestFacade.testResult(taskType, targetId, testType));
  }
}
