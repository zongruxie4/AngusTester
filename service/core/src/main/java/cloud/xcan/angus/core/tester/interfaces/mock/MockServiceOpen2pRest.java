package cloud.xcan.angus.core.tester.interfaces.mock;


import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceOpen2pFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.MockServiceInfoVo;
import cloud.xcan.angus.spec.annotations.Unused;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author XiaoLong Liu
 */
@Tag(name = "MockServiceOpen2p", description = "Mock Instance Config Query - Internal endpoints for querying mock service instance configurations.")
@Validated
@RestController
@RequestMapping("/openapi2p/v1/mock/service")
public class MockServiceOpen2pRest {

  @Resource
  private MockServiceOpen2pFacade mockServiceOpen2pFacade;

  @Unused
  @Operation(description = "Query the info of mock service", operationId = "mock:service:info:openapi2p")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping("/{id}/info")
  public MockServiceInfoVo info(
      @Parameter(name = "id", description = "Mock service id", required = true) @PathVariable("id") Long id) {
    return mockServiceOpen2pFacade.info(id);
  }

}
