package cloud.xcan.angus.core.tester.interfaces.services.facade.vo;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ServicesApisInfoListVo {

  private Long id;

  private ApiSource source;

  private ApiImportSource importSource;

  private Long serviceId;

  @NameJoinField(id = "serviceId", repository = "servicesRepo")
  private String serviceName;

  private ApisProtocol protocol;

  private String method;

  private String endpoint;

  private String summary;

  private String operationId;

  //private List<Server> servers;

  private Boolean deprecated;

  private ApiStatus status;

  private Boolean favourite;

  private Boolean follow;

  private Long createdBy;

  // @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo") // Join in biz
  private String createdByName;

  private Long ownerId;

  @NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private String avatar;

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;

  private Boolean auth;

  private Boolean serviceAuth;

  /**
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testFunc;

  private Boolean testFuncPassed;

  private String testFuncFailureMessage;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerf;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
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

  private List<String> tags;

  private Long mockServiceId;

  private Long mockApisId;
}



