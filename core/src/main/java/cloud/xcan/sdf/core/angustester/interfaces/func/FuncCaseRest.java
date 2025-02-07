package cloud.xcan.sdf.core.angustester.interfaces.func;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseAttachmentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseResultModifyDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseTagReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseTesterReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseWorkloadReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseReviewVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
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

@Api(tags = "FuncCase")
@Validated
@RestController
@RequestMapping("/api/v1/func/case")
public class FuncCaseRest {

  @Resource
  private FuncCaseFacade funcCaseFacade;

  @ApiOperation(value = "Add functional test cases", nickname = "func:case:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncCaseAddDto> dto) {
    return ApiLocaleResult.success(funcCaseFacade.add(dto));
  }

  @ApiOperation(value = "Update functional test cases", nickname = "func:case:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncCaseUpdateDto> dto) {
    funcCaseFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace functional test cases", nickname = "func:case:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncCaseReplaceDto> dto) {
    return ApiLocaleResult.success(funcCaseFacade.replace(dto));
  }

  @ApiOperation(value = "Replace the name of functional test cases", nickname = "func:case:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @Length(max = DEFAULT_NAME_LENGTH_X4) @ApiParam(name = "name", value = "New case name", required = true) @RequestParam("name") String name) {
    funcCaseFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone the functional test cases", nickname = "func:case:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Clone successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/clone")
  public ApiLocaleResult<List<IdKey<Long, Object>>> clone(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(funcCaseFacade.clone(ids));
  }

  @ApiOperation(value = "Move the case to another plan", nickname = "func:case:move")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Moved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping(value = "/move")
  public ApiLocaleResult<?> move(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids,
      @ApiParam(name = "targetPlanId", value = "Target plan id", required = true) @RequestParam("targetPlanId") Long targetPlanId) {
    funcCaseFacade.move(ids, targetPlanId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the tester of case", nickname = "func:case:tester:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tester")
  public ApiLocaleResult<?> replaceTester(
      @ApiParam(name = "id", value = "Task id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseTesterReplaceDto dto) {
    funcCaseFacade.replaceTester(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the tags of functional test cases", nickname = "func:case:tags:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/tag")
  public ApiLocaleResult<?> replaceTag(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseTagReplaceDto dto) {
    funcCaseFacade.replaceTag(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the deadline of functional test cases", nickname = "func:case:deadline:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/deadline/{deadline}")
  public ApiLocaleResult<?> replaceDeadline(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "deadline", value = "Case deadline", required = true) @PathVariable("deadline") @DateTimeFormat(pattern = DATE_FMT) LocalDateTime deadlineDate) {
    funcCaseFacade.replaceDeadline(id, deadlineDate);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the priority of functional test cases", nickname = "func:case:priority:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/priority/{priority}")
  public ApiLocaleResult<?> replacePriority(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "priority", value = "Case priority", required = true) @PathVariable("priority") Priority priority) {
    funcCaseFacade.replacePriority(id, priority);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the eval workload of functional test cases", nickname = "func:case:evalWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/evalWorkload")
  public ApiLocaleResult<?> replaceEvalWorkload(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseWorkloadReplaceDto dto) {
    funcCaseFacade.replaceEvalWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the actual workload of functional test cases", nickname = "func:case:actualWorkload:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/actualWorkload")
  public ApiLocaleResult<?> replaceActualWorkload(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseWorkloadReplaceDto dto) {
    funcCaseFacade.replaceActualWorkload(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace attachment the functional test cases", nickname = "func:case:attachment:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/attachment")
  public ApiLocaleResult<?> replaceAttachment(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody FuncCaseAttachmentReplaceDto dto) {
    funcCaseFacade.replaceAttachment(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the result of functional test cases", nickname = "func:case:result:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping(value = "/result")
  public ApiLocaleResult<?> resultReplace(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncCaseResultModifyDto> dto) {
    funcCaseFacade.resultReplace(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Update the result of functional test cases", nickname = "func:case:result:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/result")
  public ApiLocaleResult<?> resultUpdate(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncCaseResultModifyDto> dto) {
    funcCaseFacade.resultUpdate(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reset the result of test result", nickname = "func:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/result/reset")
  public ApiLocaleResult<?> resultReset(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.resultReset(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Retest functional test cases, set the test as pending and skip the review when there is a review", nickname = "func:case:retest")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/result/retest")
  public ApiLocaleResult<?> retest(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.retest(ids);
    return ApiLocaleResult.success();
  }

  //  @ApiOperation(value = "Review functional test cases", nickname = "func:case:review")
  //  @ApiResponses(value = {
  //      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
  //      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  //  })
  //  @PutMapping("/review")
  //  public ApiLocaleResult<?> review(
  //      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<FuncCaseReviewDto> dto) {
  //    funcCaseFacade.review(dto);
  //    return ApiLocaleResult.success();
  //  }

  //  @ApiOperation(value = "Reset the result of case review", nickname = "func:case:review:reset")
  //  @ApiResponses(value = {
  //      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
  //      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  //  })
  //  @PatchMapping(value = "/review/reset")
  //  public ApiLocaleResult<?> reviewReset(
  //      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
  //    funcCaseFacade.reviewReset(ids);
  //    return ApiLocaleResult.success();
  //  }

  @ApiOperation(value = "Associate the tasks of the case", nickname = "func:case:association:task:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Set successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/association/task")
  public ApiLocaleResult<?> taskAssocAdd(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    funcCaseFacade.taskAssocAdd(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Cancel the association tasks of the case", nickname = "func:case:association:task:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cancel successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/task/cancel")
  public ApiLocaleResult<?> taskAssocCancel(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocTaskIds") HashSet<Long> assocTaskIds) {
    funcCaseFacade.taskAssocCancel(id, assocTaskIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Associate the cases of the case", nickname = "func:case:association:case:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Set successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/association/case")
  public ApiLocaleResult<?> caseAssocAdd(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    funcCaseFacade.caseAssocAdd(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Cancel the association cases of the case", nickname = "func:case:association:case:cancel")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cancel successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/association/case/cancel")
  public ApiLocaleResult<?> caseAssocCancel(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("assocCaseIds") HashSet<Long> assocCaseIds) {
    funcCaseFacade.caseAssocCancel(id, assocCaseIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete functional test cases", nickname = "func:case:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    funcCaseFacade.delete(ids);
  }

  @ApiOperation(value = "Query the not associated tasks list of task", nickname = "case:notAssociated:task:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/task/notAssociated")
  public ApiLocaleResult<List<TaskInfoVo>> notAssociatedTask(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId,
      @ApiParam(name = "taskType", value = "Task type", required = false) @RequestParam(value = "taskType", required = false) TaskType taskType) {
    return ApiLocaleResult.success(funcCaseFacade.notAssociatedTask(id, moduleId, taskType));
  }

  @ApiOperation(value = "Query the not associated cases list of task", nickname = "case:notAssociated:case:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/case/notAssociated")
  public ApiLocaleResult<List<FuncCaseListVo>> notAssociatedCase(
      @ApiParam(name = "id", value = "Case id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "moduleId", value = "Module id", required = false) @RequestParam(value = "moduleId", required = false) Long moduleId) {
    return ApiLocaleResult.success(funcCaseFacade.notAssociatedCase(id, moduleId));
  }

  @ApiOperation(value = "Query the detail of functional test cases", nickname = "func:case:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncCaseDetailVo> detail(
      @Valid @ApiParam(name = "id", value = "Case ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseFacade.detail(id));
  }

  @ApiOperation(value = "Query the functional case info list of review", nickname = "func:case:review:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/review")
  public ApiLocaleResult<List<FuncCaseReviewVo>> reviewList(
      @Valid @ApiParam(name = "id", value = "Case ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcCaseFacade.reviewList(id));
  }

  @ApiOperation(value = "Query the list of functional test cases", nickname = "func:case:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncCaseListVo>> list(@Valid FuncCaseFindDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of functional test cases", nickname = "func:case:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncCaseListVo>> search(@Valid FuncCaseSearchDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.search(false, dto));
  }

  @ApiOperation(value = "Import the inner functional test cases example", nickname = "func:case:example:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> exampleImport(
      @ApiParam(name = "projectId", value = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(funcCaseFacade.exampleImport(projectId));
  }

  @ApiOperation(value = "Import the functional test cases", nickname = "func:case:import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<List<IdKey<Long, Object>>> imports(@Valid FuncCaseImportDto dto) {
    return ApiLocaleResult.success(funcCaseFacade.imports(dto));
  }

  @ApiOperation(value = "Export the functional test cases by conditions", nickname = "func:case:export")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Exported successfully", response = ApiLocaleResult.class)
  })
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(@Valid FuncCaseSearchDto dto,
      HttpServletResponse response) {
    return funcCaseFacade.export(dto, response);
  }

}
