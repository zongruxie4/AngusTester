<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Modal, VuexHelper } from '@xcan-angus/vue-ui';
import ServerConfig from '@/views/apis/services/services/slider/server/index.vue';

interface Props {
  visible: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: ''
});

const { t } = useI18n();

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
    :title="t('service.serverConfig.title')"
    style="width:650px;"
    :visible="props.visible"
    :reverse="true"
    :footer="null"
    @cancel="cancelModal">
    <template v-if="props.visible">
      <ServerConfig
        :id="props.id"
        :disabled="false"
        source="modal"
        :style="{ 'max-height': '70vh' }"
        @deleteSuccess="deleteSuccess"
        @saveSuccess="saveSuccess" />
    </template>
  </Modal>
</template>
