<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue';
import { Modal, Scroll, List, Table, DropdownGroup, Icon, Input, Select, HttpMethodText, NoData, IconText } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import SelectApisCase from '@/components/apis/selector/SelectApisCase.vue';
const { t } = useI18n();

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  projectId: undefined
});
const emits = defineEmits<{(e: 'visible:update', value: boolean):void; (e: 'ok', ids: string[], rows: any[]):void}>();

const caseIds = ref<string[]>([]);
const cases = ref<any[]>([]);

const cancel = () => {
  emits('visible:update', false);
};

const ok = () => {
  emits('ok', caseIds.value, cases.value);
  cancel();
};

const handleChangeData = (ids: string[], rows: any[]) => {
  caseIds.value = ids;
  cases.value = rows;
};

onMounted(() => {
  watch(() => props.visible, () => {
    caseIds.value = [];
    cases.value = [];
  });
});

</script>
<template>
  <Modal
    :title="t('xcan_selectApisCaseModal.title')"
    :visible="props.visible"
    :width="1000"
    @cancel="cancel"
    @ok="ok">
    <SelectApisCase
      v-if="props.visible"
      :projectId="props.projectId"
      @change="handleChangeData" />
  </Modal>
</template>
