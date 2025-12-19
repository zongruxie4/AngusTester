<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Colon, DatePicker, Hints, Icon, IconCopy, Input, Modal, NoData, notification } from '@xcan-angus/vue-ui';
import { Button, Checkbox, CheckboxGroup, Radio, RadioGroup, Tooltip } from 'ant-design-vue';
import {
  EnumMessage,
  ShortTimeUnit,
  enumUtils,
  duration,
  toClipboard,
  utils,
  DomainManager,
  AppOrServiceRoute
} from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { apis, services } from '@/api/tester';
import dayjs from 'dayjs';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';

import { randomString } from '@/utils/utils';
import { ShareObj, TargetType } from './PropsType';

/**
 * Component props definition
 */
interface Props {
  source: 'add' | 'edit' | 'all'; // Operation mode: add new share, edit existing, or view all
  sharedId: string; // ID of shared item
  visible: boolean; // Modal visibility state
  id: string; // Target resource ID (service/API ID)
  name: string; // Target resource name
  type: TargetType; // Target resource type (SERVICE/API)
  share?: ShareObj; // Existing share object for editing
}

const props = withDefaults(defineProps<Props>(), {
  source: 'all',
  sharedId: '',
  visible: false,
  id: '',
  name: '',
  type: 'SERVICE',
  share: undefined
});

/**
 * Component event emitters
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void,
  (e: 'ok', value: { url: string, password: string, type: 'add' | 'edit' }): void
}>();

const { t } = useI18n();

// Share list data - default to showing the "add new share" entry expanded
const shareList = ref<ShareObj[]>([{
  id: props.sharedId || 'add',
  name: t('commonComp.shareModal.addShare'),
  targetType: {
    value: props.type,
    message: ''
  },
  targetId: props.id,
  apiIds: [],
  allFlag: false,
  url: '',
  expiredFlag: false,
  public0: true,
  password: undefined,
  createdBy: '',
  creator: '',
  createdDate: '',
  validFlag: false,
  isEdit: true,
  apiList: []
}]);

// Expiration date state - defaults to 1 day from now
const expirationDate = ref<string>(dayjs().add(1, 'day').format(DATE_TIME_FORMAT));

/**
 * Generate default share URL based on current domain
 */
const generateDefaultShareUrl = async () => {
  const host = DomainManager.getInstance().getAppDomain(AppOrServiceRoute.tester);
  const route = '/share/api';
  shareList.value[0].url = host + route;
  shareFormData.value.url = host + route;
};

// All available API IDs from the service
const allAvailableApiIds = ref<string[]>([]);

// Currently selected API IDs for sharing
const selectedApiIds = ref<string[]>([]);

// Filtered API list (for search results)
const filteredApiList = ref<{ id: string, summary: string; method: string }[]>([]);

// Complete API list from the service
const completeApiList = ref<{ id: string, summary: string; method: string }[]>([]);

// API query parameters
const apiQueryParams = ref<{ pageSize: number }>({ pageSize: 2000 });

/**
 * Load API list from the service
 * Fetches all APIs associated with the target service
 */
const loadApiList = async () => {
  const [error, { data }] = await services.loadApisByServicesId(props.id, apiQueryParams.value);
  if (error) {
    return;
  }

  allAvailableApiIds.value = [];
  filteredApiList.value = data?.list.map(item => {
    allAvailableApiIds.value.push(item.id);
    return {
      id: item.id,
      summary: item.summary,
      method: item.method
    };
  }) || [];

  shareList.value[0].apiList = filteredApiList.value;
  completeApiList.value = filteredApiList.value;
};

/**
 * Load share history list
 * Fetches all existing shares for the target resource
 */
const loadShareHistory = async () => {
  const params = { targetId: props.id, targetType: props.type };
  const [error, { data = { list: [] } }] = await apis.getShareList(params);
  if (error || !data?.list?.length) {
    return;
  }

  shareList.value = [shareList.value[0], ...data.list.map(item => ({
    ...item,
    isEdit: false,
    isLoading: false,
    apiList: []
  }))];
};

