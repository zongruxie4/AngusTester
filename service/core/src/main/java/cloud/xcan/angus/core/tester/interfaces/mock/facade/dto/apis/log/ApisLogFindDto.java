package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.log;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_APIS_URI_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Deprecated

@Setter
@Getter
@Accessors(chain = true)
public class ApisLogFindDto extends PageQuery {

  private String remote;

  private Long mockServiceId;

  private String method;

  @Length(max = MAX_APIS_URI_LENGTH)
  @Schema(description = "Mock apis Uri")
  private String mockApisUri;

  private Integer status;

}



