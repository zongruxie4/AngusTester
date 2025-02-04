package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisCaseFindDto extends PageQuery {

  private Long id;

  private Long projectId;

  private Long serviceId;

  private Long apisId;

  private String name;

  @ApiModelProperty(value = "Enable test cases flag default is `enabled`")
  private Boolean enabled;

  @ApiModelProperty(value = "Apis test cases type, default is `USER_DEFINED`")
  private ApisCaseType type;

  @ApiModelProperty(value = "Apis test cases method, default is `NULL`")
  private CaseTestMethod testMethod;

  private ApisProtocol protocol;

  private HttpMethod method;

  private String endpoint;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

}



