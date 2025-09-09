<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane } from '@xcan-angus/infra';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  projectName: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const TaskList = defineAsyncComponent(() => import('@/views/task/task/list/index.vue'));
const TaskDetails = defineAsyncComponent(() => import('@/views/task/task/details/index.vue'));

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const browserTabRef = ref();

const sprintId = ref<string>();
const sprintName = ref<string>();
const taskId = ref<string>();

const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((ids: string[]) => {
      if (!ids.includes('taskList')) {
        return {
          _id: 'taskList',
          value: 'taskList',
          name: t('task.title'),
          closable: false // 是否允许关闭，true - 允许关闭，false - 禁止关闭
        };
      }
    });
  }

  hashChange(route.hash);
};

const hashChange = (hash:string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  const queryParameters = queryString.split('&').reduce((prev, cur) => {
    const [key, value] = cur.split('=');
    prev[key] = value;
    return prev;
  }, {} as { [key: string]: string });

  const { sprintId: _sprintId, sprintName: _sprintName, taskId: _taskId } = queryParameters;
  sprintId.value = _sprintId;
  sprintName.value = _sprintName;
  taskId.value = _taskId;

  if (_sprintId) {
    browserTabRef.value.add(() => {
      return {
        _id: 'taskList',
        value: 'taskList',
        name: t('task.title'),
        closable: false // 是否允许关闭，true - 允许关闭，false - 禁止关闭
      };
    });
  } else {
    if (_taskId) {
      browserTabRef.value.add(() => {
        const query = queryString.replace(/&taskId=[^&]+/, '');
        return {
          _id: _taskId,
          value: 'taskDetails',
          data: { id: _taskId, query }
        };
      });
    }
  }

  router.replace('/task#task');
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#task')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `task${props.projectId}`;
});

// 添加指定的tabPane
provide('addTabPane', addTabPane);

// 获取tabPane
provide('getTabPane', getTabPane);

// 删除指定的tabPane
provide('deleteTabPane', deleteTabPane);

// 更新指定的tabPane
provide('updateTabPane', updateTabPane);

// 替换指定tabPane
provide('replaceTabPane', replaceTabPane);
</script>
<template>
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'taskList'">
        <TaskList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :projectName="props.projectName"
          :sprintId="sprintId"
          :sprintName="sprintName" />
      </template>

      <template v-else-if="record.value === 'taskDetails'">
        <TaskDetails
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :sprintId="sprintId"
          :sprintName="sprintName" />
      </template>
    </template>
  </BrowserTab>
</template>
<style scoped>
:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 36px;
  padding-left: 0;
  line-height: 20px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 34px;
  margin-top: 2px;
  line-height: 34px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 36px;
  margin: 0;
  padding: 0;
  line-height: 36px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 36px;
  line-height: 36px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}
</style>
