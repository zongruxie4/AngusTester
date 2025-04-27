<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { MoveModal, notification, IconText } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';

interface Props {
  type: 'api';
  visible: boolean;
  projectId: string;
  id: string | undefined;
  parentName: string;//  上级目录名称
  pid: string, // 上级目录id
  adminFlag?: boolean; // 修复警告Missing required prop: "adminFlag"->adminFlag改为非必传
}

const props = withDefaults(defineProps<Props>(), {
  type: 'api',
  visible: false,
  id: undefined,
  parentName: '',
  pid: '',
  adminFlag: false,
  projectId: undefined
});

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
  notification.success('移动成功');
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
  switch (props.type) {
    case 'api':
      return '移动接口';
    default:
      return '移动接口';
  }
});

const params = computed(() => {
  return {
    adminFlag: props.adminFlag,
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
  // return props.type === 'service' ? '移动服务到项目时，默认不授权被移动服务和接口关联用户查看项目权限，如果需要请通过项目“权限”来授权。' : '移动接口到项目或服务时，默认不授权接口关联用户查看项目和服务权限，如果需要请通过项目和服务“权限”来授权。';
  return '移动接口到服务时，默认不授权接口关联用户查看服务权限，如果需要请通过服务“权限”来授权。';
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
    :action="`${TESTER}/services/search?projectId=${props.projectId}`"
    subTitle="选择要移动到的服务"
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
