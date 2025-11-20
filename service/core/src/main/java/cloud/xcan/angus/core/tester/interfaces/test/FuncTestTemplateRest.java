package cloud.xcan.angus.core.tester.interfaces.test;

import cloud.xcan.angus.core.tester.interfaces.test.facade.TestTemplateFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.template.TestTemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.template.TestTemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.template.TestTemplateListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test Template", description = "Test Template Management - APIs for creating, updating, deleting, and querying test templates with quota management (max 200 custom templates)")
@Validated
@RestController
@RequestMapping("/api/v1/func/test/template")
public class FuncTestTemplateRest {

  @Resource
  private TestTemplateFacade testTemplateFacade;

  @Operation(summary = "Create test template",
      description = "Create a new custom test template with detailed configuration. Maximum 200 custom templates allowed.",
      operationId = "func:test:template:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test template created successfully"),
      @ApiResponse(responseCode = "400", description = "Maximum custom template quota exceeded (200 templates)")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody TestTemplateAddDto dto) {
    return ApiLocaleResult.success(testTemplateFacade.add(dto));
  }

  @Operation(summary = "Update custom test template",
      description = "Update an existing custom test template with partial modification support. System templates cannot be updated.",
      operationId = "func:test:template:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test template updated successfully"),
      @ApiResponse(responseCode = "404", description = "Test template not found"),
      @ApiResponse(responseCode = "400", description = "System template cannot be updated")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody TestTemplateUpdateDto dto) {
    testTemplateFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete custom test template",
      description = "Delete a custom test template from the system. System templates cannot be deleted.",
      operationId = "func:test:template:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test template deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Test template not found"),
      @ApiResponse(responseCode = "400", description = "System template cannot be deleted")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Test template identifier for deletion", required = true) @PathVariable("id") Long id) {
    testTemplateFacade.delete(id);
  }

  @Operation(summary = "List all test templates",
      description = "Retrieve list of all test templates (both system and custom templates) without pagination",
      operationId = "func:test:template:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test template list retrieved successfully")
  })
  @GetMapping
  public ApiLocaleResult<List<TestTemplateListVo>> list() {
    return ApiLocaleResult.success(testTemplateFacade.list());
  }
}
