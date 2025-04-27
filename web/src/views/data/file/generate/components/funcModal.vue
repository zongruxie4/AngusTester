<script setup lang="ts">
import { Modal } from '@xcan-angus/vue-ui';

interface Props {
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});
const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

const close = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    title="选择Mock函数"
    width="1200px"
    :visible="props.visible"
    @cancel="close">
  </Modal>
</template>
