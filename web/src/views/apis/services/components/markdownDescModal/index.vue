<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Input, Modal } from '@xcan-angus/vue-ui';
import mavonEditor from '@xcan/markdown';
import '@xcan/markdown/dist/css/index.css';

const Editor = mavonEditor.mavonEditor;

interface Props {
  visible: boolean;
  isEdit: boolean;
  value: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  isEdit: false,
  value: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean): void, (e: 'ok', value: string):void, (e:'cancel'):void}>();

const cancelEditDescription = () => {
  emits('update:visible', false);
  emits('cancel');
};

const saveDescription = () => {
  emits('ok', description.value);
};

const description = ref();

onMounted(() => {
  watch(() => props.visible, () => {
    description.value = props.value;
  }, {
    immediate: true
  });
});

const footer = computed(() => {
  return !!props.isEdit;
});

const bodyStyle = {
  padding: '0 24px 16px 24px',
  height: 'calc(100% - 84px)'
};
</script>
<template>
  <Modal
    :visible="props.visible"
    :bodyStyle="bodyStyle"
    :footer="footer"
    style="width: 90%;height: 90%;"
    wrapClassName="api-desc-markdown-modal"
    @cancel="cancelEditDescription"
    @ok="saveDescription">
    <div
      :class="{ 'border': isEdit }"
      class="flex items-start h-full rounded border-solid border-border-divider">
      <Input
        v-if="isEdit"
        v-model:value="description"
        type="textarea"
        :maxlength="20000"
        style="flex: 0 0 50%;height: 100%;padding: 18px;border: none;resize: none;"
        placeholder="请输入描述，支持Markdown语法。"
        :autoSize="false" />
      <Editor
        v-model="description"
        defaultOpen="preview"
        style="flex:1;height: 100%;border: none;font-size: 12px;word-break: break-all;"
        :boxShadow="false"
        :subfield="false" />
    </div>
  </Modal>
</template>
<style>

.api-desc-markdown-modal .ant-modal-content {
  height: 100%;
}

.api-desc-markdown-modal .v-note-wrapper .v-note-read-model {
  padding: 18px;
}
</style>
