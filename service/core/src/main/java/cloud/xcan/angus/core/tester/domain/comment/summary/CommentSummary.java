package cloud.xcan.angus.core.tester.domain.comment.summary;


import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class CommentSummary {

  private Long id;

  private Long pid;

  private String content;

  private Long userId;

  private String userName;

  private long totalCommentNum;

  private String avatar;

  private LocalDateTime createdDate;

  private boolean allowDeleted;
}



