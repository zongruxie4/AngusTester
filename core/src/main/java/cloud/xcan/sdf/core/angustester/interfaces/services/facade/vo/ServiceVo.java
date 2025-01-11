package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo;


import cloud.xcan.sdf.model.apis.ApiStatus;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@ApiModel
@Accessors(chain = true)
public class ServiceVo {

  private Long id;

  private Long projectId;

  private String name;

  private Boolean authFlag;

  private ApiStatus status;

  private Long apisNum;

  private Long createdBy;

  private LocalDateTime createdDate;

  private Boolean hasApisFlag;

  private Long mockServiceId;

}



