package cloud.xcan.angus.core.tester.interfaces.ctrl;

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

@Tag(name = "Controller", description = "Controller Management API - Comprehensive controller health monitoring, leadership election, and agent connectivity management with resource-based load balancing")
@Validated
@RestController
@RequestMapping(value = "/api/v1/ctrl")
public class CtrlRest {

  @Resource
  private CtrlFacade ctrlFacade;

  @Operation(summary = "Controller health check ping",
      description = "Perform health check ping to validate controller availability and binding status",
      operationId = "ctrl:ping")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Controller health check successful")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(DISCOVERY_PING_ENDPOINT_SUFFIX)
  public Principal ping() {
    return PrincipalContext.get();
  }

  @Operation(summary = "Controller discovery and election",
      description = "Query available controllers and perform leadership election based on resource utilization and task load",
      operationId = "ctrl:discovery")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Controller discovery and election completed successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(DISCOVERY_ENDPOINT_SUFFIX)
  public /*ApiLocaleResult<DiscoveryNodeVo>*/ DiscoveryNodeVo discovery(
      @ParameterObject DiscoveryNodeDto dto) {
    return ctrlFacade.discovery(dto);
  }

  @Operation(summary = "Controller connection information",
      description = "Retrieve comprehensive connection information for all controller agents and routing channels",
      operationId = "ctrl:connections:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Controller connection information retrieved successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/connections/info")
  public ApiLocaleResult<List<ChannelRouter>> connectionsInfo() {
    return ApiLocaleResult.success(ctrlFacade.connectionsInfo());
  }

}
