<script lang="ts" setup>
import { defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue';
import { AsyncComponent, Icon, IconCopy, Input, NoData, notification } from '@xcan-angus/vue-ui';
import { Button, Divider, Tooltip } from 'ant-design-vue';
import { toClipboard } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import ColumnItem from '@/components/share/ColumnItem.vue';
import { apis } from '@/api/tester';

const Share = defineAsyncComponent(() => import('@/components/share/index.vue'));

// Define interfaces directly in the component file
export interface ListType{
  targetId: string;
  apiIds?: string[];
  expiredDuration: {
    value: string;
    unit: {
      value: string;
      message: string;
    }
  };
  password: string;
  url?: string;
  id: string;
  editPassd?: boolean;
  tempPass?: string;
  public0: boolean;
  expiredFlag?: boolean;
  targetType: 'SERVICE' | 'API';
  remark?: string;
}

export interface StateType{
  visible: boolean;
  list: Array<ListType>;
}

type Type = 'API' | 'SERVICE'

interface Props {
  disabled: boolean;
  id?: string;
  type?: Type;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: undefined,
  type: 'API'
});

const { t } = useI18n();

// Reactive pagination state for share list
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
});

// Reactive state for share modal and list data
const state: StateType = reactive({
  visible: false,
  list: []
});

// Reactive reference for showing load more button
const showMore = ref(false);

/**
 * Load share list data with pagination support
 * @param loadMore - Whether to load more data (for pagination)
 */
const loadList = async (loadMore = false) => {
  if (loadMore) {
    pagination.current += 1;
  }

  const params = {
    pageNo: pagination.current,
    pageSize: pagination.pageSize,
    targetId: props.id,
    targetType: props.type
  };

  const [error, res = { data: { list: [] } }] = await apis.getShareList(params);
  if (error) {
    return;
  }

  if (loadMore) {
    state.list.push(...res.data.list);
  } else {
    state.list = res.data.list || [];
  }

  if (state.list.length === +res.data.total) {
    showMore.value = false;
  } else {
    showMore.value = true;
  }
};

/**
 * Delete a share item
 * @param id - The ID of the share item to delete
 */
const delShare = async (id: string) => {
  const [error] = await apis.deleteShare(id);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.deleteSuccess'));
  pagination.current = 1;
  loadList();
};

// Reactive reference for the ID of the share being edited
const sharedId = ref<string | undefined>();

/**
 * Open the share dialog for creating a new share
 */
const openShareDialog = () => {
  state.visible = true;
  sharedId.value = undefined;
};

/**
 * Edit an existing share item
 * @param item - The share item to edit
 */
const edit = (item: ListType) => {
  sharedId.value = item.id;
  state.visible = true;
};

/**
 * Enable password editing for a share item
 * @param item - The share item to edit password for
 */
const openEditPassd = (item: ListType) => {
  item.editPassd = true;
  item.tempPass = item.password;
};

/**
 * Update the password for a share item
 * @param item - The share item to update password for
 */
const patchPassd = async (item: ListType) => {
  if (!item.tempPass) {
    notification.error(t('service.shareModal.messages.passwordRequired'));
    return; // Add early return to prevent further execution
  }

  const { expiredDuration, apiIds, id, public0, remark, url } = item;
  const expiredFlag = !!expiredDuration.value;

  const params = {
    apiIds: apiIds || undefined,
    id,
    url,
    public0,
    remark,
    password: item.tempPass,
    expiredFlag,
    expiredDuration: expiredFlag ? { ...expiredDuration, unit: expiredDuration.unit.value } : undefined
  };

  const [error] = await apis.patchShared(params);
  if (error) {
    return;
  }

  item.password = item.tempPass as string;
  item.editPassd = false;
  notification.success(t('service.shareModal.messages.updatePasswordSuccess'));
};

/**
 * Cancel password editing for a share item
 * @param item - The share item to cancel password editing for
 */
const cancelPassd = (item: ListType) => {
  item.editPassd = false;
};

/**
 * Copy share link and password to clipboard
 * @param item - The share item to copy
 */
const copy = (item: ListType) => {
  let message;
  if (!item.public0) {
    message = t('service.shareModal.copyFormat.withPassword', { url: item.url, password: item.password || '' });
  } else {
    message = t('service.shareModal.copyFormat.withoutPassword', { url: item.url });
  }
  toClipboard(message).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  });
};

/**
 * Handle share operation completion
 */
const handleShareEnd = () => {
  pagination.current = 1;
  loadList();
};

// Watch for ID prop changes and reload list
watch(() => props.id, newValue => {
  if (newValue) {
    pagination.current = 1;
    loadList();
  }
});

// Load initial data on component mount
onMounted(() => {
  pagination.current = 1;
  loadList();
});
</script>

<template>
  <div class="mr-5 mt-6">
    <Button
      size="middle"
      type="primary"
      class="rounded text-3 leading-3 h-7"
      :disabled="disabled"
      @click="openShareDialog">
      {{ t('service.shareModal.actions.addShare') }}
    </Button>
    <Divider class="my-3"></Divider>
    <template v-if="state.list.length">
      <div
        v-for="(item,index) in state.list"
        :key="index">
        <column-item
          :label="t('common.link')"
          className="w-18">
          <Tooltip>
            <template #title>
              {{ item.url }}
            </template>
            <p class="text-text-link w-40 overflow-ellipsis overflow-hidden"> {{ item.url }}</p>
          </Tooltip>
        </column-item>
        <column-item
          v-if="!item.public0"
          :label="t('common.password')"
          className="w-18">
          <div class="flex items-center">
            <template v-if="!item.editPassd">
              <span>{{ item.password }}</span>
              <Icon
                icon="icon-zhongzhi"
                class="ml-2 text-gray-icon cursor-pointer"
                :class="{'cursor-not-allowed': disabled}"
                @click="!disabled && openEditPassd(item)" />
            </template>
            <template v-else>
              <Input
                v-model:value="item.tempPass"
                :allowClear="false"
                size="small" />
              <Button
                type="primary"
                size="small"
                class="mx-2.5 text-3 leading-3"
                @click="patchPassd(item)">
                {{ t('actions.confirm') }}
              </Button>
              <Button
                size="small"
                class="text-3 leading-3"
                @click="cancelPassd(item)">
                {{ t('actions.cancel') }}
              </Button>
            </template>
          </div>
        </column-item>
        <column-item
          :label="t('service.shareModal.form.validityPeriod')"
          className="w-18">
          <div class="flex justify-between w-full">
            <span>
              {{ item.expiredFlag ? item.expiredDuration.value : t('service.shareModal.form.permanent') }}
            </span>
            <span class="text-gray-icon">
              <Icon
                icon="icon-bianji"
                class="mr-3 cursor-pointer"
                @click="edit(item)" />
              <IconCopy class="mr-3" @click="copy(item)" />
              <Icon
                icon="icon-qingchu"
                class="cursor-pointer text-3.5"
                :class="{'cursor-not-allowed': disabled}"
                @click="!disabled && delShare(item.id)" />
            </span>
          </div>
        </column-item>
        <Divider class="my-2"></Divider>
      </div>
    </template>
    <template v-else>
      <NoData></NoData>
    </template>
    <div v-show="showMore" class="w-full text-center">
      <Button type="link" @click="loadList(true)">{{ t('common.loadMore') }}</Button>
    </div>
    <AsyncComponent :visible="state.visible">
      <Share
        v-if="state.visible"
        :id="sharedId"
        v-model:visible="state.visible"
        :targetId="props.id"
        :targetType="props.type"
        @ok="handleShareEnd" />
    </AsyncComponent>
  </div>
</template>
