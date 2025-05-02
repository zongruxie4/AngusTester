package cloud.xcan.angus.core.tester.domain.exec.node;

import cloud.xcan.angus.spec.annotations.Nullable;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The scheduling failure node is equal to the election success node minus the execution
 * node.
 */
@Entity
@Table(name = "exec_node")
@Setter
@Getter
@Accessors(chain = true)
public class ExecNode extends EntitySupport<ExecNode, Long> {

  @Id
  private Long id;

  @Nullable
  @Column(name = "exec_id")
  private Long execId;

  @Column(name = "node_id")
  private Long nodeId;

  @Override
  public Long identity() {
    return id;
  }

}
