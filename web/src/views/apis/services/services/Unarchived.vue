<script setup lang="ts">
import { computed, inject, ref, Ref } from 'vue';
import { Dropdown, HttpMethodText, Icon, IconRefresh, notification, Scroll } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';


import {UnarchivedItem} from "@/views/apis/services/services/PropsType";

interface Props {
  keywords: string | undefined;
  total: number;
}

const props = withDefaults(defineProps<Props>(), {
  keywords: undefined,
  total: 0
});
const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'delete'): void;
  (e: 'deleteAll'): void;
  (e: 'refresh'): void;
}>();

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
const projectId = inject<Ref<string>>('projectId', ref(''));

// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

const scrollRef = ref();

const notify = ref(0);
const loading = ref(false);
const dataList = ref<UnarchivedItem[]>([]);

const scrollChange = (data:UnarchivedItem[]) => {
  dataList.value = data;
};

const toDelete = async (value:UnarchivedItem) => {
  const id = value.id;
  const [error] = await apis.deleteUnarchivedApi(id);
  if (error) {
    return;
  }

  scrollDelete(id);
  emit('delete');

  const _key = getKey(value.protocol?.value);
  deleteTabPane([id + _key]);
  notification.success(t('actions.tips.deleteSuccess'));
};

const scrollDelete = (id:string) => {
  if (typeof scrollRef.value?.delete === 'function') {
    scrollRef.value.del(id);
  }
};

const deleteAll = async () => {
  const [error] = await apis.deleteAllUnarchivedApi();
  if (error) {
    return;
  }

  // 删除已经打开的tab
  const delIds = dataList.value.map(item => item.id + getKey(item.protocol?.value));
  deleteTabPane(delIds);

  dataList.value = [];

  if (typeof scrollRef.value?.pureDelAll === 'function') {
    scrollRef.value.pureDelAll();
  }

  emit('deleteAll');
  notification.success(t('actions.tips.deleteAllSuccess'));
};

const refresh = () => {
  notify.value++;
  emit('refresh');
};

const select = (data:UnarchivedItem) => {
  const value = getKey(data.protocol?.value);
  addTabPane({ ...data, name: data.summary, value, unarchived: true, _id: data.id + value, pid: data.id + value });
};

const getKey = (protocol:string) => {
  return protocol.includes('ws') ? 'socket' : 'API';
};

const menuItemClick = ({ key }:{key:'delete'}, value:UnarchivedItem) => {
  switch (key) {
    case 'delete':
      toDelete(value);
      break;
  }
};

const buttonDisabled = computed(() => {
  return !props.total || loading.value;
});

const params = computed(() => {
  const filters: { key: 'summary'; op: 'MATCH', value: string; }[] = [];
  if (props.keywords) {
    filters.push({ key: 'summary', op: 'MATCH', value: props.keywords });
  }
  return {
    filters,
    targetType: 'API',
    projectId: projectId.value
  };
});

const menuItems:{
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

defineExpose({
  refresh: () => {
    notify.value++;
  }
});
</script>

<template>
  <div class="h-107.25">
    <Scroll
      ref="scrollRef"
      v-model:spinning="loading"
      :action="`${TESTER}/apis/unarchived?fullTextSearch=true`"
      :params="params"
      :lineHeight="32"
      :notify="notify"
      class="py-2"
      style="height: calc(100% - 29px);"
      @change="scrollChange">
      <Dropdown
        v-for="item in dataList"
        :key="item.id"
        :trigger="['contextmenu']"
        :menuItems="menuItems"
        class="mb-1"
        @click="menuItemClick($event, item)">
        <div class="item-container leading-5 flex items-center pl-3.5 py-1 pr-2 space-x-2 rounded cursor-pointer" @click="select(item)">
          <div class="flex-1 truncate space-y-1">
            <div class="truncate" :title="item.summary">{{ item.summary }}</div>
            <div class="flex items-center space-x-2">
              <HttpMethodText style="min-width: auto;" :value="item.method" />
              <div class="truncate text-theme-sub-content" :title="item.endpoint">{{ item.endpoint }}</div>
            </div>
          </div>

          <div class="flex-shrink-0 flex items-center space-x-2 text-3.5 invisible">
            <span :title="t('actions.delete')" @click="toDelete(item)">
              <Icon icon="icon-qingchu" class="cursor-pointer text-theme-text-hover" />
            </span>
          </div>
        </div>
      </Dropdown>
    </Scroll>

    <div class="flex items-center justify-end space-x-3 px-5 border-t border-solid border-theme-text-box">
      <Button
        :disabled="buttonDisabled"
        size="small"
        type="text"
        @click="deleteAll">
        <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
        <span>{{ t('service.sidebar.deleteAllAction') }}</span>
      </Button>
      <Button
        :disabled="loading"
        size="small"
        type="text"
        @click="refresh">
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
