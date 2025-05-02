package cloud.xcan.angus.api.tester.node;


import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.angus.api.tester.node.dto.NodeRenewDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.annotations.CloudServiceEdition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface NodeInnerRemote {

  @CloudServiceEdition
  @Operation(description = "Online purchase nodes by order", operationId = "node:online:purchase:byorder:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful purchase or change")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/innerapi/v1/node/purchase/byorder")
  ApiLocaleResult<?> purchase(@Valid @RequestBody NodeOnlinePurchaseDto dto);

  @CloudServiceEdition
  @Operation(description = "Online renewal nodes by order", operationId = "node:online:renewal:byorder:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful renew")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/innerapi/v1/node/renewal/byorder")
  ApiLocaleResult<?> renew(@Valid @RequestBody NodeRenewDto dto);

  @Operation(description = "Query the list of node", operationId = "node:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/innerapi/v1/node")
  ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid @SpringQueryMap NodeFindDto dto);
}
