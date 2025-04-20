package cloud.xcan.angus.core.tester.domain.scenario.favorite;


import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "scenario_favourite")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioFavourite extends TenantEntity<ScenarioFavourite, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "scenario_id")
  private Long scenarioId;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  @Column(name = "created_date")
  @CreatedDate
  private LocalDateTime createdDate;

  @Transient
  private String scenarioName;
  @Transient
  private String plugin;

  @Override
  public Long identity() {
    return this.id;
  }
}
