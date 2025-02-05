package cloud.xcan.sdf.core.angustester.application.cmd.data.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.core.angustester.application.cmd.data.DatasourceCmd;
import cloud.xcan.sdf.core.angustester.application.converter.MockDatasourceConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasourceQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.DatasourceRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Biz
@Slf4j
public class DatasourceCmdImpl extends CommCmd<Datasource, Long> implements DatasourceCmd {

  @Resource
  private DatasourceRepo datasourceRepo;

  @Resource
  private DatasourceQuery datasourceQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Datasource datasource) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the member permission
        projectMemberQuery.checkMember(datasource.getProjectId(), getUserId());

        // Check the name exists
        datasourceQuery.checkNameExists(datasource.getName());

        // Check the mock datasource quota
        commonQuery.checkTenantQuota(QuotaResource.AngusTesterMockDatasource, null,
            datasourceRepo.countAllByTenantId(getOptTenantId()) + 1);
      }

      @Override
      protected IdKey<Long, Object> process() {
        return insert(datasource);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Datasource datasource) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Datasource datasourceDb;

      @Override
      protected void checkParams() {
        if (nonNull(datasource.getId())) {
          // Check the datasource exists
          datasourceDb = datasourceQuery.checkAndFind(datasource.getId());

          // Check the database is not allowed to be modified
          ProtocolAssert.assertTrue(datasource.getDatabase().equals(datasourceDb.getDatabase()),
              "Database is not allowed to be modified");

          // Check the name exists
          if (!datasourceDb.getName().equals(datasource.getName())) {
            datasourceQuery.checkNameExists(datasource.getName());
          }
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(datasource.getId())) {
          return add(datasource);
        }

        // Save replaced datasource
        MockDatasourceConverter.setReplaceInfo(datasource, datasourceDb);
        datasourceRepo.save(datasourceDb);
        return new IdKey<Long, Object>().setId(datasourceDb.getId()).setKey(datasourceDb.getName());
      }
    }.execute();
  }

  //@Transactional(rollbackFor = Exception.class)
  @Override
  public Datasource testByConfig(Datasource datasource) {
    return new BizTemplate<Datasource>() {

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Datasource process() {
        boolean connSuccess = true;
        String failureMessage = null;
        try {
          datasourceQuery.connDatabase(datasource);
        } catch (Exception e) {
          connSuccess = false;
          failureMessage = e.getMessage();
        }
        datasource.setConnSuccess(connSuccess).setConnFailureMessage(failureMessage);
        return datasource;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public Datasource testById(Long id) {
    return new BizTemplate<Datasource>() {
      Datasource datasourceDb;

      @Override
      protected void checkParams() {
        datasourceDb = datasourceQuery.checkAndFind(id);
      }

      @Override
      protected Datasource process() {
        boolean connSuccess = true;
        String failureMessage = null;
        try {
          datasourceQuery.connDatabase(datasourceDb);
        } catch (Exception e) {
          connSuccess = false;
          failureMessage = e.getMessage();
        }
        datasourceDb.setConnSuccess(connSuccess).setConnFailureMessage(failureMessage);
        return datasourceDb;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
      }

      @Override
      protected Void process() {
        datasourceRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<Datasource, Long> getRepository() {
    return this.datasourceRepo;
  }
}

