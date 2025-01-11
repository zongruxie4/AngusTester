package cloud.xcan.sdf.core.angustester.application.query.comment.impl;


import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.COMMENT_NO_DELETE_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.COMMENT_NO_DELETE_PERMISSION_CODE;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.COMMENT_TARGET_NUM_OVER_LIMIT_CODE;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.COMMENT_TARGET_NUM_OVER_LIMIT_T;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_COMMENT_TARGET_NUM;
import static cloud.xcan.sdf.core.angustester.application.converter.CommentConverter.toCommentSummary;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.hasPolicy;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isTenantSysAdmin;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.converter.CommentConverter;
import cloud.xcan.sdf.core.angustester.application.query.comment.CommentQuery;
import cloud.xcan.sdf.core.angustester.domain.comment.Comment;
import cloud.xcan.sdf.core.angustester.domain.comment.CommentRepo;
import cloud.xcan.sdf.core.angustester.domain.comment.summary.CommentSummary;
import cloud.xcan.sdf.core.angustester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.biz.exception.QuotaException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
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
      protected void checkParams() {
        // NOOP
      }

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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<Comment> process() {
        return commentRepo.findAllByTargetIdAndTargetTypeOrderByIdDesc(targetId, targetType);
      }
    }.execute();
  }

  @Override
  public List<CommentTreeSummary> treeSummary(CombinedTargetType targetType, Long targetId) {
    List<Comment> comments = find(targetId, targetType.getValue());
    if (CollectionUtils.isEmpty(comments)) {
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
