package cloud.xcan.angus.core.tester.application.cmd.comment.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.comment.CommentCmd;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.domain.comment.CommentRepo;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for managing comments on tasks and functional cases.
 * <p>
 * Provides methods for adding and deleting comments, including recursive deletion of sub-comments.
 * Handles permission checks, quota validation, activity logging, and notification events.
 */
@Biz
public class CommentCmdImpl extends CommCmd<Comment, Long> implements CommentCmd {

  @Resource
  private CommentRepo commentRepo;
  @Resource
  private CommentQuery commentQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a new comment to a task or functional case.
   * <p>
   * Validates quota, target existence, and comment hierarchy. Inserts the comment, updates comment count,
   * logs activity, and sends notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public Comment add(Comment comment) {
    return new BizTemplate<Comment>() {
      int commentNum;
      CombinedTargetType targetType;
      CombinedTarget combinedTarget;

      @Override
      protected void checkParams() {
        commentNum = commentQuery.checkQuota(comment);
        targetType = CombinedTargetType.valueOf(comment.getTargetType());
        combinedTarget = commonQuery.checkAndGetCombinedTarget(targetType, comment.getTargetId(),
            false);
      }

      @Override
      protected Comment process() {
        comment.setId(uidGenerator.getUID());
        if (comment.isRootComment()) {
          comment.setLevel(1);
        } else {
          Comment pComment = commentQuery.checkAndFind(comment.getPid());
          comment.setLevel(pComment.getLevel() + 1);
        }
        insert0(comment);

        // Calculate the number of latest comments for web
        comment.setTotalCommentNum(commentNum + 1);

        // Add activity and comment notice event
        if (targetType.isTask()) {
          Activity activity = toActivity(TASK, combinedTarget, ActivityType.COMMENT_ADD,
              comment.getContent());
          activityCmd.add(activity);
          taskQuery.assembleAndSendModifyNoticeEvent(combinedTarget.getTaskInfo(), activity);
        } else if (targetType.isFuncCase()) {
          Activity activity = toActivity(FUNC_CASE, combinedTarget, ActivityType.COMMENT_ADD,
              comment.getContent());
          activityCmd.add(activity);
          funcCaseQuery.assembleAndSendModifyNoticeEvent(combinedTarget.getCaseInfo(), activity);
        }
        return comment;
      }
    }.execute();
  }

  /**
   * Delete a comment and all its sub-comments recursively.
   * <p>
   * Validates permission, deletes the comment and its sub-comments, logs activity, and sends notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Comment commentDb;

      @Override
      protected void checkParams() {
        commentDb = commentQuery.checkAndFind(id);
        commentQuery.checkDeletePermission(commentDb);
      }

      @Override
      protected Void process() {
        commentRepo.deleteById(id);

        List<Comment> subComments = commentRepo.findAllByPid(id);
        deleteSubComment(subComments);

        // Add activity and comment notice event
        CombinedTargetType targetType = CombinedTargetType.valueOf(commentDb.getTargetType());
        try {
          CombinedTarget combinedTarget = commonQuery.checkAndGetCombinedTarget(targetType,
              commentDb.getTargetId(), false);
          if (targetType.isTask()) {
            Activity activity = toActivity(TASK, combinedTarget, ActivityType.COMMENT_DELETE,
                commentDb.getContent());
            activityCmd.add(activity);
            taskQuery.assembleAndSendModifyNoticeEvent(combinedTarget.getTaskInfo(), activity);
          } else if (targetType.isFuncCase()) {
            Activity activity = toActivity(FUNC_CASE, combinedTarget, ActivityType.COMMENT_DELETE,
                commentDb.getContent());
            activityCmd.add(activity);
            funcCaseQuery.assembleAndSendModifyNoticeEvent(combinedTarget.getCaseInfo(), activity);
          }
        } catch (Exception e) {
          // NOOP: Target resource not found
        }
        return null;
      }
    }.execute();
  }

  /**
   * Recursively delete all sub-comments for a given list of comments.
   * <p>
   * This is a private helper method used during comment deletion.
   */
  private void deleteSubComment(List<Comment> subComments) {
    commentRepo.deleteAll(subComments);
    for (Comment comment : subComments) {
      List<Comment> allByPid = commentRepo.findAllByPid(comment.getId());
      if (isNotEmpty(allByPid)) {
        deleteSubComment(allByPid);
      }
    }
  }

  /**
   * Get the repository for Comment entity.
   * <p>
   * @return the CommentRepo instance
   */
  @Override
  protected BaseRepository<Comment, Long> getRepository() {
    return this.commentRepo;
  }
}