// Share form data state
const shareFormData = ref({
  public0: true, // Is share public (true) or private (false)
  url: '',
  name: '',
  targetId: '',
  targetType: 'API',
  expiredFlag: true // true = permanent, false = has expiration
});

// Currently editing share object
const currentlyEditingShare = ref<ShareObj>();

// Previous share data before editing (for restore on cancel)
const previousShareData = ref<ShareObj>();

// Reference to share list DOM element for scrolling
const shareListRef = ref<HTMLElement | null>(null);

/**
 * Handle edit action for a share item
 * Opens the edit form and populates it with existing data
 * @param share - The share object to edit
 * @param index - Index in the share list for scrolling
 */
const handleEditShare = (share, index: number) => {
  // Load complete API list if available
  if (completeApiList.value.length) {
    share.apiList = completeApiList.value;
    allAvailableApiIds.value = completeApiList.value.map(item => item.id);
  }

  // Save data before editing for potential rollback
  previousShareData.value = share;

  // If clicking on already expanded item, do nothing
  if (currentlyEditingShare.value?.id === share?.id && share.isEdit) {
    return;
  }

  // Show password field
  isPasswordVisible.value = true;
  share.isEdit = true;

  // Only allow editing one share at a time
  for (let i = 0; i < shareList.value.length; i++) {
    if (shareList.value[i]?.id !== share?.id && share.isEdit) {
      shareList.value[i].isEdit = false;
    }
  }

  // Set current editing share
  currentlyEditingShare.value = share;
  currentShareId.value = share.id;

  // Populate form with share data
  shareFormData.value.name = share.id === 'add' ? '' : share.name;
  shareFormData.value.url = share.url;
  shareFormData.value.expiredFlag = !share.expiredFlag;
  shareFormData.value.public0 = share.public0;
  shareFormData.value.targetId = share.targetId;
  shareFormData.value.targetType = share.targetType.value;
  sharePassword.value = share.password || '';
  selectedApiIds.value = share.apiIds || [];
  expirationDate.value = share.expiredDate;

  // Save original data for comparison (to detect changes)
  originalShareData.value.name = share.id === 'add' ? '' : share.name;
  originalShareData.value.id = share.id;
  originalShareData.value.url = share.url;
  originalShareData.value.expiredFlag = !share.expiredFlag;
  if (!share.expiredFlag) {
    originalShareData.value.expiredDate = share.expiredDate;
  }
  originalShareData.value.targetId = share.targetId;
  originalShareData.value.targetType = share.targetType.value;
  originalShareData.value.public0 = share.public0;
  if (!share.public0) {
    originalShareData.value.password = share.password;
  }
  if (share.apiIds?.length) {
    originalShareData.value.apiIds = share.apiIds;
  }

  if (props.source === 'edit') {
    shareList.value[0].url = share.url;
  }

  // Scroll to the edited item
  const timer = setTimeout(() => {
    if (shareListRef.value) {
      shareListRef.value.scrollTop = 42 * index;
    }
    clearTimeout(timer);
  }, 500);
};

/**
 * Cancel edit and restore previous data
 * @param share - The share object being edited
 */
const handleCancelEdit = (share) => {
  if (props.source === 'all') {
    if (shareList.value.length === 1) {
      if (shareList.value[0].isEdit) {
        emit('update:visible', false);
      }
      return;
    }
    share.isEdit = false;
    share = previousShareData.value;
    return;
  }

  emit('update:visible', false);
};

// Public/Private share options
const publicAccessOptions = [{
  label: t('commonComp.shareModal.publicFlagOptions.public'),
  value: true
}, {
  label: t('commonComp.shareModal.publicFlagOptions.private'),
  value: false
}];

// Expiration time options
const expirationOptions = [{
  label: t('commonComp.shareModal.expirationTimeOptions.permanent'),
  value: true
}, {
  label: t('commonComp.shareModal.expirationTimeOptions.expired'),
  value: false
}];

// Share password state
const sharePassword = ref('');

/**
 * Handle public/private flag change
 * Generates random password when switching to private
 */
