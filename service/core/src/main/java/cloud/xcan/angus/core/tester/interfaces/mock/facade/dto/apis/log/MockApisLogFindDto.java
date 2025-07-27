package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_APIS_URI_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class MockApisLogFindDto extends PageQuery {

  @Schema(description = "Mock API identifier for log filtering")
  private Long mockApisId;

  @Schema(description = "Mock API name for log filtering")
  private String summary;

  @Schema(description = "HTTP method for log filtering")
  private String method;

  @Length(max = MAX_APIS_URI_LENGTH)
  @Schema(description = "Mock API endpoint path for log filtering")
  private String endpoint;

  @Schema(description = "HTTP status code for log filtering")
  private Integer status;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Log creation date for temporal filtering")
  private LocalDateTime createdDate;

}
