package cloud.xcan.angus.core.tester.domain.func.review.record;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "func_review_case_record")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewCaseRecord extends EntitySupport<FuncReviewCaseRecord, Long> {

  @Id
  private Long id;

  @Column(name = "plan_id")
  private Long planId;

  @Column(name = "review_case_id")
  private Long reviewCaseId;

  @Column(name = "review_id")
  private Long reviewId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "case_name")
  private String caseName;

  @Column(name = "case_code")
  private String caseCode;

  @Type(JsonType.class)
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
  private String createdByName;
  @Transient
  private String avatar;

  @Override
  public Long identity() {
    return id;
  }
}
