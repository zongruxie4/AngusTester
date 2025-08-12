<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext } from '@xcan-angus/infra';

const { t } = useI18n();

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
const userInfo = ref(appContext.getUser());

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
    :title="t('taskSprint.workCalendar.title')"
    @cancel="cancel">
    <WorkCalendar
      :projectId="props.projectId"
      :userInfo="userInfo"
      :sprintId="props.sprintId" />
  </Modal>
</template>
