<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, reactive, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { AsyncComponent, AuthorizeModal, Drawer, Icon, IconRefresh, Input, modal, Table } from '@xcan-angus/vue-ui';
import { useRouter } from 'vue-router';
import { duration, STORAGE, site } from '@xcan-angus/tools';

import { space } from '@/api/storage';
import { FileCapacity, SpaceInfo } from './components';
import { PaginationType, SPACE_PERMISSIONS, SpaceInfoType } from './PropsType';

const Share = defineAsyncComponent(() => import('@/views/data/file/share/index.vue'));
const EditSpaceModal = defineAsyncComponent(() => import('@/views/data/file/editSpace/index.vue'));
const Shared = defineAsyncComponent(() => import('@/views/data/file/share/shareList.vue'));
const GlobalAuth = defineAsyncComponent(() => import('@/views/data/file/globalAuth/index.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/data/file/introduce/index.vue'));

const router = useRouter();
const appInfo = inject('appInfo', ref());
const isAdmin = inject('isAdmin', ref(false));
const projectInfo = inject('projectInfo', ref({ id: '' }));
const isPrivate = ref(false);

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const globalAuthVisible = ref(false);

const pagination = reactive<PaginationType>({
  current: 1,
  pageSize: 10,
  total: 0,
  hideOnSinglePage: true
});

const dataList = ref<SpaceInfoType[]>([]);
const tableLoading = ref(false);
const keyword = ref();
const getParams = () => {
  return {
    pageSize: pagination.pageSize,
    pageNo: pagination.current,
    filters: keyword.value ? [{ value: keyword.value, op: 'MATCH_END', key: 'name' }] : undefined
  };
};

// 获取空间数据
const loadData = async () => {
  if (tableLoading.value) {
    return;
  }
  const params = getParams();
  tableLoading.value = true;

  const [error, resp] = await space.getSpaceList({ ...params, appCode: 'AngusTester', admin: true, projectId: projectId.value });
  tableLoading.value = false;
  if (error) {
    return;
  }
  const { list, total } = resp.data || {};
  dataList.value = list || [];
  pagination.total = +total || 0;
  selectedRowKey.value = '';
  if (isAdmin.value) {
    dataList.value.forEach(space => {
      space.auth = SPACE_PERMISSIONS;
    });
    return;
  }
  if (dataList.value?.length > 0) {
    loadDataAuth(dataList.value);
  }
};

const loadDataAuth = async (list) => {
  const ids = list.map(space => space.id);
  const [error, res] = await space.getListAuth({ ids, admin: true });
  if (error) {
    return;
  }
  const authList = res.data || {};
  dataList.value.forEach(space => {
    space.auth = authList[space.id].spaceAuthFlag
      ? (authList[space.id].permissions || []).map(auth => auth.value)
      : SPACE_PERMISSIONS;
  });
};

// 当前选中的数据的 id
const selectedRowKey = ref<string>('');
const selectedRow = ref();

watch(() => keyword.value, debounce(duration.search, () => {
  pagination.current = 1;
  loadData();
}));

// 变更分页
const changePage = ({ pageSize, current }) => {
  pagination.current = current;
  pagination.pageSize = pageSize;
  loadData();
};

const selectId = ref<string>();
const authFlag = ref(false);
const selectName = ref();

// 删除
const delConfirm = (record) => {
  modal.confirm({
    content: `确认删除【${record.name}】？`,
    onOk () {
      return new Promise<void>((resolve, reject) => {
        space.delete(record.id).then(([error]) => {
          if (error) {
            // eslint-disable-next-line prefer-promise-reject-errors
            reject();
            return;
          }
          resolve();

          if (dataList.value.length === 1 && pagination.current > 1) {
            pagination.current--;
          }

          loadData();
        });
      });
    }
  });
};

// 分享
const shareVisible = ref(false);
// const share = (record) => {
//   selectId.value = record.id;
//   selectName.value = record.name;
//   shareVisible.value = true;
// };

// 权限
const authModalVisible = ref(false);
const editAuth = (record) => {
  selectId.value = record.id;
  authFlag.value = record.authFlag;
  authModalVisible.value = true;
};

const openSpace = (record) => {
  router.push({ path: `/data/file/${record.id}`, query: { spaceName: record.name } });
};

const drawer = ref();
const activeDrawerKey = ref();

const putCustom = (record) => {
  return {
    onClick: () => {
      if (record.id === selectedRowKey.value) {
        selectedRow.value = undefined;
        selectedRowKey.value = '';
        drawer.value.close();
      } else {
        selectedRowKey.value = record.id;
        selectedRow.value = record;
        // if (!activeDrawerKey.value) {
        //   drawer.value.open('info');
        // }
      }
    }
  };
};

