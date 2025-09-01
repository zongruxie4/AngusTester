<script lang="ts" setup>
import { Divider, Tooltip } from 'ant-design-vue';
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, IconCopy, Input, NoData, notification } from '@xcan-angus/vue-ui';

import ColumnItem from '@/components/share/columnItem/index.vue';
import { useShareList } from './composables/useShareList';
import type { ShareListItem } from './types';

const { t } = useI18n();

const Share = defineAsyncComponent(() => import('./index.vue'));

type Type = 'API' | 'PROJECT' | 'SERVICE' | 'DATA'

interface Props {
  disabled: boolean;
  id: string;
  type?: Type;
  name: string;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: '',
  type: 'API'
});

// Use the share list composable
const {
  state,
  showMore,
  remark,
  shareId,
  loadList,
  deleteShare,
  copyToClipboard,
  openEditPassword,
  updatePassword,
  cancelPasswordEdit,
  openShareDialog,
  handleShareEnd
} = useShareList();

// Initialize the composable
useShareList().init(props);

/**
 * Edit an existing share
 * 
 * @param item - Share item to edit
 */
const edit = (item: ShareListItem) => {
  shareId.value = item.id;
  state.visible = true;
};

/**
 * Add a new share
 */
const addShare = () => {
  shareId.value = undefined;
  state.visible = true;
};
</script>

<template>
  <div class="mr-5 mt-6">
    <Button
      size="default"
      type="primary"
      class="rounded text-3 leading-3 h-7"
      :disabled="disabled"
      @click="addShare">
      {{ t('fileSpace.share.shareList.addShare') }}
    </Button>
    <Divider class="my-3"></Divider>
    <Input
      v-model:value="remark"
      :placeholder="t('fileSpace.share.shareList.searchRemark')"
      size="small"
      class="mb-3" />
    <template v-if="state.list.length">
      <div
        v-for="(item,index) in state.list"
        :key="index">
        <!-- <column-item label="查看权限"
                     class-name="w-18">
          {{ item.type.message }}
        </column-item> -->
        <column-item
          :label="t('fileSpace.share.shareList.link')"
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
          :label="t('fileSpace.share.shareList.password')"
          className="w-18">
          <div class="flex items-center">
            <template v-if="!item.editPassd">
              <span>{{ item.password }}</span>
              <Icon
                icon="icon-zhongzhi"
                class="ml-2 text-gray-icon cursor-pointer"
                :class="{'cursor-not-allowed': disabled}"
                @click="!disabled && openEditPassword(item)" />
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
                @click="updatePassword(item)">
                {{ t('fileSpace.share.shareList.confirm') }}
              </Button>
              <Button
                size="small"
                class="text-3 leading-3"
                @click="cancelPasswordEdit(item)">
                {{ t('fileSpace.share.shareList.cancel') }}
              </Button>
            </template>
          </div>
        </column-item>
        <column-item
          :label="t('fileSpace.share.shareList.validityPeriod')"
          className="w-18">
          <div class="flex justify-between w-full">
            <span>
              {{ item.expiredFlag ? item.expiredDuration : t('fileSpace.share.shareList.permanentValid') }}
            </span>
            <span class="text-gray-icon">
              <Icon
                icon="icon-bianji"
                class="mr-3 cursor-pointer"
                @click="edit(item)" />
              <IconCopy class="mr-3" @click="copyToClipboard(item)" />
              <Icon
                icon="icon-qingchu"
                class="cursor-pointer text-3.5"
                @click="deleteShare(item.id)" />
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
      <Button type="link" @click="loadList(true)">{{ t('fileSpace.share.shareList.loadMore') }}</Button>
    </div>
    <AsyncComponent :visible="state.visible">
      <Share
        :id="shareId"
        v-model:visible="state.visible"
        :spaceId="props.id"
        :spaceName="props.name"
        :defaultIds="[props.id]"
        @ok="handleShareEnd" />
    </AsyncComponent>
  </div>
</template>
