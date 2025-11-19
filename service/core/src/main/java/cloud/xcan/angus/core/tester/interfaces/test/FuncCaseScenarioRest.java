package cloud.xcan.angus.core.tester.interfaces.test;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioListVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncCaseScenarioFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Functional Test Case Scenario Association",
    description = "Functional Test Case Scenario Association Management - APIs for associating and managing scenarios with functional test cases")
@Validated
@RestController
@RequestMapping("/api/v1/func/case/scenario")
public class FuncCaseScenarioRest {

  @Resource
  private FuncCaseScenarioFacade funcCaseScenarioFacade;

  @Operation(summary = "Associate scenarios with test case",
      description = "Link multiple scenarios to a specific test case for comprehensive scenario management. Maximum 20 scenarios per case.",
      operationId = "func:case:scenario:assoc:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenarios associated with test case successfully"),
      @ApiResponse(responseCode = "404", description = "Test case or scenario not found"),
      @ApiResponse(responseCode = "400", description = "Maximum scenario association limit exceeded (20 scenarios per case)")
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/association")
  public ApiLocaleResult<?> scenarioAssocAdd(
      @Parameter(name = "id", description = "Test case identifier for scenario association", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("scenarioIds") HashSet<Long> scenarioIds) {
    funcCaseScenarioFacade.scenarioAssocAdd(id, scenarioIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Remove scenario associations from test case",
      description = "Unlink multiple scenarios from a specific test case to remove scenario associations",
      operationId = "func:case:scenario:assoc:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario associations removed successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/association")
  public ApiLocaleResult<?> scenarioAssocCancel(
      @Parameter(name = "id", description = "Test case identifier for scenario association removal", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("scenarioIds") HashSet<Long> scenarioIds) {
    funcCaseScenarioFacade.scenarioAssocCancel(id, scenarioIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Get scenarios associated with test case",
      description = "Retrieve list of scenarios associated with a specific test case for association management",
      operationId = "func:case:scenario:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Associated scenarios retrieved successfully")
  })
  @GetMapping("/{id}")
  public ApiLocaleResult<List<ScenarioListVo>> listScenarios(
      @Parameter(name = "id", description = "Test case identifier for scenario list query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseScenarioFacade.listScenarios(id));
  }
}
