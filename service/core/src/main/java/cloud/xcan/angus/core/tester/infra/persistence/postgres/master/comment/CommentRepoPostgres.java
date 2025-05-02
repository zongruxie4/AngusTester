package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.comment;

import cloud.xcan.angus.core.tester.domain.comment.CommentRepo;
import org.springframework.stereotype.Repository;

@Repository("commentRepo")
public interface CommentRepoPostgres extends CommentRepo {

}
