<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext } from '@xcan-angus/infra';

const { t } = useI18n();

// Component Props & Emits
interface Props {
  visible: boolean;
  sprintId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  sprintId: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

// Reactive Data
const currentUser = ref(appContext.getUser() as unknown as { id: string });

/**
 * <p>
 * Lazy-loaded work calendar component.
 * <p>
 * Dynamically imports the work calendar component to reduce initial bundle size.
 */
const WorkCalendar = defineAsyncComponent(() => import('@/views/task/home/WorkCalendar.vue'));

/**
 * <p>
 * Closes the work calendar modal.
 * <p>
 * Emits update event to parent component to hide the modal.
 */
const closeModal = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    :title="t('taskSprint.workCalendar.title')"
    @cancel="closeModal">
    <WorkCalendar
      :projectId="props.projectId"
      :userInfo="currentUser"
      :sprintId="props.sprintId" />
  </Modal>
</template>
