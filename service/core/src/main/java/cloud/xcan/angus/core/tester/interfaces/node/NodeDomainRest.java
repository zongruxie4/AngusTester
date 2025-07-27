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

@Tag(name = "Node Domain", description = "Cloud Subdomain Management - Lifecycle management of subdomains for cloud service nodes, supporting AliCloud hosted zones and domain registration workflows.")
@Validated
@RestController
@RequestMapping("/api/v1/node/domain")
@Conditional(CloudServiceEditionCondition.class)
public class NodeDomainRest {

  @Resource
  private NodeDomainFacade nodeDomainFacade;

  @Operation(summary = "Create subdomain for cloud service node",
      description = "Register a new subdomain for cloud service node to enable domain-based service routing.",
      operationId = "node:domain:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Subdomain created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody NodeDomainAddDto dto) {
    return ApiLocaleResult.success(nodeDomainFacade.add(dto));
  }

  @Operation(summary = "Update subdomain configuration for cloud service node",
      description = "Modify existing subdomain properties and configuration settings.",
      operationId = "node:domain:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Subdomain updated successfully"),
      @ApiResponse(responseCode = "404", description = "Subdomain not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody NodeDomainUpdateDto dto) {
    nodeDomainFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete subdomain for cloud service node",
      description = "Remove subdomain registration to disable domain-based service routing.",
      operationId = "node:domain:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Subdomain deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(
      @Parameter(name = "id", description = "Subdomain identifier for deletion", required = true) @PathVariable("id") Long id) {
    nodeDomainFacade.delete(id);
  }

  @Operation(summary = "Query subdomain details for cloud service node",
      description = "Retrieve detailed information of a specific subdomain configuration.",
      operationId = "node:domain:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Subdomain details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Subdomain not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<NodeDomainDetailVo> detail(
      @Parameter(name = "id", description = "Subdomain identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeDomainFacade.detail(id));
  }

  @Operation(summary = "Query subdomain list for cloud service nodes",
      description = "Retrieve paginated list of subdomains with filtering and search capabilities.",
      operationId = "node:domain:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Subdomain list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDomainDetailVo>> list(
      @Valid @ParameterObject NodeDomainFindDto dto) {
    return ApiLocaleResult.success(nodeDomainFacade.list(dto));
  }

}
