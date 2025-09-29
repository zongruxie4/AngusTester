import { reactive, ref, computed, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { notification } from '@xcan-angus/vue-ui';
import { download, toClipboard } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { fileApi, space } from '@/api/storage';
import { parseQuery } from '@/utils/url';
import { CrumbType, SearchType, SortType, SourceType, SPACE_PERMISSIONS } from '../types';

/**
 * <p>Composable for managing file operations including listing, CRUD operations,</p>
 * <p>navigation, and file-specific actions like download and move.</p>
 *
 * @returns Object containing reactive state and methods for file management
 */
export function useFileManagement () {
  const { t } = useI18n();
  const route = useRoute();

  // Core state
  const loading = ref(false);
  const parentDirectoryId = ref<string>('-1');
  const spaceId = ref('');
  const spaceName = ref('');

  // File list state
  const state = reactive<{
    dataSource: SourceType[];
    crumb: CrumbType[];
    pagination: any;
    searchParam: SearchType;
    sortParam: SortType;
    selectedRowKeys: string[];
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

  // Target selection state
  const targetId = ref('');
  const targetType = ref<'file' | 'directory' | undefined>();

  // Upload and operation states
  const isShowUpload = ref(false);
  const moveVisible = ref(false);
  const moveIds = ref<string[]>([]);

  // Permission states
  const shareAuth = ref(true);
  const downloadAuth = ref(true);
  const editAuth = ref(true);
  const deleteAuth = ref(true);

  /**
   * <p>Handle row selection changes in table.</p>
   *
   * @param keys - Array of selected row keys
   */
  const onSelectChange = (keys: string[]) => {
    state.selectedRowKeys = keys;
  };

  /**
   * <p>Computed row selection configuration for table.</p>
   */
  const rowSelection = computed(() => {
    return {
      selectedRowKeys: state.selectedRowKeys,
      onChange: onSelectChange,
      onSelect: (_record: any, _selected: boolean, _selectedRows: any[], e: Event) => e.stopPropagation()
    };
  });

  /**
   * <p>Custom row configuration for table interactions.</p>
   * <p>Handles row clicks and target selection.</p>
   *
   * @param record - Table row record
   * @returns Row configuration object
   */
  const customRow = (record: SourceType) => {
    return {
      onClick: () => {
        if (record.id === targetId.value) {
          targetId.value = '';
          targetType.value = undefined;
        } else {
          targetId.value = record.id;
          targetType.value = record.type.value.toLowerCase() as 'file' | 'directory';
        }
      }
    };
  };

  /**
   * <p>Get current space action authorization.</p>
   * <p>Fetches and sets permission flags for current user in the space.</p>
   */
  const getActionAuth = async () => {
    const [error, res] = await space.getSpaceCurrentAuth({ id: spaceId.value });
    if (error) {
      return;
    }

    const { data = {} } = res;
    const authData = data.spaceAuth
      ? (data.permissions || []).map((auth: any) => auth.value)
      : SPACE_PERMISSIONS;

    downloadAuth.value = authData.includes('OBJECT_READ');
    editAuth.value = authData.includes('OBJECT_WRITE');
    deleteAuth.value = authData.includes('OBJECT_DELETE');
    shareAuth.value = authData.includes('SHARE');
  };

  /**
   * <p>Load file list from API with current parameters.</p>
   * <p>Handles loading state and updates data source.</p>
   */
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

    try {
      const [error, res = { data: { list: [] } }] = await space.getFileList(params);
      if (error) {
        return;
      }

      state.pagination.total = +(res.data?.total || 0);
      state.dataSource = res.data?.list.map((item: any) => {
        return { ...item, renameFlag: false, cacheName: item.name };
      });

      targetId.value = '';
      targetType.value = undefined;
      state.selectedRowKeys = [];
    } finally {
      loading.value = false;
    }
  };

  /**
   * <p>Navigate to specified directory level.</p>
   * <p>Updates parent directory ID and reloads file list.</p>
   *
   * @param record - Directory record to navigate to
   */
  const getFileData = (record: SourceType) => {
    if (record.type.value === 'DIRECTORY' && !record.renameFlag) {
      parentDirectoryId.value = record.id;
      state.pagination.current = 1;
      getList();
    }
  };

  /**
   * <p>Create new directory with input field.</p>
   * <p>Adds temporary input row to data source.</p>
   */
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
          message: t('file.type.directory'),
          value: 'DIRECTORY'
        },
        lastModifiedDate: '--'
      } as SourceType);

      nextTick(() => {
        const createInput = document.querySelector('input');
        if (createInput) {
          createInput.focus();
        }
      });
    }
  };

  /**
   * <p>Computed flag for showing add directory button.</p>
   * <p>Checks edit permissions and directory depth limit.</p>
   */
  const showAddDirectory = computed(() => {
    return editAuth.value && state.crumb.length < 9;
  });

  /**
   * <p>Handle directory creation input blur event.</p>
   * <p>Validates input and creates directory if valid.</p>
   *
   * @param record - Directory record being created
   */
  const createBlur = (record: SourceType) => {
    if (!record.name) {
      state.dataSource.shift();
      return;
    }
    sureAdd(record);
  };

  /**
   * <p>Handle directory creation input enter key event.</p>
   *
   * @param e - Keyboard event
   */
  const createEnter = (e: KeyboardEvent) => {
    (e.target as HTMLInputElement).blur();
  };

  /**
   * <p>Confirm and create new directory.</p>
   *
   * @param record - Directory record to create
   */
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

    notification.success(t('file.fileManagement.messages.addDirectorySuccess'));
    await getList();
  };

  /**
   * <p>Show delete confirmation modal.</p>
   *
   * @param _fileList - List of file IDs to delete
   */
  const delConfirm = (_fileList = state.selectedRowKeys) => {
    // Implementation for delete confirmation
  };

  /**
   * <p>Delete files and update list.</p>
   *
   * @param ids - Array of file IDs to delete
   */
  const delFile = async (ids: (string | number)[]) => {
    const [error] = await space.deleteFile(ids);
    if (error) {
      return;
    }

    state.selectedRowKeys = [];
    notification.success(t('actions.tips.deleteSuccess'));

    if (state.dataSource.length === ids.length && state.pagination.current > 1) {
      state.pagination.current--;
    }

    getList();
  };

  /**
   * <p>Search files by directory ID.</p>
   *
   * @param id - Directory ID to search in
   */
  const search = (id: string) => {
    parentDirectoryId.value = id;
    state.pagination.current = 1;
    getList();
  };

  /**
   * <p>Sort files by specified parameters.</p>
   *
   * @param params - Sorting parameters
   */
  const sort = (params: SortType) => {
    if (!params.orderBy) {
      state.sortParam = {};
    } else {
      state.sortParam = params;
    }
    getList();
  };

  /**
   * <p>Copy download URL to clipboard.</p>
   *
   * @param record - File record to copy URL for
   */
  const copyDownloadUrl = async (record: SourceType) => {
    if (record.type.value === 'FILE') {
      const [error, { data }] = await space.getQuickShareUrl({ objectId: record.id });
      if (error) {
        return;
      }

      setTimeout(async () => {
        await toClipboard(data);
        notification.success(t('file.fileManagement.messages.copyLinkSuccess'));
      });
    }
  };

  /**
   * <p>Handle file download operations.</p>
   * <p>Supports single file download and multiple file compression.</p>
   *
   * @param fileList - List of file IDs to download
   */
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

  /**
   * <p>Compress multiple files for download.</p>
   *
   * @param ids - Array of file IDs to compress
   */
  const compressFile = async (ids: string[]) => {
    fileApi.compressFile({
      ids,
      name: t('common.name'),
      parentDirectoryId: +parentDirectoryId.value > -1 ? parentDirectoryId.value : undefined
    });
  };

  /**
   * <p>Download single file.</p>
   *
   * @param id - File ID to download
   */
  const downSingleFile = async (id: string) => {
    const [error, res] = await space.getFileDetail(id);
    if (error) {
      return;
    }

    const params = parseQuery(res.data.file.url);
    download({ filename: res.data.file.name, ...params });
  };

  /**
   * <p>Get file icon based on file type.</p>
   *
   * @param record - File record
   * @returns Icon class name
   */
  const getFileIcon = (record: SourceType) => {
    return record.type.value === 'FILE' ? 'icon-wenjian' : 'icon-wenjianjia';
  };

  /**
   * <p>Handle table pagination changes.</p>
   *
   * @param pagination - New pagination information
   */
  const changePage = (pagination: any) => {
    state.pagination.current = pagination.current;
    state.pagination.pageSize = pagination.pageSize;
    getList();
  };

  /**
   * <p>Toggle upload panel visibility.</p>
   */
  const uploadFile = () => {
    isShowUpload.value = !isShowUpload.value;
  };

  /**
   * <p>Close upload panel and refresh file list.</p>
   */
  const closeUpload = () => {
    isShowUpload.value = false;
    getList();
  };

  /**
   * <p>Handle file move operations.</p>
   *
   * @param record - File record to move
   */
  const handleMove = (record: SourceType) => {
    moveVisible.value = true;
    moveIds.value = [record.id];
  };

  /**
   * <p>Handle multiple file move operations.</p>
   */
  const handleMultiMove = () => {
    moveVisible.value = true;
    moveIds.value = state.selectedRowKeys as string[];
  };

  /**
   * <p>Confirm and execute file move operation.</p>
   *
   * @param target - Target directory and space information
   */
  const confirmMove = async (target: { targetDirectoryId?: string; targetSpaceId: string }) => {
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

  /**
   * <p>Load file navigation path.</p>
   * <p>Updates breadcrumb navigation based on current directory.</p>
   */
  const loadPath = async () => {
    const [error, res = { data: {} }] = await space.getFileNavigation(parentDirectoryId.value);
    if (error) {
      return;
    }

    const { spaceName: navSpaceName, current, parentChain = [] } = res.data;
    state.crumb = [{ name: navSpaceName, id: '-1' }, ...(parentChain || []), current];
  };

  /**
   * <p>Jump to specific path in navigation.</p>
   *
   * @param id - Directory ID to jump to
   */
  const jumpPath = (id: string) => {
    parentDirectoryId.value = id;
    state.pagination.current = 1;
    getList();
  };

  /**
   * <p>Watch for parent directory changes and update navigation.</p>
   */
  watch(() => parentDirectoryId.value, () => {
    sessionStorage.setItem('parentDirectoryId', parentDirectoryId.value);

    if (+parentDirectoryId.value > -1) {
      // Handle navigation within existing path
      const currentIndex = state.crumb.findIndex(crumb => crumb.id === parentDirectoryId.value);
      if (currentIndex > -1) {
        state.crumb = state.crumb.slice(0, currentIndex + 1);
        return;
      }

      // Handle navigation to new directory
      const currentDirectory = state.dataSource.find(directory => directory.id === parentDirectoryId.value);
      if (currentDirectory) {
        state.crumb.push({ id: currentDirectory.id, name: currentDirectory.name });
        return;
      }

      // Load path from API
      loadPath();
    } else {
      state.crumb = [{ name: spaceName.value, id: '-1' }];
    }
  });

  return {
    // State
    loading,
    parentDirectoryId,
    spaceId,
    spaceName,
    state,
    targetId,
    targetType,
    isShowUpload,
    moveVisible,
    moveIds,
    shareAuth,
    downloadAuth,
    editAuth,
    deleteAuth,

    // Computed
    rowSelection,
    showAddDirectory,

    // Methods
    onSelectChange,
    customRow,
    getActionAuth,
    getList,
    getFileData,
    create,
    createBlur,
    createEnter,
    sureAdd,
    delConfirm,
    delFile,
    search,
    sort,
    copyDownloadUrl,
    downConfirm,
    compressFile,
    downSingleFile,
    getFileIcon,
    changePage,
    uploadFile,
    closeUpload,
    handleMove,
    handleMultiMove,
    confirmMove,
    loadPath,
    jumpPath
  };
}
