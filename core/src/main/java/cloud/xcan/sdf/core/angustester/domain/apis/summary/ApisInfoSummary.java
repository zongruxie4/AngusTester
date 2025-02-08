package cloud.xcan.sdf.core.angustester.domain.apis.summary;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.http.HttpMethod;
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

  public Boolean authFlag;

  private Boolean serviceAuthFlag;

  public Boolean securityFlag;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testFuncFlag;

  private Boolean testFuncPassedFlag;

  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerfFlag;

  private Boolean testPerfPassedFlag;

  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testStabilityFlag;

  private Boolean testStabilityPassedFlag;

  private String testStabilityFailureMessage;

  private Long createdBy;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
