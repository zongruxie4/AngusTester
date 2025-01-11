package cloud.xcan.sdf.core.angustester.domain.node.role;


import cloud.xcan.sdf.spec.experimental.EntitySupport;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
