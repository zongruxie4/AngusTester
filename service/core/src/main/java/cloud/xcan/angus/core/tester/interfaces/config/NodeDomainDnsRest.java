package cloud.xcan.angus.core.tester.interfaces.config;


import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.core.tester.interfaces.config.facade.NodeDomainDnsFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.dns.NodeDomainDnsDetailVo;
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

@Tag(name = "Node Domain Dns", description = "Cloud DNS Resolution Management - Dynamic management of subdomain DNS records for cloud service nodes, supporting AliCloud DNS resolution rules and domain routing configuration")
@Validated
@RestController
@RequestMapping("/api/v1/node/domain/dns")
@Conditional(CloudServiceEditionCondition.class)
public class NodeDomainDnsRest {

  @Resource
  private NodeDomainDnsFacade nodeDomainDnsFacade;

  @Operation(summary = "Create DNS resolution record for node domain",
      description = "Add a new DNS resolution record to enable subdomain routing for cloud service nodes.",
      operationId = "node:domain:dns:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "DNS record created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody NodeDomainDnsAddDto dto) {
    return ApiLocaleResult.success(nodeDomainDnsFacade.add(dto));
  }

  @Operation(summary = "Update DNS resolution record for node domain",
      description = "Modify existing DNS resolution record properties for domain routing configuration.",
      operationId = "node:domain:dns:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "DNS record updated successfully"),
      @ApiResponse(responseCode = "404", description = "DNS record not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody NodeDomainDnsUpdateDto dto) {
    nodeDomainDnsFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete DNS resolution record for node domain",
      description = "Remove DNS resolution record to disable subdomain routing for cloud service nodes.",
      operationId = "node:domain:dns:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "DNS record deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(
      @Parameter(name = "id", description = "DNS record identifier for deletion", required = true) @PathVariable("id") Long id) {
    nodeDomainDnsFacade.delete(id);
  }

  @Operation(summary = "Query DNS resolution records for node domains",
      description = "Retrieve paginated list of DNS resolution records with filtering and search capabilities.",
      operationId = "node:domain:dns:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "DNS records retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDomainDnsDetailVo>> list(
      @Valid @ParameterObject NodeDomainDnsFindDto dto) {
    return ApiLocaleResult.success(nodeDomainDnsFacade.list(dto));
  }

}
