<script setup lang="ts">
import { Modal, VuexHelper } from '@xcan-angus/vue-ui';
import Config from './index.vue';

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
const { updateList } = useMutations(['updateList'], 'apiSyncStore');

const deleteSuccess = () => {
  updateList();
};

const saveSuccess = () => {
  updateList();
};
</script>
<template>
  <Modal
    title="同步配置"
    :visible="visible"
    :reverse="true"
    :footer="null"
    @cancel="cancelModal">
    <template v-if="props.visible">
      <Config
        :id="props.id"
        :disabled="false"
        source="modal"
        :style="{ 'max-height': '70vh' }"
        @deleteSuccess="deleteSuccess"
        @saveSuccess="saveSuccess" />
    </template>
  </Modal>
</template>
