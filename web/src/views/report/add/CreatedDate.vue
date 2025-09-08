<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { CreatedAt, DayOfWeek, EnumMessage, enumUtils, PeriodicCreationUnit } from '@xcan-angus/infra';
import { DatePicker, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { Select } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { CreateTimeSetting } from '@/types/types';

// Component props definition
interface Props {
  createTimeSetting: CreateTimeSetting;
  showPeriodically: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  createTimeSetting: () => ({
    createdAt: 'NOW',
    timeOfDay: '07:00:00'
  }),
  showPeriodically: true
});

// Field names for enum select components
const enumFieldNames = {
  label: 'message',
  value: 'value'
};

// Reactive data for creation time settings
const createTimeSettingData = ref<CreateTimeSetting>({ createdAt: 'NOW' });

// Enum options for select components
const createdAtAllOpt = ref<EnumMessage<CreatedAt>[]>([]);
const createdAtOpt = ref<EnumMessage<CreatedAt>[]>([]);
const periodicCreationUnitOpt = ref<EnumMessage<PeriodicCreationUnit>[]>([]);
const dayOfWeekOpt = ref<EnumMessage<DayOfWeek>[]>([]);

// Options for day of month (1-31)
const dayOfMonthOpt = ref<EnumMessage<DayOfWeek>[]>(Array.from(new Array(31)).map((_i, idx) => {
  return {
    message: idx + 1 + '',
    value: idx + 1 + ''
  };
}));

// Time and date values
const timeOfDay = ref();
const createdAtSomeDate = ref();

/**
 * Load enum values for select options
 * Fetches enum messages for CreatedAt, PeriodicCreationUnit, and DayOfWeek
 */
const loadEnum = async () => {
  createdAtAllOpt.value = enumUtils.enumToMessages(CreatedAt);
  periodicCreationUnitOpt.value = enumUtils.enumToMessages(PeriodicCreationUnit);
  dayOfWeekOpt.value = enumUtils.enumToMessages(DayOfWeek);
};

/**
 * Watch for changes in showPeriodically prop and update createdAt options accordingly
 * If showPeriodically is false, filter out 'PERIODICALLY' option
 */
watch([() => props.showPeriodically, () => createdAtAllOpt.value], ([newValue]) => {
  if (newValue) {
    createdAtOpt.value = createdAtAllOpt.value;
  } else {
    createTimeSettingData.value.createdAt = 'NOW';
    createdAtOpt.value = createdAtAllOpt.value.filter(i => i.value !== 'PERIODICALLY');
  }
}, {
  immediate: true
});

/**
 * Format and set time of day value
 * Extracts hours, minutes, and seconds from timeOfDay and formats as HH:mm:ss
 */
const getTimeOfDay = () => {
  createTimeSettingData.value.timeOfDay = dayjs(timeOfDay.value).format('HH:mm:ss');
};

/**
 * Format and set specific creation date
 * Formats createdAtSomeDate as YYYY-MM-DD HH:mm:ss
 */
const getCreatedAtSomeDate = () => {
  createTimeSettingData.value.createdAtSomeDate = dayjs(createdAtSomeDate.value).format('YYYY-MM-DD HH:mm:ss');
};

/**
 * Lifecycle hook - Initialize component
 * Loads enum values and sets up watchers for props
 */
onMounted(async () => {
  await loadEnum();

  watch(() => props.createTimeSetting, () => {
    createTimeSettingData.value = JSON.parse(JSON.stringify(props.createTimeSetting));
    if (createTimeSettingData.value.timeOfDay) {
      const [hour, minute, second] = createTimeSettingData.value.timeOfDay.split(':');
      timeOfDay.value = dayjs().set('hour', +hour).set('minute', +minute).set('second', +second);
    } else {
      timeOfDay.value = dayjs().set('hour', 7).set('minute', 0).set('second', 0);
      getTimeOfDay();
    }
    if (createTimeSettingData.value.createdAtSomeDate) {
      createdAtSomeDate.value = dayjs(createTimeSettingData.value.createdAtSomeDate);
    } else {
      createdAtSomeDate.value = dayjs().add(1, 'hours');
      getCreatedAtSomeDate();
    }

    const { periodicCreationUnit, createdAt, dayOfWeek } = props.createTimeSetting;

    if (periodicCreationUnit) {
      createTimeSettingData.value.periodicCreationUnit = periodicCreationUnit?.value || periodicCreationUnit;
    }
    if (createdAt) {
      createTimeSettingData.value.createdAt = createdAt?.value || createdAt;
    }

    if (dayOfWeek) {
      createTimeSettingData.value.dayOfWeek = dayOfWeek?.value;
    }
  }, {
    deep: true,
    immediate: true
  });
});

