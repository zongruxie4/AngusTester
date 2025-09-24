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
  parentName: string;//  上级目录名称
  pid: string, // 上级目录id
  admin?: boolean; // 修复警告Missing required prop: "admin"->adminFlag改为非必传
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

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'cancel'): void;
  (e: 'ok', value: { id: string, fromId: string }): void;
}>();

const toId = ref<string>();
const fieldNames = { label: 'name', value: 'id', children: 'children' };
const defaultValue = ref<{ name: string; id: string; }>();
const cancel = () => {
  emits('cancel');
  emits('update:visible', false);
};

const onOk = async () => {
  const id = props.id;
  cancel();
  notification.success(t('actions.tips.moveSuccess'));
  emits('ok', { id: id!, fromId: props.pid });
};

onMounted(() => {
  watch([() => props.pid, () => props.parentName], ([pid, pname]) => {
    toId.value = pid;
    defaultValue.value = { name: pname, id: pid };
  }, {
    immediate: true
  });
});

const title = computed(() => {
  return t('service.moveModal.title');
});

const params = computed(() => {
  return {
    admin: props.admin,
    hasPermission: 'ADD'
  };
});

const getMoveAction = () => {
  return `${TESTER}/apis/move?projectId=${props.projectId}`;
};

const getokParams = (id: string, targetId: string) => {
  return {
    apiIds: [id],
    targetServiceId: targetId
  };
};

const filterOpt = () => {
  return false;
};

const hints = computed(() => {
  return t('service.moveModal.hints');
});

const parent = computed(() => {
  return {
    id: props.pid,
    name: props.parentName
  };
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
