package cloud.xcan.angus.core.tester.domain.node.info;

import cloud.xcan.angus.api.pojo.auth.AgentAuth;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "node_info")
@Setter
@Getter
@Accessors(chain = true)
public class NodeInfo extends TenantEntity<NodeInfo, Long> {

  /**
   * Node Id, Equal to AngusTester node ID
   */
  @Id
  private Long id;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Info info;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Os os;

  @Column(name = "agent_installed")
  private Boolean agentInstalled;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "agent_auth")
  private AgentAuth agentAuth;

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
  @Column(name = "last_modified_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime lastModifiedDate;

  @Transient
  private Boolean agentOnline;
  @Transient
  private String selectFailureReason;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeInfo nodeInfo = (NodeInfo) o;
    return Objects.equals(id, nodeInfo.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
