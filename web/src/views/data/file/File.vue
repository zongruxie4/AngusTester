<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Drawer, Icon, Input, modal, notification, Table } from '@xcan-angus/vue-ui';
import { download, toClipboard } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

import { fileApi, space } from '@/api/storage';
import { parseQuery } from '@/utils/url';
import { columns, CrumbType, SearchType, SortType, SourceType, SPACE_PERMISSIONS } from './PropsType';
import { FileCapacity, FileCrumb, FileIcon, FileSearch, FileSort, FileUpload, SpaceInfo } from './components';

type TargetType = 'file' | 'directory' | undefined

const Share = defineAsyncComponent(() => import('@/views/data/file/share/index.vue'));
const MoveModal = defineAsyncComponent(() => import('./components/move.vue'));

const route = useRoute();
const loading = ref(false); // 表格loading
const parentDirectoryId = ref<string>('-1');
const spaceId = ref('');
const spaceName = ref('');
const shareVisible = ref(false);
// const operateFileId = ref('');
// const operateFileType = ref<'APIS' | 'FILE' | 'PROJECT'>('FILE');
const state = reactive<{
    dataSource: SourceType[],
    crumb: CrumbType[],
    pagination: any,
    searchParam: SearchType,
    sortParam: SortType,
    selectedRowKeys:string[]
      }>({
        dataSource: [],
        pagination: {
          current: 1,
          pageSize: 10,
          total: 0
        },
        searchParam: {},
        crumb: [],
        sortParam: {},
        selectedRowKeys: []
      });

/**
 * selectedRowKeys 勾选数据id的集合
 */

const drawerMenu = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('fileSpace.drawer.basicInfo'),
    key: 'info'
  }
  // {
  //   icon: 'icon-fenxiang',
  //   title: '分享',
  //   id: 'share'
  // }
];
const targetId = ref(''); // 点击行选中的数据 Id;
const targetType = ref<TargetType>();

const onSelectChange = (keys:string[]) => {
  state.selectedRowKeys = keys;
};
const rowSelection = computed(() => {
  return {
    selectedRowKeys: state.selectedRowKeys,
    onChange: onSelectChange,
    onSelect: (_record, _selected, _selectedRows, e) => e.stopPropagation()
  };
});

// 鼠标移入行显示操作 移出隐藏
const customRow = (record: SourceType) => {
  return {
    onClick: () => {
      if (record.id === targetId.value) {
        targetId.value = '';
        targetType.value = undefined;
        sideRef.value.close();
      } else {
        targetId.value = record.id;
        targetType.value = record.type.value.toLowerCase() as TargetType;
        // if (!activeDrawerKey.value) {
        //   sideRef.value.open('info');
        // }
      }
    }
  };
};

// 重命名显示输入框获取焦点
const rename = (record: SourceType) => {
  record.renameFlag = true;
  record.cacheName = record.name;
  nextTick(() => {
    const dom = document.getElementById(record.id + '') as HTMLInputElement;
    if (dom) {
      dom.select();
    }
  });
};

// 重命名离焦 修改文件名
const renameBlur = async (record: SourceType) => {
  const { id, name, cacheName } = record;
  if (name === cacheName || !name) {
    if (cacheName) {
      record.name = cacheName;
    }
    record.renameFlag = false;
    return;
  }

  const [error] = await space.renameFile({ id, name });
  if (error) {
    return;
  }
  notification.success(t('fileSpace.fileManagement.messages.renameSuccess'));
  record.renameFlag = false;
};

const renameEnter = (e) => {
  e.target.blur();
};

const shareAuth = ref(true);
const downloadAuth = ref(true); // 下载权限
const editAuth = ref(true); // 编辑上传权限
const deleteAuth = ref(true);

// 获取当前空间下读写对象权限
const getActionAuth = async () => {
  const [error, res] = await space.getSpaceCurrentAuth({ id: spaceId.value });
  if (error) {
    return;
  }
  const { data = {} } = res;
  const authData = data.spaceAuth
    ? (data.permissions || []).map(auth => auth.value)
    : SPACE_PERMISSIONS;
  downloadAuth.value = authData.includes('OBJECT_READ');
  editAuth.value = authData.includes('OBJECT_WRITE');
  deleteAuth.value = authData.includes('OBJECT_DELETE');
  shareAuth.value = authData.includes('SHARE');
};

