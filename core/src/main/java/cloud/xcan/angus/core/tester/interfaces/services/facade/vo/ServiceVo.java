package cloud.xcan.angus.core.tester.interfaces.services.facade.vo;


import cloud.xcan.angus.model.apis.ApiStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter

@Accessors(chain = true)
public class ServiceVo {

  private Long id;

  private Long projectId;

  private String name;

  private Boolean auth;

  private ApiStatus status;

  private Long apisNum;

  private Long createdBy;

  private LocalDateTime createdDate;

  private Boolean hasApis;

  private Long mockServiceId;

}



