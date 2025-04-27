package cloud.xcan.angus.core.tester.domain.comment;


import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.experimental.EntitySupport;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "comment")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class Comment extends EntitySupport<Comment, Long> {

  @Id
  private Long id;

  @Column(name = "pid")
  private Long pid;

  @Column(name = "level")
  private Integer level;

  @Column(name = "content")
  private String content;

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "target_type")
  //@Enumerated(EnumType.STRING)
  private String targetType;

  @Column(name = "user_id")
  private Long userId;

  @CreatedDate
  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @Transient
  private long totalCommentNum;

  public boolean isRootComment() {
    return nonNull(pid) && pid.equals(DEFAULT_ROOT_PID);
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
