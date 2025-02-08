package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesApisInfoListVo {

  private Long id;

  private ApiSource source;

  private ApiImportSource importSource;

  private ApisProtocol protocol;

  private String method;

  private String endpoint;

  private String summary;

  private String operationId;

  //private List<Server> servers;

  private Boolean deprecated;

  private ApiStatus status;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private Long createdBy;

  // @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo") // Join in biz
  private String createdByName;

  private Long ownerId;

  @NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private String avatar;

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;

  private Boolean authFlag;

  private Boolean serviceAuthFlag;

  /**
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testFuncFlag;

  private Boolean testFuncPassedFlag;

  private String testFuncFailureMessage;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerfFlag;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
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


  private List<String> tags;

  private Long mockServiceId;

  private Long mockApisId;
}



