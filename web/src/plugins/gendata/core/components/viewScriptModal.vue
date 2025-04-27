
<script lang="ts" setup>
/**
 * description: 生成的脚本 yaml格式 在弹窗中显示
 * props: visible =>弹窗显示; data => 脚本内容
 */

import { Modal } from '@xcan-angus/vue-ui';

export interface Props {
  visible: boolean;
  data: string;
}

const emits = defineEmits<{(e: 'update:visible', value: boolean): void}>();

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  data: ''
});

const cancel = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    title="查看脚本"
    :visible="props.visible"
    :footer="null"
    width="100%"
    @cancel="cancel">
    <div class="whitespace-pre overflow-auto" :style="{maxHeight: 'calc(100vh - 100px)'}">
      {{ props.data }}
    </div>
  </Modal>
</template>
