package cloud.xcan.sdf.core.angustester.application.cmd.comment;

import cloud.xcan.sdf.core.angustester.domain.comment.Comment;

public interface CommentCmd {

  Comment add(Comment comments);

  void delete(Long id);

}




