import { useI18n } from 'vue-i18n';
import { computed, ref, watch, onMounted } from 'vue';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { ExtractionMethod, ExtractionSource } from '@xcan-angus/infra';
import { JdbcDatasetFormState, DataSetItem } from '../../types';

/**
 * Composable for managing JDBC dataset logic in dataset detail components
 * Handles data management, API calls, and form validation for JDBC-based datasets
 */
export function useJdbcDataset (
  props: {
    projectId: string;
    dataSource?: DataSetItem;
  },
  emit: (event: string, ...args: any[]) => void
) {
  const { t } = useI18n();

  // Reactive references for component state
  const confirmLoading = ref(false);
  const activeKey = ref<'value' | 'preview' | 'use'>('value');
  const dataSetName = ref<string>('');
  const description = ref<string>('');
  const parameters = ref<{ name: string; }[]>([]);
  const defaultParameters = ref<{ name: string; }[]>([]);
  const dbType = ref<string>();
  const jdbcUrl = ref<string>('');
  const username = ref<string>('');
  const password = ref<string>('');
  const selectSqlString = ref<string>('');
  const rowIndex = ref<string>('0');
  const columnIndex = ref<string>('0');
  const method = ref<ExtractionMethod>(ExtractionMethod.EXACT_VALUE);
  const defaultValue = ref<string>('');
  const expression = ref<string>('');
  const matchItem = ref<string>('');
  const previewData = ref<{
    id: string;
    projectId: string;
    extracted: boolean;
    name: string;
    parameters: {
      name: string;
      value: string; // 添加 value 属性
    }[];
    extraction: {
      source: ExtractionSource;
      method: ExtractionMethod;
      expression: string;
      defaultValue: string;
      matchItem: string;
      select: string;
      rowIndex: string;
      columnIndex: string;
      datasource: {
        type: string;
        username: string;
        password: string;
        jdbcUrl: string;
      };
      // 添加缺失的属性
      failureMessage: string;
      finalValue: string;
      name: string;
      value: string;
      fileType: any;
      path: string;
      encoding: any;
      quoteChar: string;
      escapeChar: string;
      separatorChar: string;
      parameterName: string;
    };
  } | undefined>(undefined);
  const modalVisible = ref(false);
  const parametersRef = ref();

  /**
   * Computed property for dataset ID
   */
  const dataSetId = computed(() => {
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
    let disabled = !dataSetName.value ||
      !dbType.value ||
      !jdbcUrl.value ||
      !username.value ||
      !password.value ||
      !selectSqlString.value ||
      !rowIndex.value ||
      !columnIndex.value ||
      !method.value;

    if (!disabled) {
      if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method.value)) {
        disabled = !expression.value;
      }
    }

    return disabled;
  });

  /**
   * Open the data source selection modal
   */
  const openDataSourceModal = () => {
    modalVisible.value = true;
  };

  /**
   * Handle selected data source from modal
   */
  const handleSelectedDataSource = (data: {
    [key: string]: any;
  }) => {
    dbType.value = data.database;
    jdbcUrl.value = data.jdbcUrl;
    username.value = data.username;
    password.value = data.password;
  };

  /**
   * Handle button group click events
   */
  const handleButtonClick = (
    key: 'ok' | 'delete' | 'export' | 'clone' | 'copyLink' | 'refresh'
  ) => {
    if (key === 'ok') {
      saveDataset();
      return;
    }

    if (key === 'delete') {
      emit('delete', dataSetId.value);
      return;
    }

    if (key === 'export') {
      emit('export', dataSetId.value);
      return;
    }

    if (key === 'clone') {
      emit('clone', dataSetId.value);
      return;
    }

    if (key === 'copyLink') {
      emit('copyLink', dataSetId.value);
      return;
    }

    if (key === 'refresh') {
      emit('refresh', dataSetId.value);
    }
  };

  /**
   * Save dataset (either create or update)
   */
  const saveDataset = async () => {
    let validFlag = true;
    if (typeof parametersRef.value?.isValid === 'function') {
      validFlag = parametersRef.value.isValid();
    }

    if (!validFlag) {
      return;
    }

    if (editFlag.value) {
      updateDataset();
      return;
    }

    createDataset();
  };

  /**
   * Update existing dataset
   */
  const updateDataset = async () => {
    const params = getDatasetParams();
    confirmLoading.value = true;
    const [error] = await dataSet.putDataSet(params);
    confirmLoading.value = false;
    if (error) {
      return;
    }

    notification.success(t('dataset.detail.jdbcDataset.notifications.updateSuccess'));
    emit('ok', params, true);
  };

  /**
   * Create new dataset
   */
  const createDataset = async () => {
    const params = getDatasetParams();
    confirmLoading.value = true;
    const [error, res] = await dataSet.addDataSet(params);
    confirmLoading.value = false;
    if (error) {
      return;
    }

    notification.success(t('dataset.detail.jdbcDataset.notifications.addSuccess'));
    const id = res?.data?.id;
    emit('ok', { ...params, id }, false);
  };

  /**
   * Handle parameter change events
   */
  const handleParametersChange = (data: { name: string; }[]) => {
    parameters.value = data;
  };

  /**
   * Get dataset parameters for API calls
   */
  const getDatasetParams = (): JdbcDatasetFormState => {
    const params: JdbcDatasetFormState = {
      projectId: props.projectId,
      name: dataSetName.value,
      description: description.value,
      parameters: parameters.value,
      extraction: {
        source: 'JDBC',
        method: method.value,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value,
        select: selectSqlString.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
        datasource: {
          type: dbType.value,
          jdbcUrl: jdbcUrl.value,
          username: username.value,
          password: password.value
        }
      }
    };

    const id = dataSetId.value;
    if (id) {
      params.id = id;
    }

    return params;
  };

  /**
   * Initialize component with data from props
   */
  const initialize = () => {
    const data = props.dataSource;
    if (!data) {
      return;
    }

    const { extraction } = data || {};
    dataSetName.value = data.name;
    description.value = data.description;
    parameters.value = data.parameters || [];
    defaultParameters.value = cloneDeep(data.parameters);

    const datasource = extraction.datasource;
    dbType.value = datasource.type?.value;
    jdbcUrl.value = datasource.jdbcUrl;
    username.value = datasource.username;
    password.value = datasource.password;

    selectSqlString.value = extraction.select;
    rowIndex.value = extraction.rowIndex;
    columnIndex.value = extraction.columnIndex;

    defaultValue.value = extraction.defaultValue;
    expression.value = extraction.expression;
    matchItem.value = extraction.matchItem;
  };

  /**
   * Update preview data when active tab changes to preview
   */
  const updatePreviewData = () => {
    if (activeKey.value !== 'preview') {
      return;
    }

    // 如果没有数据源 ID，则不设置 previewData
    if (!props.dataSource?.id) {
      previewData.value = undefined;
      return;
    }

    const newData = {
      id: props.dataSource.id,
      projectId: props.projectId,
      extracted: false,
      name: dataSetName.value,
      parameters: parameters.value.map(param => ({
        name: param.name,
        value: '' // 添加 value 属性，默认为空字符串
      })),
      extraction: {
        source: 'JDBC' as ExtractionSource,
        method: method.value,
        expression: expression.value,
        defaultValue: defaultValue.value,
        matchItem: matchItem.value,
        select: selectSqlString.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
        datasource: {
          type: dbType.value || '',
          username: username.value,
          password: password.value,
          jdbcUrl: jdbcUrl.value
        },
        // 添加缺失的属性，使用默认值
        failureMessage: '',
        finalValue: '',
        name: '',
        value: '',
        fileType: undefined,
        path: '',
        encoding: undefined,
        quoteChar: '',
        escapeChar: '',
        separatorChar: '',
        parameterName: ''
      }
    };

    if (!isEqual(newData, previewData.value)) {
      previewData.value = newData;
    }
  };

  // Watch for changes in data source and reinitialize
  onMounted(() => {
    watch(() => props.dataSource, (newValue) => {
      if (!newValue) {
        return;
      }

      initialize();
    }, { immediate: true });

    watch(() => activeKey.value, () => {
      updatePreviewData();
    }, { immediate: true });
  });

  return {
    // State
    confirmLoading,
    activeKey,
    dataSetName,
    description,
    parameters,
    defaultParameters,
    dbType,
    jdbcUrl,
    username,
    password,
    selectSqlString,
    rowIndex,
    columnIndex,
    method,
    defaultValue,
    expression,
    matchItem,
    previewData,
    modalVisible,
    parametersRef,

    // Computed properties
    dataSetId,
    editFlag,
    okButtonDisabled,

    // Methods
    openDataSourceModal,
    handleSelectedDataSource,
    handleButtonClick,
    saveDataset,
    handleParametersChange,
    initialize,
    updatePreviewData
  };
}
