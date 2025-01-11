package cloud.xcan.sdf.core.angustester.interfaces.task;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultDetailVo;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskAssocVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "TaskTest")
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class TaskTestRest {

  @Resource
  private TaskTestFacade taskTestFacade;

  @ApiOperation(value = "Query the associated target list of task", nickname = "task:assoc:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/{taskType}/{targetId}/test/association")
  public ApiLocaleResult<Map<ScriptType, List<TaskAssocVo>>> assocList(
      @ApiParam(name = "taskType", value = "Task type", allowableValues = "API_TEST,SCENARIO_TEST", required = true) @PathVariable("taskType") TaskType taskType,
      @ApiParam(name = "targetId", value = "Associated apis or scenario id", required = true) @PathVariable("targetId") Long targetId) {
    return ApiLocaleResult.success(taskTestFacade.assocList(taskType, targetId));
  }

  @ApiOperation(value = "Query the test results of task", nickname = "apis:test:result:info")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{taskType}/{targetId}/{testType}/result")
  public ApiLocaleResult<ExecTestResultDetailVo> testResult(
      @ApiParam(name = "taskType", value = "Task type", allowableValues = "API_TEST,SCENARIO_TEST", required = true) @PathVariable("taskType") TaskType taskType,
      @ApiParam(name = "targetId", value = "Associated apis or scenario id", required = true) @PathVariable("targetId") Long targetId,
      @ApiParam(name = "testType", value = "Test type", required = true) @PathVariable("testType") TestType testType) {
    return ApiLocaleResult.success(taskTestFacade.testResult(taskType, targetId, testType));
  }
}
