package cloud.xcan.angus.core.tester.interfaces.node;


import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.angus.api.tester.node.dto.NodeRenewDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.annotations.CloudServiceEdition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "NodeInner", description = "Node Procurement (Internal) - Resource purchase and renewal workflows for cloud service nodes")
@Validated
@RestController
@RequestMapping("/innerapi/v1/node")
public class NodeInnerRest {

  @Resource
  private NodeFacade nodeFacade;

  @CloudServiceEdition
  @Operation(summary = "Online purchase nodes by order", operationId = "node:online:purchase:byorder:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful purchase or change")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/purchase/byorder")
  public ApiLocaleResult<?> purchase(@Valid @RequestBody NodeOnlinePurchaseDto dto) {
    nodeFacade.purchase(dto);
    return ApiLocaleResult.success();
  }

  @CloudServiceEdition
  @Operation(summary = "Online renewal nodes by order", operationId = "node:online:renewal:byorder:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful renew")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/renewal/byorder")
  public ApiLocaleResult<?> renew(@Valid @RequestBody NodeRenewDto dto) {
    nodeFacade.renew(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the list of node", operationId = "node:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid @ParameterObject NodeFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.list(dto));
  }
}
