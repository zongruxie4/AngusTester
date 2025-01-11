package cloud.xcan.sdf.core.angustester.domain.script.tag;

import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "script_tag")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class ScriptTag extends TenantEntity<ScriptTag, Long> {

  @Id
  private Long id;

  private String name;

  @Column(name = "script_id")
  private Long scriptId;

  @CreatedDate
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  private Long createdBy;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScriptTag)) {
      return false;
    }
    ScriptTag taskTag = (ScriptTag) o;
    return Objects.equals(id, taskTag.id) &&
        Objects.equals(name, taskTag.name) &&
        Objects.equals(scriptId, taskTag.scriptId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, scriptId);
  }

}