<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { defineAsyncComponent, inject, ref } from 'vue';

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
const userInfo = inject('tenantInfo', ref({ id: '' }));

const WorkCalendar = defineAsyncComponent(() => import('@/views/task/homepage/workCalendar/index.vue'));

const cancel = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    title="工作日历"
    @cancel="cancel">
    <WorkCalendar
      :projectId="props.projectId"
      :userInfo="userInfo"
      :sprintId="props.sprintId" />
  </Modal>
</template>
