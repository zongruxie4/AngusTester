<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { MoveModal, notification, IconText } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

interface Props {
  type: 'api';
  visible: boolean;
  projectId: string;
  id: string | undefined;
  parentName: string;
  pid: string,
  admin?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'api',
  visible: false,
  id: undefined,
  parentName: '',
  pid: '',
  admin: false,
  projectId: undefined
});
const { t } = useI18n();

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'cancel'): void;
  (e: 'ok', value: { id: string, fromId: string }): void;
}>();

// Reactive references for move modal state
const targetId = ref<string>();
const fieldNames = { label: 'name', value: 'id', children: 'children' };
const defaultValue = ref<{ name: string; id: string; }>();

/**
 * Cancel the move operation and close the modal
 */
const cancel = () => {
  emits('cancel');
  emits('update:visible', false);
};

/**
 * Handle successful move operation
 * @returns Promise that resolves when move operation completes
 */
const onOk = async () => {
  const id = props.id;
  cancel();
  notification.success(t('actions.tips.moveSuccess'));
  if (id !== undefined) {
    emits('ok', { id, fromId: props.pid });
  }
};

// Computed properties for modal configuration
const title = computed(() => {
  return t('service.moveModal.title');
});

const params = computed(() => {
  return {
    admin: props.admin,
    hasPermission: 'ADD'
  };
});

/**
 * Get the move action URL
 * @returns Move action URL string
 */
const getMoveAction = () => {
  return `${TESTER}/apis/move?projectId=${props.projectId}`;
};

/**
 * Get parameters for the move operation
 * @param id - ID of the item to move
 * * @param targetId - Target ID to move to
 * @returns Object containing move parameters
 */
const getokParams = (id: string, targetId: string) => {
  return {
    apiIds: [id],
    targetServiceId: targetId
  };
};

/**
 * Filter options for the move modal
 * @returns Boolean indicating if option should be filtered
 */
const filterOpt = () => {
  return false;
};

// Computed hint text for the modal
const hints = computed(() => {
  return t('service.moveModal.hints');
});

// Computed parent object for the modal
const parent = computed(() => {
  return {
    id: props.pid,
    name: props.parentName
  };
});

// Initialize component with parent information
onMounted(() => {
  watch([() => props.pid, () => props.parentName], ([pid, pname]) => {
    targetId.value = pid;
    defaultValue.value = { name: pname, id: pid };
  }, {
    immediate: true
  });
});
</script>
<template>
  <MoveModal
    :id="props.id"
    :title="title"
    :parent="parent"
    :visible="props.visible"
    :fieldNames="fieldNames"
    :rootNode="false"
    :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
    :subTitle="t('service.moveModal.selectServiceTitle')"
    :hints="hints"
    :moveAction="getMoveAction"
    :excludes="filterOpt"
    :params="params"
    :okParams="getokParams"
    @cancel="cancel"
    @ok="onOk">
    <template #title="{ name }">
      <div class="flex items-center" :title="name">
        <IconText
          text="S"
          class="bg-blue-badge-s mr-2 text-3"
          style="width: 16px; height: 16px;" />
        <span class="truncate flex-1">{{ name }}</span>
      </div>
    </template>
  </MoveModal>
</template>
