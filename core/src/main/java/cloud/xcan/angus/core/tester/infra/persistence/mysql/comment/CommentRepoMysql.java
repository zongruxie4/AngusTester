package cloud.xcan.angus.core.tester.infra.persistence.mysql.comment;

import cloud.xcan.angus.core.tester.domain.comment.CommentRepo;
import org.springframework.stereotype.Repository;

@Repository("commentRepo")
public interface CommentRepoMysql extends CommentRepo {

}
