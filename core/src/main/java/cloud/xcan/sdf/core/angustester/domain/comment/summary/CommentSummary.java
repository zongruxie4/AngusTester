package cloud.xcan.sdf.core.angustester.domain.comment.summary;


import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
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