const getNameById = computed(() => {
  if (!selectedRowKey.value) {
    return '';
  }

  const selectRow = dataList.value.find(row => row.id === selectedRowKey.value);
  return selectRow?.name;
});

const editVisible = ref(false);
const editSpace = (id:string) => {
  editVisible.value = true;
  selectId.value = id;
};

const createSpace = () => {
  editVisible.value = true;
  selectId.value = undefined;
};

const openAuthorizeModal = () => {
  globalAuthVisible.value = true;
};

const authFlagChange = ({ authFlag }:{authFlag:boolean}) => {
  const data = dataList.value;
  const targetId = selectId.value;
  for (let i = 0, len = data.length; i < len; i++) {
    if (data[i].id === targetId) {
      data[i].authFlag = authFlag;
      break;
    }
  }
};

// 保存弹窗内 空间信息
const saveSpace = async (form) => {
  const [error] = await (form.id ? space.patch({ ...form }) : space.addSpace({ ...form, projectId: projectId.value }));
  if (error) {
    return;
  }
  editVisible.value = false;
  loadData();
};

onMounted(async () => {
  isPrivate.value = await site.isPrivate();
  watch(() => projectId.value, newValue => {
    if (newValue) {
      pagination.current = 1;
      loadData();
    }
  }, {
    immediate: true
  });
});

const defaultIds = computed(() => {
  return selectId.value ? [selectId.value] : [];
});

const drawerMenu = computed(() => {
  const { quotaSize } = selectedRow.value || {};
  return [
    selectedRowKey.value && {
      icon: 'icon-fuwuxinxi',
      name: '基本信息',
      key: 'info'
    },
    selectedRowKey.value && {
      icon: 'icon-rongliang',
      name: selectedRowKey.value ? '空间容量：' + quotaSize?.value + quotaSize.unit?.message : '账户存储容量：',
      key: 'size'
    }
    // selectedRowKey.value && {
    //   icon: 'icon-fenxiang',
    //   name: '分享',
    //   key: 'share'
    // }
  ].filter(Boolean);
});

