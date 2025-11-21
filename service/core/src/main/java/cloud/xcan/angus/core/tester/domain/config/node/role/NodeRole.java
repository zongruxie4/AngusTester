package cloud.xcan.angus.core.tester.domain.config.node.role;


import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Entity
@Table(name = "node_role")
@Setter
@Getter
@Accessors(chain = true)
public class NodeRole extends EntitySupport<NodeRole, Long> {

  @Id
  private Long id;

  @Column(name = "node_id")
  private Long nodeId;

  @Column(name = "role")
  private String role;

  @Override
  public Long identity() {
    return this.id;
  }
}
