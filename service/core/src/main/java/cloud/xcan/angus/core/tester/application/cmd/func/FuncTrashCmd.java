package cloud.xcan.angus.core.tester.application.cmd.func;

import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrash;
import java.util.List;

public interface FuncTrashCmd {

  void add0(List<FuncTrash> trashes);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

}
