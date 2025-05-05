<script lang="ts" setup>
import { ref, watch } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { CheckboxGroup, Modal } from 'ant-design-vue';
import { apis, services } from 'src/api/tester';

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
    label: '功能测试',
    value: 'FUNCTIONAL'
  },
  {
    label: '性能测试',
    value: 'PERFORMANCE'
  },
  {
    label: '稳定性测试',
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
  const [error] = await (props.type === 'API' ? apis.delApiScript(props.id, checked.value) : services.delApiScript(props.id, checked.value));
  if (error) {
    return;
  }
  loading.value = false;
  notification.success('删除测试脚本成功');
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
    title="删除测试脚本"
    :confirmLoading="loading"
    :visible="visible"
    @cancel="handleClose"
    @ok="handleDel">
    <div class="flex items-center">
      <p class="py-2">脚本类型</p>
      <CheckboxGroup
        v-model:value="checked"
        :options="testTypes"
        class="ml-2"></CheckboxGroup>
    </div>
    <div v-show="validated && !checked.length" class="text-rule">请选择要删除的脚本类型</div>
  </Modal>
</template>
