<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { CheckboxGroup, Modal } from 'ant-design-vue';
import { apis, services } from 'src/api/tester';

const { t } = useI18n();

interface Props {
  visible: boolean;
  id?: string;
  type: 'API'|'SERVICE'
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean)}>();

const checked = ref(['PERFORMANCE', 'STABILITY', 'FUNCTIONAL']);
const validated = ref(false);
const testTypes = [
  {
    label: t('commonComp.delScriptModal.functionalTest'),
    value: 'FUNCTIONAL'
  },
  {
    label: t('commonComp.delScriptModal.performanceTest'),
    value: 'PERFORMANCE'
  },
  {
    label: t('commonComp.delScriptModal.stabilityTest'),
    value: 'STABILITY'
  }
];

const loading = ref(false);
const handleDel = async () => {
  if (!checked.value.length) {
    validated.value = true;
    return;
  }
  loading.value = true;
  const [error] = await (props.type === 'API' ? apis.deleteTestScript(props.id, checked.value) : services.delApiScript(props.id, checked.value));
  if (error) {
    return;
  }
  loading.value = false;
  notification.success(t('commonComp.delScriptModal.deleteSuccess'));
  emits('update:visible', false);
};

const handleClose = () => {
  emits('update:visible', false);
};

watch(() => props.visible, () => {
  checked.value = ['PERFORMANCE', 'STABILITY', 'FUNCTIONAL'];
  validated.value = false;
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('commonComp.delScriptModal.title')"
    :confirmLoading="loading"
    :visible="visible"
    @cancel="handleClose"
    @ok="handleDel">
    <div class="flex items-center">
      <p class="py-2">{{ t('commonComp.delScriptModal.scriptType') }}</p>
      <CheckboxGroup
        v-model:value="checked"
        :options="testTypes"
        class="ml-2"></CheckboxGroup>
    </div>
    <div v-show="validated && !checked.length" class="text-rule">{{ t('commonComp.delScriptModal.selectScriptType') }}</div>
  </Modal>
</template>
