package cloud.xcan.angus.core.tester.interfaces.func;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseAttachmentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseImportDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseResultModifyDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseTagReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseTesterReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseWorkloadReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseReviewVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionRefReplaceDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.hibernate.validator.constraints.Length;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Functional Test Case", description = "Functional Test Case Management - Comprehensive APIs for creating, updating, organizing, and managing functional test cases with advanced batch operations, association management, and lifecycle control")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseRest {

  @Resource
  private FuncCaseFacade funcCaseFacade;

  @Operation(summary = "Create functional test cases", 
      description = "Create one or more new functional test cases with detailed configuration, steps, and expected results",
      operationId = "func:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Functional test cases created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseAddDto> dto) {
    return ApiLocaleResult.success(funcCaseFacade.add(dto));
  }

  @Operation(summary = "Update functional test cases", 
      description = "Update one or more functional test cases with partial modification support and batch operation",
      operationId = "func:case:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test cases updated successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test case not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseUpdateDto> dto) {
    funcCaseFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace functional test cases", 
      description = "Replace one or more functional test cases with complete new configuration and batch operation support",
      operationId = "func:case:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test cases replaced successfully")})
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseReplaceDto> dto) {
    return ApiLocaleResult.success(funcCaseFacade.replace(dto));
  }

  @Operation(summary = "Rename functional test case", 
      description = "Update the name of a specific functional test case for better identification and organization",
      operationId = "func:case:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test case renamed successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Test case identifier for rename operation", required = true) @PathVariable("id") Long id,
      @Valid @Length(max = MAX_NAME_LENGTH_X4) @Parameter(name = "name", description = "New test case name for identification", required = true) @RequestParam("name") String name) {
    funcCaseFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone functional test cases", 
      description = "Create copies of existing functional test cases with all configurations and associations",
      operationId = "func:case:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test cases cloned successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test case not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(funcCaseFacade.clone(ids));
  }

  @Operation(summary = "Move test cases to another plan", 
      description = "Transfer multiple test cases from current plan to target plan with batch operation support",
      operationId = "func:case:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test cases moved successfully"),
      @ApiResponse(responseCode = "404", description = "Test case or target plan not found")})
  @PatchMapping(value = "/move")
  public ApiLocaleResult<?> move(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids,
      @Parameter(name = "targetPlanId", description = "Target plan identifier for case transfer", required = true) @RequestParam("targetPlanId") Long targetPlanId) {
    funcCaseFacade.move(ids, targetPlanId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case tester", 
      description = "Update the assigned tester for a specific test case with comprehensive tester information",
      operationId = "func:case:tester:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case tester replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tester")
  public ApiLocaleResult<?> replaceTester(
      @Parameter(name = "id", description = "Test case identifier for tester replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseTesterReplaceDto dto) {
    funcCaseFacade.replaceTester(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case tags", 
      description = "Update the tags associated with a specific test case for better categorization and filtering",
      operationId = "func:case:tags:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case tags replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tag")
  public ApiLocaleResult<?> replaceTag(
      @Parameter(name = "id", description = "Test case identifier for tag replacement", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseTagReplaceDto dto) {
    funcCaseFacade.replaceTag(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case deadline", 
      description = "Update the completion deadline for a specific test case with date-time precision",
      operationId = "func:case:deadline:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case deadline replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/deadline/{deadline}")
  public ApiLocaleResult<?> replaceDeadline(
      @Parameter(name = "id", description = "Test case identifier for deadline update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "deadline", description = "New deadline date-time for test case completion", required = true) @PathVariable("deadline") @DateTimeFormat(pattern = DATE_FMT) LocalDateTime deadlineDate) {
    funcCaseFacade.replaceDeadline(id, deadlineDate);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case priority", 
      description = "Update the priority level of a specific test case for resource allocation and scheduling",
      operationId = "func:case:priority:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case priority replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/priority/{priority}")
  public ApiLocaleResult<?> replacePriority(
      @Parameter(name = "id", description = "Test case identifier for priority update", required = true) @PathVariable("id") Long id,
      @Parameter(name = "priority", description = "New priority level for test case execution", required = true) @PathVariable("priority") Priority priority) {
    funcCaseFacade.replacePriority(id, priority);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case software version", 
      description = "Update the software version reference for a specific test case for version-specific testing",
      operationId = "func:case:softwareVersion:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case software version replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/software/version")
  public ApiLocaleResult<?> replaceSoftwareVersion(
      @Parameter(name = "id", description = "Test case identifier for software version update", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody SoftwareVersionRefReplaceDto version) {
    funcCaseFacade.replaceSoftwareVersion(id, version);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case evaluation workload", 
      description = "Update the estimated workload for a specific test case for resource planning",
      operationId = "func:case:evalWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case evaluation workload replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/evalWorkload")
  public ApiLocaleResult<?> replaceEvalWorkload(
      @Parameter(name = "id", description = "Test case identifier for evaluation workload update", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseWorkloadReplaceDto dto) {
    funcCaseFacade.replaceEvalWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case actual workload", 
      description = "Update the actual workload for a specific test case for performance tracking",
      operationId = "func:case:actualWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case actual workload replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/actualWorkload")
  public ApiLocaleResult<?> replaceActualWorkload(
      @Parameter(name = "id", description = "Test case identifier for actual workload update", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseWorkloadReplaceDto dto) {
    funcCaseFacade.replaceActualWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case attachments", 
      description = "Update the attachments associated with a specific test case for comprehensive documentation",
      operationId = "func:case:attachment:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case attachments replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/attachment")
  public ApiLocaleResult<?> replaceAttachment(
      @Parameter(name = "id", description = "Test case identifier for attachment update", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseAttachmentReplaceDto dto) {
    funcCaseFacade.replaceAttachment(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace test case results", 
      description = "Update test results for multiple test cases with batch operation support",
      operationId = "func:case:result:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case results replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PutMapping(value = "/result")
  public ApiLocaleResult<?> resultReplace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseResultModifyDto> dto) {
    funcCaseFacade.resultReplace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update test case results", 
      description = "Update test results for multiple test cases with partial modification support",
      operationId = "func:case:result:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case results updated successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/result")
  public ApiLocaleResult<?> resultUpdate(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseResultModifyDto> dto) {
    funcCaseFacade.resultUpdate(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reset test case results", 
      description = "Reset test results for multiple test cases to initial state for re-execution",
      operationId = "func:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case results reset successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/result/reset")
  public ApiLocaleResult<?> resultReset(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.resultReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Retest functional test cases", 
      description = "Set multiple test cases to pending status and skip review when review exists for re-execution",
      operationId = "func:case:retest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test cases set to retest successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PatchMapping(value = "/result/retest")
  public ApiLocaleResult<?> retest(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.retest(ids);
    return ApiLocaleResult.success();
  }

  //  @Operation(summary = "Review functional test cases", operationId = "func:case:review")
  //  @ApiResponses(value = {
  //      @ApiResponse(responseCode = "200", description = "Updated successfully"),
  //      @ApiResponse(responseCode = "404", description = "Resource not found")
  //  })
  //  @PutMapping("/review")
  //  public ApiLocaleResult<?> review(
  //      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseReviewDto> dto) {
  //    funcCaseFacade.review(dto);
  //    return ApiLocaleResult.success();
  //  }

  //  @Operation(summary = "Reset the result of case review", operationId = "func:case:review:reset")
  //  @ApiResponses(value = {
  //      @ApiResponse(responseCode = "200", description = "Updated successfully"),
  //      @ApiResponse(responseCode = "404", description = "Resource not found")
  //  })
  //  @PatchMapping(value = "/review/reset")
  //  public ApiLocaleResult<?> reviewReset(
  //      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
  //    funcCaseFacade.reviewReset(ids);
  //    return ApiLocaleResult.success();
  //  }

  @Operation(summary = "Associate tasks with test case", 
      description = "Link multiple tasks to a specific test case for comprehensive task management",
      operationId = "func:case:association:task:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tasks associated with test case successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PutMapping("/{id}/association/task")
  public ApiLocaleResult<?> taskAssocAdd(
      @Parameter(name = "id", description = "Test case identifier for task association", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    funcCaseFacade.taskAssocAdd(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Remove task associations from test case", 
      description = "Unlink multiple tasks from a specific test case to remove task associations",
      operationId = "func:case:association:task:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task associations removed successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/task/cancel")
  public ApiLocaleResult<?> taskAssocCancel(
      @Parameter(name = "id", description = "Test case identifier for task association removal", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    funcCaseFacade.taskAssocCancel(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Associate test cases with test case", 
      description = "Link multiple test cases to a specific test case for hierarchical test case management",
      operationId = "func:case:association:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test cases associated successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @PutMapping("/{id}/association/case")
  public ApiLocaleResult<?> caseAssocAdd(
      @Parameter(name = "id", description = "Test case identifier for case association", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    funcCaseFacade.caseAssocAdd(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Remove test case associations", 
      description = "Unlink multiple test cases from a specific test case to remove case associations",
      operationId = "func:case:association:case:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case associations removed successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/case/cancel")
  public ApiLocaleResult<?> caseAssocCancel(
      @Parameter(name = "id", description = "Test case identifier for case association removal", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    funcCaseFacade.caseAssocCancel(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import functional test cases", 
      description = "Import functional test cases from external files with comprehensive import configuration",
      operationId = "func:case:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test cases imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid FuncCaseImportDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.imports(dto));
  }

  @Operation(summary = "Import functional test case examples", 
      description = "Import example functional test cases for project initialization and template creation",
      operationId = "func:case:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Example functional test cases imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for example import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(funcCaseFacade.importExample(projectId));
  }

  @Operation(summary = "Delete functional test cases", 
      description = "Delete multiple functional test cases with batch operation support",
      operationId = "func:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Functional test cases deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.delete(ids);
  }

  @Operation(summary = "Get unassociated tasks for test case", 
      description = "Retrieve list of tasks not associated with a specific test case for association management",
      operationId = "case:notAssociated:task:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unassociated tasks retrieved successfully")})
  @GetMapping(value = "/{id}/task/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedTask(
      @Parameter(name = "id", description = "Test case identifier for unassociated task query", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module identifier for task filtering", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "taskType", description = "Task type for specific filtering", required = false) @RequestParam(value = "taskType", required = false) TaskType taskType) {
    return ApiLocaleResult.success(funcCaseFacade.notAssociatedTask(id, moduleId, taskType));
  }

  @Operation(summary = "Get unassociated test cases", 
      description = "Retrieve list of test cases not associated with a specific test case for association management",
      operationId = "case:notAssociated:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Unassociated test cases retrieved successfully")})
  @GetMapping(value = "/{id}/case/notAssociated")
  public ApiLocaleResult<List<FuncCaseListVo>> notAssociatedCase(
      @Parameter(name = "id", description = "Test case identifier for unassociated case query", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module identifier for case filtering", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(funcCaseFacade.notAssociatedCase(id, moduleId));
  }

  @Operation(summary = "Get functional test case details", 
      description = "Retrieve comprehensive details of a specific functional test case for analysis and review",
      operationId = "func:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test case details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Functional test case not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncCaseDetailVo> detail(
      @Valid @Parameter(name = "id", description = "Test case identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseFacade.detail(id));
  }

  @Operation(summary = "Get test case review information", 
      description = "Retrieve review information list for a specific test case for review management",
      operationId = "func:case:review:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test case review information retrieved successfully")})
  @GetMapping(value = "/{id}/review")
  public ApiLocaleResult<List<FuncCaseReviewVo>> reviewList(
      @Valid @Parameter(name = "id", description = "Test case identifier for review information retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseFacade.reviewList(id));
  }

  @Operation(summary = "List functional test cases", 
      description = "Retrieve paginated list of functional test cases with comprehensive filtering and search options",
      operationId = "func:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test case list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncCaseListVo>> list(
      @Valid @ParameterObject FuncCaseFindDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.list(false, dto));
  }

  @Operation(summary = "Export functional test cases", 
      description = "Export functional test cases based on specified conditions in various formats for external analysis",
      operationId = "func:case:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Functional test cases exported successfully")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject FuncCaseFindDto dto, HttpServletResponse response) {
    return funcCaseFacade.export(dto, response);
  }
}
