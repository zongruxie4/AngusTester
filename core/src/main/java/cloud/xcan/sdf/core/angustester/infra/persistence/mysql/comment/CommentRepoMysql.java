package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.comment;

import cloud.xcan.sdf.core.angustester.domain.comment.CommentRepo;
import org.springframework.stereotype.Repository;

@Repository("commentRepo")
public interface CommentRepoMysql extends CommentRepo {

}
