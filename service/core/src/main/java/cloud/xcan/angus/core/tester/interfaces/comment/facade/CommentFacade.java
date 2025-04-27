package cloud.xcan.angus.core.tester.interfaces.comment.facade;

import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentAddDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentFindDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentDetailVo;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentTreeVo;
import java.util.List;

public interface CommentFacade {

  AngusCommentDetailVo add(AngusCommentAddDto dto);

  void delete(Long id);

  List<AngusCommentTreeVo> tree(AngusCommentFindDto dto);
}
