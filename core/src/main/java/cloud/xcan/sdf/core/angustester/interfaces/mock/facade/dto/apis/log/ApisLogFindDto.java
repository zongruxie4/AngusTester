package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_APIS_URI_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Deprecated
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisLogFindDto extends PageQuery {

  private String remote;

  private Long mockServiceId;

  private String method;

  @Length(max = MAX_APIS_URI_LENGTH)
  @ApiModelProperty(value = "Mock apis Uri")
  private String mockApisUri;

  private Integer status;

}



