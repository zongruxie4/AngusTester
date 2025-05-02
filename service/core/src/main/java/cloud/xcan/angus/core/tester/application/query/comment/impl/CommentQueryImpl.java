package cloud.xcan.angus.core.tester.application.query.comment.impl;


import static cloud.xcan.angus.api.commonlink.TesterApisMessage.COMMENT_NO_DELETE_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.COMMENT_NO_DELETE_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.COMMENT_TARGET_NUM_OVER_LIMIT_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.COMMENT_TARGET_NUM_OVER_LIMIT_T;
import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_COMMENT_TARGET_NUM;
import static cloud.xcan.angus.core.tester.application.converter.CommentConverter.toCommentSummary;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.tester.application.converter.CommentConverter;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.domain.comment.CommentRepo;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentSummary;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reuse for functional testing plugin.
 */
@Biz
public class CommentQueryImpl implements CommentQuery {

  @Autowired(required = false)
  private CommentRepo commentRepo;

  @Autowired(required = false)
  private UserManager userManager;

  @Override
  public Comment detail(Long id) {
    return new BizTemplate<Comment>() {

      @Override
      protected Comment process() {
        return commentRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "Comment"));
      }
    }.execute();
  }

  @Override
  public List<Comment> find(Long targetId, String targetType) {
    return new BizTemplate<List<Comment>>() {

      @Override
      protected List<Comment> process() {
        return commentRepo.findAllByTargetIdAndTargetTypeOrderByIdDesc(targetId, targetType);
      }
    }.execute();
  }

  @Override
  public List<CommentTreeSummary> treeSummary(CombinedTargetType targetType, Long targetId) {
    List<Comment> comments = find(targetId, targetType.getValue());
    if (isEmpty(comments)) {
      return null;
    }
    List<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toList());
    List<CommentSummary> contentCommentDetailVos = comments.stream().map(
            c -> toCommentSummary(userManager.getUserBaseMap(userIds), c))
        .collect(Collectors.toList());
    return CommentConverter.getTreeList(contentCommentDetailVos, comments.size());
  }

  @Override
  public int getCommentNum(Long targetId, String targetType) {
    return commentRepo.countByTargetIdAndTargetType(targetId, targetType);
  }

  @Override
  public Comment checkAndFind(Long id) {
    return commentRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Comment"));
  }

  @Override
  public int checkQuota(Comment comment) {
    int count = commentRepo.countByTargetIdAndTargetType(comment.getTargetId(),
        comment.getTargetType());
    if (count > MAX_COMMENT_TARGET_NUM) {
      throw QuotaException.of(COMMENT_TARGET_NUM_OVER_LIMIT_CODE, COMMENT_TARGET_NUM_OVER_LIMIT_T,
          new Object[]{MAX_COMMENT_TARGET_NUM});
    }
    return count;
  }

  @Override
  public void checkDeletePermission(Comment commentDb) {
    if (!hasDeletePermission(commentDb)) {
      throw BizException.of(COMMENT_NO_DELETE_PERMISSION_CODE, COMMENT_NO_DELETE_PERMISSION);
    }
  }

  public static boolean hasDeletePermission(Comment commentDb) {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN)
        || isTenantSysAdmin() || getUserId().equals(commentDb.getUserId());
  }
}
