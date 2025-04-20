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

  @Schema(description = "Mock apis id")
  private Long mockApisId;

  @Schema(description = "Mock name")
  private String summary;

  private String method;

  @Length(max = MAX_APIS_URI_LENGTH)
  @Schema(description = "Mock apis path")
  private String endpoint;

  private Integer status;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
