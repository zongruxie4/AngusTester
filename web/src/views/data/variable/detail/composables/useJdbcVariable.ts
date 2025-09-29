import { useI18n } from 'vue-i18n';
import { computed, onMounted, ref, watch } from 'vue';
import { isEqual } from 'lodash-es';
import { variable } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { ExtractionMethod, ExtractionSource } from '@xcan-angus/infra';
import { JdbcVariableFormState, DetailTabKey, ButtonGroupAction } from '../types';
import { VariableDetail } from '../../types';

/**
 * Composable for managing JDBC variable logic in variable detail components
 * Handles data management, API calls, and form validation for JDBC-based variables
 */
export function useJdbcVariable (
  props: {
    projectId: string;
    dataSource?: VariableDetail;
  },
  emit: (event: string, ...args: any[]) => void
) {
  const { t } = useI18n();

  // Reactive references for component state
  const confirmLoading = ref(false);
  const activeKey = ref<DetailTabKey>('value');

  const variableName = ref<string>('');
  const variableNameError = ref(false);
  const description = ref<string>('');

  // Database configuration fields
  const dbType = ref<string>();
  const jdbcUrl = ref<string>('');
  const username = ref<string>('');
  const password = ref<string>('');
  const selectSqlString = ref<string>('');
  const rowIndex = ref<string>('0');
  const columnIndex = ref<string>('0');

  // Extraction configuration fields
  const method = ref<ExtractionMethod>(ExtractionMethod.EXACT_VALUE);
  const defaultValue = ref<string>('');
  const expression = ref<string>('');
  const matchItem = ref<string>('');

  // Modal visibility
  const modalVisible = ref(false);

  const previewData = ref<{
    name: string;
    extraction: {
      source: ExtractionSource;
      method: ExtractionMethod;
      expression: string;
      defaultValue: string;
      matchItem: string;
      datasource: {
        type: string | undefined;
        username: string;
        password: string;
        jdbcUrl: string;
      };
      select: string;
      rowIndex: string;
      columnIndex: string;
    };
  }>();

  /**
   * Computed property for variable ID
   */
  const variableId = computed(() => {
    return props.dataSource?.id || '';
  });

  /**
   * Computed property to determine if we're in edit mode
   */
  const editFlag = computed(() => {
    return !!props.dataSource?.id;
  });

  /**
   * Computed property to determine if the OK button should be disabled
   */
  const okButtonDisabled = computed(() => {
    let disabled = !variableName.value ||
      !dbType.value ||
      !jdbcUrl.value ||
      !username.value ||
      !selectSqlString.value ||
      !rowIndex.value ||
      !columnIndex.value;

    if (!disabled) {
      if ([ExtractionMethod.JSON_PATH, ExtractionMethod.REGEX, ExtractionMethod.X_PATH].includes(method.value)) {
        disabled = !expression.value;
      }
    }

    return disabled;
  });

  /**
   * Handle variable name change event
   */
  const nameChange = () => {
    variableNameError.value = false;
  };

  /**
   * Handle variable name blur event
   */
  const nameBlur = (event: { target: { value: string; } }) => {
    const name = event.target.value;
    if (!name) {
      return;
    }

    validName(name);
  };

  /**
   * Validate variable name
   * @param name - The name to validate
   * @returns boolean indicating if name is valid
   */
  const validName = (name: string) => {
    // eslint-disable-next-line prefer-regex-literals
    const rex = new RegExp(/[^a-zA-Z0-9!$%^&*_\-+=./]/);
    if (rex.test(name)) {
      variableNameError.value = true;
      return false;
    }

    return true;
  };

  /**
   * Handle button group click events
   */
  const buttonGroupClick = (key: ButtonGroupAction) => {
    if (key === 'ok') {
      ok();
      return;
    }

    if (key === 'delete') {
      emit('delete', variableId.value);
      return;
    }

    if (key === 'export') {
      emit('export', variableId.value);
      return;
    }

    if (key === 'clone') {
      emit('clone', variableId.value);
      return;
    }

    if (key === 'copyLink') {
      emit('copyLink', variableId.value);
      return;
    }

    if (key === 'refresh') {
      emit('refresh', variableId.value);
    }
  };

  /**
   * Open data source selection modal
   */
  const toSelectDataSource = () => {
    modalVisible.value = true;
  };

  /**
   * Handle data source selection confirmation
   */
  const selectedDataSourceOk = (data: {
    database: string;
    jdbcUrl: string;
    username: string;
    password: string
  }) => {
    dbType.value = data.database;
    jdbcUrl.value = data.jdbcUrl;
    username.value = data.username;
    password.value = data.password;
  };

  /**
   * Save variable (either create or update)
   */
  const ok = async () => {
    if (!validName(variableName.value)) {
      return;
    }

    if (editFlag.value) {
      toEdit();
      return;
    }

    toCreate();
  };

  /**
   * Update existing variable
   */
  const toEdit = async () => {
    const params = getParams();
    confirmLoading.value = true;
    const [error] = await variable.putVariables(params);
    confirmLoading.value = false;
    if (error) {
      return;
    }

    notification.success(t('actions.tips.editSuccess'));
    emit('ok', params, true);
  };

  /**
   * Create new variable
   */
  const toCreate = async () => {
    const params = getParams();
    confirmLoading.value = true;
    const [error, res] = await variable.addVariables(params);
    confirmLoading.value = false;
    if (error) {
      return;
    }

    notification.success(t('actions.tips.addSuccess'));
    const id = res?.data?.id;
    emit('ok', { ...params, id }, false);
  };

  /**
   * Get parameters for API call
   * @returns JdbcVariableFormState object
   */
  const getParams = (): JdbcVariableFormState => {
    const params: JdbcVariableFormState = {
      projectId: props.projectId,
      name: variableName.value,
      description: description.value,
      passwordValue: false,
      extraction: {
        source: ExtractionSource.JDBC,
        method: method.value,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value,
        datasource: {
          type: dbType.value,
          username: username.value,
          password: password.value,
          jdbcUrl: jdbcUrl.value
        },
        select: selectSqlString.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value
      }
    };

    const id = variableId.value;
    if (id) {
      params.id = id;
    }

    return params;
  };

  /**
   * Initialize form with data from props
   */
  const initialize = () => {
    const data = props.dataSource;
    if (!data) {
      return;
    }

    variableName.value = data.name;
    description.value = data.description;

    // Initialize database configuration
    dbType.value = data.extraction.datasource.type?.value;
    jdbcUrl.value = data.extraction.datasource.jdbcUrl;
    username.value = data.extraction.datasource.username;
    password.value = data.extraction.datasource.password;
    selectSqlString.value = data.extraction.select;
    rowIndex.value = data.extraction.rowIndex;
    columnIndex.value = data.extraction.columnIndex;

    // Initialize extraction configuration
    method.value = data.extraction.method.value;
    defaultValue.value = data.extraction.defaultValue;
    expression.value = data.extraction.expression;
    matchItem.value = data.extraction.matchItem;
  };

  /**
   * Update preview data when active tab changes to preview
   */
  const updatePreviewData = () => {
    const newData = {
      name: variableName.value,
      extraction: {
        source: ExtractionSource.JDBC,
        method: method.value,
        expression: expression.value,
        defaultValue: defaultValue.value,
        matchItem: matchItem.value,
        datasource: {
          type: dbType.value,
          username: username.value,
          password: password.value,
          jdbcUrl: jdbcUrl.value
        },
        select: selectSqlString.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value
      }
    };

    if (!isEqual(newData, previewData.value)) {
      previewData.value = newData;
    }
  };

  // Initialize component when mounted
  onMounted(() => {
    watch(() => props.dataSource, (newValue) => {
      if (!newValue) {
        return;
      }

      initialize();
    }, { immediate: true });

    watch(() => activeKey.value, (newValue) => {
      if (newValue !== 'preview') {
        return;
      }

      updatePreviewData();
    }, { immediate: true });
  });

  return {
    // State
    confirmLoading,
    activeKey,
    variableName,
    variableNameError,
    description,
    previewData,
    modalVisible,

    // Database configuration fields
    dbType,
    jdbcUrl,
    username,
    password,
    selectSqlString,
    rowIndex,
    columnIndex,

    // Extraction configuration fields
    method,
    defaultValue,
    expression,
    matchItem,

    // Computed properties
    variableId,
    editFlag,
    okButtonDisabled,

    // Methods
    nameChange,
    nameBlur,
    buttonGroupClick,
    toSelectDataSource,
    selectedDataSourceOk,
    initialize,
    updatePreviewData
  };
}
