package cloud.xcan.sdf.model.remoting.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_HTTP_METHOD_LENGTH;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockApisDetailDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long mockServiceId;

  //@NotNull
  //@ApiModelProperty(required = true)
  //private Long apisId;

  @Length(max = MAX_HTTP_METHOD_LENGTH)
  private String method;

  @Length(max = DEFAULT_URL_LENGTH_X4)
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
