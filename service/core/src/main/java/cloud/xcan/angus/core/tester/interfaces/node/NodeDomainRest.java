package cloud.xcan.angus.core.tester.interfaces.node;


import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeDomainFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.annotation.Conditional;
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

@Tag(name = "NodeDomain", description = "Cloud Subdomain Registry - Lifecycle management of subdomains for cloud service nodes (e.g., AliCloud hosted zones).")
@Validated
@RestController
@RequestMapping("/api/v1/node/domain")
@Conditional(CloudServiceEditionCondition.class)
public class NodeDomainRest {

  @Resource
  private NodeDomainFacade nodeDomainFacade;

  @Operation(summary = "Add node domains ", operationId = "node:domain:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody NodeDomainAddDto dto) {
    return ApiLocaleResult.success(nodeDomainFacade.add(dto));
  }

  @Operation(summary = "Update node domains", operationId = "node:domain:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody NodeDomainUpdateDto dto) {
    nodeDomainFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete node domains", operationId = "node:domain:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(
      @Parameter(name = "id", description = "Domain id", required = true) @PathVariable("id") Long id) {
    nodeDomainFacade.delete(id);
  }

  @Operation(summary = "Query the detail of node domain", operationId = "node:domain:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<NodeDomainDetailVo> detail(
      @Parameter(name = "id", description = "Domain id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeDomainFacade.detail(id));
  }

  @Operation(summary = "Query the list of node domain", operationId = "node:domain:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDomainDetailVo>> list(
      @Valid @ParameterObject NodeDomainFindDto dto) {
    return ApiLocaleResult.success(nodeDomainFacade.list(dto));
  }

}
