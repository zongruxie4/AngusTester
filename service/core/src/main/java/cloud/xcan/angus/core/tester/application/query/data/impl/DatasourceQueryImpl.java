package cloud.xcan.angus.core.tester.application.query.data.impl;


import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_DS_NAME_EXISTED_T;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cloud.xcan.angus.api.manager.UserManager;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.data.DatasourceQuery;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceRepo;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceSearchRepo;
import cloud.xcan.angus.jdbc.pool.SimpleConnectionPool;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of datasource query operations for database connection management.
 *
 * <p>This class provides functionality for querying, validating, and managing
 * datasources including connection testing and name validation.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Datasource detail retrieval and validation</li>
 *   <li>Datasource listing with search and pagination</li>
 *   <li>Name existence validation</li>
 *   <li>Database connection testing</li>
 *   <li>User information enrichment for results</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Service
@Slf4j
public class DatasourceQueryImpl implements DatasourceQuery {

  @Resource
  private DatasourceRepo mockDatasourceRepo;
  @Resource
  private DatasourceSearchRepo mockDatasourceSearchRepo;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed datasource information by ID.
   *
   * <p>This method fetches a datasource by its ID, performing validation
   * to ensure the datasource exists.</p>
   *
   * @param id the datasource ID to retrieve
   * @return the detailed datasource information
   */
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

  /**
   * Lists datasources with optional search and pagination.
   *
   * <p>This method retrieves datasources based on specification criteria,
   * supporting both full-text search and standard filtering, with user information enrichment for
   * the results.</p>
   *
   * @param spec           the specification for filtering datasources
   * @param pageable       the pagination parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the match fields for full-text search
   * @return paginated list of datasources with enriched user information
   */
  @Override
  public Page<Datasource> find(GenericSpecification<Datasource> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Datasource>>() {
      @Override
      protected Page<Datasource> process() {
        // Execute search based on search type
        Page<Datasource> page = fullTextSearch
            ? mockDatasourceSearchRepo.find(spec.getCriteria(), pageable, Datasource.class, match)
            : mockDatasourceRepo.findAll(spec, pageable);

        // Enrich results with user information if content exists
        if (page.hasContent()) {
          userManager.setUserNameAndAvatar(page.getContent(), "modifiedBy",
              "modifier", "avatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * Validates and retrieves a datasource by ID.
   *
   * <p>This method fetches a datasource by its ID, throwing a ResourceNotFound
   * exception if the datasource does not exist.</p>
   *
   * @param id the datasource ID to check and retrieve
   * @return the datasource if found
   * @throws ResourceNotFound if the datasource is not found
   */
  @Override
  public Datasource checkAndFind(Long id) {
    return mockDatasourceRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "Datasource"));
  }

  /**
   * Validates that a datasource name does not already exist.
   *
   * <p>This method checks if a datasource with the specified name already
   * exists in the system.</p>
   *
   * @param name the datasource name to check
   * @throws ResourceExisted if a datasource with the same name already exists
   */
  @Override
  public void checkNameExists(String name) {
    long count = mockDatasourceRepo.countByName(name);
    if (count > 0) {
      throw ResourceExisted.of(MOCK_DS_NAME_EXISTED_T, new Object[]{name});
    }
  }

  /**
   * Tests database connection for a datasource.
   *
   * <p>This method validates the database connection by checking the driver
   * class existence and attempting to establish a connection using the provided datasource
   * configuration.</p>
   *
   * @param datasource the datasource configuration to test
   * @throws ClassNotFoundException if the driver class is not found
   * @throws SQLException           if the connection fails or driver is invalid
   */
  @Override
  public void connDatabase(Datasource datasource) throws ClassNotFoundException, SQLException {
    // Validate driver class if specified
    if (isNotEmpty(datasource.getDriverClassName())) {
      try {
        SimpleConnectionPool.checkDriverExists(datasource.getDriverClassName());
      } catch (Exception e) {
        throw new SQLException("Driver class not found: " + e.getMessage());
      }
    }

    // Attempt to establish connection and immediately close it
    Connection conn = SimpleConnectionPool.create(datasource.getJdbcUrl(), datasource.getUsername(),
        datasource.getDecryptPassword());
    conn.close();
  }

}
