<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';
import { Modal, notification } from '@xcan-angus/vue-ui';
import AddSchemaTypeModel from './AddSchemaTypeModel.vue';
import { useI18n } from 'vue-i18n';

interface Props {
  visible: boolean;
  data: {[key: string]: any},
  parentType: 'object'|'array';
  excludesAttr: string[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  data: () => ({}),
  parentType: 'object',
  excludesAttr: () => ([])
});
const { t } = useI18n();

const emits = defineEmits<{(e: 'update:visible', value: boolean): void, (e: 'ok', value: {name: string, [key: string]: any}): void, (e: 'cancel'):void}>();

const addSchemaModelRef = ref();

const type = ref(); // 类型

const activeTab = ref('attr');

const validate = ref(false);

const submit = () => {
  const data = addSchemaModelRef.value.getData();
  if (props.excludesAttr.includes(data.name)) {
    notification.warning(t('service.dataModel.addAttrTip'));
    return;
  }
  if (data) {
    emits('ok', data);
  }
};

const cancel = () => {
  emits('update:visible', false);
  emits('cancel');
};

watch(() => props.visible, newValue => {
  if (newValue) {
    nextTick(() => {
      addSchemaModelRef.value?.resetValues();
    });
  }
});

watch([() => type.value, activeTab.value], () => {
  validate.value = false;
});
</script>
<template>
  <Modal
    :title="t('service.dataModel.addAttrTitle')"
    :visible="props.visible"
    @cancel="cancel"
    @ok="submit">
    <AddSchemaTypeModel
      ref="addSchemaModelRef"
      :data="props.data"
      :parentType="props.parentType" />
  </Modal>
</template>
