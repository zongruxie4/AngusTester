<script setup lang="ts">
import { ref } from 'vue';
import { Radio, RadioGroup } from 'ant-design-vue';
import { Colon, Modal } from '@xcan-angus/vue-ui';
import { download, TESTER } from '@xcan-angus/tools';

interface Props {
  visible: boolean;
  projectId: string;
  id: string[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: undefined,
  id: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'ok', value: string): void;
}>();

const loading = ref(false);
const fileType = ref<'JSON' | 'YAML'>('JSON');

const cancel = () => {
  fileType.value = 'JSON';
  emit('update:visible', false);
};

const ok = () => {
  let url = `${TESTER}/variable/export?projectId=${props.projectId}&format=${fileType.value}`;
  if (props.id) {
    url += `&ids=${props.id}`;
  }
  download(url);
  cancel();
};
</script>
<template>
  <Modal
    :visible="props.visible"
    :width="500"
    :confirmLoading="loading"
    title="下载服务器"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-center">
      <div class="flex items-center mr-3.5">
        <span>格式</span>
        <Colon />
      </div>
      <RadioGroup v-model:value="fileType" name="fileType">
        <Radio value="JSON">JSON</Radio>
        <Radio value="YAML">YAML</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>
