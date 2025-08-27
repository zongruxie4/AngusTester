<script lang="ts" setup>
import { inject, ref, watch } from 'vue';
import { Modal, NoData, notification } from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';
import { CheckboxGroup } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

// import http from '@/utils/http';

export interface Props {
    visible: boolean;
    apisId: string; // 用例所属接口
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  apisId: ''
});
const { t } = useI18n();

const projectInfo = inject('projectInfo', ref({ id: '' }));

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

const caseData = ref([]);
const checkeCaseIds = ref<string[]>([]);
const loadCaseData = async () => {
  const [error, { data }] = await apis.loadApiCases({ apisId: props.apisId, projectId: projectInfo.value?.id, pageSize: 100, pageNo: 1 });
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
  if (checkeCaseIds.value.length) {
    const [error] = await apis.execCase(checkeCaseIds.value, props.apisId);
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
        v-model:value="checkeCaseIds"
        :options="caseData" />
    </div>
    <NoData
      v-else
      size="small"
      class="my-5" />
  </Modal>
</template>
