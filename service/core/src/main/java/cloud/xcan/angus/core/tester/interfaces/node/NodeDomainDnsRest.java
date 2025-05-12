package cloud.xcan.angus.core.tester.interfaces.node;


import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeDomainDnsFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.dns.NodeDomainDnsDetailVo;
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

@Tag(name = "NodeDomainDns", description = "Cloud DNS Resolution - Dynamic management of subdomain DNS records (e.g., AliCloud DNS resolution rules).")
@Validated
@RestController
@RequestMapping("/api/v1/node/domain/dns")
@Conditional(CloudServiceEditionCondition.class)
public class NodeDomainDnsRest {

  @Resource
  private NodeDomainDnsFacade nodeDomainDnsFacade;

  @Operation(description = "Add the dns of node domain", operationId = "node:domain:dns:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody NodeDomainDnsAddDto dto) {
    return ApiLocaleResult.success(nodeDomainDnsFacade.add(dto));
  }

  @Operation(description = "Update the dns of node domain", operationId = "node:domain:dns:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody NodeDomainDnsUpdateDto dto) {
    nodeDomainDnsFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete the dns of node domain", operationId = "node:domain:dns:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(
      @Parameter(name = "id", description = "Domain dns id", required = true) @PathVariable("id") Long id) {
    nodeDomainDnsFacade.delete(id);
  }

  @Operation(description = "Query the list of node domain", operationId = "node:domain:dns:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDomainDnsDetailVo>> list(
      @Valid @ParameterObject NodeDomainDnsFindDto dto) {
    return ApiLocaleResult.success(nodeDomainDnsFacade.list(dto));
  }

}
