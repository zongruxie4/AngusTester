<script setup lang="ts">
import { computed, inject, ref, Ref } from 'vue';
import { Dropdown, HttpMethodText, Icon, IconRefresh, notification, Scroll } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { TESTER, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';
import { CombinedTargetType } from '@/enums/enums';
import { ApisUnarchivedListInfo } from '@/views/apis/services/apis/types';

// props and emits
interface Props {
  searchKeyword: string | undefined;
  total: number;
}

const props = withDefaults(defineProps<Props>(), {
  searchKeyword: undefined,
  total: 0
});

const emit = defineEmits<{
  (e: 'delete'): void;
  (e: 'deleteAll'): void;
  (e: 'refresh'): void;
}>();

// i18n
const { t } = useI18n();

// injections
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
const projectId = inject<Ref<string>>('projectId', ref(''));
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

// refs and state
const scrollRef = ref();
const refreshCounter = ref(0);
const isLoading = ref(false);
const unarchivedApis = ref<ApisUnarchivedListInfo[]>([]);

// computed
const isDeleteAllDisabled = computed(() => {
  return !props.total || isLoading.value;
});

const fetchParams = computed(() => {
  const filters: { key: 'summary'; op: SearchCriteria.OpEnum; value: string; }[] = [];
  if (props.searchKeyword) {
    filters.push({ key: 'summary', op: SearchCriteria.OpEnum.Match, value: props.searchKeyword });
  }
  return {
    filters,
    targetType: CombinedTargetType.API,
    projectId: projectId.value
  };
});

const contextMenuItems:{
  key:'delete';
  name:string;
  icon:string;
  noAuth:boolean;}[] = [
    {
      key: 'delete',
      name: t('actions.delete'),
      icon: 'icon-qingchu',
      noAuth: true
    }
  ];

// handlers
/**
 * Handle list data changes from the Scroll component and refresh local cache
 */
const handleScrollChange = (data:ApisUnarchivedListInfo[]) => {
  unarchivedApis.value = data;
};

/**
 * Delete a single unarchived API and update UI states and tabs
 */
const deleteOne = async (apiItem:ApisUnarchivedListInfo) => {
  const id = apiItem.id;
  const [error] = await apis.deleteUnarchivedApi(id);
  if (error) {
    return;
  }

  deleteFromScroll(id);
  emit('delete');

  const tabKey = getTabKeyByProtocol(apiItem.protocol?.value);
  deleteTabPane([id + tabKey]);
  notification.success(t('actions.tips.deleteSuccess'));
};

/**
 * Remove a record from the Scroll component without triggering network calls
 */
const deleteFromScroll = (id:string) => {
  if (typeof scrollRef.value?.delete === 'function') {
    scrollRef.value.del(id);
  }
};

/**
 * Delete all unarchived APIs and clear list and related tabs
 */
const deleteAllUnarchived = async () => {
  const [error] = await apis.deleteAllUnarchivedApi();
  if (error) {
    return;
  }

  const delIds = unarchivedApis.value.map(item => item.id + getTabKeyByProtocol(item.protocol?.value));
  deleteTabPane(delIds);

  unarchivedApis.value = [];

  if (typeof scrollRef.value?.pureDelAll === 'function') {
    scrollRef.value.pureDelAll();
  }

  emit('deleteAll');
  notification.success(t('actions.tips.deleteAllSuccess'));
};

/**
 * Notify Scroll to fetch again and bubble refresh event upward
 */
const triggerRefresh = () => {
  refreshCounter.value++;
  emit('refresh');
};

/**
 * Open chosen API item in a new tab pane
 */
const openInTab = (apiItem:ApisUnarchivedListInfo) => {
  const value = getTabKeyByProtocol(apiItem.protocol?.value);
  addTabPane({ ...apiItem, name: apiItem.summary, value, unarchived: true, _id: apiItem.id + value, pid: apiItem.id + value });
};

/**
 * Derive tab key by protocol value for consistent tab identity
 */
const getTabKeyByProtocol = (protocol:string) => {
  return protocol.includes('ws') ? 'socket' : 'API';
};

/**
 * Handle context menu item click actions
 */
const onMenuItemClick = ({ key }:{key:'delete'}, apiItem:ApisUnarchivedListInfo) => {
  switch (key) {
    case 'delete':
      deleteOne(apiItem);
      break;
  }
};

// expose
defineExpose({
  refresh: () => {
    refreshCounter.value++;
  }
});
</script>

<template>
  <div class="h-107.25">
    <Scroll
      ref="scrollRef"
      v-model:spinning="isLoading"
      :action="`${TESTER}/apis/unarchived?fullTextSearch=true`"
      :params="fetchParams"
      :lineHeight="32"
      :notify="refreshCounter"
      class="py-2"
      style="height: calc(100% - 29px);"
      @change="handleScrollChange">
      <Dropdown
        v-for="item in unarchivedApis"
        :key="item.id"
        :trigger="['contextmenu']"
        :menuItems="contextMenuItems"
        class="mb-1"
        @click="onMenuItemClick($event, item)">
        <div class="item-container leading-5 flex items-center pl-3.5 py-1 pr-2 space-x-2 rounded cursor-pointer" @click="openInTab(item)">
          <div class="flex-1 truncate space-y-1">
            <div class="truncate" :title="item.summary">{{ item.summary }}</div>
            <div class="flex items-center space-x-2">
              <HttpMethodText style="min-width: auto;" :value="item.method" />
              <div class="truncate text-theme-sub-content" :title="item.endpoint">{{ item.endpoint }}</div>
            </div>
          </div>

          <div class="flex-shrink-0 flex items-center space-x-2 text-3.5 invisible">
            <span :title="t('actions.delete')" @click="deleteOne(item)">
              <Icon icon="icon-qingchu" class="cursor-pointer text-theme-text-hover" />
            </span>
          </div>
        </div>
      </Dropdown>
    </Scroll>

    <div class="flex items-center justify-end space-x-3 px-5 border-t border-solid border-theme-text-box">
      <Button
        :disabled="isDeleteAllDisabled"
        size="small"
        type="text"
        @click="deleteAllUnarchived">
        <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
        <span>{{ t('service.sidebar.deleteAllAction') }}</span>
      </Button>
      <Button
        :disabled="isLoading"
        size="small"
        type="text"
        @click="triggerRefresh">
        <IconRefresh class="text-3.5 mr-1" />
        <span>{{ t('actions.refresh') }}</span>
      </Button>
    </div>
  </div>
</template>

<style scoped>

.item-container:hover {
  background-color: var(--content-tabs-bg-hover);
}

.item-container:hover .invisible {
  visibility: visible;
}

.ant-btn-text {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  line-height: 14px;
}

.ant-btn-text:not([disabled]) {
  color: var(--content-text-content);
}

.ant-btn-text:not([disabled]):hover {
  color: var(--content-special-text-hover);
}
</style>
