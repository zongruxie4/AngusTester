<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { CreatedAt, DayOfWeek, EnumMessage, enumUtils, PeriodicCreationUnit } from '@xcan-angus/infra';
import { Colon } from '@xcan-angus/vue-ui';
import { CreateTimeSetting } from '@/types/types';

const { t } = useI18n();

// Component props definition
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

// Enum options for select components
const createdAtAllOpt = ref<EnumMessage<CreatedAt>[]>([]);
const periodicCreationUnitOpt = ref<EnumMessage<PeriodicCreationUnit>[]>([]);
const dayOfWeekOpt = ref<EnumMessage<DayOfWeek>[]>([]);

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
 * Get week name based on day of week value
 * @returns Week name message
 */
const getWeekName = () => {
  const value = props.createTimeSetting.dayOfWeek?.value;
  return dayOfWeekOpt.value.find(i => i.value === value)?.message;
};

/**
 * Lifecycle hook - Initialize component
 * Load enum values when component is mounted
 */
onMounted(() => {
  loadEnum();
});
</script>
<template>
  <div>
    <!-- Display creation time setting for NOW type -->
    <template v-if="props.createTimeSetting.createdAt?.value === CreatedAt.NOW">
      <div class="flex items-center space-x-2">
        <span>{{ props.createTimeSetting.createdAt?.message }}</span>
      </div>
    </template>

    <!-- Display creation time setting for AT_SOME_DATE type -->
    <template v-if="props.createTimeSetting.createdAt?.value === CreatedAt.AT_SOME_DATE">
      <div class="flex items-center space-x-2">
        <span>{{ props.createTimeSetting.createdAt?.message }}</span><Colon />
        <div>{{ props.createTimeSetting?.createdAtSomeDate }}</div>
      </div>
    </template>

    <!-- Display creation time setting for PERIODICALLY type -->
    <template v-if="props.createTimeSetting.createdAt?.value === CreatedAt.PERIODICALLY">
      <div class="flex items-center space-x-2">
        <span>{{ props.createTimeSetting.createdAt?.message }}</span><Colon />
        <div>{{ props.createTimeSetting?.periodicCreationUnit?.message }}</div>
        <div v-if="props.createTimeSetting.dayOfWeek">{{ getWeekName() }}</div>
        <div v-if="props.createTimeSetting.dayOfMonth">{{ props.createTimeSetting.dayOfMonth }}{{ t('reportHome.reportDetail.generateTime.dayOfMonth') }}</div>
        <div v-if="props.createTimeSetting.timeOfDay">{{ props.createTimeSetting.timeOfDay }}</div>
      </div>
      <div class="flex items-center space-x-2 mt-2">
        <span>{{ t('reportHome.reportDetail.generateTime.nextGenerateTime') }}</span><Colon />
        <div>{{ props.createTimeSetting?.nextGenerationDate }}</div>
      </div>
    </template>
  </div>
</template>
