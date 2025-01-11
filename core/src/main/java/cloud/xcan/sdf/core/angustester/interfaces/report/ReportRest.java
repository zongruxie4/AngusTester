package cloud.xcan.sdf.core.angustester.interfaces.report;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.ReportUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportListVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.FuncCaseContentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content.TaskContentVo;
import cloud.xcan.sdf.core.spring.condition.NotPrivateEditionCondition;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
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

@Api(tags = "Report")
@Validated
@RestController
@RequestMapping("/api/v1/report")
@Conditional(NotPrivateEditionCondition.class)
public class ReportRest {

  @Resource
  private ReportFacade reportFacade;

  @ApiOperation(value = "Add report", nickname = "report:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ReportAddDto dto) {
    return ApiLocaleResult.success(reportFacade.add(dto));
  }

  @ApiOperation(value = "Update report", nickname = "report:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ReportUpdateDto dto) {
    reportFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace report", nickname = "report:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ReportReplaceDto dto) {
    return ApiLocaleResult.success(reportFacade.replace(dto));
  }

  @ApiOperation(value = "Generate report now", nickname = "report:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Generated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/{id}/generate/now")
  public ApiLocaleResult<?> generateNow(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long id) {
    reportFacade.generateNow(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete report", nickname = "report:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Report ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    reportFacade.delete(ids);
  }

  /*@ApiOperation(value = "Query the share token of report", nickname = "report:share:token:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/shareToken")
  public ApiLocaleResult<String> shareToken(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.successData(reportFacade.shareToken(id));
  }*/

  @ApiOperation(value = "Query the content of report", nickname = "report:content:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/content")
  public ApiLocaleResult<Object> content(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.successData(reportFacade.content(id));
  }

  @ApiOperation(value = "Query the content of task report or record", nickname = "report:content:task:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/content/task/{taskId}")
  public ApiLocaleResult<TaskContentVo> taskContent(
      @ApiParam(name = "taskId", value = "Task id", required = true) @PathVariable("taskId") Long taskId) {
    return ApiLocaleResult.success(reportFacade.taskContent(taskId));
  }

  @ApiOperation(value = "Query the content of testing case report or record", nickname = "report:content:funcCase:query")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/content/funcCase/{caseId}")
  public ApiLocaleResult<FuncCaseContentVo> funcCaseContent(
      @ApiParam(name = "caseId", value = "Case id", required = true) @PathVariable("caseId") Long caseId) {
    return ApiLocaleResult.success(reportFacade.funcCaseContent(caseId));
  }

  @ApiOperation(value = "Query the detail of report", nickname = "report:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ReportDetailVo> detail(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(reportFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of report", nickname = "report:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ReportListVo>> list(@Valid ReportFindDto dto) {
    return ApiLocaleResult.success(reportFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of report", nickname = "report:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ReportListVo>> search(@Valid ReportSearchDto dto) {
    return ApiLocaleResult.success(reportFacade.search(dto));
  }

}
