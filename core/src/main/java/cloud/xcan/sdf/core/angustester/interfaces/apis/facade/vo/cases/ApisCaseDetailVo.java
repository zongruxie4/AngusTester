package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.cases;


import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisCaseDetailVo {

  private Long id;

  private Long apisId;

  private String apisSummary;

  private Long apisServiceId;

  @NameJoinField(id = "apisServiceId", repository = "servicesRepo")
  private String apisServiceName;

  private String name;

  private String description;

  @ApiModelProperty(value = "Enable test cases? default is enabled")
  private Boolean enabled;

  @ApiModelProperty(value = "Apis test cases type, default is USER_DEFINED")
  private ApisCaseType type;

  @ApiModelProperty(value = "Apis test cases method, default is `NULL`")
  private CaseTestMethod testMethod;

  private ApisProtocol protocol;

  private HttpMethod method;

  private String endpoint;

  private Server currentServer;

  private List<Parameter> parameters;

  private RequestBody requestBody;

  private SecurityScheme authentication;

  private List<Assertion<HttpExtraction>> assertions;

  @ApiModelProperty(value = "Process actions when the dataset reaches the end of reading, default `RECYCLE`.")
  private ActionOnEOF datasetActionOnEOF;

  @ApiModelProperty(value = "Dataset sharing mode when multi threads, default `ALL_THREAD`.")
  private SharingMode datasetSharingMode;

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
