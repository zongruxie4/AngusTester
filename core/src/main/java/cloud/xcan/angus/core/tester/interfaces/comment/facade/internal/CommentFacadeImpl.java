package cloud.xcan.angus.core.tester.interfaces.comment.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.comment.facade.internal.assembler.AngusCommentAssembler.toAngusCommentDetailVo;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.tester.application.cmd.comment.CommentCmd;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.CommentFacade;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentAddDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentFindDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.internal.assembler.AngusCommentAssembler;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentDetailVo;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentTreeVo;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
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
    Comment contentComment = AngusCommentAssembler.addDtoToDomain(dto);
    return AngusCommentAssembler.toAngusCommentDetailVo(userManager.getUserBaseMap(
        Collections.singleton(getUserId())), commentCmd.add(contentComment));
  }

  @Override
  public void delete(Long id) {
    commentCmd.delete(id);
  }

  @Override
  public List<AngusCommentTreeVo> tree(AngusCommentFindDto dto) {
    List<Comment> comments = commentQuery.find(
        dto.getTargetId(), dto.getTargetType().getValue());
    if (CollectionUtils.isEmpty(comments)) {
      return null;
    }
    List<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toList());
    List<AngusCommentDetailVo> contentCommentDetailVos = comments.stream().map(
            c -> toAngusCommentDetailVo(userManager.getUserBaseMap(userIds), c))
        .collect(Collectors.toList());
    return AngusCommentAssembler.getTreeList(contentCommentDetailVos, comments.size());
  }

}
