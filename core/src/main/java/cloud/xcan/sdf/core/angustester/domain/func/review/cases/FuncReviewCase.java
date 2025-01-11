package cloud.xcan.sdf.core.angustester.domain.func.review.cases;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseDetailSummary;
import cloud.xcan.sdf.spec.experimental.EntitySupport;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "func_review_case")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewCase extends EntitySupport<FuncReviewCase, Long> {

  @Id
  private Long id;

  @Column(name = "plan_id")
  private Long planId;

  @Column(name = "review_id")
  private Long reviewId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "case_name")
  private String caseName;

  @Column(name = "case_code")
  private String caseCode;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "reviewed_case")
  private FuncCaseDetailSummary reviewedCase;

  @Column(name = "reviewer_id")
  private Long reviewerId;

  @Column(name = "review_date")
  private LocalDateTime reviewDate;

  @Column(name = "review_status")
  @Enumerated(EnumType.STRING)
  private ReviewStatus reviewStatus;

  @Column(name = "review_remark")
  private String reviewRemark;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  private Long createdBy;

  @Transient
  private FuncCaseInfo caseInfo;

  @Override
  public Long identity() {
    return id;
  }
}
