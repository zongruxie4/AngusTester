<script setup lang="ts">
import { computed, ref } from 'vue';
import { Modal, SelectApi } from '@xcan-angus/vue-ui';

// Types
type APIInfo = {
  id: string;
  summary: string;
  endpoint: string;
  method: string;
  protocol: { message: string; value: string; };
  disabled: boolean;
  mockApisId: string|undefined;
}

// Props & Emits
interface Props {
  visible:boolean;
  title:string;
  type:'link'|'copy';
  width?:number;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  title: undefined,
  type: 'copy',
  width: 700
});


const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:APIInfo):void;
  (e:'cancel'):void;
}>();

// ==================== Reactive State ====================
const apiInfo = ref<APIInfo>();

// ==================== Computed Properties ====================
/**
 * OK button properties based on selected API
 */
const okButtonProps = computed(() => {
  return {
    disabled: !apiInfo.value
  };
});

/**
 * Modal body style configuration
 */
const bodyStyle = {
  height: 'calc(100% - 84px)',
  paddingTop: '4px'
};

/**
 * Scroll properties for SelectApi component
 */
const scrollProps = {
  params: {
    filters: [{ key: 'protocol', op: 'IN', value: ['http', 'https'] }]
  },
  format: (data:APIInfo) => {
    return {
      ...data,
      disabled: !!data.mockApisId
    };
  }
};

// ==================== Methods ====================
/**
 * Handle API selection change
 * @param param0 - Selection change event with apiOptions
 */
const handleApiChange = ({ apiOptions }) => {
  apiInfo.value = apiOptions[0];
};

/**
 * Handle modal confirmation
 */
const handleModalConfirm = () => {
  if (!apiInfo.value) {
    return;
  }

  emit('ok', apiInfo.value);
};

/**
 * Handle modal cancellation
 */
const handleModalCancel = () => {
  apiInfo.value = undefined;
  emit('update:visible', false);
  emit('cancel');
};
</script>
<template>
  <Modal
    :title="props.title"
    :visible="props.visible"
    :centered="true"
    :width="props.width"
    :bodyStyle="bodyStyle"
    :okButtonProps="okButtonProps"
    class="select-api-modal-wrap"
    @ok="handleModalConfirm"
    @cancel="handleModalCancel">
    <SelectApi
      v-if="props.visible"
      mode="single"
      :showLinkText="true"
      :scrollProps="scrollProps"
      @change="handleApiChange" />
  </Modal>
</template>

<style>
.select-api-modal-wrap {
  height: calc(70%);
}

.select-api-modal-wrap .ant-modal-content {
  height: 100%;
}
</style>
