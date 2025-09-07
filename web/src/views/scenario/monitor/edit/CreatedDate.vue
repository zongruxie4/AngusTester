<script lang="ts" setup>
import { onMounted, toRefs, watch } from 'vue';
import { DatePicker, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { Select } from '@xcan-angus/vue-ui';
import { CreatedAt, PeriodicUnit } from '@xcan-angus/infra';

// Import types and composables
import type { CreatedDateProps } from '../types';
import { useTimeData } from './composables/useTimeData';
import { useTimeValidation } from './composables/useTimeValidation';
import { useTimeOptions } from './composables/useTimeOptions';

// Component props with default values
const props = withDefaults(defineProps<CreatedDateProps>(), {
  createTimeSetting: () => ({
    createdAt: CreatedAt.NOW,
    timeOfDay: '07:00:00'
  }),
  showPeriodically: true
});

// Destructure props to reactive refs
const { createTimeSetting, showPeriodically } = toRefs(props);

// Initialize composables
const {
  createTimeSettingData,
  timeOfDay,
  createdAtSomeDate,
  isValid,
  createdAtAllOpt,
  createdAtOpt,
  periodicCreationUnitOpt,
  dayOfWeekOpt,
  dayOfMonthOpt,
  minutesOpt,
  hoursOpt,
  enumFieldNames,
  loadEnum,
  updateTimeOfDay,
  updateCreatedAtSomeDate,
  initializeTimeValues,
  setDefaultPeriodicValues,
  isDateInPast
} = useTimeData();

// Initialize validation composable
const { getTimeSettingData, validateTimeSetting } = useTimeValidation(
  createTimeSettingData,
  timeOfDay,
  createdAtSomeDate,
  isDateInPast
);

// Initialize options composable
const { watchPeriodicallyOption, shouldShowTimePicker, shouldShowPeriodicOption } = useTimeOptions(
  showPeriodically,
  createdAtAllOpt,
  createdAtOpt,
  createTimeSettingData
);

/**
 * Initialize component data and watch for prop changes
 */
const initializeComponent = async () => {
  // Load enum data
  await loadEnum();

  // Watch for prop changes and update internal state
  watch(
    () => createTimeSetting.value,
    (newValue) => {
      if (!newValue) return;

      // Deep clone the prop data
      createTimeSettingData.value = JSON.parse(JSON.stringify(newValue));

      // Initialize time values
      initializeTimeValues();

      // Handle enum value extraction
      const { periodicCreationUnit, createdAt, dayOfWeek } = newValue;

      if (periodicCreationUnit) {
        createTimeSettingData.value.periodicCreationUnit =
          (periodicCreationUnit as any)?.value || periodicCreationUnit;
      }

      if (createdAt) {
        createTimeSettingData.value.createdAt =
          (createdAt as any)?.value || createdAt;
      }

      if (dayOfWeek) {
        createTimeSettingData.value.dayOfWeek = (dayOfWeek as any)?.value;
      }

      // Set default periodic values
      setDefaultPeriodicValues();
    },
    { deep: true, immediate: true }
  );
};

/**
 * Validate the current time setting
 */
const validate = () => {
  const result = validateTimeSetting();
  isValid.value = result.isValid;
  return result;
};

/**
 * Get the current time setting data
 */
const getData = () => {
  return getTimeSettingData();
};

// Initialize component
onMounted(async () => {
  await initializeComponent();
  watchPeriodicallyOption();
});

// Expose methods to parent component
defineExpose({
  validate,
  getData
});
</script>

<template>
  <div class="flex">
    <!-- Time creation mode selection -->
    <RadioGroup v-model:value="createTimeSettingData.createdAt">
      <Radio
        v-for="item in createdAtOpt"
        :key="item.value"
        :value="item.value"
        class="flex h-10">
        {{ item.message }}
      </Radio>
    </RadioGroup>

    <!-- Time configuration panel -->
    <div class="">
      <div class="h-10"></div>

      <!-- Specific date selection -->
      <div class="h-10">
        <FormItem :class="{'ant-form-item-has-error': isDateInPast}">
          <DatePicker
            v-show="createTimeSettingData.createdAt === CreatedAt.AT_SOME_DATE"
            v-model:value="createdAtSomeDate"
            showTime
            :allowClear="false"
            @change="updateCreatedAtSomeDate" />
        </FormItem>
      </div>

      <!-- Periodic configuration options -->
      <div class="h-7 flex items-center space-x-2">
        <template v-if="createTimeSettingData.createdAt === CreatedAt.PERIODICALLY">
          <!-- Periodic unit selection -->
          <Select
            v-model:value="createTimeSettingData.periodicCreationUnit"
            :defaultActiveFirstOption="true"
            :options="periodicCreationUnitOpt"
            :fieldNames="enumFieldNames"
            class="w-40" />

          <!-- Weekly day selection -->
          <Select
            v-show="shouldShowPeriodicOption(PeriodicUnit.WEEKLY, createTimeSettingData.periodicCreationUnit)"
            v-model:value="createTimeSettingData.dayOfWeek"
            :options="dayOfWeekOpt"
            :defaultActiveFirstOption="true"
            :fieldNames="enumFieldNames"
            class="w-40" />

          <!-- Monthly day selection -->
          <Select
            v-show="shouldShowPeriodicOption(PeriodicUnit.MONTHLY, createTimeSettingData.periodicCreationUnit)"
            v-model:value="createTimeSettingData.dayOfMonth"
            :options="dayOfMonthOpt"
            :defaultActiveFirstOption="true"
            :fieldNames="enumFieldNames"
            class="w-40" />

          <!-- Time picker for weekly, monthly, and daily -->
          <DatePicker
            v-if="shouldShowTimePicker(createTimeSettingData.periodicCreationUnit)"
            v-model:value="timeOfDay"
            mode="time"
            picker="time"
            :allowClear="false"
            @change="updateTimeOfDay" />

          <!-- Hourly interval selection -->
          <Select
            v-if="shouldShowPeriodicOption(PeriodicUnit.HOURLY, createTimeSettingData.periodicCreationUnit)"
            v-model:value="createTimeSettingData.hourOfDay"
            class="inline-block min-w-15"
            :options="hoursOpt" />

          <!-- Every minute interval selection -->
          <Select
            v-if="shouldShowPeriodicOption(PeriodicUnit.EVERY_MINUTE, createTimeSettingData.periodicCreationUnit)"
            v-model:value="createTimeSettingData.minuteOfHour"
            class="inline-block min-w-15"
            :options="minutesOpt" />
        </template>
      </div>
    </div>
  </div>
</template>
