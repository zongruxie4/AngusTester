package cloud.xcan.sdf.core.angustester.interfaces.node;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.NodeDomainFacade;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.domain.NodeDomainDetailVo;
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

@Api(tags = "NodeRole")
@Validated
@RestController
@RequestMapping("/api/v1/node/domain")
@Conditional(CloudServiceEditionCondition.class)
public class NodeDomainRest {

  @Resource
  private NodeDomainFacade nodeDomainFacade;

  @ApiOperation(value = "Add node domains ", nickname = "node:domain:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody NodeDomainAddDto dto) {
    return ApiLocaleResult.success(nodeDomainFacade.add(dto));
  }

  @ApiOperation(value = "Update node domains", nickname = "node:domain:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody NodeDomainUpdateDto dto) {
    nodeDomainFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete node domains", nickname = "node:domain:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{id}")
  public void delete(
      @ApiParam(name = "id", value = "Domain id", required = true) @PathVariable("id") Long id) {
    nodeDomainFacade.delete(id);
  }

  @ApiOperation(value = "Query the detail of node domain", nickname = "node:domain:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<NodeDomainDetailVo> detail(
      @ApiParam(name = "id", value = "Domain id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeDomainFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of node domain", nickname = "node:domain:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDomainDetailVo>> list(@Valid NodeDomainFindDto dto) {
    return ApiLocaleResult.success(nodeDomainFacade.list(dto));
  }

}
