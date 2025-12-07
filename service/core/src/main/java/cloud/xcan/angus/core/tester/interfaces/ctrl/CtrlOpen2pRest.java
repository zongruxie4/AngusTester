package cloud.xcan.angus.core.tester.interfaces.ctrl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.OPEN2P_DISCOVER_URI_PREFIX;
import static cloud.xcan.angus.remoting.common.node.ServerDiscovery.DISCOVERY_ENDPOINT_SUFFIX;
import static cloud.xcan.angus.remoting.common.node.ServerDiscovery.DISCOVERY_PING_ENDPOINT_SUFFIX;

import cloud.xcan.angus.core.tester.interfaces.ctrl.facade.CtrlFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeDto;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeVo;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Controller - Private Environment", description = "Private Environment Controller API - On-premises controller status monitoring, leadership election, and agent connection management for private deployment environments")
@Validated
@RestController
@RequestMapping(OPEN2P_DISCOVER_URI_PREFIX)
public class CtrlOpen2pRest {

  @Resource
  private CtrlFacade ctrlFacade;

  @Operation(summary = "Private controller health check ping",
      description = "Perform health check ping to validate private environment controller availability and binding status",
      operationId = "ctrl:ping:open2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Private controller health check successful")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(DISCOVERY_PING_ENDPOINT_SUFFIX)
  public Principal ping() {
    return PrincipalContext.get();
  }

  @Operation(summary = "Private controller discovery and election",
      description = "Query available private environment controllers and perform leadership election based on resource utilization and task load",
      operationId = "ctrl:discovery:open2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Private controller discovery and election completed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(DISCOVERY_ENDPOINT_SUFFIX)
  public /*ApiLocaleResult<DiscoveryNodeVo>*/ DiscoveryNodeVo discovery(
      @ParameterObject DiscoveryNodeDto dto) {
    return ctrlFacade.discovery(dto);
  }

  @Operation(summary = "Private controller connection information",
      description = "Retrieve comprehensive connection information for all private environment controller agents and routing channels",
      operationId = "ctrl:connections:info:open2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Private controller connection information retrieved successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/connections/info")
  public ApiLocaleResult<List<ChannelRouter>> connectionsInfo() {
    return ApiLocaleResult.success(ctrlFacade.connectionsInfo());
  }

}
