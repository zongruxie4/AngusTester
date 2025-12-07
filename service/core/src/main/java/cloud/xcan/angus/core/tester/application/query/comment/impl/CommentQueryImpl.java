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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of comment query operations for functional and general plugin use.
 *
 * <p>This class provides comprehensive functionality for querying and managing
 * comments, including detail retrieval, listing, tree summary, quota checking, and permission
 * validation.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Comment detail and list queries</li>
 *   <li>Tree summary generation for threaded comments</li>
 *   <li>Comment quota management and limit enforcement</li>
 *   <li>Delete permission validation for users and admins</li>
 *   <li>Integration with user information for enrichment</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
public class CommentQueryImpl implements CommentQuery {

  @Autowired(required = false)
  private CommentRepo commentRepo;

  @Autowired(required = false)
  private UserManager userManager;

  /**
   * Retrieves detailed comment information by ID.
   *
   * <p>This method fetches a comment by its ID, throwing a ResourceNotFound exception
   * if the comment does not exist.</p>
   *
   * @param id the comment ID to retrieve
   * @return the detailed comment information
   * @throws ResourceNotFound if the comment is not found
   */
  @Override
  public Comment detail(Long id) {
    return new BizTemplate<Comment>() {
      @Override
      protected Comment process() {
        // Retrieve comment or throw not found exception
        return commentRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "Comment"));
      }
    }.execute();
  }

  /**
   * Finds all comments for a given target, ordered by ID descending.
   *
   * <p>This method retrieves all comments associated with a specific target ID and type,
   * ordered from newest to oldest.</p>
   *
   * @param targetId   the target entity ID
   * @param targetType the target entity type
   * @return list of comments for the target
   */
  @Override
  public List<Comment> find(Long targetId, String targetType) {
    return new BizTemplate<List<Comment>>() {
      @Override
      protected List<Comment> process() {
        // Retrieve all comments for the target, ordered by ID descending
        return commentRepo.findAllByTargetIdAndTargetTypeOrderByIdDesc(targetId, targetType);
      }
    }.execute();
  }

  /**
   * Generates a tree summary of comments for a target.
   *
   * <p>This method builds a threaded comment tree summary for a given target,
   * enriching each comment with user information.</p>
   *
   * @param targetType the combined target type
   * @param targetId   the target entity ID
   * @return list of comment tree summaries, or null if no comments exist
   */
  @Override
  public List<CommentTreeSummary> treeSummary(CombinedTargetType targetType, Long targetId) {
    List<Comment> comments = find(targetId, targetType.getValue());
    if (isEmpty(comments)) {
      return null;
    }
    // Collect all user IDs for enrichment
    List<Long> userIds = comments.stream().map(Comment::getUserId).toList();
    // Enrich each comment with user information
    List<CommentSummary> contentCommentDetailVos = comments.stream().map(
            c -> toCommentSummary(userManager.getUserBaseMap(userIds), c))
        .toList();
    // Build threaded comment tree
    return CommentConverter.getTreeList(contentCommentDetailVos, comments.size());
  }

  /**
   * Gets the number of comments for a given target.
   *
   * <p>This method returns the total number of comments associated with a specific
   * target ID and type.</p>
   *
   * @param targetId   the target entity ID
   * @param targetType the target entity type
   * @return the number of comments for the target
   */
  @Override
  public int getCommentNum(Long targetId, String targetType) {
    return commentRepo.countByTargetIdAndTargetType(targetId, targetType);
  }

  /**
   * Checks and retrieves a comment by ID.
   *
   * <p>This method fetches a comment by its ID, throwing a ResourceNotFound exception
   * if the comment does not exist.</p>
   *
   * @param id the comment ID to check and retrieve
   * @return the comment if found
   * @throws ResourceNotFound if the comment is not found
   */
  @Override
  public Comment checkAndFind(Long id) {
    return commentRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Comment"));
  }

  /**
   * Checks if adding a comment would exceed the quota for a target.
   *
   * <p>This method validates that the number of comments for a target does not exceed
   * the maximum allowed. Throws a QuotaException if the limit is exceeded.</p>
   *
   * @param comment the comment to be added
   * @return the current number of comments for the target
   * @throws QuotaException if the comment quota is exceeded
   */
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

  /**
   * Checks if the current user has permission to delete a comment.
   *
   * <p>This method validates whether the current user is allowed to delete the comment,
   * throwing a BizException if not permitted.</p>
   *
   * @param commentDb the comment to check delete permission for
   * @throws BizException if the user lacks delete permission
   */
  @Override
  public void checkDeletePermission(Comment commentDb) {
    if (!hasDeletePermission(commentDb)) {
      throw BizException.of(COMMENT_NO_DELETE_PERMISSION_CODE, COMMENT_NO_DELETE_PERMISSION);
    }
  }

  /**
   * Determines if the current user has permission to delete a comment.
   *
   * <p>This method checks if the user is an AngusTester admin, tenant system admin,
   * or the original author of the comment.</p>
   *
   * @param commentDb the comment to check
   * @return true if the user has delete permission, false otherwise
   */
  public static boolean hasDeletePermission(Comment commentDb) {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN)
        || isTenantSysAdmin() || getUserId().equals(commentDb.getUserId());
  }
}
