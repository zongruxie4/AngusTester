import { useI18n } from 'vue-i18n';
import { computed, onMounted, ref, watch } from 'vue';
import { isEqual } from 'lodash-es';
import { variable } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { ExtractionMethod, ExtractionSource, Encoding, ExtractionFileType } from '@xcan-angus/infra';
import { FileVariableFormState, DetailTabKey, ButtonGroupAction } from '../types';
import { VariableItem } from '../../types';

/**
 * Composable for managing file variable logic in variable detail components
 * Handles data management, API calls, and form validation for file-based variables
 */
export function useFileVariable (
  props: {
    projectId: string;
    dataSource?: VariableItem;
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

  // File configuration fields
  const filePath = ref<string>('');
  const fileType = ref<ExtractionFileType>('CSV');
  const encoding = ref<Encoding>('UTF-8');
  const rowIndex = ref<string>('0');
  const columnIndex = ref<string>('0');
  const separatorChar = ref<string>(',');
  const escapeChar = ref<string>('\\');
  const quoteChar = ref<string>('"');

  // Extraction configuration fields
  const method = ref<ExtractionMethod>(ExtractionMethod.EXACT_VALUE);
  const defaultValue = ref<string>('');
  const expression = ref<string>('');
  const matchItem = ref<string>('');

  const previewData = ref<{
    name: string;
    extraction: {
      source: ExtractionSource;
      fileType: ExtractionFileType;
      path: string;
      encoding: string;
      quoteChar: string;
      escapeChar: string;
      separatorChar: string;
      rowIndex: string;
      columnIndex: string;
      method: ExtractionMethod;
      defaultValue: string;
      expression: string;
      matchItem: string;
    };
  }>();

  // Encoding options for select input
  const encodingOptions = [
    { label: 'UTF-8', value: 'UTF-8' },
    { label: 'UTF-16', value: 'UTF-16' },
    { label: 'UTF-16BE', value: 'UTF-16BE' },
    { label: 'UTF-16LE', value: 'UTF-16LE' },
    { label: 'US-ASCII', value: 'US-ASCII' },
    { label: 'ISO-8859-1', value: 'ISO-8859-1' }
  ];

  // Input props for encoding select input
  const inputProps = {
    dataType: 'mixin-en' as const,
    excludes: '{}',
    includes: '\\!\\$%\\^&\\*_\\-\\+\\=\\.\\/'
  };

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
      !filePath.value ||
      !fileType.value ||
      !encoding.value ||
      !rowIndex.value ||
      !columnIndex.value ||
      !separatorChar.value ||
      !escapeChar.value ||
      !quoteChar.value;

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

    if (key === 'cancel') {
      emit('cancel');
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
   * @returns FileVariableFormState object
   */
  const getParams = (): FileVariableFormState => {
    const params: FileVariableFormState = {
      projectId: props.projectId,
      name: variableName.value,
      description: description.value,
      passwordValue: false,
      extraction: {
        columnIndex: columnIndex.value,
        encoding: encoding.value,
        escapeChar: escapeChar.value,
        fileType: fileType.value,
        path: filePath.value,
        quoteChar: quoteChar.value,
        rowIndex: rowIndex.value,
        separatorChar: separatorChar.value,
        source: ExtractionSource.FILE,
        method: method.value,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value
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

    // Initialize file configuration
    filePath.value = data.extraction.path;
    fileType.value = data.extraction.fileType.value;
    encoding.value = data.extraction.encoding;
    rowIndex.value = data.extraction.rowIndex;
    columnIndex.value = data.extraction.columnIndex;
    separatorChar.value = data.extraction.separatorChar;
    escapeChar.value = data.extraction.escapeChar;
    quoteChar.value = data.extraction.quoteChar;

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
        source: ExtractionSource.FILE,
        fileType: fileType.value,
        path: filePath.value,
        encoding: encoding.value,
        quoteChar: quoteChar.value,
        escapeChar: escapeChar.value,
        separatorChar: separatorChar.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
        method: method.value,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value
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

    // File configuration fields
    filePath,
    fileType,
    encoding,
    rowIndex,
    columnIndex,
    separatorChar,
    escapeChar,
    quoteChar,

    // Extraction configuration fields
    method,
    defaultValue,
    expression,
    matchItem,

    // Options and props
    encodingOptions,
    inputProps,

    // Computed properties
    variableId,
    editFlag,
    okButtonDisabled,

    // Methods
    nameChange,
    nameBlur,
    buttonGroupClick,
    initialize,
    updatePreviewData
  };
}
