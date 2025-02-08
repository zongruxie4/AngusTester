package cloud.xcan.sdf.api.angustester.services;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.model.services.ApisTestCount;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ServicesTestDoorRemote {

  @ApiOperation(value = "The testing apis summary the functionality, performance, stability testing of service", nickname = "services:apis:test:count:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/doorapi/v1/services/{id}/test/apis/count")
  ApiLocaleResult<ApisTestCount> countServiceTestApis(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long serviceId,
      @SpringQueryMap OrgAndDateFilterDto dto);

  @ApiOperation(value = "The testing apis summary the functionality, performance, stability testing of project", nickname = "project:apis:test:count:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/doorapi/v1/project/{id}/test/apis/count")
  ApiLocaleResult<ApisTestCount> countProjectTestApis(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long projectId,
      @SpringQueryMap OrgAndDateFilterDto dto);
}
