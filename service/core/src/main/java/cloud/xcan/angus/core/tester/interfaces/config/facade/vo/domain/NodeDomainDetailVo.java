package cloud.xcan.angus.core.tester.interfaces.config.facade.vo.domain;


import cloud.xcan.angus.api.enums.NormalStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


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

  private Long modifiedBy;

  private LocalDateTime modifiedDate;

}



