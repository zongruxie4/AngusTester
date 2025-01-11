package cloud.xcan.sdf.api.angustester.node;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.node.dto.NodeFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeRenewDto;
import cloud.xcan.sdf.api.angustester.node.vo.NodeDetailVo;
import cloud.xcan.sdf.spec.annotations.CloudServiceEdition;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface NodeDoorRemote {

  @CloudServiceEdition
  @ApiOperation(value = "Online purchase nodes by order", nickname = "node:online:purchase:byorder:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful purchase or change", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/doorapi/v1/node/purchase/byorder")
  ApiLocaleResult<?> purchase(@Valid @RequestBody NodeOnlinePurchaseDto dto);

  @CloudServiceEdition
  @ApiOperation(value = "Online renewal nodes by order", nickname = "node:online:renewal:byorder:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful renew", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/doorapi/v1/node/renewal/byorder")
  ApiLocaleResult<?> renew(@Valid @RequestBody NodeRenewDto dto);

  @ApiOperation(value = "Query the list of node", nickname = "node:list:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/doorapi/v1/node")
  ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid @SpringQueryMap NodeFindDto dto);
}
