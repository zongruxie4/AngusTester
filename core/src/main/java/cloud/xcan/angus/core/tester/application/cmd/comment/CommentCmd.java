package cloud.xcan.angus.core.tester.application.cmd.comment;

import cloud.xcan.angus.core.tester.domain.comment.Comment;

public interface CommentCmd {

  Comment add(Comment comments);

  void delete(Long id);

}




