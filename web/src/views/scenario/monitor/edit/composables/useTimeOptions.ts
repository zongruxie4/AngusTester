import { Ref, watch } from 'vue';
import type { CreateTimeSetting } from '../../types';
import { CreatedAt, EnumMessage } from '@xcan-angus/infra';

/**
 * Composable for managing time option visibility and filtering
 */
export function useTimeOptions (
  showPeriodically: Ref<boolean>,
  createdAtAllOpt: Ref<EnumMessage<CreatedAt>[]>,
  createdAtOpt: Ref<EnumMessage<CreatedAt>[]>,
  createTimeSettingData: Ref<CreateTimeSetting>
) {
  /**
   * Watch for changes in periodically visibility and update options accordingly
   */
  const watchPeriodicallyOption = () => {
    watch(
      [() => showPeriodically.value, () => createdAtAllOpt.value],
      ([newShowPeriodically]) => {
        if (newShowPeriodically) {
          // Show all options when periodically is enabled
          createdAtOpt.value = createdAtAllOpt.value;
        } else {
          // Filter out PERIODICALLY option and set default to NOW
          createTimeSettingData.value.createdAt = 'NOW';
          createdAtOpt.value = createdAtAllOpt.value.filter(
            item => item.value !== 'PERIODICALLY'
          );
        }
      },
      { immediate: true }
    );
  };

  /**
   * Check if time picker should be shown for periodic options
   */
  const shouldShowTimePicker = (periodicUnit?: string): boolean => {
    return ['WEEKLY', 'MONTHLY', 'DAILY'].includes(periodicUnit || '');
  };

  /**
   * Check if specific periodic option should be shown
   */
  const shouldShowPeriodicOption = (optionType: string, periodicUnit?: string): boolean => {
    switch (optionType) {
      case 'WEEKLY':
        return periodicUnit === 'WEEKLY';
      case 'MONTHLY':
        return periodicUnit === 'MONTHLY';
      case 'HOURLY':
        return periodicUnit === 'HOURLY';
      case 'EVERY_MINUTE':
        return periodicUnit === 'EVERY_MINUTE';
      default:
        return false;
    }
  };

  return {
    watchPeriodicallyOption,
    shouldShowTimePicker,
    shouldShowPeriodicOption
  };
}
