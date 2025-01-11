package cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.domain;


import cloud.xcan.sdf.api.enums.NormalStatus;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainDetailVo {

  private Long id;

  private String name;

  private NormalStatus status;

  private Integer dnsNum;

  private Long createdBy;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

}



