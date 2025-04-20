package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class MockApisFindDto extends PageQuery {

  private Long id;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  private String summary;

  private MockApisSource source;

  private ApiImportSource importSource;

  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  private String endpoint;

  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  private Long assocProjectId;

  private Long assocApisId;

  private Long requestNum;

  private Long pushbackNum;

  private Long simulateErrorNum;

  private Long successNum;

  private Long exceptionNum;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
