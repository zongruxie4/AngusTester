import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { datasource } from '@/api/tester';
import { DataSourceFormState, DataSourceDetail, DatabaseConfig } from '../types';

/**
 * <p>Composable for managing Add/Edit modal functionality</p>
 * <p>Handles form state, validation, and database configuration logic</p>
 */
export function useAddModal (projectId: string) {
  const { t } = useI18n();

  // Form state
  const loading = ref(false);
  const formRef = ref();
  const formState = ref<DataSourceFormState>({
    name: '',
    database: undefined as any,
    driverClassName: '',
    jdbcUrl: '',
    password: '',
    username: ''
  });

  /**
   * <p>Database configuration templates for different database types</p>
   * <p>Provides default driver class names and JDBC URLs for each database</p>
   */
  const databaseConfigs: Record<string, DatabaseConfig> = {
    H2: {
      driverClassName: 'org.h2.Driver',
      jdbcUrl: 'jdbc:h2:~/sample_db'
    },
    HSQLDB: {
      driverClassName: 'org.hsqldb.jdbc.JDBCDriver',
      jdbcUrl: 'jdbc:hsqldb:mem:sample_db'
    },
    SQLITE: {
      driverClassName: 'org.sqlite.JDBC',
      jdbcUrl: 'jdbc:sqlite:sample_db'
    },
    POSTGRES: {
      driverClassName: 'org.postgresql.Driver',
      jdbcUrl: 'jdbc:postgresql://127.0.0.1:5432/sample_db'
    },
    MARIADB: {
      driverClassName: 'org.mariadb.jdbc.Driver',
      jdbcUrl: 'jdbc:mariadb://127.0.0.1:3306/sample_db'
    },
    MYSQL: {
      driverClassName: 'com.mysql.cj.jdbc.Driver',
      jdbcUrl: 'jdbc:mysql://127.0.0.1:3306/sample_db'
    },
    ORACLE: {
      driverClassName: 'oracle.jdbc.driver.OracleDriver',
      jdbcUrl: 'jdbc:oracle:thin://127.0.0.1:1521:sample_db'
    },
    SQLSERVER: {
      driverClassName: 'com.microsoft.sqlserver.jdbc.SQLServerDriver',
      jdbcUrl: 'jdbc:sqlserver://127.0.0.1:1433;databaseName=sample_db'
    },
    DB2: {
      driverClassName: 'com.ibm.db2.jcc.DB2Driver',
      jdbcUrl: 'jdbc:db2://127.0.0.1:50000/sample_db'
    }
  };

  /**
   * <p>Initialize form state with edit data</p>
   * <p>Populates form fields when editing existing data source</p>
   */
  const initializeFormWithEditData = (editData: DataSourceDetail): void => {
    formState.value = {
      name: editData.name,
      database: editData.database,
      driverClassName: editData.driverClassName,
      jdbcUrl: editData.jdbcUrl,
      username: editData.username,
      password: editData.password
    };
  };

  /**
   * <p>Reset form state to default values</p>
   * <p>Clears all form fields for creating new data source</p>
   */
  const resetFormState = (): void => {
    formState.value = {
      name: '',
      database: undefined as any,
      driverClassName: '',
      jdbcUrl: '',
      password: '',
      username: ''
    };
  };

  /**
   * <p>Handle database type change</p>
   * <p>Updates driver class name and JDBC URL based on selected database type</p>
   */
  const handleDatabaseChange = (databaseType: string): void => {
    const config = databaseConfigs[databaseType];
    if (config) {
      formState.value.driverClassName = config.driverClassName;
      formState.value.jdbcUrl = config.jdbcUrl;

      // Clear validation errors
      if (formRef.value) {
        formRef.value.clearValidate();
      }
    }
  };

  /**
   * <p>Validate form and submit</p>
   * <p>Performs form validation and calls appropriate submit method</p>
   */
  const handleFormSubmit = async (): Promise<boolean> => {
    if (!formRef.value) {
      return false;
    }

    try {
      await formRef.value.validate();

      if (formState.value.id) {
        return await updateDataSource();
      } else {
        return await createDataSource();
      }
    } catch (error) {
      // Form validation failed
      console.error('Form validation error:', error);
      return false;
    }
  };

  /**
   * <p>Create new data source</p>
   * <p>Sends API request to create new data source</p>
   */
  const createDataSource = async (): Promise<boolean> => {
    if (loading.value) {
      return false;
    }

    loading.value = true;

    try {
      const [error] = await datasource.addDataSource({
        ...formState.value,
        projectId
      });

      if (error) {
        return false;
      }

      notification.success(t('actions.tips.addSuccess'));
      return true;
    } finally {
      loading.value = false;
    }
  };

  /**
   * <p>Update existing data source</p>
   * <p>Sends API request to update existing data source</p>
   */
  const updateDataSource = async (): Promise<boolean> => {
    if (loading.value) {
      return false;
    }

    const params: DataSourceFormState = {
      id: formState.value.id,
      name: formState.value.name,
      database: formState.value.database,
      driverClassName: formState.value.driverClassName,
      jdbcUrl: formState.value.jdbcUrl,
      password: formState.value.password,
      projectId
    };

    loading.value = true;

    try {
      const [error] = await datasource.putDataSource(params);
      if (error) {
        return false;
      }

      notification.success(t('actions.tips.modifySuccess'));
      return true;
    } finally {
      loading.value = false;
    }
  };

  return {
    // State
    loading,
    formRef,
    formState,

    // Methods
    initializeFormWithEditData,
    resetFormState,
    handleDatabaseChange,
    handleFormSubmit,
    createDataSource,
    updateDataSource
  };
}
