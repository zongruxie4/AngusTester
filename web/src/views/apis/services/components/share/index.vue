<script lang="ts" setup>
import { defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue';
import { AsyncComponent, Icon, IconCopy, Input, NoData, notification } from '@xcan-angus/vue-ui';
import { Button, Divider, Tooltip } from 'ant-design-vue';
import { clipboard } from '@xcan-angus/tools';

import ColumnItem from '@/components/share/columnItem/index.vue';
import { apis } from 'src/api/tester';
import type { ListType, StateType } from './interface';

const Share = defineAsyncComponent(() => import('@/components/share/index.vue'));

type Type = 'API' | 'SERVICE'

interface Props {
  disabled:boolean,
  id?: string,
  type?: Type,
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: undefined,
  type: 'API'
});

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
});

const state:StateType = reactive({
  visible: false,
  list: []
});

const showMore = ref(false);

watch(() => props.id, newValue => {
  if (newValue) {
    pagination.current = 1;
    loadList();
  }
});

// 查询服务分享记录
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
// 删除分享
const delShare = async (id:string) => {
  const [error] = await apis.delShare(id);
  if (error) {
    return;
  }
  notification.success('删除分享记录成功');
  pagination.current = 1;
  loadList();
};

const sharedId = ref();
// 添加分享
const openShareDialog = () => {
  state.visible = true;
  sharedId.value = undefined;
};
// 编辑
const edit = (item:ListType) => {
  sharedId.value = item.id;
  state.visible = true;
};

// 设置密码可以编辑
const openEditPassd = (item: ListType) => {
  item.editPassd = true;
  item.tempPass = item.password;
};

// 修改密码
const patchPassd = async (item:ListType) => {
  if (!item.tempPass) {
    notification.error('输入的密码不能为空');
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
  notification.success('修改密码成功');
};

// 取消修改密码
const cancelPassd = (item: ListType) => {
  item.editPassd = false;
};

// 复制密码和链接
const copy = (item:ListType) => {
  let message;
  if (!item.public0) {
    message = `链接: ${item.url} 密码: ${item.password || ''}`;
  } else {
    message = `链接: ${item.url}`;
  }
  clipboard.toClipboard(message).then(() => {
    notification.success('复制成功');
  });
};

//
const handleShareEnd = () => {
  pagination.current = 1;
  loadList();
};

onMounted(() => {
  pagination.current = 1;
  loadList();
});
</script>

<template>
  <div class="mr-5 mt-6">
    <Button
      size="default"
      type="primary"
      class="rounded text-3 leading-3 h-7"
      :disabled="disabled"
      @click="openShareDialog">
      添加分享
    </Button>
    <Divider class="my-3"></Divider>
    <template v-if="state.list.length">
      <div
        v-for="(item,index) in state.list"
        :key="index">
        <column-item
          label="链接"
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
          label="密码"
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
                确认
              </Button>
              <Button
                size="small"
                class="text-3 leading-3"
                @click="cancelPassd(item)">
                取消
              </Button>
            </template>
          </div>
        </column-item>
        <column-item
          label="有效期"
          className="w-18">
          <div class="flex justify-between w-full">
            <span>
              {{ item.expiredFlag ? item.expiredDuration.value : '永久有效' }}
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
      <Button type="link" @click="loadList(true)">加载更多</Button>
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
