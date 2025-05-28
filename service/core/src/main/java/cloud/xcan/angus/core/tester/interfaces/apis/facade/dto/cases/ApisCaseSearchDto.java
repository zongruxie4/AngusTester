package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class ApisCaseSearchDto extends PageQuery {

  private Long id;

  private Long projectId;

  private Long serviceId;

  private Long apisId;

  private String name;

  @Schema(description = "Enable test cases flag default is `enabled`")
  private Boolean enabled;

  @Schema(description = "Apis test cases type, default is `USER_DEFINED`")
  private ApisCaseType type;

  @Schema(description = "Apis test cases method, default is `NULL`")
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
