<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button, Popconfirm, Tag, TypographyParagraph } from 'ant-design-vue';
import { Colon, Icon, Image, NoData, notification, SearchPanel, Spin } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/tools';

import GridList from '@/views/apis/server/list/gridList/index.vue';
import { ServerInfo } from '../PropsType';
import { cloneDeep } from 'lodash-es';

type FilterItem = { key: string; op: string; value: string; };

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const Introduce = defineAsyncComponent(() => import('@/views/apis/server/list/introduce/index.vue'));

const router = useRouter();

const MAX_NUM = 200;
const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const dataList = ref<{serviceId:string;serviceName:string;server:ServerInfo}[]>([]);
const showList = ref<{serviceId:string;serviceName:string;server:ServerInfo}[]>([]);

const getVariableLength = (data) => {
  return !!Object.keys(data.server?.variables || {}).length;
};

const toCreateServer = () => {
  router.push('/apis#server?source=add');
};

const toUpdate = async (data) => {
  const [error] = await http.put(`${TESTER}/services/${data.serviceId}/schema/server/${data.server?.['x-xc-id']}/apis/sync`);
  if (error) {
    return;
  }

  notification.success('更新到已关联接口成功');
};

const toClone = async (data:{serviceId:string;serviceName:string;server:ServerInfo}) => {
  const params:{
    description?:string;
    url:string;
    variables:{
      [key:string]:{
        default:string;
        description:string;
        enum:string[];
      }
    };
  } = {
    description: data.server?.description,
    url: data.server?.url,
    variables: data.server?.variables
  };

  loading.value = true;
  const [error] = await http.put(`${TESTER}/services/${data.serviceId}/schema/server`, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('克隆成功');
  searchedFlag.value = false;
  loadData();
};

const toDelete = async (data:{serviceId:string;serviceName:string;server:ServerInfo}) => {
  loading.value = true;
  const [error] = await http.del(`${TESTER}/services/${data.serviceId}/schema/server`, { ids: [data.server?.['x-xc-id']] });
  loading.value = false;
  if (error) {
    return;
  }

  searchedFlag.value = false;
  loadData();
};

const searchChange = (value: FilterItem[]): void => {
  searchedFlag.value = true;
  if (value.length) {
    const description = value.find(item => item.key === 'description');
    const serviceId = value.find(item => item.key === 'serviceId');

    showList.value = dataList.value.filter((item) => {
      let flag = false;
      if (description && serviceId) {
        flag = item.server?.description?.includes(description.value) && item.serviceId === serviceId.value;
      } else if (description) {
        flag = item.server?.description?.includes(description.value);
      } else if (serviceId) {
        flag = item.serviceId === serviceId.value;
      }

      return flag;
    });

    return;
  }

  showList.value = cloneDeep(dataList.value);
};

const refresh = () => {
  searchedFlag.value = false;
  loadData();
};

const loadData = async () => {
  const params: {
    projectId: string;
  } = {
    projectId: props.projectId
  };

  loading.value = true;
  const [error, res] = await http.get(`${TESTER}/services/schema/server`, params);
  loaded.value = true;
  loading.value = false;

  if (error) {
    dataList.value = [];
    return;
  }

  const data = res?.data || [];
  dataList.value = data.map((item) => {
    return {
      ...item,
      editLinkUrl: `/apis#server?serviceId=${item.serviceId}&serverId=${item.server?.['x-xc-id']}`
    };
  });

  showList.value = cloneDeep(dataList.value);
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    searchedFlag.value = false;
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    searchedFlag.value = false;
    loadData();
  }, { immediate: true });
});