// 得到文件列表
const getList = async () => {
  if (loading.value) {
    return;
  }
  const params = {
    spaceId: spaceId.value,
    pageSize: state.pagination.pageSize,
    pageNo: state.pagination.current,
    parentDirectoryId: parentDirectoryId.value,
    ...state.searchParam,
    ...state.sortParam
  };
  loading.value = true;

  const [error, res = { data: { list: [] } }] = await space.getFileList(params);
  loading.value = false;
  if (error) {
    return;
  }
  state.pagination.total = +(res.data?.total || 0);
  state.dataSource = res.data?.list.map(item => {
    return { ...item, renameFlag: false, cacheName: item.name };
  });
  targetId.value = '';
  targetType.value = undefined;
  state.selectedRowKeys = [];
};

// 得到指定层级数据
const getFileData = (record: SourceType) => {
  if (record.type.value === 'DIRECTORY' && !record.renameFlag) {
    parentDirectoryId.value = record.id;
    state.pagination.current = 1;
    getList();
  }
};

const createInput = ref<any>(null);

// 添加目录 插入数据 添加input
const create = () => {
  if (state.dataSource[0]?.id !== '-1') {
    state.dataSource.unshift({
      id: '-1',
      name: '',
      spaceId: '',
      summary: {
        usedSize: 0,
        subFileNum: 0
      },
      type: {
        message: '文件夹',
        value: 'DIRECTORY'
      },
      lastModifiedDate: '--'
    });
    nextTick(() => {
      if (createInput.value) {
        const el = createInput.value.$el.querySelector('input');
        el.focus();
      }
    });
  }
};

const showAddDirectory = computed(() => {
  return editAuth.value && state.crumb.length < 9;
});

// 添加input离焦
const createBlur = (record: SourceType) => {
  if (!record.name) {
    state.dataSource.shift();
    return;
  }
  sureAdd(record);
};

const createEnter = (e) => {
  e.target.blur();
};

// 确定添加
const sureAdd = async (record: SourceType) => {
  const params = {
    name: record.name as string,
    parentDirectoryId: parentDirectoryId.value === '-1' ? undefined : parentDirectoryId.value,
    spaceId: spaceId.value
  };
  const [error] = await space.addDirectory(params);
  if (error) {
    return;
  }
  notification.success(t('fileSpace.fileManagement.messages.addDirectorySuccess'));
  getList();
};

// 删除文件弹框
const delConfirm = (fileList = state.selectedRowKeys) => {
  modal.confirm({
    centered: true,
    content: t('fileSpace.fileManagement.messages.deleteConfirm'),
    onOk: () => {
      delFile(fileList);
    }
  });
};

const delFile = async (ids: (string | number)[]) => {
  const [error] = await space.deleteFile(ids);
  if (error) {
    return;
  }
  state.selectedRowKeys = [];
  notification.success(t('fileSpace.fileManagement.messages.deleteSuccess'));
  if (state.dataSource.length === ids.length && state.pagination.current > 1) {
    state.pagination.current--;
  }
  getList();
};

// 查询
const search = (id) => {
  parentDirectoryId.value = id;
  state.pagination.current = 1;
  getList();
};

// 排序
const sort = (params) => {
  if (!params.orderBy) {
    state.sortParam = {};
  } else {
    state.sortParam = params;
  }
  getList();
};

const copyDownloadUrl = async (record) => {
  if (record.type.value === 'FILE') {
    // const [error, res] = await space.getFileDetail(record.id);
    // if (error) {
    //   return;
    // }
    // const { file = {} } = res.data;
    // let { url } = file;
    // if (url.includes('/api/')) {
    //   const urlObj = new URL(url);
    //   urlObj.searchParams.set('access_token', cookieUtils.getTokenInfo().access_token || '');
    //   url = urlObj.toString();
    // }
    // clipboard.toClipboard(url).then(() => {
    //   notification.success('复制链接成功');
    // });
    const [error, { data }] = await space.getQuickShareUrl({ objectId: record.id });
    if (error) {
      return;
    }
    setTimeout(async () => {
      await toClipboard(data);
      notification.success(t('fileSpace.fileManagement.messages.copyLinkSuccess'));
    });
  }
};

