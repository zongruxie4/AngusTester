package cloud.xcan.angus.core.tester.domain.apis.summary;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApiServerSource;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApisInfoSummary {

  private Long id;

  private Long projectId;

  public Long serviceId;

  public ApiSource source;

  public ApiImportSource importSource;

  /**
   * @see Operation#getSummary()
   */
  private String summary;

  /**
   * @see Operation#getTags()
   */
  private List<String> tags;

  /**
   * @see Operation#getOperationId()
   */
  private String operationId;

  public HttpMethod method;

  /**
   * Note: The available api servers {@link Apis#getAvailableServers()} source
   * {@link ApiServerSource} includes the current request server, api servers, and parent services
   * servers.
   *
   * @see Operation#getServers()
   */
  private Server currentServer;

  public String endpoint;

  public ApisProtocol protocol;

  public String description;

  /**
   * @see Operation#getDeprecated()
   */
  private Boolean deprecated;

  private Long ownerId;

  public ApiStatus status;

  public Boolean auth;

  private Boolean serviceAuth;

  public Boolean secured;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testFunc;

  private Boolean testFuncPassed;

  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerf;

  private Boolean testPerfPassed;

  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testStability;

  private Boolean testStabilityPassed;

  private String testStabilityFailureMessage;

  private Long createdBy;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long modifiedBy;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime modifiedDate;

}
