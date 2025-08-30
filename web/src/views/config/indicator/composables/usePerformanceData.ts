import { ref, onMounted } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { EnumMessage, enumUtils, Percentile } from '@xcan-angus/infra';
import { setting } from '@/api/gm';
import { splitDuration } from '@/utils/utils';
import { useI18n } from 'vue-i18n';
import { PerformanceIndicator } from '../types';

/**
 * Get default performance indicator data
 * @returns Default performance indicator data object
 */
export const getDefaultPerfData = () => {
  return {
    art: '0',
    threads: '0',
    errorRate: '0',
    tps: '0',
    percentile: 'ALL',
    targetId: '0',
    duration: '0s',
    rampUpInterval: '0min'
  };
};

/**
 * Composable for managing performance indicator data and operations
 * @returns Object containing reactive data and methods for performance indicators
 */
export function usePerformanceData () {
  const { t } = useI18n();

  // Reactive state for performance indicator data
  const editable = ref(false);
  const editInfo = ref<PerformanceIndicator>({
    art: '',
    threads: '',
    errorRate: '',
    tps: '',
    percentile: '',
    duration: '',
    rampUpThreads: undefined,
    rampUpInterval: '1min'
  });

  const info = ref<PerformanceIndicator>({
    art: '',
    threads: '',
    errorRate: '',
    tps: '',
    percentile: '',
    duration: '',
    rampUpThreads: undefined,
    rampUpInterval: '1min'
  });

  // Percentile options for select dropdown
  const percentileOpt = ref<EnumMessage<Percentile>[]>([]);

  /**
   * Load percentile options from enum utilities
   */
  const loadPercentileOpt = () => {
    const data = enumUtils.enumToMessages(Percentile);
    percentileOpt.value = data || [];
  };

  /**
   * Toggle edit mode for performance indicators
   */
  const toggleEditMode = () => {
    editable.value = !editable.value;
    // When entering edit mode, copy current values to editInfo
    if (editable.value) {
      editInfo.value = JSON.parse(JSON.stringify(info.value));
    }
  };

  /**
   * Load performance indicator data from API
   */
  const loadPerformanceInfo = async () => {
    const [error, res] = await setting.getPerfIndicator();
    if (error) {
      return;
    }

    if (res.data) {
      // Map API response to local state
      info.value.art = res.data.art;
      info.value.threads = res.data.threads;
      info.value.errorRate = res.data.errorRate;
      info.value.tps = res.data.tps;
      info.value.percentile = res.data.percentile?.value || '';
      info.value.percentileName = res.data.percentile?.message;
      info.value.duration = res.data.duration;
      info.value.rampUpThreads = res.data.rampUpThreads;
      info.value.rampUpInterval = res.data.rampUpInterval;
    }
  };

  /**
   * Validate duration input when user leaves the field
   */
  const validateDuration = () => {
    const [value, unit] = splitDuration(editInfo.value.duration);
    // Ensure duration has a minimum value of 1
    if (!value || value === '0') {
      editInfo.value.duration = 1 + unit;
    }

    // Ensure rampUpInterval is not greater than duration
    const max = splitDuration(editInfo.value.duration)[0] >= splitDuration(editInfo.value.rampUpInterval)[0]
      ? editInfo.value.duration
      : editInfo.value.rampUpInterval;

    if (max === editInfo.value.rampUpInterval) {
      editInfo.value.rampUpInterval = editInfo.value.duration;
    }
  };

  /**
   * Validate ramp up interval input when user leaves the field
   */
  const validateRampUpInterval = () => {
    const [value] = splitDuration(editInfo.value.rampUpInterval);
    // Ensure rampUpInterval has a valid value
    if (!value) {
      editInfo.value.rampUpInterval = '0min';
    }

    // Ensure rampUpInterval is not greater than duration
    const max = splitDuration(editInfo.value.duration)[0] >= splitDuration(editInfo.value.rampUpInterval)[0]
      ? editInfo.value.duration
      : editInfo.value.rampUpInterval;

    if (max === editInfo.value.rampUpInterval) {
      editInfo.value.rampUpInterval = editInfo.value.duration;
    }
  };

  /**
   * Save performance indicator data to API
   */
  const savePerformanceInfo = async () => {
    const [durationValue] = splitDuration(editInfo.value.duration);

    // Validate all required fields
    if (!durationValue || !editInfo.value.art || !editInfo.value.threads ||
        !editInfo.value.errorRate || !editInfo.value.tps) {
      notification.error(t('indicator.performance.messages.allFieldsRequired'));
      return;
    }

    // Validate threads count
    if (editInfo.value.threads === '0') {
      notification.error(t('indicator.performance.messages.concurrentUsersMustBeGreaterThanZero'));
      return;
    }

    // Validate duration
    if (durationValue === '0') {
      notification.error(t('indicator.performance.messages.testDurationMustBeGreaterThanZero'));
      return;
    }

    // Validate response time
    if (editInfo.value.art === '0') {
      notification.error(t('indicator.performance.messages.responseTimeMustBeGreaterThanZero'));
      return;
    }

    // Validate TPS
    if (editInfo.value.tps === '0') {
      notification.error(t('indicator.performance.messages.tpsMustBeGreaterThanZero'));
      return;
    }

    // Validate error rate range
    if (+editInfo.value.errorRate < 0 || +editInfo.value.errorRate > 100) {
      notification.error(t('indicator.performance.messages.errorRateMustBeBetween0And100'));
      return;
    }

    // Remove percentileName from payload as it's only for display
    const { percentileName, ...otherInfo } = editInfo.value;

    // Save to API
    const [error] = await setting.savePerIndicator({ ...otherInfo });
    if (error) {
      return;
    }

    // Exit edit mode and reload data
    editable.value = false;
    await loadPerformanceInfo();
  };

  // Initialize data on component mount
  onMounted(() => {
    loadPerformanceInfo();
    loadPercentileOpt();
  });

  return {
    // Reactive data
    editable,
    editInfo,
    info,
    percentileOpt,

    // Methods
    toggleEditMode,
    loadPerformanceInfo,
    validateDuration,
    validateRampUpInterval,
    savePerformanceInfo,
    loadPercentileOpt
  };
}
