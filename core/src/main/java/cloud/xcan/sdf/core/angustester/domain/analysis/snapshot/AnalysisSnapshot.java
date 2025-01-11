package cloud.xcan.sdf.core.angustester.domain.analysis.snapshot;

import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.jpa.auditor.AuditingEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
