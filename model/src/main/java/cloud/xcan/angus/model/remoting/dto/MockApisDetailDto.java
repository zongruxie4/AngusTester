package cloud.xcan.angus.model.remoting.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_HTTP_METHOD_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockApisDetailDto {

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  //@NotNull
  //@Schema(required = true)
  //private Long apisId;

  @Length(max = MAX_HTTP_METHOD_LENGTH)
  private String method;

  @Length(max = MAX_URL_LENGTH_X4)
  private String endpoint;

  public MockApisDetailDto() {
  }

  private MockApisDetailDto(Builder builder) {
    setMockServiceId(builder.mockServiceId);
    setMethod(builder.method);
    setEndpoint(builder.endpoint);
  }

  public static Builder newBuilder() {
    return new Builder();
  }


  public static final class Builder {

    private Long mockServiceId;
    private String method;
    private String endpoint;

    private Builder() {
    }

    public Builder mockServiceId(Long val) {
      mockServiceId = val;
      return this;
    }

    public Builder method(String val) {
      method = val;
      return this;
    }

    public Builder endpoint(String val) {
      endpoint = val;
      return this;
    }

    public MockApisDetailDto build() {
      return new MockApisDetailDto(this);
    }
  }
}
