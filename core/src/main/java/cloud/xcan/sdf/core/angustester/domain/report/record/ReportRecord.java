package cloud.xcan.sdf.core.angustester.domain.report.record;

import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ReportContent;
import cloud.xcan.sdf.spec.experimental.EntitySupport;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "report_record")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class ReportRecord extends EntitySupport<ReportRecord, Long> {

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

  @Type(type = "json")
  private ReportContent content;

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