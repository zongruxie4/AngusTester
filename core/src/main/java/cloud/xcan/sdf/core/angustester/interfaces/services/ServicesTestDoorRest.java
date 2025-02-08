package cloud.xcan.sdf.core.angustester.interfaces.services;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesTestFacade;
import cloud.xcan.sdf.model.services.ApisTestCount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ServicesTestDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1")
public class ServicesTestDoorRest {

  @Resource
  private ServicesTestFacade servicesTestFacade;

  @ApiOperation(value = "The testing apis summary the functionality, performance, stability testing of service", nickname = "services:test:apis:count:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/services/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countServiceTestApis(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long serviceId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countServiceTestApis(serviceId, dto));
  }

  @ApiOperation(value = "The testing apis summary the functionality, performance, stability testing of project", nickname = "project:test:apis:count:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countProjectTestApis(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long projectId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countProjectTestApis(projectId, dto));
  }

}
