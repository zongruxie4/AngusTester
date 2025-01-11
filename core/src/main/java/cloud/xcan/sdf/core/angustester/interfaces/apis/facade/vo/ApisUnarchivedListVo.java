package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisUnarchivedListVo {

  private Long id;

  private Long projectId;

  @ApiModelProperty(example = "http")
  private ApisProtocol protocol;

  @ApiModelProperty(example = "GET")
  private String method;

  @ApiModelProperty(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  private String summary;

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;
}