const searchOptions = [
  {
    type: 'input',
    placeholder: '查询描述',
    valueKey: 'description',
    allowClear: true,
    maxlength: 150,
    trim: true
  },
  {
    type: 'select',
    placeholder: '选择服务',
    valueKey: 'serviceId',
    allowClear: true,
    fieldNames: { label: 'name', value: 'id' },
    action: `${TESTER}/services/search?projectId=${props.projectId}`
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />

    <div class="flex items-center text-3.5 font-semibold mb-1">
      <span class="flex-shrink-0 mr-1">已添加的服务器</span>
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 cursor-pointer mr-1" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap text-3 font-normal text-theme-sub-content">
        <span>已添加{{ dataList.length }}个，最多允许添加{{ MAX_NUM }}个。</span>
      </div>
    </div>

    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-7">
            <span>尚未添加任何服务器，立即</span>

            <Button
              type="link"
              size="small"
              class="text-3.5 py-0 px-0 mx-1"
              @click="toCreateServer">
              <span>添加服务器</span>
            </Button>
          </div>
        </div>

        <template v-else>
          <div class="flex items-center justify-between mt-2.5 mb-3.5 space-x-5">
            <SearchPanel
              class="flex-1"
              :options="searchOptions"
              @change="searchChange" />

            <div class="flex-shrink-0 flex items-center space-x-3">
              <Button
                :disabled="dataList.length>=MAX_NUM"
                type="primary"
                size="small"
                class="flex items-center flex-shrink-0"
                @click="toCreateServer">
                <div class="flex items-center">
                  <Icon icon="icon-jia" class="text-3.5" />
                  <span class="ml-1">添加服务器</span>
                </div>
              </Button>

              <Button
                type="default"
                size="small"
                class="flex items-center flex-shrink-0"
                @click="refresh">
                <Icon icon="icon-shuaxin" class="mr-1 text-3.5" />
                <span>刷新</span>
              </Button>
            </div>
          </div>

          <NoData v-if="showList.length === 0" class="flex-1" />

          <div class="flex flex-wrap">
            <div
              v-for="record in showList"
              :key="record.server['x-xc-id']"
              class="h-35.5 w-70 mb-2 mr-3 px-3 py-2.5 border rounded border-theme-text-box">
              <div class="flex items-center mb-2">
                <div class="flex-shrink-0 flex items-center mr-1.5 flex-1 min-w-0"><span class="font-semibold">ID</span><Colon /><div class="flex-1 truncate min-w-0 " :title="record.server?.['x-xc-id']">{{ record.server?.['x-xc-id'] }}</div></div>
                <Tag class="relative -top-1 mr-0 px-0.5 h-5" :color="getVariableLength(record) ? 'processing' : 'default'">{{ getVariableLength(record) ? '有变量' : '无变量' }}</Tag>
              </div>
              <div v-if="!record.server?.description" class="h-9 leading-4.5 mb-2.5 text-theme-sub-content">无描述~</div>
              <TypographyParagraph
                v-else
                class="h-9 leading-4.5 mb-2.5 text-theme-sub-content"
                :content="record.server?.description"
                :ellipsis="{ tooltip: record.server?.description, rows: 2 }" />

              <div class="flex items-center mb-2">
                <div class="flex-shrink-0 flex items-center mr-1.5"><span class="font-semibold">所属服务</span><Colon /></div>
                <div class="flex-1 truncate" :title="record.serviceName">{{ record.serviceName }}</div>
              </div>

              <div v-if="false" class="flex items-center space-x-3 mb-2.5 text-theme-sub-content">
                <Image
                  :src="record.avatar"
                  type="avatar"
                  class="flex-shrink-0 w-6 h-6 rounded-xl" />
                <div class="flex items-center space-x-3">
                  <span>{{ record.createdByName }}</span>
                  <span>{{ record.createdDate }}</span>
                </div>
              </div>

              <div class="flex items-center justify-end space-x-1.75">
                <Button
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="编辑">
                  <RouterLink :to="record.editLinkUrl" class="w-full h-full flex items-center justify-center">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                  </RouterLink>
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="更新到已关联接口">
                  <Icon
                    icon="icon-shoudongtuisong"
                    class="text-theme-text-hover cursor-pointer text-3.5"
                    @click="toUpdate(record)" />
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="克隆"
                  @click="toClone(record)">
                  <Icon icon="icon-fuzhizujian2" class="text-3.5" />
                </Button>

                <Popconfirm
                  placement="topRight"
                  title="确认删除该服务器吗？"
                  @confirm="toDelete(record)">
                  <Button
                    type="text"
                    size="small"
                    class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                    title="删除">
                    <Icon icon="icon-qingchu" class="text-3.5" />
                  </Button>
                </Popconfirm>
              </div>
            </div>
          </div>

          <!-- <GridList :itemWidth="282" :dataSource="showList">
            <template #default="record">
            </template>
          </GridList> -->
        </template>
      </template>
    </Spin>
  </div>
</template>
