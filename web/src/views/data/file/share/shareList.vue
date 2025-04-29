<script lang="ts" setup>
import { Divider, Tooltip } from 'ant-design-vue';
import { defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue';
import { AsyncComponent, Icon, IconCopy, Input, NoData, notification } from '@xcan-angus/vue-ui';
import { clipboard } from '@xcan-angus/tools';

import ColumnItem from '@/components/share/columnItem/index.vue';
import type { ListType, StateType } from './interface';
import { space } from '@/api/storage';

const Share = defineAsyncComponent(() => import('./index.vue'));

type Type = 'API' | 'PROJECT' | 'SERVICE' | 'DATA'

interface Props {
  disabled:boolean,
  id: string,
  type?: Type,
  name: string
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: '',
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
const remark = ref();

watch(() => remark.value, () => {
  pagination.current = 1;
  loadList();
});

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
    spaceId: props.id,
    filters: remark.value ? [{ value: remark.value, key: 'remark', op: 'MATCH_END' }] : undefined
  };
  const [error, res = { data: { list: [] } }] = await space.loadSharedList(params);
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
  const [error] = await space.delShare([id]);
  if (error) {
    return;
  }
  notification.success('删除分享记录成功');
  pagination.current = 1;
  loadList();
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
  const params = {
    id: item.id,
    expiredFlag: item.expiredFlag,
    objectIds: item.objectIds,
    public0: item.public0,
    remark: item.remark,
    expiredDuration: item.expiredDuration ? { ...item.expiredDuration, unit: item.expiredDuration.unit.value } : undefined,
    password: item.tempPass
  };
  const [error] = await space.patchShare(params);
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

const shareId = ref();
// 编辑分享
const edit = (item) => {
  shareId.value = item.id;
  state.visible = true;
};

// 添加分享
const openShareDialog = () => {
  shareId.value = undefined;
  state.visible = true;
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
    <Input
      v-model:value="remark"
      placeholder="查询备注信息"
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
              {{ item.expiredFlag ? item.expiredDuration : '永久有效' }}
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
                @click="delShare(item.id)" />
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
        :id="shareId"
        v-model:visible="state.visible"
        :spaceId="props.id"
        :spaceName="props.name"
        :defaultIds="[props.id]"
        @ok="handleShareEnd" />
    </AsyncComponent>
  </div>
</template>
