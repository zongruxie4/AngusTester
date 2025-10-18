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

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void,
  (e: 'ok', value: string):void,
  (e:'cancel'):void
}>();

/**
 * Cancel editing description and close the modal
 */
const cancelEditDescription = () => {
  emits('update:visible', false);
  emits('cancel');
};

/**
 * Save the description and close the modal
 */
const saveDescription = () => {
  description.value = easyMdRef.value.getValue();
  emits('ok', description.value);
};

// Reactive references for markdown editor
const easyMdRef = ref();
const description = ref();

// Computed property for modal footer visibility
const footer = computed(() => {
  return props.isEdit;
});

// Initialize component with description value
onMounted(() => {
  watch(() => props.visible, () => {
    description.value = props.value;
  }, {
    immediate: true
  });
});

// Modal body style configuration
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
