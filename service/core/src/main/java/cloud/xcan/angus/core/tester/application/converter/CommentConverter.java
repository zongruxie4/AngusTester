package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.tester.application.query.comment.impl.CommentQueryImpl.hasDeletePermission;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentSummary;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.angus.spec.utils.TreeUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentConverter {

  public static CommentSummary toCommentSummary(
      Map<Long, UserBase> userMap, Comment comment) {
    return new CommentSummary()
        .setId(comment.getId())
        .setPid(comment.getPid())
        .setContent(comment.getContent())
        .setUserId(comment.getUserId())
        .setUserName(isEmpty(userMap.get(comment.getUserId())) ? ""
            : userMap.get(comment.getUserId()).getFullName())
        .setAvatar(isEmpty(userMap.get(comment.getUserId())) ? ""
            : userMap.get(comment.getUserId()).getAvatar())
        .setCreatedDate(comment.getCreatedDate())
        .setAllowDeleted(hasDeletePermission(comment));
  }

  public static List<CommentTreeSummary> getTreeList(List<CommentSummary> detailVos,
      int totalCommentNum) {
    List<CommentTreeSummary> vos = detailVos.stream()
        .map(comment -> new CommentTreeSummary()
            .setPid(comment.getPid())
            .setContent(comment.getContent())
            .setId(comment.getId())
            .setUserId(comment.getUserId())
            .setUserName(comment.getUserName())
            .setAvatar(comment.getAvatar())
            .setCreatedDate(comment.getCreatedDate())
            .setTotalCommentNum(totalCommentNum))
        .collect(Collectors.toList());
    return TreeUtils.toTree(vos, false);  // Use database sort
  }
}