// 下载 多个文件压缩下载 单个文件如果是目录压缩 文件直接下载
const downConfirm = (fileList = state.selectedRowKeys) => {
  if (fileList?.length > 1) {
    compressFile(fileList);
    return;
  }
  const type = state.dataSource.find(file => file.id === fileList[0])?.type.value;
  if (type === 'DIRECTORY') {
    compressFile(fileList);
  } else {
    downSingleFile(fileList[0]);
  }
};

// 压缩多个文件生成文件id 然后调用下载单个文件
const compressFile = async (ids: string[]) => {
  fileApi.compressFile({ ids, name: '压缩文件', parentDirectoryId: +parentDirectoryId.value > -1 ? parentDirectoryId.value : undefined });
};

// 下载单个文件
const downSingleFile = async (id) => {
  const [error, res] = await space.getFileDetail(id);
  if (error) {
    return;
  }
  const params = parseQuery(res.data.file.url);
  download({ filename: res.data.file.name, ...params });
};

// @TODO 需要在查询到表格数据时就替换前缀图标
const getFileIcon = (record: SourceType) => {
  let icon = '';
  if (record.type.value === 'FILE') {
    icon = 'icon-wenjian';
  } else {
    icon = 'icon-wenjianjia';
  }
  return icon;
};

// 表格翻页
const changePage = (pagination) => {
  state.pagination.current = pagination.current;
  state.pagination.pageSize = pagination.pageSize;
  getList();
};

const isShowUpload = ref(false);
const uploadFile = () => {
  isShowUpload.value = !isShowUpload.value;
};

// 关闭上传面板
const closeUpload = () => {
  isShowUpload.value = false;
  getList();
};

const sideRef = ref();
const activeDrawerKey = ref();

const openSide = (record: SourceType) => {
  targetId.value = record.id;
  targetType.value = record.type.value.toLowerCase() as TargetType;
  sideRef.value.open('info');
};

const shareIds = ref<string[]>([]);
// const share = (file) => {
//   shareIds.value = [spaceId.value, file.id].concat(state.crumb.slice(1).map(data => data.id));
//   shareVisible.value = true;
//   operateFileId.value = file.id;
//   operateFileType.value = file.type.value;
// };

onMounted(() => {
  if (route.params.id) {
    spaceId.value = route.params.id as string || '-1';
    spaceName.value = route.query.spaceName as string;
    const parentId = sessionStorage.getItem('parentDirectoryId');
    if (parentId && +parentId > -1) {
      parentDirectoryId.value = parentId;
    } else {
      state.crumb.push({ name: spaceName.value, id: '-1' });
    }
    getList();
    getActionAuth();
  }
});

onBeforeUnmount(() => {
  sessionStorage.removeItem('parentDirectoryId');
});

const loadPath = async () => {
  const [error, res = { data: {} }] = await space.getFileNavigation(parentDirectoryId.value);
  if (error) {
    return;
  }
  const { spaceName, current, parentChain = [] } = res.data;
  state.crumb = [{ name: spaceName, id: '-1' }, ...(parentChain || []), current];
};

watch(() => parentDirectoryId.value, () => {
  sessionStorage.setItem('parentDirectoryId', parentDirectoryId.value);
  if (+parentDirectoryId.value > -1) {
    // 在路径中点击某个历史文件夹
    const currentIndex = state.crumb.findIndex(crumb => crumb.id === parentDirectoryId.value);
    if (currentIndex > -1) {
      state.crumb = state.crumb.slice(0, currentIndex + 1);
      return;
    }
    // 在 table 中点击进入某新文件夹
    const currentFirectory = state.dataSource.find(directory => directory.id === parentDirectoryId.value);
    if (currentFirectory) {
      state.crumb.push({ id: currentFirectory.id, name: currentFirectory.name });
      return;
    }
    // 查询到某文件路径
    loadPath();
  } else {
    state.crumb = [{ name: spaceName.value, id: '-1' }];
  }
});

