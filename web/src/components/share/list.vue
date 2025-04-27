<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, watch } from 'vue';
import { Arrow, AsyncComponent, Colon, Hints, Icon, IconCopy, Input, notification, Scroll, Spin } from '@xcan-angus/vue-ui';
import { Button, Tooltip } from 'ant-design-vue';
import { clipboard, TESTER } from '@xcan-angus/tools';

import { apis } from '@/api/altester';
import { ListType, ShareObj, TargetType } from './PropsType';

const Share = defineAsyncComponent(() => import('@/components/share/index.vue'));

interface Props {
  disabled: boolean;
  name?:string;
  id?: string;
  type?: TargetType;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: true,
  id: undefined,
  name: undefined,
  type: 'API'
});

const notify = ref(0);
const loading = ref(false);
const visible = ref(false);

const list = ref<Array<ListType>>([]);
const oldList = ref<Array<ListType>>([]);

const getList = (data:ListType[]) => {
  list.value = data?.map(item => ({ ...item, seeUrl: false, seePassword: false })) || [];
  oldList.value = JSON.parse(JSON.stringify(list.value));
};

// 删除分享
const delShare = async (id:string) => {
  loading.value = true;
  const [error] = await apis.delShare(id);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('删除分享记录成功');
  notify.value++;
};

const sourceType = ref<'edit' | 'add' | 'all'>('add');
const sharedId = ref();

// 创建分享
const openShareDialog = () => {
  visible.value = true;
  sourceType.value = 'add';
  sharedId.value = undefined;
};
// 编辑
const editItem = ref<ShareObj>();
const edit = (item:ShareObj) => {
  editItem.value = item;
  visible.value = true;
  sourceType.value = 'edit';
};

// 设置密码可以编辑
const openEditPassd = (item: ListType) => {
  item.editPassd = true;
  item.tempPass = item.passd;
};

// 修改密码
const patchPassd = async (item:ListType, index:number) => {
  if (!item.tempPass) {
    notification.error('输入的密码不能为空');
    return;
  }

  if (item.tempPass === oldList.value[index].passd) {
    item.editPassd = false;
    return;
  }
  item.passd = item.tempPass;
  delete item.editPassd;
  delete item.seeUrl;
  delete item.tempPass;

  loading.value = true;
  const [error] = await apis.patchShared(item);
  loading.value = false;
  if (error) {
    return;
  }
  item.editPassd = false;
  notification.success('修改密码成功');
};

// 取消修改密码
const cancelPassd = (item: ListType) => {
  item.editPassd = false;
};

// 复制密码和链接
const copy = (item:ListType) => {
  let message;
  if (!item.publicFlag) {
    message = `名称: ${item.name}\n链接: ${item.url}\n密码: ${item.passd || ''}`;
  } else {
    message = `名称: ${item.name}\n链接: ${item.url}`;
  }
  clipboard.toClipboard(message).then(() => {
    notification.success('复制成功');
  });
};

const handleShareEnd = () => {
  notify.value++;
};

const handleSeeUrl = (item:ListType) => {
  item.seeUrl = !item.seeUrl;
};

const handleSeePassword = (share) => {
  share.seePassword = !share.seePassword;
};

watch(() => props.id, newValue => {
  if (newValue) {
    notify.value++;
  }
});

