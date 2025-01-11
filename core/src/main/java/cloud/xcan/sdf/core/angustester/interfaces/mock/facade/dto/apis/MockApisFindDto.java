package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisSource;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
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

  @Length(max = DEFAULT_URL_LENGTH_X4)
  private String endpoint;

  @ApiModelProperty(required = true)
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
