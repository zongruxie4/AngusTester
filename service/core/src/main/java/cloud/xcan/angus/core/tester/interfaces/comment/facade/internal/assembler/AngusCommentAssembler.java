package cloud.xcan.angus.core.tester.interfaces.comment.facade.internal.assembler;

import static cloud.xcan.angus.core.tester.application.query.comment.impl.CommentQueryImpl.hasDeletePermission;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.pidSafe;

import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.core.tester.domain.comment.Comment;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentAddDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentDetailVo;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentTreeVo;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.TreeUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AngusCommentAssembler {

  public static Comment addDtoToDomain(AngusCommentAddDto dto) {
    return new Comment()
        .setContent(dto.getContent())
        .setTargetId(dto.getTargetId())
        .setPid(pidSafe(dto.getPid()))
        .setTargetType(dto.getTargetType().getValue())
        .setUserId(PrincipalContext.getUserId());
  }

  public static List<AngusCommentTreeVo> getTreeList(List<AngusCommentDetailVo> detailVos,
      int totalCommentNum) {
    List<AngusCommentTreeVo> vos = detailVos.stream()
        .map(comment -> new AngusCommentTreeVo()
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

  public static AngusCommentDetailVo toAngusCommentDetailVo(
      Map<Long, UserBase> userMap, Comment comment) {
    return new AngusCommentDetailVo()
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

}
