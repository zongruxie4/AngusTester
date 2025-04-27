<script setup lang="ts">
import { Modal } from '@xcan-angus/vue-ui';

interface Props {
  visible: boolean,
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void; }>();

const cancel = () => {
  emit('update:visible', false);
};
</script>

<template>
  <Modal
    title="任务处理流程图"
    :visible="props.visible"
    :footer="null"
    :bodyStyle="{ padding: '30px 30px' }"
    style="width: auto; max-width: 1050px;"
    @cancel="cancel">
    <img src="./images/flow.png" class="w-full" />
  </Modal>
</template>
