package cloud.xcan.angus.api.tester.script;


import cloud.xcan.angus.api.tester.script.dto.ScriptAddDto;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author XiaoLong Liu
 */
@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ScriptRemote {

  @Operation(summary = "Add script", operationId = "script:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/api/v1/script")
  ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ScriptAddDto dto);

}
