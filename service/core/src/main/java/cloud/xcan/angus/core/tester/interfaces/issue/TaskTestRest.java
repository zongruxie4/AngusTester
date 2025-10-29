package cloud.xcan.angus.core.tester.interfaces.issue;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.TaskTestFacade;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.TaskAssocVo;
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

@Tag(name = "Task Test", description = "Task Test Associations and Results API - Query system for task-related test resources, execution results, and performance analytics")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskTestRest {

  @Resource
  private TaskTestFacade taskTestFacade;

  @Operation(summary = "Get task-associated test resources", operationId = "task:assoc:list", description = "Retrieve all test resources associated with a specific task, organized by script type for test coverage analysis")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task-associated test resources retrieved successfully")})
  @GetMapping("/{taskType}/{targetId}/test/association")
  public ApiLocaleResult<Map<ScriptType, List<TaskAssocVo>>> assocList(
      @Parameter(name = "taskType", description = "Task type for resource association", required = true) @PathVariable("taskType") TaskType taskType,
      @Parameter(name = "targetId", description = "Associated API or scenario identifier", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(taskTestFacade.assocList(taskType, targetId));
  }

  @Operation(summary = "Get task test execution results", operationId = "apis:test:result:info", description = "Retrieve detailed test execution results for a specific task, including performance metrics, success rates, and execution summaries")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task test results retrieved successfully")})
  @GetMapping(value = "/{taskType}/{targetId}/{testType}/result")
  public ApiLocaleResult<ExecTestResultDetailSummary> testResult(
      @Parameter(name = "taskType", description = "Task type for result retrieval", required = true) @PathVariable("taskType") TaskType taskType,
      @Parameter(name = "targetId", description = "Associated API or scenario identifier", required = true) @PathVariable("targetId") Long targetId,
      @Parameter(name = "testType", description = "Test type for result filtering", required = true) @PathVariable("testType") TestType testType) {
    return ApiLocaleResult.success(taskTestFacade.testResult(taskType, targetId, testType));
  }
}
