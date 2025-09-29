import { ref, reactive, watch, computed } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import { space } from '@/api/storage';
import store from '@/store';
import { notification } from '@xcan-angus/vue-ui';
import { AppOrServiceRoute, DomainManager, enumUtils, ShortTimeUnit, toClipboard, utils } from '@xcan-angus/infra';
import { randomString } from '@/utils/utils';
import type { ShareForm, ShareModalState } from '../types';
import { useI18n } from 'vue-i18n';

interface Props {
  visible: boolean;
  id: string;
  spaceId: string;
  spaceName: string;
  defaultIds: string[];
}

/**
 * Composable for managing share modal functionality
 * Handles share creation, editing, and form management
 */
export function useShareModal () {
  const { t } = useI18n();

  // Component props and emits references
  let props: Props;
  let emits: any;

  // Tree state
  const expandedKeys = ref<string[]>([]);
  const checkedKeys = ref<{ checked: string[] }>({ checked: [] });
  const treeData = ref<TreeProps['treeData']>([]);

  // Checked items map for efficient lookup
  const checkedMap: Record<string, string[]> = {};

  // Loading state
  const isLoading = ref(false);

  // Component state
  const state = reactive<ShareModalState>({
    enums: [
      { label: t('file.share.permissionOptions.public'), value: true },
      { label: t('file.share.permissionOptions.private'), value: false }
    ],
    unitEnum: [],
    expiredEnums: [
      { label: t('file.share.validityOptions.setValidity'), value: true },
      { label: t('file.share.validityOptions.permanent'), value: false }
    ]
  });

  // Share form data
  const form = reactive<ShareForm>({
    expiredDuration: '15d',
    password: undefined,
    public0: true,
    url: '',
    expiredFlag: true,
    remark: undefined,
    id: undefined
  });

  // Initial parameters for comparison
  let initParams: any = {};

  /**
   * Handle tree node check event
   */
  const handleChecked = (_checkedKeys: any, info: { checked: boolean; node: any }): void => {
    const { checked, node } = info;
    if (checked) {
      checkedMap[node.id] = node.idLines;
    } else {
      delete checkedMap[node.id];
    }
  };

  /**
   * Load tree node data
   */
  const loadNodeData: TreeProps['loadData'] = treeNode => {
    const spaceId = props.spaceId;
    const parentDirectoryId = treeNode.type === 'DIRECTORY' ? treeNode.id : '-1';

    return space.getFileList({
      spaceId,
      parentDirectoryId,
      pageSize: store.state.maxPageSize,
      pageNo: 1
    }).then(([error, res]) => {
      if (error) {
        return;
      }

      if (treeNode.dataRef?.children) {
        return;
      }

      if (treeNode.dataRef) {
        const idLines = parentDirectoryId === '-1' ? [] : [...treeNode.idLines, parentDirectoryId];
        treeNode.dataRef.children = (res.data.list || []).map(data => ({
          ...data,
          idLines: [...idLines, data.id],
          spaceId: spaceId,
          type: data.type.value,
          isLeaf: data.type.value === 'FILE'
        }));
      }

      treeData.value = [...(treeData.value || [])];
    });
  };

  /**
   * Load time unit enumeration data
   */
  const loadUnit = (): void => {
    if (state.unitEnum.length) {
      return;
    }

    const excludeUnit = [ShortTimeUnit.Millisecond, ShortTimeUnit.Second];
    const data = enumUtils.enumToMessages(ShortTimeUnit);
    // @ts-ignore
    state.unitEnum = data.filter(unit => !excludeUnit.includes(unit.value));
  };

  /**
   * Reset form to initial state
   */
  const resetForm = (): void => {
    form.expiredDuration = '15d';
    form.expiredFlag = true;
    form.password = undefined;
    form.public0 = true;
    form.url = '';
    form.remark = undefined;
    form.id = undefined;
  };

  /**
   * Reset default tree data
   */
  const resetDefaultData = (spaceName: string, spaceId: string): void => {
    checkedKeys.value = { checked: props.defaultIds };
    treeData.value = [{
      name: spaceName,
      id: spaceId,
      key: spaceId,
      type: 'SPACE',
      disabled: true
    }];
  };

  /**
   * Load share information for editing
   */
  const loadShareInfo = async (id: string): Promise<void> => {
    const [error, res = { data: {} }] = await space.getShareDetail(id);
    if (error) {
      return;
    }

    const {
      expiredDuration,
      password,
      public0,
      url,
      remark,
      id: shareId,
      objectIds,
      expiredFlag
    } = res.data;

    form.expiredDuration = expiredDuration;
    form.password = password;
    form.public0 = public0;
    form.url = url;
    form.expiredFlag = expiredFlag;
    form.remark = remark;
    form.id = shareId;
    checkedKeys.value.checked.push(...objectIds);

    initParams = {
      ...form,
      objectIds: objectIds
    };
  };

  /**
   * Load share URL
   */
  const loadShareUrl = async (): Promise<string> => {
    const host = DomainManager.getInstance().getAppDomain(AppOrServiceRoute.tester);
    const route = '/share/file';
    return `${host}${route}`;
  };

  /**
   * Get checked IDs from tree
   */
  const getCheckedIds = (): string[] => {
    const ids = [...checkedKeys.value.checked].filter(id => id !== props.spaceId);
    Object.keys(checkedMap).forEach(key => {
      ids.push(...checkedMap[key]);
    });
    return Array.from(new Set(ids));
  };

  /**
   * Reset password based on share type
   */
  const resetPassword = (): void => {
    if (!form.public0) {
      form.password = randomString();
    } else {
      form.password = undefined;
    }
  };

  /**
   * Copy share link and password to clipboard
   */
  const copyToClipboard = (): void => {
    let message;
    if (form.public0) {
      message = t('file.share.messages.link', { url: form.url });
    } else {
      message = t('file.share.messages.linkAndPassword', {
        url: form.url,
        password: form.password || ''
      });
    }

    toClipboard(message).then(() => {
      notification.success(t('actions.tips.copySuccess'));
    });
  };

  /**
   * Add or update share
   */
  const saveShare = async (): Promise<void> => {
    if (!form.password && !form.public0) {
      return;
    }

    if (form.url) {
      await updateShare();
      return;
    }

    const params = {
      ...form,
      spaceId: props.spaceId,
      objectIds: getCheckedIds(),
      url: await loadShareUrl()
    };

    isLoading.value = true;
    const [error, res = { data: {} }] = await space.getShareUrl(params);
    isLoading.value = false;

    if (error) {
      return;
    }

    emits('ok');
    notification.success(t('file.share.messages.generateShareLink'));
    form.url = res.data.url;
    form.id = res.data.id;
    initParams = {
      ...form,
      objectIds: params.objectIds
    };
  };

  /**
   * Update existing share
   */
  const updateShare = async (): Promise<void> => {
    if (utils.deepCompare(initParams, { ...form, objectIds: getCheckedIds() })) {
      close();
      return;
    }

    const params = {
      ...form,
      spaceId: props.spaceId,
      objectIds: getCheckedIds()
    };

    isLoading.value = true;
    const [error] = await space.patchShare(params);
    isLoading.value = false;

    if (error) {
      return;
    }

    emits('ok');
    close();
  };

  /**
   * Close modal and reset form
   */
  const close = (): void => {
    resetForm();
    emits('update:visible', false);
  };

  /**
   * Handle permission type change
   */
  const handlePermissionChange = (): void => {
    resetPassword();
  };

  /**
   * Computed property for OK button text
   */
  const okButtonText = computed(() => {
    return form.url ? t('common.confirm') : t('file.share.buttons.generateLink');
  });

  /**
   * Duration select props
   */
  const durationSelectProps = {
    excludes: (value: any) => {
      return ['ms'].includes(value.message);
    },
    class: '!w-20'
  };

  /**
   * Duration input props
   */
  const durationInputProps = {
    placeholder: t('file.share.placeholders.validityPeriod')
  };

  /**
   * Initialize composable
   */
  const init = (componentProps: Props, componentEmits: any): void => {
    props = componentProps;
    emits = componentEmits;

    // Watch for visibility changes
    watch(() => props.visible, newValue => {
      if (newValue) {
        resetPassword();
        loadUnit();
        resetDefaultData(props.spaceName, props.spaceId);

        if (props.id) {
          loadShareInfo(props.id);
        }
      }
    }, {
      immediate: true
    });
  };

  return {
    // State
    expandedKeys,
    checkedKeys,
    treeData,
    isLoading,
    state,
    form,

    // Computed
    okButtonText,

    // Props
    durationSelectProps,
    durationInputProps,

    // Methods
    handleChecked,
    loadNodeData,
    resetForm,
    resetDefaultData,
    loadShareInfo,
    loadShareUrl,
    getCheckedIds,
    resetPassword,
    copyToClipboard,
    saveShare,
    updateShare,
    close,
    handlePermissionChange,
    init
  };
}
