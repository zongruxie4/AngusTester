package cloud.xcan.angus.core.tester.application.query.comment;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentTreeSummary;
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




