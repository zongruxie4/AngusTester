<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { defineAsyncComponent, ref } from 'vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

// component definitions
const WorkCalendar = defineAsyncComponent(() => import('@/views/function/home/WorkCalendar.vue'));

// composables
const { t } = useI18n();

// interfaces
interface Props {
  visible: boolean;
  planId: string;
  projectId: string;
}

// props and emits
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  planId: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

// reactive data
const userInfo = ref(appContext.getUser());

/**
 * Handles modal cancel event and emits visibility update
 */
const handleCancel = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    :title="t('functionPlan.comp.workCalendar.title')"
    @cancel="handleCancel">
    <WorkCalendar
      :projectId="props.projectId"
      :userInfo="userInfo"
      :planId="props.planId" />
  </Modal>
</template>
