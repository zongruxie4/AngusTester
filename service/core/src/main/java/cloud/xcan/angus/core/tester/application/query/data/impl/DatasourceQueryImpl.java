package cloud.xcan.angus.core.tester.application.query.data.impl;


import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_DS_NAME_EXISTED_T;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.data.DatasourceQuery;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceRepo;
import cloud.xcan.angus.jdbc.pool.SimpleConnectionPool;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Biz
@Slf4j
public class DatasourceQueryImpl implements DatasourceQuery {

  @Resource
  private DatasourceRepo mockDatasourceRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Datasource detail(Long id) {
    return new BizTemplate<Datasource>() {
      Datasource datasourceDb;

      @Override
      protected void checkParams() {
        datasourceDb = checkAndFind(id);
      }

      @Override
      protected Datasource process() {
        return datasourceDb;
      }
    }.execute();
  }

  @Override
  public Page<Datasource> find(Specification<Datasource> spec, Pageable pageable) {
    return new BizTemplate<Page<Datasource>>() {
      @Override
      protected void checkParams() {
      }

      @Override
      protected Page<Datasource> process() {
        Page<Datasource> page = mockDatasourceRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          userManager.setUserNameAndAvatar(page.getContent(), "lastModifiedBy",
              "lastModifiedByName", "avatar");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public Datasource checkAndFind(Long id) {
    return mockDatasourceRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "Datasource"));
  }

  @Override
  public void checkNameExists(String name) {
    long count = mockDatasourceRepo.countByName(name);
    if (count > 0) {
      throw ResourceExisted.of(MOCK_DS_NAME_EXISTED_T, new Object[]{name});
    }
  }

  /**
   * Test database connection
   */
  @Override
  public void connDatabase(Datasource datasource) throws ClassNotFoundException, SQLException {
    if (isNotEmpty(datasource.getDriverClassName())) {
      try {
        SimpleConnectionPool.checkDriverExists(datasource.getDriverClassName());
      }catch (Exception e) {
        throw new SQLException("Driver class not found: " + e.getMessage());
      }
    }
    Connection conn = SimpleConnectionPool.create(datasource.getJdbcUrl(), datasource.getUsername(),
        datasource.getDecryptPassword());
    conn.close();
  }

}