const handlePublicAccessChange = (e) => {
  if (!e.target.value) {
    sharePassword.value = randomString();
    actualPassword.value = sharePassword.value;
    isPasswordVisible.value = true;
  }
};

/**
 * Reset password to a new random value
 */
const handleResetPassword = () => {
  sharePassword.value = randomString();
  hasPasswordError.value = false;
};

// Time unit options for date picker
const timeUnitOptions = ref<EnumMessage<string>[]>([]);

/**
 * Load time unit options
 * Excludes milliseconds and seconds
 */
const loadTimeUnits = () => {
  const excludedUnits = [ShortTimeUnit.Millisecond, ShortTimeUnit.Second];
  const data = enumUtils.enumToMessages(ShortTimeUnit);
  timeUnitOptions.value = data.filter(unit => !excludedUnits.includes(unit.value as ShortTimeUnit));
};

// API checkbox states
const isApiSelectionIndeterminate = ref<boolean>(false);
const isAllApisChecked = ref<boolean>(false);

/**
 * Handle select/deselect all APIs
 * @param e - Checkbox change event
 */
const handleSelectAllApis = (e) => {
  if (e.target.checked) {
    selectedApiIds.value = allAvailableApiIds.value;
    isAllApisChecked.value = true;
  } else {
    isAllApisChecked.value = false;
    selectedApiIds.value = [];
  }
  isApiSelectionIndeterminate.value = false;
};

/**
 * Handle individual API checkbox change
 * Updates select all checkbox state based on selected count
 * @param checkedValues - Array of checked API IDs
 */
const handleApiSelectionChange = (checkedValues: (string | number | boolean)[]) => {
  // Convert to string array for consistency
  const stringValues = checkedValues.map(v => String(v));
  selectedApiIds.value = stringValues;

  if (stringValues.length) {
    const isEqual = utils.deepCompare(allAvailableApiIds.value, stringValues);
    if (isEqual) {
      isApiSelectionIndeterminate.value = false;
      isAllApisChecked.value = true;
      return;
    }

    isApiSelectionIndeterminate.value = true;
    isAllApisChecked.value = true;
    return;
  }

  isApiSelectionIndeterminate.value = false;
  isAllApisChecked.value = false;
};

/**
 * Save share configuration
 * Validates form data and submits create/update request
 * @param share - The share object being saved
 */
const handleSaveShare = async (share) => {
  // Validate share name
  hasNameError.value = !shareFormData.value.name;
  if (hasNameError.value) {
    return;
  }

  let params: Record<string, any> = {
    ...shareFormData.value,
    expiredFlag: !shareFormData.value.expiredFlag
  };

  // If private share, include password
  if (!shareFormData.value.public0) {
    // Validate password
    hasPasswordError.value = !sharePassword.value;
    if (hasPasswordError.value) {
      return;
    }
    params = {
      ...params,
      password: sharePassword.value
    };
  }

  // If has expiration time, include it
  if (!shareFormData.value.expiredFlag) {
    // Validate expiration time (must be at least 30 minutes in future)
    const isExpired = isExpirationTooSoon(expirationDate.value);

    if (isExpired) {
      hasDateError.value = true;
      notification.warning(t('commonComp.shareModal.expiredTimeTip'));
      return;
    }

    params = {
      ...params,
      expiredDate: expirationDate.value
    };
  }

  // For service shares, validate API selection
  if (props.type !== 'API') {
    if (!filteredApiList.value.length) {
      notification.warning(t('commonComp.shareModal.noShareApis'));
      return;
    }

    // Ensure at least one API is selected
    if (!selectedApiIds.value.length) {
      notification.warning(t('commonComp.shareModal.noSelectedApis'));
      return;
    }

    params = {
      ...params,
      apiIds: selectedApiIds.value
    };
  }

  // If editing existing share, use PATCH request
  if (share.id !== 'add') {
    params = {
      ...params,
      id: currentShareId.value
    };
    return updateExistingShare(params, share);
  }

  // Creating new share - use POST request
  share.isLoading = true;
  const [error, res = { data: [] }] = await apis.addShare(params);
  share.isLoading = false;
  if (error) {
    return;
  }

  share.name = shareFormData.value.name;
  share.url = res.data.url;
  share.password = res.data.password;
  share.public0 = shareFormData.value.public0;
  emit('ok', { url: res.data.url, password: params.password, type: 'add' });
  copyShareInfo(share);
  emit('update:visible', false);
};

