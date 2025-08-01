<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { enumUtils } from '@xcan-angus/infra';
import { Colon } from '@xcan-angus/vue-ui';

type CreateTimeSetting = {
  createdAt: {value: 'AT_SOME_DATE'| 'NOW'| 'PERIODICALLY', message: string},
  periodicCreationUnit?: 'DAILY'| 'MONTHLY'|'WEEKLY',
  dayOfWeek?: 'FRIDAY'| 'MONDAY'|'SATURDAY'| 'SUNDAY'|'THURSDAY'| 'TUESDAY'| 'WEDNESDAY';
  createdAtSomeDate?: string;
  dayOfMonth?: string;
  timeOfDay?: string;
  nextGenerationDate?: string;
}

interface Props {
  createTimeSetting: CreateTimeSetting;
}

const props = withDefaults(defineProps<Props>(), {
  createTimeSetting: () => ({
    createdAt: {
      value: 'NOW',
      message: ''
    }
  })
});

const createTimeSettingData = ref<CreateTimeSetting>({ createdAt: 'NOW' });

const createdAtAllOpt = ref<{message: string; value: string}[]>([]);
const createdAtOpt = ref<{message: string; value: string}[]>([]);
const periodicCreationUnitOpt = ref<{message: string; value: string}[]>([]);
const dayOfWeekOpt = ref<{message: string; value: string}[]>([]);
const dayOfMonthOpt = ref<{message: string; value: string}[]>(Array.from(new Array(31)).map((_i, idx) => {
  return {
    message: idx + 1 + '',
    value: idx + 1 + ''
  };
}));

const timeOfDay = ref();
const createdAtSomeDate = ref();
const loadEnum = async () => {
  Promise.all([
    enumUtils.enumToMessages('CreatedAt'),
    enumUtils.enumToMessages('PeriodicCreationUnit'),
    enumUtils.enumToMessages('DayOfWeek')
  ]).then(([resp1, resp2, resp3]) => {
    const [_error1, data1] = resp1;
    createdAtAllOpt.value = data1 || [];
    const [_error2, data2] = resp2;
    periodicCreationUnitOpt.value = data2 || [];
    const [_error3, data3] = resp3;
    dayOfWeekOpt.value = data3 || [];
  });
};

const getWeekName = () => {
  const value = props.createTimeSetting.dayOfWeek?.value;
  return dayOfWeekOpt.value.find(i => i.value === value)?.message;
};

onMounted(() => {
  loadEnum();
});
</script>
<template>
  <div>
    <tmplate v-if="props.createTimeSetting.createdAt?.value === 'NOW'">
      <div class="flex items-center space-x-2">
        <span>{{ props.createTimeSetting.createdAt?.message }}</span>
        <!-- <Colon />
        <div>{{ props.createTimeSetting?.nextGenerationDate }}</div> -->
      </div>
    </tmplate>

    <tmplate v-if="props.createTimeSetting.createdAt?.value === 'AT_SOME_DATE'">
      <div class="flex items-center space-x-2">
        <span>{{ props.createTimeSetting.createdAt?.message }}</span><Colon />
        <div>{{ props.createTimeSetting?.createdAtSomeDate }}</div>
      </div>
    </tmplate>

    <tmplate v-if="props.createTimeSetting.createdAt?.value === 'PERIODICALLY'">
      <div class="flex items-center space-x-2">
        <span>{{ props.createTimeSetting.createdAt?.message }}</span><Colon />
        <div>{{ props.createTimeSetting?.periodicCreationUnit?.message }}</div>
        <div v-if="props.createTimeSetting.dayOfWeek">{{ getWeekName() }}</div>
        <div v-if="props.createTimeSetting.dayOfMonth">{{ props.createTimeSetting.dayOfMonth }}日</div>
        <div v-if="props.createTimeSetting.timeOfDay">{{ props.createTimeSetting.timeOfDay }}</div>
      </div>
      <div class="flex items-center space-x-2 mt-2">
        <span>下次生成时间</span><Colon />
        <div>{{ props.createTimeSetting?.nextGenerationDate }}</div>
      </div>
    </tmplate>
  </div>
</template>
