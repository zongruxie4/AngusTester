package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.cases;


import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 */
@Valid
@Setter
@Getter
@Accessors(chain = true)
public class ApisCaseListVo {

  private Long id;

  private Long projectId;

  private Long serviceId;

  private Long apisId;

  private String apisSummary;

  private String name;

  private String description;

  @Schema(description = "Enable test cases? default is enabled")
  private Boolean enabled;

  @Schema(description = "Apis test cases type, default is USER_DEFINED")
  private ApisCaseType type;

  @Schema(description = "Apis test cases method, default is `NULL`")
  private CaseTestMethod testMethod;

  private ApisProtocol protocol;

  private HttpMethod method;

  private String endpoint;

  private Boolean favourite;

  private Boolean follow;

  private Boolean apisDeleted;

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