// Original share data for comparison (to detect changes)
const originalShareData = ref<{
  public0: boolean;
  url: string;
  targetId: string;
  targetType: TargetType;
  expiredFlag: boolean;
  expiredDate?: string;
  password?: string;
  id: string;
  name: string;
  apiIds?: string[];
}>({
  public0: false,
  url: '',
  targetId: '',
  targetType: 'SERVICE',
  expiredFlag: false,
  id: '',
  name: '',
  apiIds: []
});

/**
 * Update existing share via PATCH request
 * Only updates if data has changed
 * @param params - Update parameters
 * @param share - Share object being updated
 */
const updateExistingShare = async (params, share) => {
  // Skip update if no changes detected
  if (utils.deepCompare(originalShareData.value, params)) {
    copyShareInfo(share);
    return;
  }

  share.isLoading = true;
  const [error] = await apis.patchShared(params);
  share.isLoading = false;
  if (error) {
    return;
  }

  emit('ok', { url: params.url, password: params.password, type: 'edit' });
  copyShareInfo(params);
  emit('update:visible', false);
};

// Current share ID being edited
const currentShareId = ref(props.sharedId);

// Form validation error states
const hasNameError = ref(false);
const handleNameChange = (event: ChangeEvent) => {
  const value = event.target.value;
  hasNameError.value = !value;
};

const hasPasswordError = ref(false);
const handlePasswordChange = (event: ChangeEvent) => {
  const value = event.target.value;
  hasPasswordError.value = !value;
};

const hasDateError = ref(false);
const handleDateChange = (value: string) => {
  if (!value) {
    hasDateError.value = true;
    return;
  }

  const isExpired = isExpirationTooSoon(value);

  if (isExpired) {
    hasDateError.value = true;
    notification.warning(t('commonComp.shareModal.expiredTimeTip'));
  } else {
    hasDateError.value = false;
  }
};

/**
 * Handle modal cancel action
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * Delete a share
 * @param id - Share ID to delete
 */
const handleDeleteShare = async (id: string) => {
  const [error] = await apis.deleteShare(id);
  if (error) {
    return;
  }
  notification.success(t('commonComp.shareModal.shareDeleteSuccess'));
  shareList.value = shareList.value.filter(item => item.id !== id);
};

/**
 * Handle copy icon click
 * @param item - Share object to copy
 */
const handleCopyClick = (item: ShareObj) => {
  copyShareInfo(item, true);
};

/**
 * Copy share information to clipboard
 * Copies password for private shares, URL for public shares
 * @param item - Share object containing info to copy
 * @param showCopySuccess - Whether to show copy success message
 */
const copyShareInfo = (item: ShareObj, showCopySuccess?: boolean) => {
  let message;
  if (!item.public0) {
    message = `${t('common.password')}: ${item.password || ''}`;
  } else {
    message = `${t('common.link')}: ${item.url}`;
  }
  toClipboard(message).then(() => {
    notification.success(showCopySuccess ? t('actions.tips.copySuccess') : t('actions.tips.shareSuccess'));
  });
};

/**
 * Search/filter APIs by name (debounced)
 * @param event - Input change event
 * @param share - Share object to update API list
 */
const searchApisByName = debounce(duration.search, (event: ChangeEvent, share: ShareObj) => {
  const searchTerm = event.target.value as string;

  // If no search term, show all APIs
  if (!searchTerm) {
    share.apiList = completeApiList.value;
    allAvailableApiIds.value = completeApiList.value?.map(item => item.id) || [];
    return;
  }

  // Filter APIs by search term
  filteredApiList.value = [];
  for (let i = 0; i < completeApiList.value.length; i++) {
    const apiItem = completeApiList.value[i];
    if (apiItem.summary.includes(searchTerm)) {
      filteredApiList.value.push({ id: apiItem.id, summary: apiItem.summary, method: apiItem.method });
    }
  }
  share.apiList = filteredApiList.value;
});