const jumpPath = (id: string) => {
  parentDirectoryId.value = id;
  state.pagination.current = 1;
  getList();
};

const moveVisible = ref(false);
const moveIds = ref<string[]>([]);
const handleMove = (record: SourceType) => {
  moveVisible.value = true;
  moveIds.value = [record.id];
};

const handleMultiMove = () => {
  moveVisible.value = true;
  moveIds.value = state.selectedRowKeys as string[];
};
const confirmMove = async (target: {targetDirectoryId?: string, targetSpaceId: string}) => {
  if (target.targetDirectoryId === parentDirectoryId.value) {
    moveVisible.value = false;
    return;
  }
  if (parentDirectoryId.value === '-1' && target.targetSpaceId === spaceId.value && !target.targetDirectoryId) {
    moveVisible.value = false;
    return;
  }
  const [error] = await space.moveFile({ objectIds: moveIds.value, ...target });
  if (error) {
    return;
  }
  moveVisible.value = false;
  if (state.dataSource.length === moveIds.value.length && state.pagination.current > 1) {
    state.pagination.current--;
  }
  getList();
};

</script>
<template>
  <div class="flex text-theme-content h-full">
    <div class="pl-10 pr-10 pb-12 h-full min-w-0 flex-1">
      <div class="flex justify-between items-center h-16.25">
        <FileCrumb :crumb="state.crumb" @jump="jumpPath" />
        <div class="flex items-center text-3">
          <FileIcon
            v-if="showAddDirectory"
            :title="t('fileSpace.fileManagement.directory')"
            icon="icon-chuangjianwenjianjia"
            @click="create" />
          <FileIcon
            v-if="editAuth"
            :title="t('actions.upload')"
            icon="icon-shangchuanxiao"
            @click="uploadFile" />
          <FileIcon
            v-if="state.selectedRowKeys?.length > 0 && deleteAuth"
            :title="t('actions.delete')"
            icon="icon-qingchu"
            class="text-3.5"
            @click="delConfirm()" />
          <FileIcon
            v-if="state.selectedRowKeys?.length > 0 && editAuth"
            :title="t('actions.move')"
            icon="icon-yidong"
            @click="handleMultiMove" />
          <file-search :spaceId="spaceId" @search="search" />
          <file-sort @sort="sort" />
          <FileIcon
            :title="t('actions.refresh')"
            icon="icon-shuaxin"
            @click="getList" />
        </div>
      </div>
      <FileUpload
        v-if="isShowUpload"
        :parentDirectoryId="parentDirectoryId"
        @close="closeUpload"
        @success="getList" />
      <Table
        :pagination="state.pagination"
        size="small"
        :loading="loading"
        rowKey="id"
        :columns="columns"
        :rowClassName="(record) => record.id === targetId ? 'ant-table-row-target' : ''"
        :customRow="customRow"
        :dataSource="state.dataSource"
        :rowSelection="rowSelection"
        @change="changePage">
        <template #bodyCell="{ column,text, record }">
          <template v-if="column.dataIndex == 'name'">
            <div class="flex items-center">
              <div class="flex items-center flex-1 truncate">
                <Icon :icon="getFileIcon(record)" class="mr-2.5 flex-shrink-0 text-3.5" />
                <div
                  v-if="record.id !== '-1'"
                  :class="{flex: !!record.renameFlag}"
                  class="flex-1 items-center flex-shrink-0 truncate">
                  <template v-if="!record.renameFlag">
                    <span
                      class="cursor-pointer text-theme-text-hover"
                      :title="text"
                      @click.stop="getFileData(record)">{{ text }}</span>
                  </template>
                  <template v-else>
                    <Input
                      :id="record.id"
                      v-model:value="record.name"
                      :disabled="!record.renameFlag"
                      :allowClear="false"
                      :maxlength="400"
                      trimAll
                      @keyup.enter="renameEnter"
                      @blur="renameBlur(record)" />
                  </template>
                </div>
                <Input
                  v-if="record.id === '-1'"
                  ref="createInput"
                  v-model:value="record.name"
                  trimAll
                  :placeholder="t('fileSpace.fileManagement.createDirectory')"
                  :maxlength="400"
                  @click="(e) => e.stopPropagation()"
                  @keyup.enter="createEnter"
                  @blur="createBlur(record)" />
              </div>
            </div>
          </template>
          <template v-if="column.dataIndex === 'fileNum'">
            <span>{{ record.type.value === 'DIRECTORY' ? record.summary.subFileNum : '--' }}</span>
          </template>
          <template v-if="column.dataIndex === 'subDirectoryNum'">
            <span>{{ record.type.value === 'DIRECTORY' ? record.summary.subDirectoryNum : '--' }}</span>
          </template>
          <template v-if="column.dataIndex == 'size'">
            <span>{{ record.summary.usedSize }}</span>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <div
              v-show="record.id !== '-1'"
              class="text-3 whitespace-nowrap">
              <!-- <Button
                :disabled="!shareAuth"
                type="text"
                size="small"
                class="!h-6 px-0"
                @click.stop="share(record)">
                <Icon icon="icon-fenxiang" class="align-text-bottom mr-0.5" />
                分享
              </Button> -->
              <Button
                :disabled="!deleteAuth"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="delConfirm([record.id])">
                <Icon icon="icon-qingchu" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.delete') }}
              </Button>
              <Button
                :disabled="!editAuth"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="rename(record)">
                <Icon icon="icon-bianji" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.rename') }}
              </Button>
              <Button
                :disabled="!editAuth"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="handleMove(record)">
                <Icon icon="icon-yidong" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.move') }}
              </Button>
              <Button
                type="text"
                size="small"
                class="!h-6"
                @click.stop="openSide(record)">
                <Icon icon="icon-fuwuxinxi" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.details') }}
              </Button>
              <Button
                :disabled="!downloadAuth || record.type.value !== 'FILE'"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="downConfirm([record.id])">
                <Icon icon="icon-daochu" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.download') }}
              </Button>
              <Button
                :disabled="!downloadAuth || record.type.value !== 'FILE'"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="copyDownloadUrl(record)">
                <Icon icon="icon-fuzhi" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.shareLink') }}
              </Button>
            </div>
          </template>
        </template>
      </Table>
      <AsyncComponent :visible="shareVisible">
        <Share
          v-model:visible="shareVisible"
          :spaceId="spaceId"
          :spaceName="spaceName"
          :defaultIds="shareIds" />
      </AsyncComponent>

      <AsyncComponent :visible="moveVisible">
        <MoveModal
          v-model:visible="moveVisible"
          :moveIds="moveIds"
          @ok="confirmMove" />
      </AsyncComponent>
    </div>

    <Drawer
      v-show="!!targetId"
      ref="sideRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="drawerMenu">
      <template #info>
        <SpaceInfo :id="targetId" :type="targetType" />
      </template>

      <template #size>
        <FileCapacity :id="targetId" />
      </template>
    </Drawer>
  </div>
</template>
  <style scoped>
.ant-input-affix-wrapper {
  @apply w-72.5 absolute left-10;
}

.ant-popover-inner-content {
  @apply py-2 px-0;
}

.disabled.ant-input-affix-wrapper-disabled {
  @apply border-none bg-transparent cursor-default;
}

.disabled.ant-input-affix-wrapper-disabled:hover {
  border-color: transparent;
}

:deep(.ant-table-wrapper .ant-table-tbody>tr.ant-table-row-selected>td) {
  background: #f7f8fb;
}

:deep(.ant-table-wrapper .ant-table-tbody>tr.ant-table-row-target>td) {
  background: #e6f7ff;
}

.disabled.ant-input-affix-wrapper-disabled :deep(.ant-input) {
  @apply cursor-default text-ellipsis select-none;
}

button.ant-btn-text {
  @apply !bg-transparent;
}
</style>
./components
