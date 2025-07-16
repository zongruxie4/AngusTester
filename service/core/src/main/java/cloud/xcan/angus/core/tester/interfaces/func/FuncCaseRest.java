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
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseSearchDto;
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

@Tag(name = "FuncCase", description = "Test Case Information Management - Centralized control for maintaining test case details (steps, expected results, preconditions)")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseRest {

  @Resource
  private FuncCaseFacade funcCaseFacade;

  @Operation(summary = "Add functional test cases", operationId = "func:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseAddDto> dto) {
    return ApiLocaleResult.success(funcCaseFacade.add(dto));
  }

  @Operation(summary = "Update functional test cases", operationId = "func:case:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseUpdateDto> dto) {
    funcCaseFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace functional test cases", operationId = "func:case:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseReplaceDto> dto) {
    return ApiLocaleResult.success(funcCaseFacade.replace(dto));
  }

  @Operation(summary = "Replace the name of functional test cases", operationId = "func:case:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @Length(max = MAX_NAME_LENGTH_X4) @Parameter(name = "name", description = "New case name", required = true) @RequestParam("name") String name) {
    funcCaseFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Clone the functional test cases", operationId = "func:case:clone")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Clone successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(funcCaseFacade.clone(ids));
  }

  @Operation(summary = "Move the case to another plan", operationId = "func:case:move")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Moved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @PatchMapping(value = "/move")
  public ApiLocaleResult<?> move(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids,
      @Parameter(name = "targetPlanId", description = "Target plan id", required = true) @RequestParam("targetPlanId") Long targetPlanId) {
    funcCaseFacade.move(ids, targetPlanId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the tester of case", operationId = "func:case:tester:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tester")
  public ApiLocaleResult<?> replaceTester(
      @Parameter(name = "id", description = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseTesterReplaceDto dto) {
    funcCaseFacade.replaceTester(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the tags of functional test cases", operationId = "func:case:tags:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tag")
  public ApiLocaleResult<?> replaceTag(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseTagReplaceDto dto) {
    funcCaseFacade.replaceTag(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the deadline of functional test cases", operationId = "func:case:deadline:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/deadline/{deadline}")
  public ApiLocaleResult<?> replaceDeadline(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "deadline", description = "Case deadline", required = true) @PathVariable("deadline") @DateTimeFormat(pattern = DATE_FMT) LocalDateTime deadlineDate) {
    funcCaseFacade.replaceDeadline(id, deadlineDate);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the priority of functional test cases", operationId = "func:case:priority:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/priority/{priority}")
  public ApiLocaleResult<?> replacePriority(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "priority", description = "Case priority", required = true) @PathVariable("priority") Priority priority) {
    funcCaseFacade.replacePriority(id, priority);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the software version of functional test case", operationId = "func:case:softwareVersion:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/software/version")
  public ApiLocaleResult<?> replaceSoftwareVersion(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody SoftwareVersionRefReplaceDto version) {
    funcCaseFacade.replaceSoftwareVersion(id, version);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the eval workload of functional test cases", operationId = "func:case:evalWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/evalWorkload")
  public ApiLocaleResult<?> replaceEvalWorkload(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseWorkloadReplaceDto dto) {
    funcCaseFacade.replaceEvalWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the actual workload of functional test cases", operationId = "func:case:actualWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/actualWorkload")
  public ApiLocaleResult<?> replaceActualWorkload(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseWorkloadReplaceDto dto) {
    funcCaseFacade.replaceActualWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace attachment the functional test cases", operationId = "func:case:attachment:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/attachment")
  public ApiLocaleResult<?> replaceAttachment(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseAttachmentReplaceDto dto) {
    funcCaseFacade.replaceAttachment(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the result of functional test cases", operationId = "func:case:result:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping(value = "/result")
  public ApiLocaleResult<?> resultReplace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseResultModifyDto> dto) {
    funcCaseFacade.resultReplace(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Update the result of functional test cases", operationId = "func:case:result:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/result")
  public ApiLocaleResult<?> resultUpdate(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<FuncCaseResultModifyDto> dto) {
    funcCaseFacade.resultUpdate(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Reset the result of test result", operationId = "func:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping(value = "/result/reset")
  public ApiLocaleResult<?> resultReset(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.resultReset(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Retest functional test cases, set the test as pending and skip the review when there is a review", operationId = "func:case:retest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
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

  @Operation(summary = "Associate the tasks of the case", operationId = "func:case:association:task:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Set successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/association/task")
  public ApiLocaleResult<?> taskAssocAdd(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    funcCaseFacade.taskAssocAdd(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Cancel the association tasks of the case", operationId = "func:case:association:task:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cancel successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/task/cancel")
  public ApiLocaleResult<?> taskAssocCancel(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    funcCaseFacade.taskAssocCancel(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Associate the cases of the case", operationId = "func:case:association:case:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Set successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/association/case")
  public ApiLocaleResult<?> caseAssocAdd(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    funcCaseFacade.caseAssocAdd(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Cancel the association cases of the case", operationId = "func:case:association:case:cancel")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cancel successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/case/cancel")
  public ApiLocaleResult<?> caseAssocCancel(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    funcCaseFacade.caseAssocCancel(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Import the functional test cases", operationId = "func:case:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE), schema = @Schema(type = "object")) @Valid FuncCaseImportDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.imports(dto));
  }

  @Operation(summary = "Import the functional test cases example", operationId = "func:case:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(funcCaseFacade.importExample(projectId));
  }

  @Operation(summary = "Delete functional test cases", operationId = "func:case:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.delete(ids);
  }

  @Operation(summary = "Query the not associated tasks list of task", operationId = "case:notAssociated:task:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/task/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedTask(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @Parameter(name = "taskType", description = "Task type", required = false) @RequestParam(value = "taskType", required = false) TaskType taskType) {
    return ApiLocaleResult.success(funcCaseFacade.notAssociatedTask(id, moduleId, taskType));
  }

  @Operation(summary = "Query the not associated cases list of task", operationId = "case:notAssociated:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/case/notAssociated")
  public ApiLocaleResult<List<FuncCaseListVo>> notAssociatedCase(
      @Parameter(name = "id", description = "Case id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "moduleId", description = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(funcCaseFacade.notAssociatedCase(id, moduleId));
  }

  @Operation(summary = "Query the detail of functional test cases", operationId = "func:case:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncCaseDetailVo> detail(
      @Valid @Parameter(name = "id", description = "Case ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseFacade.detail(id));
  }

  @Operation(summary = "Query the functional case info list of review", operationId = "func:case:review:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/review")
  public ApiLocaleResult<List<FuncCaseReviewVo>> reviewList(
      @Valid @Parameter(name = "id", description = "Case ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseFacade.reviewList(id));
  }

  @Operation(summary = "Query the list of functional test cases", operationId = "func:case:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncCaseListVo>> list(
      @Valid @ParameterObject FuncCaseFindDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of functional test cases", operationId = "func:case:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> search(
      @Valid @ParameterObject FuncCaseSearchDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.search(false, dto));
  }

  @Operation(summary = "Export the functional test cases by conditions", operationId = "func:case:export")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exported successfully")
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid @ParameterObject FuncCaseSearchDto dto, HttpServletResponse response) {
    return funcCaseFacade.export(dto, response);
  }
}