// Password visibility state
const isPasswordVisible = ref(true);
// Actual password value (for display purposes)
const actualPassword = ref('');

/**
 * Initialize/reset all data to default state
 * Called when modal opens
 */
const initializeData = () => {
  // Reset share list to default "add new" entry
  shareList.value = [{
    id: props.sharedId || 'add',
    name: t('commonComp.shareModal.addShare'),
    targetType: {
      value: props.type,
      message: ''
    },
    targetId: props.id,
    apiIds: [],
    allFlag: false,
    url: '',
    expiredFlag: false,
    public0: true,
    password: undefined,
    createdBy: '',
    creator: '',
    createdDate: '',
    validFlag: false,
    isEdit: true,
    apiList: []
  }];

  // Reset all state values
  expirationDate.value = dayjs().add(24.1, 'hour').format(DATE_TIME_FORMAT);
  allAvailableApiIds.value = [];
  selectedApiIds.value = [];
  filteredApiList.value = [];
  completeApiList.value = [];

  shareFormData.value = {
    public0: true,
    url: '',
    name: '',
    targetId: '',
    targetType: 'API',
    expiredFlag: true
  };

  currentlyEditingShare.value = undefined;
  previousShareData.value = undefined;
  sharePassword.value = '';

  isApiSelectionIndeterminate.value = false;
  isAllApisChecked.value = false;

  originalShareData.value = {
    public0: false,
    url: '',
    targetId: '',
    targetType: 'SERVICE',
    expiredFlag: false,
    id: '',
    name: '',
    apiIds: []
  };

  currentShareId.value = props.sharedId;

  // Reset error states
  hasNameError.value = false;
  hasPasswordError.value = false;
  hasDateError.value = false;

  // Reset password visibility
  isPasswordVisible.value = true;
  actualPassword.value = '';
};

/**
 * Component lifecycle - mounted
 * Sets up watchers for modal visibility
 */
onMounted(async () => {
  await loadTimeUnits();

  watch(() => props.visible, async (newValue) => {
    if (!newValue) {
      return;
    }

    // Initialize data when modal opens
    initializeData();
    await loadApiList();

    // Load share history for 'all' mode
    if (props.source === 'all') {
      await loadShareHistory();
    }

    // Pre-populate form for 'edit' mode
    if (props.source === 'edit') {
      handleEditShare(props.share as ShareObj, 0);
    } else {
      generateDefaultShareUrl();
    }

    // Set target type and ID
    shareFormData.value.targetType = props.type;
    shareFormData.value.targetId = props.id;
  }, {
    immediate: true
  });
});

/**
 * Get CSS class for API method badge color
 * @param method - HTTP method string
 * @returns CSS class name
 */
const getApiMethodColorClass = (method: string) => {
  switch (method) {
    case 'GET':
      return 'text-http-get';
    case 'POST':
      return 'text-http-post';
    case 'DELETE':
      return 'text-http-delete';
    case 'PUT':
      return 'text-http-put';
    case 'PATCH':
      return 'text-http-patch';
    case 'HEAD':
      return 'text-http-head';
    case 'OPTIONS':
      return 'text-http-options';
    case 'TRACE':
      return 'text-http-trace';
    default:
      return '';
  }
};

/**
 * Disable dates in the past for date picker
 * @param current - Current date being evaluated
 * @returns true if date should be disabled
 */
const isDateDisabled = (current) => {
  return current && current < dayjs().subtract(1, 'day').endOf('day');
};

/**
 * Check if expiration time is too soon (less than 30 minutes from now)
 * Validates that user hasn't selected a time that's too close to current time
 * @param timeStr - ISO date/time string
 * @returns true if expiration is less than 30 minutes away
 */
