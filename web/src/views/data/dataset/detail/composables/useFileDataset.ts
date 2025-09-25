import { useI18n } from 'vue-i18n';
import { computed, ref, watch, onMounted } from 'vue';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { FileDataSetFormState, DataSetItem } from '../../types';
import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

/**
 * Composable for managing file dataset logic in dataset detail components
 * Handles data management, API calls, and form validation for file-based datasets
 */
export function useFileDataset (
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
  const filePath = ref<string>('');
  const fileType = ref<'CSV' | 'EXCEL' | 'TXT'>('CSV');
  const encoding = ref<'UTF-8' | 'UTF-16' | 'UTF-16BE' | 'UTF-16LE' | 'US-ASCII' | 'ISO-8859-1'>('UTF-8');
  const rowIndex = ref<string>('0');
  const columnIndex = ref<string>('0');
  const separatorChar = ref<string>(',');
  const escapeChar = ref<string>('\\');
  const quoteChar = ref<string>('"');
  const method = ref<'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH'>('EXACT_VALUE');
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
      value: string;
    }[];
    extraction: {
      defaultValue: string;
      expression: string;
      failureMessage: string;
      finalValue: string;
      matchItem: string;
      method: ExtractionMethod;
      name: string;
      source: ExtractionSource;
      value: string;
      fileType: ExtractionFileType;
      path: string;
      encoding: Encoding;
      quoteChar: string;
      escapeChar: string;
      separatorChar: string;
      rowIndex: string;
      columnIndex: string;
      select: string;
      parameterName: string;
      datasource: {
        type: string;
        username: string;
        password: string;
        jdbcUrl: string;
      };
    };
  } | undefined>(undefined);
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
      !filePath.value ||
      !fileType.value ||
      !encoding.value ||
      !rowIndex.value ||
      !columnIndex.value ||
      !separatorChar.value ||
      !escapeChar.value ||
      !quoteChar.value;

    if (!disabled) {
      if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method.value)) {
        disabled = !expression.value;
      }
    }

    return disabled;
  });

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

    notification.success(t('actions.tips.updateSuccess'));
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

    notification.success(t('actions.tips.addSuccess'));
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
  const getDatasetParams = (): FileDataSetFormState => {
    const params: FileDataSetFormState = {
      projectId: props.projectId,
      name: dataSetName.value,
      description: description.value,
      parameters: parameters.value,
      extraction: {
        columnIndex: columnIndex.value,
        encoding: encoding.value,
        escapeChar: escapeChar.value,
        fileType: fileType.value,
        path: filePath.value,
        quoteChar: quoteChar.value,
        rowIndex: rowIndex.value,
        separatorChar: separatorChar.value,
        source: 'FILE',
        method: method.value,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value
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

    filePath.value = extraction.path;
    fileType.value = extraction.fileType?.value;
    encoding.value = extraction.encoding;
    rowIndex.value = extraction.rowIndex;
    columnIndex.value = extraction.columnIndex;
    separatorChar.value = extraction.separatorChar;
    escapeChar.value = extraction.escapeChar;
    quoteChar.value = extraction.quoteChar;

    method.value = extraction.method?.value;
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
        source: 'FILE' as ExtractionSource,
        fileType: fileType.value as ExtractionFileType,
        path: filePath.value,
        encoding: encoding.value as Encoding,
        quoteChar: quoteChar.value,
        escapeChar: escapeChar.value,
        separatorChar: separatorChar.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
        method: method.value as ExtractionMethod,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value,
        // 添加缺失的属性，使用默认值
        failureMessage: '',
        finalValue: '',
        name: '',
        value: '',
        select: '',
        parameterName: '',
        datasource: {
          type: '',
          username: '',
          password: '',
          jdbcUrl: ''
        }
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
    filePath,
    fileType,
    encoding,
    rowIndex,
    columnIndex,
    separatorChar,
    escapeChar,
    quoteChar,
    method,
    defaultValue,
    expression,
    matchItem,
    previewData,
    parametersRef,

    // Computed properties
    dataSetId,
    editFlag,
    okButtonDisabled,

    // Methods
    handleButtonClick,
    saveDataset,
    handleParametersChange,
    initialize,
    updatePreviewData
  };
}
