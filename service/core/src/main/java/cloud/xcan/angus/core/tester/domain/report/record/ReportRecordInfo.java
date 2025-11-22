package cloud.xcan.angus.core.tester.domain.report.record;

import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "report_record")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class ReportRecordInfo extends EntitySupport<ReportRecordInfo, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "report_id")
  private Long reportId;

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  private ReportCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "template")
  private ReportTemplate template;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  @Column(name = "created_date")
  @CreatedDate
  private LocalDateTime createdDate;

  @Override
  public Long identity() {
    return id;
  }
}
