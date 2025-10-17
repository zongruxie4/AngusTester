<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Modal } from '@xcan-angus/vue-ui';
import EasyMd from '@/components/easyMd/index.vue';

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
  description.value = easyMdRef.value.getValue();
  emits('ok', description.value);
};

const easyMdRef = ref();
const description = ref();

const footer = computed(() => {
  return !!props.isEdit;
});

onMounted(() => {
  watch(() => props.visible, () => {
    description.value = props.value;
  }, {
    immediate: true
  });
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
    <EasyMd
      ref="easyMdRef"
      :value="description"
      class="h-full overflow-auto" />
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
