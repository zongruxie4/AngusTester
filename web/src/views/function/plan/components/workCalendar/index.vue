<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { defineAsyncComponent, ref } from 'vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  visible: boolean;
  planId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  planId: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();
const userInfo = ref(appContext.getUser());

const WorkCalendar = defineAsyncComponent(() => import('@/views/function/homepage/workCalendar/index.vue'));

const cancel = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    :title="t('functionPlan.comp.workCalendar.title')"
    @cancel="cancel">
    <WorkCalendar
      :projectId="props.projectId"
      :userInfo="userInfo"
      :planId="props.planId" />
  </Modal>
</template>
