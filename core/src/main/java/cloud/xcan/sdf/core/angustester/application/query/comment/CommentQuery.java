package cloud.xcan.sdf.core.angustester.application.query.comment;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.comment.Comment;
import cloud.xcan.sdf.core.angustester.domain.comment.summary.CommentTreeSummary;
import java.util.List;

public interface CommentQuery {

  Comment detail(Long id);

  List<Comment> find(Long targetId, String targetType);

  List<CommentTreeSummary> treeSummary(CombinedTargetType targetType, Long targetId);

  int getCommentNum(Long targetId, String targetType);

  Comment checkAndFind(Long id);

  int checkQuota(Comment contentComment);

  void checkDeletePermission(Comment commentDb);
}