const params = computed(() => {
  return { targetId: props.id, targetType: props.type };
});
</script>
<template>
  <Spin class="h-full flex flex-col w-full" :spinning="loading">
    <Hints text="通过分享能够授权其他人在指定时间内查看接口文档和调试接口。" class="mb-2" />
    <div class="flex justify-end mb-2">
      <Button
        size="small"
        type="primary"
        class="flex items-center"
        :disabled="props.disabled"
        @click="openShareDialog">
        <Icon class="mr-1" icon="icon-jia" />
        添加
      </Button>
    </div>
    <Scroll
      v-model:spinning="loading"
      :action="`${TESTER}/apis/share/search`"
      :params="params"
      :lineHeight="56"
      :transition="false"
      :notify="notify"
      class="flex-1"
      @change="getList">
      <div
        v-for="(item,index) in list"
        :key="index"
        class="relative border border-border-divider px-2 py-1 rounded mb-2 mr-1.5 text-3 ">
        <div class="truncate cursor-pointer text-3.25">
          <Tooltip
            :overlayStyle="{maxWidth:'278px',whiteSpace:'pre-wrap'}"
            :title="item.name"
            placement="topLeft">
            {{ item.name }}
          </Tooltip>
        </div>
        <div
          :class="item.seeUrl ? 'open-info' : 'stop-info'"
          class="transition-height duration-500 overflow-hidden">
          <div class="flex mt-1">
            <span class="text-text-sub-content flex-none">链接<Colon /></span>
            <div class="break-all whitespace-normal ml-2 text-text-link hover:text-text-link-hover cursor-pointer">{{ item.url }}</div>
          </div>
          <div
            v-if="!item.publicFlag"
            style="width: 264px;"
            class="flex items-center h-7 mt-1">
            <span class="text-text-sub-content">密码<Colon /></span>
            <div class="flex items-center">
              <template v-if="!item.editPassd">
                <span class="ml-2">{{ item.seePassword ? item.passd :'*'.repeat(item.passd?.length) }}</span>
                <Icon
                  :icon="item.seePassword?'icon-zhengyan':'icon-biyan'"
                  class="ml-1 cursor-pointer -mt-0.5 hover:text-text-link-hover"
                  @click="handleSeePassword(item)" />
                <Tooltip
                  title="修改密码"
                  placement="top">
                  <template v-if="props.disabled">
                    <Icon
                      icon="icon-shuxie"
                      class="mr-1 cursor-not-allowed text-text-disabled" />
                  </template>
                  <template v-else>
                    <Icon
                      icon="icon-shuxie"
                      class="ml-2 cursor-pointer -mt-0.5"
                      @click="openEditPassd(item)" />
                  </template>
                </Tooltip>
              </template>
              <template v-else>
                <div class="-mt-1.25 flex-1 flex space-x-2 ml-2">
                  <Input
                    v-model:value="item.tempPass"
                    :allowClear="false"
                    :maxlength="20"
                    dataType="mixin-en"
                    size="small"
                    class="flex-1" />
                  <Button
                    type="primary"
                    size="small"
                    @click="patchPassd(item,index)">
                    确认
                  </Button>
                  <Button size="small" @click="cancelPassd(item)">取消</Button>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div class="flex justify-between mt-2">
          <div class="-mt-0.5 h-4 leading-4">
            <span class="text-text-sub-content">有效期<Colon /></span>
            <span class="ml-2 text-text-content">
              {{ item.expiredFlag ? item.expiredDate : '永久有效' }}
            </span>
          </div>
          <div class="flex justify-between items-center ml-2 -mr-1 -mt-1">
            <Tooltip
              title="复制链接"
              placement="top">
              <IconCopy class="mr-2" @click="copy(item)" />
            </Tooltip>
            <Tooltip
              title="编辑"
              placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-shuxie"
                  class="mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="mr-2 cursor-pointer"
                  @click="edit(item)" />
              </template>
            </Tooltip>
            <Tooltip
              title="删除"
              placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-qingchu"
                  class="mr-1 cursor-not-allowed text-text-disabled text-3.5" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-qingchu"
                  class="cursor-pointer mr-1.5 text-3.5"
                  @click="delShare(item.id)" />
              </template>
            </Tooltip>
            <Arrow :open="item.seeUrl" @click="handleSeeUrl(item)" />
          </div>
        </div>
      </div>
    </Scroll>
    <AsyncComponent :visible="visible">
      <Share
        :id="props?.id"
        v-model:visible="visible"
        class="mt-2 pr-5"
        :share="editItem"
        :source="sourceType"
        :name="props?.name"
        :sharedId="editItem?.id"
        :type="props.type"
        @ok="handleShareEnd" />
    </AsyncComponent>
  </Spin>
</template>
<style scoped>
.open-info {
  max-height: 512px;
}

.stop-info {
  max-height: 0;
}

</style>
