import { ref, onMounted } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { EnumMessage, enumUtils, Percentile, ShortTimeUnit } from '@xcan-angus/infra';
import { setting } from '@/api/gm';
import { splitDuration } from '@/utils/utils';
import { useI18n } from 'vue-i18n';
import { StabilityIndicator } from '../types';

/**
 * Get default stability indicator data
 * @returns Default stability indicator data object
 */
export const getDefaultStabilityData = () => {
  return {
    threads: '0',
    cpu: '0',
    disk: '0',
    duration: '0s',
    errorRate: '0',
    memory: '0',
    network: '0',
    targetId: '0',
    percentile: 'ALL',
    art: '',
    tps: '0'
  };
};

/**
 * Composable for managing stability indicator data and operations
 * @returns Object containing reactive data and methods for stability indicators
 */
export function useStabilityData () {
  const { t } = useI18n();

  // Reactive state for stability indicator data
  const editable = ref(false);
  const visible = ref(true);

  // Edit form data
  const editInfo = ref<StabilityIndicator>({
    cpu: '',
    threads: '',
    disk: '',
    duration: '',
    errorRate: '',
    network: '',
    memory: '',
    percentile: 'ALL',
    art: '',
    tps: ''
  });

  // Display data
  const info = ref<StabilityIndicator>({
    cpu: '',
    threads: '',
    disk: '',
    duration: '',
    errorRate: '',
    network: '',
    memory: '',
    percentile: 'ALL',
    art: '',
    tps: ''
  });

  // Options for select dropdowns
  const percentileOpt = ref<EnumMessage<Percentile>[]>([]);
  const durationUnitOpt = ref<EnumMessage<ShortTimeUnit>[]>([]);

  /**
   * Load percentile options from enum utilities
   */
  const loadPercentileOpt = async () => {
    percentileOpt.value = enumUtils.enumToMessages(Percentile);
  };

  /**
   * Load duration unit options from enum utilities
   */
  const loadDurationUnitOpt = () => {
    durationUnitOpt.value = enumUtils.enumToMessages(ShortTimeUnit);
  };

  /**
   * Toggle edit mode for stability indicators
   */
  const toggleEditMode = () => {
    editable.value = !editable.value;
    visible.value = true;
    // When entering edit mode, copy current values to editInfo
    if (editable.value) {
      editInfo.value = JSON.parse(JSON.stringify(info.value));
    }
  };

  /**
   * Load stability indicator data from API
   */
  const loadStabilityInfo = async () => {
    const [error, res] = await setting.getStabilityIndicator();
    if (error) {
      return;
    }

    if (res.data) {
      // Map API response to local state
      const { threads, cpu, disk, duration, errorRate, memory, network } = res.data;
      info.value.threads = threads;
      info.value.cpu = cpu;
      info.value.errorRate = errorRate;
      info.value.disk = disk;
      info.value.memory = memory;
      info.value.network = network;
      info.value.duration = duration;
      info.value.art = res.data.art;
      info.value.tps = res.data.tps;
      info.value.percentile = res.data.percentile?.value || 'ALL';
      info.value.percentileName = res.data.percentile?.message;
    }
  };

  /**
   * Save stability indicator data to API
   */
  const saveStabilityInfo = async () => {
    const [durationValue] = splitDuration(editInfo.value.duration);

    // Validate all required fields
    if (!durationValue || !editInfo.value.threads || !editInfo.value.errorRate) {
      notification.error(t('indicator.stability.messages.allFieldsRequired'));
      return;
    }

    // Validate threads count
    if (editInfo.value.threads === '0') {
      notification.error(t('indicator.stability.messages.concurrentUsersMustBeGreaterThanZero'));
      return;
    }

    // Validate duration
    if (durationValue === '0') {
      notification.error(t('indicator.stability.messages.testDurationMustBeGreaterThanZero'));
      return;
    }

    // Validate error rate range
    if (+editInfo.value.errorRate < 0 || +editInfo.value.errorRate > 100) {
      notification.error(t('indicator.stability.messages.errorRateMustBeBetween0And100'));
      return;
    }

    // Validate CPU usage
    if (+editInfo.value.cpu > 100) {
      notification.error(t('indicator.stability.messages.cpuUsageMustBeLessThan100'));
      return;
    }

    // Validate memory usage
    if (+editInfo.value.memory > 100) {
      notification.error(t('indicator.stability.messages.memoryUsageMustBeLessThan100'));
      return;
    }

    // Validate disk usage
    if (+editInfo.value.disk > 100) {
      notification.error(t('indicator.stability.messages.diskUsageMustBeLessThan100'));
      return;
    }

    // Validate network usage
    if (+editInfo.value.network > 10000) {
      notification.error(t('indicator.stability.messages.networkUsageMustBeLessThan10000MB'));
      return;
    }

    // Remove percentileName from payload as it's only for display
    const { percentileName, ...otherInfo } = editInfo.value;

    // Save to API
    const [error] = await setting.saveStabilityIndicator({ ...otherInfo });
    if (error) {
      return;
    }

    // Exit edit mode and reload data
    editable.value = false;
    await loadStabilityInfo();
  };

  // Initialize data on component mount
  onMounted(() => {
    loadPercentileOpt();
    loadStabilityInfo();
    loadDurationUnitOpt();
  });

  return {
    // Reactive data
    editable,
    visible,
    editInfo,
    info,
    percentileOpt,
    durationUnitOpt,

    // Methods
    toggleEditMode,
    loadStabilityInfo,
    saveStabilityInfo,
    loadPercentileOpt,
    loadDurationUnitOpt
  };
}