const columns = [
  {
    dataIndex: 'name',
    key: 'name',
    title: '空间名称'
  },
  {
    dataIndex: 'subdirectoryNum',
    key: 'subdirectoryNum',
    title: '文件夹数'
  },
  {
    dataIndex: 'subfileNum',
    key: 'subfileNum',
    title: '文件数'
  },
  {
    dataIndex: 'size',
    key: 'size',
    title: '已用'
  },
  {
    dataIndex: 'quotaSize',
    key: 'quotaSize',
    title: '配额'
  },
  {
    dataIndex: 'createdDate',
    key: 'createdDate',
    title: '添加时间'
  },
  {
    dataIndex: 'createdByName',
    key: 'createdByName',
    title: '添加人'
  },
  {
    dataIndex: 'action',
    key: 'action',
    title: '操作',
    width: 160
  }
];
</script>
<template>
  <div class="flex h-full">
    <div class="p-5 flex-1 overflow-y-auto">
      <div class="text-3.5 font-semibold mb-2.5">关于文件</div>
      <Introduce />
      <div class="text-3.5 font-semibold mb-2.5 mt-4">已添加空间</div>
      <div class="flex justify-between pb-3">
        <Input
          v-model:value="keyword"
          class="w-70"
          :maxlength="100"
          allowClear
          placeholder="查询空间名称">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-theme-placeholder" />
          </template>
        </Input>

        <div class="flex items-center space-x-2.5">
          <!-- <ButtonAuth
            code="DataGenerate"
            type="primary"
            href="/data/generate"
            icon="icon-shengchengshuju" /> -->
          <Button
            type="primary"
            size="small"
            href="/data/generate"
            class="flex space-x-1">
            <Icon icon="icon-shengchengshuju" />
            生成数据
          </Button>
          <!-- <ButtonAuth
            code="DataSpaceAdd"
            type="primary"
            icon="icon-create-script"
            @click="createSpace" /> -->
          <Button
            type="primary"
            size="small"
            class="flex space-x-1"
            @click="createSpace">
            <Icon icon="icon-create-script" />
            添加空间
          </Button>
          <Button
            class="flex items-center"
            size="small"
            type="default"
            @click="openAuthorizeModal">
            <Icon icon="icon-quanxian1" class="mr-1" />
            <span>空间权限</span>
          </Button>
          <Button
            :disabled="tableLoading"
            class="flex items-center"
            size="small"
            type="default"
            @click="loadData">
            <IconRefresh />
          </Button>
        </div>
      </div>
      <Table
        rowKey="id"
        size="small"
        :columns="columns"
        :pagination="pagination"
        :dataSource="dataList"
        :loading="tableLoading"
        :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
        :customRow="putCustom"
        @change="changePage">
        <template #bodyCell="{record, column}">
          <template v-if="column.dataIndex === 'name'">
            <div class="flex items-center">
              <Icon icon="icon-kongjian" class="flex-shrink-0 text-4 mr-2" />
              <div class="flex items-center w-75 space-name-wrapper">
                <span class="cursor-pointer text-theme-text-hover" @click.stop="openSpace(record)">{{ record.name }}</span>
              </div>
            </div>
          </template>
          <template v-if="column.dataIndex === 'quotaSize'">
            {{ record.quotaSize ? (record.quotaSize.value + record.quotaSize.unit.message) : '--' }}
          </template>
          <template v-if="column.dataIndex === 'action'">
            <div class="space-x-2.5 flex items-center leading-4">
              <template v-if="record.auth?.includes('MODIFY')">
                <a class="whitespace-nowrap" @click.stop="editSpace(record.id)">
                  <Icon icon="icon-bianji" class="align-text-bottom" />
                  编辑
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap">
                  <Icon icon="icon-bianji" class="align-text-bottom" />
                  编辑
                </span>
              </template>
              <template v-if="record.auth?.includes('GRANT')">
                <a class="whitespace-nowrap" @click.stop="editAuth(record)">
                  <Icon icon="icon-quanxian1" class="align-text-bottom" />
                  权限
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap">
                  <Icon icon="icon-quanxian1" class="align-text-bottom" />
                  权限
                </span>
              </template>
              <!-- <template v-if="record.auth?.includes('SHARE')">
                <a class="whitespace-nowrap" @click.stop="share(record)">
                  <Icon icon="icon-fenxiang" class="align-text-bottom" />
                  分享
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap">
                  <Icon icon="icon-fenxiang" class="align-text-bottom" />
                  分享
                </span>
              </template> -->
              <template v-if="record.auth?.includes('DELETE')">
                <a class="whitespace-nowrap" @click.stop="delConfirm(record)">
                  <Icon icon="icon-qingchu" class="align-text-bottom" />
                  删除
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap">
                  <Icon icon="icon-qingchu" class="align-text-bottom" />
                  删除
                </span>
              </template>
            </div>
          </template>
        </template>
      </Table>
    </div>

    <Drawer
      v-show="selectedRow"
      ref="drawer"
      v-model:activeKey="activeDrawerKey"
      :menuItems="drawerMenu">
      <template #info>
        <SpaceInfo
          v-if="activeDrawerKey==='info'"
          :id="selectedRowKey"
          type="space" />
      </template>

      <template #size>
        <FileCapacity
          v-if="activeDrawerKey==='size'"
          :id="selectedRowKey" />
      </template>

      <template #share>
        <Shared
          v-if="activeDrawerKey==='share'"
          :id="selectedRowKey"
          :name="getNameById" />
      </template>
    </Drawer>

    <AsyncComponent :visible="shareVisible">
      <Share
        :id="selectId"
        v-model:visible="shareVisible"
        :spaceId="selectId"
        :spaceName="selectName"
        :defaultIds="defaultIds" />
    </AsyncComponent>

    <AsyncComponent :visible="editVisible">
      <EditSpaceModal
        :id="selectId"
        v-model:visible="editVisible"
        @ok="saveSpace" />
    </AsyncComponent>

    <AsyncComponent :visible="authModalVisible">
      <AuthorizeModal
        v-model:visible="authModalVisible"
        enumKey="SpacePermission"
        :appId="appInfo?.id"
        :listUrl="`${STORAGE}/space/auth?spaceId=${selectId}`"
        :delUrl="`${STORAGE}/space/auth`"
        :addUrl="`${STORAGE}/space/${selectId}/auth`"
        :updateUrl="`${STORAGE}/space/auth`"
        :enabledUrl="`${STORAGE}/space/${selectId}/auth/enabled`"
        :initStatusUrl="`${STORAGE}/space/${selectId}/auth/status`"
        onTips="开启权限控制后，需要手动授权后才会有相应操作权限。"
        offTips="无权限限制，账号中的所有用户都可以查看、操作，默认不开启权限控制。"
        title="空间权限"
        @change="authFlagChange" />
    </AsyncComponent>

    <AsyncComponent :visible="globalAuthVisible">
      <GlobalAuth v-model:visible="globalAuthVisible" :appId="appInfo?.id" />
    </AsyncComponent>
  </div>
</template>
./components
