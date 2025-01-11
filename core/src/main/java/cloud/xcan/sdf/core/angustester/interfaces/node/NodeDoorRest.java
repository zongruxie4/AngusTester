package cloud.xcan.sdf.core.angustester.interfaces.node;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.node.dto.NodeFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeRenewDto;
import cloud.xcan.sdf.api.angustester.node.vo.NodeDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.NodeFacade;
import cloud.xcan.sdf.spec.annotations.CloudServiceEdition;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "NodeDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1/node")
public class NodeDoorRest {

  @Resource
  private NodeFacade nodeFacade;

  @CloudServiceEdition
  @ApiOperation(value = "Online purchase nodes by order", nickname = "node:online:purchase:byorder:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful purchase or change", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/purchase/byorder")
  public ApiLocaleResult<?> purchase(@Valid @RequestBody NodeOnlinePurchaseDto dto) {
    nodeFacade.purchase(dto);
    return ApiLocaleResult.success();
  }

  @CloudServiceEdition
  @ApiOperation(value = "Online renewal nodes by order", nickname = "node:online:renewal:byorder:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful renew", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/renewal/byorder")
  public ApiLocaleResult<?> renew(@Valid @RequestBody NodeRenewDto dto) {
    nodeFacade.renew(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of node", nickname = "node:list:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid NodeFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.list(dto));
  }
}
