<script setup lang="ts">
import { Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

interface Props {
  visible: boolean,
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void; }>();

const { t } = useI18n();

const cancel = () => {
  emit('update:visible', false);
};
</script>

<template>
  <Modal
    :title="t('task.flowChart.title')"
    :visible="props.visible"
    :footer="null"
    :bodyStyle="{ padding: '30px 30px' }"
    style="width: auto; max-width: 1050px;"
    @cancel="cancel">
    <img src="images/flow.png" class="w-full" />
  </Modal>
</template>
