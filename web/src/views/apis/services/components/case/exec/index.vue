<script lang="ts" setup>
import { inject, ref, Ref, watch } from 'vue';
import { Modal, NoData, notification } from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';
import { CheckboxGroup } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

export interface Props {
    visible: boolean;
    apisId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  apisId: ''
});
const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

const caseData = ref([]);
const checkedCaseIds = ref<string[]>([]);
const loadCaseData = async () => {
  const [error, { data }] = await apis.loadApiCases({ apisId: props.apisId, projectId: projectId.value, pageSize: 100, pageNo: 1 });
  if (error) {
    return;
  }
  caseData.value = (data.list || []).map(i => {
    return {
      ...i,
      value: i.id,
      label: i.name
    };
  });
};

const ok = async () => {
  if (checkedCaseIds.value.length) {
    const [error] = await apis.execCase(checkedCaseIds.value, props.apisId);
    if (error) {
      return;
    }
    notification.success(t('tips.execSuccess'));
  }
  emits('update:visible', false);
};

const cancel = () => {
  emits('update:visible', false);
};

watch(() => props.visible, newValue => {
  if (newValue) {
    loadCaseData();
  }
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('service.case.execModalTitle')"
    :visible="props.visible"
    @ok="ok"
    @cancel="cancel">
    <div v-if="!!caseData.length">
      <CheckboxGroup
        v-model:value="checkedCaseIds"
        :options="caseData" />
    </div>
    <NoData
      v-else
      size="small"
      class="my-5" />
  </Modal>
</template>
