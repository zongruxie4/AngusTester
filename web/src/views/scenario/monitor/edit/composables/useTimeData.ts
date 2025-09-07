import { computed, ref } from 'vue';
import { CreatedAt, DayOfWeek, EnumMessage, enumUtils, PeriodicUnit } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import type { CreateTimeSetting, DayOfMonthOption, EnumFieldNames, OptionItem } from '../types';

/**
 * Composable for managing time-related data and state
 */
export function useTimeData () {
  // Reactive data state
  const createTimeSettingData = ref<CreateTimeSetting>({ createdAt: 'NOW' as any });
  const timeOfDay = ref<dayjs.Dayjs>();
  const createdAtSomeDate = ref<dayjs.Dayjs>();
  const isValid = ref(false);

  // Enum options
  const createdAtAllOpt = ref<EnumMessage<CreatedAt>[]>([]);
  const createdAtOpt = ref<EnumMessage<CreatedAt>[]>([]);
  const periodicCreationUnitOpt = ref<EnumMessage<PeriodicUnit>[]>([]);
  const dayOfWeekOpt = ref<EnumMessage<DayOfWeek>[]>([]);

  // Day of month options (1-31)
  const dayOfMonthOpt = ref<DayOfMonthOption[]>(
    Array.from(new Array(31)).map((_, idx) => ({
      message: (idx + 1).toString(),
      value: (idx + 1).toString()
    }))
  );

  // Minutes options (1-59)
  const minutesOpt = ref<OptionItem[]>(
    Array.from(new Array(59)).map((_, idx) => ({
      label: (idx + 1).toString(),
      value: (idx + 1).toString()
    }))
  );

  // Hours options (1-23)
  const hoursOpt = ref<OptionItem[]>(
    Array.from(new Array(23)).map((_, idx) => ({
      label: (idx + 1).toString(),
      value: (idx + 1).toString()
    }))
  );

  // Enum field names configuration
  const enumFieldNames: EnumFieldNames = {
    label: 'message',
    value: 'value'
  };

  /**
   * Load enum data from infrastructure
   */
  const loadEnum = async (): Promise<void> => {
    createdAtAllOpt.value = enumUtils.enumToMessages(CreatedAt);
    periodicCreationUnitOpt.value = enumUtils.enumToMessages(PeriodicUnit);
    dayOfWeekOpt.value = enumUtils.enumToMessages(DayOfWeek);
  };

  /**
   * Update time of day in the setting data
   */
  const updateTimeOfDay = (): void => {
    if (timeOfDay.value) {
      createTimeSettingData.value.timeOfDay = timeOfDay.value.format('HH:mm:ss');
    }
  };

  /**
   * Update created at some date in the setting data
   */
  const updateCreatedAtSomeDate = (): void => {
    if (createdAtSomeDate.value) {
      createTimeSettingData.value.createdAtSomeDate = createdAtSomeDate.value.format('YYYY-MM-DD HH:mm:ss');
    }
  };

  /**
   * Initialize time values with default settings
   */
  const initializeTimeValues = (): void => {
    // Initialize time of day
    if (createTimeSettingData.value.timeOfDay) {
      const [hour, minute, second] = createTimeSettingData.value.timeOfDay.split(':');
      timeOfDay.value = dayjs()
        .set('hour', +hour)
        .set('minute', +minute)
        .set('second', +second);
    } else {
      timeOfDay.value = dayjs().set('hour', 7).set('minute', 0).set('second', 0);
      updateTimeOfDay();
    }

    // Initialize created at some date
    if (createTimeSettingData.value.createdAtSomeDate) {
      createdAtSomeDate.value = dayjs(createTimeSettingData.value.createdAtSomeDate);
    } else {
      createdAtSomeDate.value = dayjs().add(1, 'hours');
      updateCreatedAtSomeDate();
    }
  };

  /**
   * Set default values for periodic creation settings
   */
  const setDefaultPeriodicValues = (): void => {
    if (!createTimeSettingData.value.hourOfDay) {
      createTimeSettingData.value.hourOfDay = '1';
    }
    if (!createTimeSettingData.value.minuteOfHour) {
      createTimeSettingData.value.minuteOfHour = '1';
    }
  };

  /**
   * Validate if the selected date is in the past
   */
  const isDateInPast = computed((): boolean => {
    if (!isValid.value || !createdAtSomeDate.value) {
      return false;
    }
    return dayjs().isAfter(createdAtSomeDate.value);
  });

  return {
    // State
    createTimeSettingData,
    timeOfDay,
    createdAtSomeDate,
    isValid,

    // Options
    createdAtAllOpt,
    createdAtOpt,
    periodicCreationUnitOpt,
    dayOfWeekOpt,
    dayOfMonthOpt,
    minutesOpt,
    hoursOpt,
    enumFieldNames,

    // Methods
    loadEnum,
    updateTimeOfDay,
    updateCreatedAtSomeDate,
    initializeTimeValues,
    setDefaultPeriodicValues,
    isDateInPast
  };
}
