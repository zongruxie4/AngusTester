<script setup lang="ts">
import { Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

export interface Props {
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});
const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

const { t } = useI18n();

const close = () => {
  emits('update:visible', false);
};

</script>
<template>
  <Modal
    :title="t('genDataPlugin.FuncModal.title')"
    width="1200px"
    :visible="props.visible"
    @cancel="close">
  </Modal>
</template>
