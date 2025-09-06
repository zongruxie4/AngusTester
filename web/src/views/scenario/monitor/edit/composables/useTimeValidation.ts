import { Ref } from 'vue';
import dayjs from 'dayjs';
import type { CreateTimeSetting, TimeValidationResult } from '../types';

/**
 * Composable for time validation logic
 */
export function useTimeValidation (
  createTimeSettingData: Ref<CreateTimeSetting>,
  timeOfDay: Ref<dayjs.Dayjs | undefined>,
  createdAtSomeDate: Ref<dayjs.Dayjs | undefined>,
  isDateInPast: Ref<boolean>
) {
  /**
   * Get the current time setting data based on selected options
   */
  const getTimeSettingData = (): CreateTimeSetting | undefined => {
    const { createdAt } = createTimeSettingData.value;

    // Handle NOW option
    if (createdAt === 'NOW') {
      return { createdAt: 'NOW' };
    }

    // Handle AT_SOME_DATE option
    if (createdAt === 'AT_SOME_DATE') {
      if (!createdAtSomeDate.value) {
        return undefined;
      }
      return {
        createdAt: 'AT_SOME_DATE',
        createdAtSomeDate: createdAtSomeDate.value.format('YYYY-MM-DD HH:mm:ss')
      };
    }

    // Handle PERIODICALLY option
    if (createdAt === 'PERIODICALLY') {
      const {
        periodicCreationUnit,
        dayOfWeek,
        dayOfMonth,
        timeOfDay: timeOfDayValue,
        hourOfDay,
        minuteOfHour
      } = createTimeSettingData.value;

      const result: CreateTimeSetting = {
        createdAt: 'PERIODICALLY',
        periodicCreationUnit
      };

      // Add specific fields based on periodic unit
      if (periodicCreationUnit === 'WEEKLY') {
        result.dayOfWeek = dayOfWeek;
        result.timeOfDay = timeOfDayValue;
      } else if (periodicCreationUnit === 'MONTHLY') {
        result.dayOfMonth = dayOfMonth;
        result.timeOfDay = timeOfDayValue;
      } else if (periodicCreationUnit === 'DAILY') {
        result.timeOfDay = timeOfDayValue;
      } else if (periodicCreationUnit === 'HOURLY') {
        result.hourOfDay = hourOfDay;
      } else if (periodicCreationUnit === 'EVERY_MINUTE') {
        result.minuteOfHour = minuteOfHour;
      }

      return result;
    }

    return undefined;
  };

  /**
   * Validate the current time setting configuration
   */
  const validateTimeSetting = (): TimeValidationResult => {
    // Check if date is in the past for AT_SOME_DATE option
    if (createTimeSettingData.value.createdAt === 'AT_SOME_DATE' && isDateInPast.value) {
      return {
        isValid: false,
        data: undefined
      };
    }

    const data = getTimeSettingData();
    return {
      isValid: true,
      data
    };
  };

  return {
    getTimeSettingData,
    validateTimeSetting
  };
}