function isExpirationTooSoon(timeStr: string) {
  const currentTime = dayjs();
  const targetTime = dayjs(timeStr);
  const diffInMinutes = targetTime.diff(currentTime, 'minute');
  return diffInMinutes < 30;
}
</script>
<template>
  <Modal
    :title="t('commonComp.shareModal.title')"
    :visible="props.visible"
    :reverse="true"
    :width="600"
    :footer="null"
    @cancel="handleModalCancel">
    <Hints :text="t('commonComp.shareModal.tooltip.publicShareDesc')" />
    <div
      ref="shareListRef"
      style="max-height: 72vh;scrollbar-gutter: stable;"
      class="overflow-y-auto flex flex-col space-y-2 pr-2.5 -mr-4.5 mb-5">
      <div
        v-for="item,index in shareList"
        :key="item.id"
        class="border border-border-divider p-2 rounded mt-2">
        <div>
          <div class="flex items-start justify-between">
            <template v-if="item.isEdit">
              <Input
                v-model:value="shareFormData.name"
                :maxlength="100"
                :error="hasNameError"
                :placeholder="t('common.placeholders.searchKeyword')"
                @change="handleNameChange" />
            </template>
            <template v-else>
              <template v-if="item.id === 'add'">
                <div
                  class="flex-1 mr-2 truncate cursor-pointer text-text-link hover:text-text-link-hover"
                  @click="handleEditShare(item,index)">
                  {{ item.name }}
                </div>
              </template>
              <template v-else>
                <Tooltip
                  :title="item.name"
                  placement="bottomLeft"
                  :overlayStyle="{maxWidth:'486px'}">
                  <div class="flex-1 mr-2 truncate cursor-pointer text-text-sub-content">{{ item.name }}</div>
                </Tooltip>
              </template>
            </template>
            <div class="text-3.25 flex-none">
              <template v-if="props.source === 'all' && !item.isEdit">
                <Tooltip :title="t('commonComp.shareModal.tooltip.copyUrl')" placement="top">
                  <IconCopy
                    v-if="item.id !== 'add'"
                    class="ml-3.5"
                    @click="handleCopyClick(item)" />
                </Tooltip>
                <Tooltip :title="t('actions.edit')" placement="top">
                  <Icon
                    icon="icon-shuxie"
                    class="cursor-pointer ml-2"
                    @click="handleEditShare(item,index)" />
                </Tooltip>
                <Tooltip :title="t('actions.delete')" placement="top">
                  <Icon
                    v-if="item.id !== 'add'"
                    icon="icon-qingchu"
                    class="cursor-pointer ml-2 text-3.5"
                    @click="handleDeleteShare(item.id)" />
                </Tooltip>
              </template>
            </div>
          </div>
        </div>
        <div
          :class="item.isEdit ? 'open-info' : 'stop-info'"
          class="transition-height duration-500 overflow-hidden px-1">
          <template v-if="item.isEdit">
            <div class="text-text-sub-content flex-none mr-2 mt-2.5">
              {{ t('commonComp.shareModal.shareUrl') }}
              <Colon />
            </div>
            <div class="break-all cursor-pointer text-text-link hover:text-text-link-hover text-3 px-2">
              {{
                item.url
              }}
            </div>
            <div class="text-text-sub-content flex-none mr-2 mt-2.5">
              {{ t('common.actions') }}
              <Colon />
            </div>
            <div class="flex items-center h-7 -mt-0.5 px-2">
              <RadioGroup
                v-model:value="shareFormData.public0"
                size="small"
                class="w-1/2 flex"
                @change="handlePublicAccessChange">
                <Radio
                  v-for="option,oIndex in publicAccessOptions"
                  :key="oIndex"
                  :value="option.value"
                  class="w-1/2 whitespace-nowrap">
                  {{ option.label }}
                </Radio>
              </RadioGroup>
              <template v-if="!shareFormData.public0">
                <Input
                  v-model:value="sharePassword"
                  :error="hasPasswordError"
                  :maxlength="20"
                  type="password"
                  dataType="mixin-en"
                  size="small"
                  :placeholder="t('commonComp.shareModal.form.passwordPlaceholder')"
                  class="flex-1"
                  @change="handlePasswordChange">
                  <template #addonAfter>
                    <a class="text-3 text-text-sub-content" @click="handleResetPassword">
                      <Icon icon="icon-shuaxin" class="mr-1 -mt-0.5" />
                      {{ t('actions.refresh') }}</a>
                  </template>
                </Input>
              </template>
            </div>
            <div class="text-text-sub-content flex-none mr-2 mt-2.5">
              {{ t('common.expiredDate') }}
              <Colon />
            </div>
            <div class="flex items-center h-7 -mt-0.5  px-2">
              <RadioGroup
                v-model:value="shareFormData.expiredFlag"
                size="small"
                class="w-1/2 flex">
                <Radio
                  v-for="option,oIndex in expirationOptions"
                  :key="oIndex"
                  :value="option.value"
                  class="w-1/2 whitespace-nowrap">
                  {{ option.label }}
                </Radio>
              </RadioGroup>
              <template v-if="!shareFormData.expiredFlag">
                <DatePicker
                  v-model:value="expirationDate"
                  :allowClear="false"
                  :showNow="false"
                  :disabledDate="isDateDisabled"
                  :showTime="{hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) }"
                  :error="hasDateError"
                  internal
                  size="small"
                  type="date"
                  class="flex-grow"
                  @change="handleDateChange" />
              </template>
            </div>
            <template v-if="props.type !== 'API'">
              <div class="text-text-sub-content flex-none mr-2 mt-2.5">
                {{ t('commonComp.shareModal.selectApis') }}
                <Colon />
              </div>
              <div class=" border border-border-divider p-2 rounded" :class="completeApiList.length > 0 ? 'h-73' : 'h-40'">
                <Input
                  v-if="completeApiList.length > 0"
                  :placeholder="t('commonComp.shareModal.form.apiSearchPlaceholder')"
                  size="small"
                  class="mb-2"
                  allowClear
                  @change="(event)=>searchApisByName(event,item)">
                  <template #suffix>
                    <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
                  </template>
                </Input>
                <template v-if="item.apiList.length">
                  <div
                    class="overflow-hidden hover:overflow-y-auto -mr-2 pr-2.5 -mt-1.5"
                    style="scrollbar-gutter: stable;max-height: 216px;">
                    <CheckboxGroup
                      v-model:value="selectedApiIds"
                      class="text-3 w-full"
                      @change="handleApiSelectionChange">
                      <div
                        v-for="api in item.apiList"
                        :key="api.id"
                        class="flex items-center space-x-2 mt-1.75 h-5 leading-5">
                        <Checkbox :value="api.id" class="-mt-0.25" />
                        <div class="w-13 whitespace-nowrap" :class="getApiMethodColorClass(api.method)">
                          {{
                            api.method
                          }}
                        </div>
                        <div
                          style="width: 376px;"
                          class="truncate"
                          :title="api.summary">
                          {{ api.summary }}
                        </div>
                      </div>
                    </CheckboxGroup>
                  </div>
                  <div class="flex mt-2 text-text-title text-3 font-normal">
                    <Checkbox
                      v-model:checked="isAllApisChecked"
                      class="checkbox-border-black"
                      :indeterminate="isApiSelectionIndeterminate"
                      @change="handleSelectAllApis">
                      {{ t('actions.selectAll') }}
                    </Checkbox>
                  </div>
                </template>
                <NoData v-else />
              </div>
            </template>
            <div class="flex justify-end mt-2">
              <Button
                type="link"
                class="mr-2"
                size="small"
                :loading="item.isLoading"
                @click="handleSaveShare(item)">
                {{ t('actions.saveAndCopy') }}
              </Button>
              <Button
                type="link"
                size="small"
                class="-mx-2"
                @click="handleCancelEdit(item)">
                {{ t('actions.cancel') }}
              </Button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </Modal>
</template>
<style scoped>
.open-info {
  max-height: 528px;
}

.stop-info {
  max-height: 0;
}
</style>
