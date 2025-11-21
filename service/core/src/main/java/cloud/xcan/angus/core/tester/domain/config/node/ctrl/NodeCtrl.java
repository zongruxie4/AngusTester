package cloud.xcan.angus.core.tester.domain.config.node.ctrl;

import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@DoInFuture("When there are more than 3 controller nodes, it is necessary to reduce the number of broadcast romote calls")
@Entity
@Table(name = "node_ctrl")
@Setter
@Getter
@Accessors(chain = true)
public class NodeCtrl extends EntitySupport<NodeCtrl, Long> {

  @Id
  @Column(name = "id")
  private Long id;

  /**
   * Node Id, Equal to AngusTester node ID
   */
  @Column(name = "node_id")
  private Long nodeId;

  @Column(name = "ctrl_instance_ip")
  private String ctrlInstanceIp;

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
  @Column(name = "created_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @Override
  public Long identity() {
    return this.id;
  }

}
