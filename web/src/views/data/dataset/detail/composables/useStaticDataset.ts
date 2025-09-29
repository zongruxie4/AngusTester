import { useI18n } from 'vue-i18n';
import { computed, ref, watch, onMounted } from 'vue';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { StaticDataSetFormState, DataSetDetail } from '../../types';

/**
 * Composable for managing static dataset logic in dataset detail components
 * Handles data management, API calls, and form validation for static datasets
 */
export function useStaticDataset (
  props: {
    projectId: string;
    dataSource?: DataSetDetail;
  },
  emit: (event: string, ...args: any[]) => void
) {
  const { t } = useI18n();

  // Reactive references for component state
  const confirmLoading = ref(false);
  const activeKey = ref<'value' | 'preview' | 'use'>('value');
  const dataSetName = ref<string>('');
  const description = ref<string>('');
  const parameters = ref<{ name: string; value: string; }[]>([]);
  const defaultParameters = ref<{ name: string; value: string; }[]>([]);
  const previewData = ref<{
    id: string | undefined;
    projectId: string;
    name: string;
    parameters: { name: string; value: string; }[];
  }>();
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
    return !dataSetName.value || !parameters.value?.length;
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

    notification.success(t('actions.tips.editSuccess'));
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
  const handleParametersChange = (data: { name: string; value: string; }[]) => {
    parameters.value = data;
  };

  /**
   * Get dataset parameters for API calls
   */
  const getDatasetParams = (): StaticDataSetFormState => {
    const params: StaticDataSetFormState = {
      projectId: props.projectId,
      name: dataSetName.value,
      parameters: parameters.value,
      description: description.value
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

    dataSetName.value = data.name;
    description.value = data.description;
    parameters.value = data.parameters || [];
    defaultParameters.value = cloneDeep(data.parameters) || [];
  };

  /**
   * Update preview data when active tab changes to preview
   */
  const updatePreviewData = () => {
    if (activeKey.value !== 'preview') {
      return;
    }

    const newData = {
      id: props.dataSource?.id,
      projectId: props.projectId,
      name: dataSetName.value,
      parameters: parameters.value
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
