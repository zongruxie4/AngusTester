<script lang="ts" setup>
// Vue core imports
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Modal } from '@xcan-angus/vue-ui';

// Local component imports
import SelectApisCase from '@/components/apis/selectCaseModal/selectCase/index.vue';

const { t } = useI18n();

/**
 * Component props interface for select case modal
 */
interface Props {
  visible: boolean;
  projectId: string;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  visible: true,
  projectId: undefined
});

// Component events
const emits = defineEmits<{(e: 'visible:update', value: boolean): void; (e: 'ok', ids: string[], rows: any[]): void}>();

// Component state
const selectedCaseIds = ref<string[]>([]);
const selectedCases = ref<any[]>([]);

/**
 * Handle modal cancel
 */
const handleModalCancel = () => {
  emits('visible:update', false);
};

/**
 * Handle modal confirmation
 */
const handleModalConfirmation = () => {
  emits('ok', selectedCaseIds.value, selectedCases.value);
  handleModalCancel();
};

/**
 * Handle case selection data change
 * @param ids - Selected case IDs
 * @param rows - Selected case rows
 */
const handleCaseSelectionChange = (ids: string[], rows: any[]) => {
  selectedCaseIds.value = ids;
  selectedCases.value = rows;
};

// Watch for modal visibility changes
onMounted(() => {
  watch(() => props.visible, () => {
    selectedCaseIds.value = [];
    selectedCases.value = [];
  });
});

</script>
<template>
  <Modal
    :title="t('commonComp.apis.selectCaseModal.title')"
    :visible="props.visible"
    :width="1000"
    @cancel="handleModalCancel"
    @ok="handleModalConfirmation">
    <SelectApisCase
      v-if="props.visible"
      :projectId="props.projectId"
      @change="handleCaseSelectionChange" />
  </Modal>
</template>
