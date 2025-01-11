package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.cases;


import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 */
@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisCaseListVo {

  private Long id;

  private Long projectId;

  private Long serviceId;

  private Long apisId;

  private String apisSummary;

  @ApiModelProperty(value = "Enable test cases? default is enabled")
  private Boolean enabled;

  @ApiModelProperty(value = "Apis test cases type, default is USER_DEFINED")
  private ApisCaseType type;

  private String name;

  private String description;

  private ApisProtocol protocol;

  private HttpMethod method;

  private String endpoint;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private Boolean apisDeletedFlag;

  private Result execResult;

  private String execFailureMessage;

  private Integer execTestNum;

  private Integer execTestFailureNum;

  private Long execId;

  private String execName;

  private Long execBy;

  @NameJoinField(id = "execBy", repository = "commonUserBaseRepo")
  private String execByName;

  private LocalDateTime execDate;

  // private int commentNum;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  //@NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
