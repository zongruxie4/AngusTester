<script setup lang="ts">
// UI component imports
import { Modal } from '@xcan-angus/vue-ui';

// Local component imports
import Authorize from '@/components/auth/index.vue';

/**
 * Component props interface for authorization modal
 */
interface Props {
  auth: boolean;
  appId: string;
  visible: boolean;
  title: string;
  enumKey: string;
  listUrl: string;
  delUrl: string;
  addUrl: string;
  updateUrl: string;
  enabledUrl: string;
  initStatusUrl: string;
  onTips: string;
  offTips: string;

  style?: string | { [key: string]: string };
  class?: string;
  width?: number;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  auth: false,
  appId: undefined,
  visible: false,
  title: undefined,

  enumKey: undefined,
  listUrl: undefined,
  delUrl: undefined,
  addUrl: undefined,
  updateUrl: undefined,
  enabledUrl: undefined,
  initStatusUrl: undefined,
  onTips: undefined,
  offTips: undefined,

  style: () => ({
    height: '520px'
  }),
  class: undefined,

  width: 1100
});

// Component events
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'change', value: { auth: boolean }): void;
  (e: 'cancel'): void;
}>();

/**
 * Handle authorization change event
 */
const handleAuthorizationChange = (value: { auth: boolean }) => {
  emits('change', value);
};

/**
 * Handle modal cancel event
 */
const handleModalCancel = () => {
  emits('update:visible', false);
  emits('cancel');
};
</script>
<template>
  <Modal
    :title="props.title"
    :visible="props.visible"
    :centered="true"
    :width="props.width"
    :footer="false"
    @cancel="handleModalCancel">
    <template v-if="props.visible">
      <Authorize
        :appId="props.appId"
        :enumKey="props.enumKey"
        :listUrl="props.listUrl"
        :delUrl="props.delUrl"
        :addUrl="props.addUrl"
        :updateUrl="props.updateUrl"
        :enabledUrl="props.enabledUrl"
        :initStatusUrl="props.initStatusUrl"
        :onTips="props.onTips"
        :offTips="props.offTips"
        :style="props.style"
        :class="props.class"
        :auth="props.auth"
        :emptyTextStyle="{margin:'50px 0'}"
        noDataSize="small"
        style="overflow-y: auto;"
        @change="handleAuthorizationChange" />
    </template>
  </Modal>
</template>