/**
 * Get creation time data based on selected options
 * Returns formatted data according to the selected creation time type
 * @returns Object containing creation time settings
 */
const getData = () => {
  if (createTimeSettingData.value.createdAt === 'NOW') {
    return {
      createdAt: 'NOW'
    };
  }
  if (createTimeSettingData.value.createdAt === 'AT_SOME_DATE') {
    return {
      createdAt: 'AT_SOME_DATE',
      createdAtSomeDate: dayjs(createdAtSomeDate.value).format('YYYY-MM-DD HH:mm:ss')
    };
  }
  const { periodicCreationUnit, dayOfWeek, dayOfMonth, timeOfDay } = createTimeSettingData.value;
  if (createTimeSettingData.value.createdAt === 'PERIODICALLY') {
    return {
      createdAt: 'PERIODICALLY',
      periodicCreationUnit,
      dayOfWeek: periodicCreationUnit === 'WEEKLY' ? dayOfWeek : undefined,
      dayOfMonth: periodicCreationUnit === 'MONTHLY' ? dayOfMonth : undefined,
      timeOfDay
    };
  }
};

/**
 * Validate if the selected date is after current time
 * @returns Boolean indicating if the validation failed
 */
const getValidCreatedAtSomeData = () => {
  if (!isValid.value) {
    return false;
  }
  return dayjs().isAfter(createdAtSomeDate.value);
};

// Validation state
const isValid = ref(false);

/**
 * Validate creation time settings
 * @returns Formatted data if validation passes, false otherwise
 */
const validate = () => {
  isValid.value = true;
  if (getValidCreatedAtSomeData()) {
    return false;
  }
  isValid.value = false;
  return getData();
};

// Expose methods to parent component
defineExpose({
  validate,
  getData
});
</script>
<template>
  <div class="flex">
    <RadioGroup v-model:value="createTimeSettingData.createdAt">
      <Radio
        v-for="item in createdAtOpt"
        :key="item.value"
        :value="item.value"
        class="flex h-10">
        {{ item.message }}
      </Radio>
    </RadioGroup>
    <div class="">
      <div class="h-10"></div>
      <div class="h-10">
        <FormItem :class="{'ant-form-item-has-error': getValidCreatedAtSomeData()}">
          <DatePicker
            v-show="createTimeSettingData.createdAt == 'AT_SOME_DATE'"
            v-model:value="createdAtSomeDate"
            showTime
            :allowClear="false"
            @change="getCreatedAtSomeDate" />
        </FormItem>
      </div>
      <div class="h-7 flex items-center space-x-2">
        <template v-if="createTimeSettingData.createdAt === 'PERIODICALLY'">
          <Select
            v-model:value="createTimeSettingData.periodicCreationUnit"
            :defaultActiveFirstOption="true"
            :options="periodicCreationUnitOpt"
            :fieldNames="enumFieldNames"
            class="w-40" />
          <Select
            v-show="createTimeSettingData.periodicCreationUnit === 'WEEKLY'"
            v-model:value="createTimeSettingData.dayOfWeek"
            :options="dayOfWeekOpt"
            :defaultActiveFirstOption="true"
            :fieldNames="enumFieldNames"
            class="w-40" />
          <Select
            v-show="createTimeSettingData.periodicCreationUnit === 'MONTHLY'"
            v-model:value="createTimeSettingData.dayOfMonth"
            :options="dayOfMonthOpt"
            :defaultActiveFirstOption="true"
            :fieldNames="enumFieldNames"
            class="w-40" />
          <DatePicker
            v-model:value="timeOfDay"
            mode="time"
            picker="time"
            :allowClear="false"
            @change="getTimeOfDay" />
        </template>
      </div>
    </div>
  </div>
</template>
