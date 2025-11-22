package cloud.xcan.angus.core.tester.domain.analysis.snapshot;

import cloud.xcan.angus.core.jpa.auditor.AuditingEntity;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "analysis_snapshot")
@Setter
@Getter
@Accessors(chain = true)
public class AnalysisSnapshot extends AuditingEntity<Analysis, Long> {

  @Id
  private Long id;

  @Column(name = "analysis_id")
  private Long analysisId;

  private String data;

  @Override
  public Long identity() {
    return id;
  }
}
