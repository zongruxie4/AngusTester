package cloud.xcan.sdf.core.angustester.interfaces.node;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.NodeDomainDnsFacade;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.dns.NodeDomainDnsDetailVo;
import cloud.xcan.sdf.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@Api(tags = "NodeDomainDns")
@Validated
@RestController
@RequestMapping("/api/v1/node/domain/dns")
@Conditional(CloudServiceEditionCondition.class)
public class NodeDomainDnsRest {

  @Resource
  private NodeDomainDnsFacade nodeDomainDnsFacade;

  @ApiOperation(value = "Add the dns of node domain", nickname = "node:domain:dns:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody NodeDomainDnsAddDto dto) {
    return ApiLocaleResult.success(nodeDomainDnsFacade.add(dto));
  }

  @ApiOperation(value = "Update the dns of node domain", nickname = "node:domain:dns:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody NodeDomainDnsUpdateDto dto) {
    nodeDomainDnsFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the dns of node domain", nickname = "node:domain:dns:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(
      @ApiParam(name = "id", value = "Domain dns id", required = true) @PathVariable("id") Long id) {
    nodeDomainDnsFacade.delete(id);
  }

  @ApiOperation(value = "Query the list of node domain", nickname = "node:domain:dns:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDomainDnsDetailVo>> list(@Valid NodeDomainDnsFindDto dto) {
    return ApiLocaleResult.success(nodeDomainDnsFacade.list(dto));
  }

}