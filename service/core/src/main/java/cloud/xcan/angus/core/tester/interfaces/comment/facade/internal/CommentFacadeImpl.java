package cloud.xcan.angus.core.tester.interfaces.comment.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.comment.facade.internal.assembler.AngusCommentAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.comment.facade.internal.assembler.AngusCommentAssembler.getTreeList;
import static cloud.xcan.angus.core.tester.interfaces.comment.facade.internal.assembler.AngusCommentAssembler.toAngusCommentDetailVo;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.singleton;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.tester.application.cmd.comment.CommentCmd;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.CommentFacade;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentAddDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentFindDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentDetailVo;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentTreeVo;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CommentFacadeImpl implements CommentFacade {

  @Resource
  private CommentCmd commentCmd;

  @Resource
  private CommentQuery commentQuery;

  @Resource
  private UserManager userManager;

  @Override
  public AngusCommentDetailVo add(AngusCommentAddDto dto) {
    Comment contentComment = addDtoToDomain(dto);
    return toAngusCommentDetailVo(userManager.getUserBaseMap(singleton(getUserId())),
        commentCmd.add(contentComment));
  }

  @Override
  public void delete(Long id) {
    commentCmd.delete(id);
  }

  @Override
  public List<AngusCommentTreeVo> tree(AngusCommentFindDto dto) {
    List<Comment> comments = commentQuery.find(dto.getTargetId(), dto.getTargetType().getValue());
    if (isEmpty(comments)) {
      return null;
    }
    List<Long> userIds = comments.stream().map(Comment::getUserId).toList();
    List<AngusCommentDetailVo> detailVos = comments.stream().map(
            c -> toAngusCommentDetailVo(userManager.getUserBaseMap(userIds), c))
        .toList();
    return getTreeList(detailVos, comments.size());
  }

}
