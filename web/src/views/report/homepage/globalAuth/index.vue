<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { enumLoader } from '@xcan-angus/tools';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Modal } from '@xcan-angus/vue-ui';

const GroupSet = defineAsyncComponent(() => import('@/views/report/homepage/globalAuth/groupSet/index.vue'));
const AuthSet = defineAsyncComponent(() => import('@/views/report/homepage/globalAuth/authSet/index.vue'));

interface Props {
  projectId: string;
  appId: string;
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  appId: undefined,
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
}>();

const activeKey = ref<'user'|'dept'|'group'>('user');
const checkedUserId = ref<string>();
const checkedGroupId = ref<string>();
const checkedDeptId = ref<string>();

const permissions = ref<{value:string, label:string}[]>([]);
const loaded = ref(false);

const cancel = () => {
  emit('update:visible', false);
};

const loadEnums = async () => {
  const [error, res] = await enumLoader.load('ReportPermission');
  if (!error && res?.length) {
    permissions.value = res.map(item => ({ label: item.message, value: item.value }));
  }
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    activeKey.value = 'user';
    checkedUserId.value = undefined;
    checkedGroupId.value = undefined;
    checkedDeptId.value = undefined;

    loaded.value = false;
    // permissions.value = [];

    loadEnums();
  }, { immediate: true });
});

const text = '授权给指定的"功能"操作权限给用户、部门、组，授予权限时“查看”权限是必须的。用户只能授权自己有授权权限的功能。';

const bodyStyle = {
  padding: '0 20px',
  height: 'calc(100% - 70px)'
};
</script>

<template>
  <Modal
    title="报告权限"
    :footer="false"
    :visible="props.visible"
    :bodyStyle="bodyStyle"
    style="width: 98%;height: 95%;"
    wrapClassName="authorize-modal-wrapper"
    @cancel="cancel">
    <div class="h-full pt-2">
      <Hints :text="text" />
      <Tabs
        v-model:activeKey="activeKey"
        size="small"
        style="height: calc(100% - 18px);">
        <TabPane key="user" tab="用户">
          <GroupSet
            key="user"
            v-model:checkedId="checkedUserId"
            v-model:loaded="loaded"
            type="user"
            class="flex-shrink-0 flex-grow-0 w-75 mr-4"
            :visible="props.visible"
            :appId="props.appId" />
          <AuthSet
            v-if="loaded"
            key="user"
            type="user"
            class="flex-1"
            :projectId="props.projectId"
            :authObjectId="checkedUserId"
            :permissions="permissions" />
        </TabPane>
        <TabPane key="dept" tab="部门">
          <GroupSet
            key="dept"
            v-model:checkedId="checkedDeptId"
            v-model:loaded="loaded"
            type="dept"
            class="flex-shrink-0 flex-grow-0 w-75 mr-4"
            :visible="props.visible"
            :appId="props.appId" />
          <AuthSet
            v-if="loaded"
            key="dept"
            type="dept"
            class="flex-1"
            :projectId="props.projectId"
            :authObjectId="checkedDeptId"
            :permissions="permissions" />
        </TabPane>
        <TabPane key="group" tab="组">
          <GroupSet
            key="group"
            v-model:checkedId="checkedGroupId"
            v-model:loaded="loaded"
            type="group"
            class="flex-shrink-0 flex-grow-0 w-75 mr-4"
            :visible="props.visible"
            :appId="props.appId" />
          <AuthSet
            v-if="loaded"
            key="group"
            type="group"
            class="flex-1"
            :projectId="props.projectId"
            :authObjectId="checkedGroupId"
            :permissions="permissions" />
        </TabPane>
      </Tabs>
    </div>
  </Modal>
</template>
<style>
.authorize-modal-wrapper .ant-modal-content {
  height: 100%;
}

.authorize-modal-wrapper  .ant-tabs .ant-tabs-nav {
  margin: 0;
}

.authorize-modal-wrapper  .ant-tabs .ant-tabs-tabpane {
  display: flex;
  padding: 8px 0;
}
</style>
