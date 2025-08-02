<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { EnumMessage, CreatedAt, PeriodicCreationUnit, DayOfWeek, enumUtils } from '@xcan-angus/infra';
import { Colon } from '@xcan-angus/vue-ui';

type CreateTimeSetting = {
  createdAt: {value: CreatedAt, message: string},
  periodicCreationUnit?: PeriodicCreationUnit,
  dayOfWeek?: DayOfWeek;
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

const createdAtAllOpt = ref<EnumMessage<CreatedAt>[]>([]);
const periodicCreationUnitOpt = ref<EnumMessage<PeriodicCreationUnit>[]>([]);
const dayOfWeekOpt = refEnumMessage<DayOfWeek>[]>([]);

const loadEnum = async () => {
  createdAtAllOpt.value = enumUtils.enumToMessages(CreatedAt);
  periodicCreationUnitOpt.value = enumUtils.enumToMessages(PeriodicCreationUnit);
  dayOfWeekOpt.value = enumUtils.enumToMessages(DayOfWeek);
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
