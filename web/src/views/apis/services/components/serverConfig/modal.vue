<script setup lang="ts">
import { Modal, VuexHelper } from '@xcan-angus/vue-ui';
import SecurityConfig from './index.vue';

interface Props {
  visible: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: ''
});

const emit = defineEmits<{(e: 'update:visible', value:boolean): void}>();

const cancelModal = () => {
  emit('update:visible', false);
};

const { useMutations } = VuexHelper;
const { updateList } = useMutations(['updateList'], 'apiServerUrlStore');

const deleteSuccess = () => {
  updateList();
};

const saveSuccess = () => {
  updateList();
};
</script>
<template>
  <Modal
    title="服务器配置"
    style="width:650px;"
    :visible="visible"
    :reverse="true"
    :footer="null"
    @cancel="cancelModal">
    <template v-if="props.visible">
      <SecurityConfig
        :id="props.id"
        :disabled="false"
        source="modal"
        :style="{ 'max-height': '70vh' }"
        @deleteSuccess="deleteSuccess"
        @saveSuccess="saveSuccess" />
    </template>
  </Modal>
</template>
