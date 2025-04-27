<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Colon, DatePicker, Hints, Icon, IconCopy, Input, Modal, NoData, notification } from '@xcan-angus/vue-ui';
import { Button, Checkbox, CheckboxGroup, Radio, RadioGroup, Tooltip } from 'ant-design-vue';
import { enumLoader, duration, clipboard, site, utils } from '@xcan-angus/tools';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { apis, services } from '@/api/altester';
import dayjs from 'dayjs';

import { randomString } from '@/utils/utils';
import { ShareObj, TargetType } from './PropsType';

interface Props {
  source:'add' | 'edit' | 'all'
  sharedId:string,
  visible:boolean,
  id: string,
  name:string,
  type: TargetType,
  share?:ShareObj
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

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void,
  (e:'ok', value:{ url: string, passd: string, type: 'add' | 'edit' }):void
}>();

const { t } = useI18n();

// 默认展开新家分享数据
const shareList = ref<ShareObj[]>([{
  id: props.sharedId || 'add',
  name: '添加分享',
  targetType: {
    value: props.type,
    message: ''
  },
  targetId: props.id,
  apiIds: [],
  allFlag: false,
  url: '',
  expiredFlag: false,
  publicFlag: true,
  passd: undefined,
  createdBy: '',
  createdByName: '',
  createdDate: '',
  validFlag: false,
  isEdit: true,
  apiList: []
}]);

const expiredDate = ref<string>(dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss'));

// 设置添加数据的默认URL
const getDefaultShareUrl = async () => {
  const host = await site.getUrl('at');
  const route = '/share/api';
  shareList.value[0].url = host + route;
  formState.value.url = host + route;
};

// 保存所有接口的ID
const allAipIds = ref<string[]>([]);
// 保存所有选择的接口的ID
const apiIds = ref<string[]>([]);

// 服务下的接口列表
const apiList = ref<{ id: string, summary: string; method: string }[]>([]);
// 保存服务下所有的接口数据
const allApiList = ref<{ id: string, summary: string; method: string }[]>([]);

const apiParams = ref<{pageSize: number }>({ pageSize: 2000 });
// 获取服务下的接口列表
const loadApiList = async () => {
  const [error, { data }] = await services.loadApisByServicesId(props.id, apiParams.value);
  if (error) { return; }
  allAipIds.value = [];
  apiList.value = data?.list.map(item => {
    allAipIds.value.push(item.id);
    return {
      id: item.id,
      summary: item.summary,
      method: item.method
    };
  }) || [];
  shareList.value[0].apiList = apiList.value;
  allApiList.value = apiList.value;
};

// 加载分享历史列表
const loadSharedList = async () => {
  const params = { targetId: props.id, targetType: props.type };
  const [error, { data = { list: [] } }] = await apis.loadShareList(params);
  if (error || !data?.list?.length) {
    return;
  }
  shareList.value = [shareList.value[0], ...data.list.map(item => ({ ...item, isEdit: false, isLoading: false, apiList: [] }))];
};

const formState = ref({
  publicFlag: true,
  url: '',
  name: '',
  targetId: '',
  targetType: 'API',
  expiredFlag: true
});

// 当前编辑的分享数据 todo
const editShare = ref<ShareObj>();
// 编辑之前的数据，用于取消编辑恢复数据
const oldShare = ref<ShareObj>();

const shareListRef = ref<HTMLElement | null>(null);
// 开启编辑
const handleEdit = (share, index:number) => {
  if (allApiList.value.length) {
    share.apiList = allApiList.value;
    allAipIds.value = allApiList.value.map(item => item.id);
  }
  // 保存编辑之前的数据
  oldShare.value = share;
  // 如果点击的是已经展开的数据 不做任何处理
  if (editShare.value?.id === share?.id && share.isEdit) {
    return;
  }
  // 开启编辑
  seePassword.value = true;
  share.isEdit = true;
  // 同时只能编辑一条数据
  for (let i = 0; i < shareList.value.length; i++) {
    if (shareList.value[i]?.id !== share?.id && share.isEdit) {
      shareList.value[i].isEdit = false;
    }
  }

  // 保存正在编辑的数据
  editShare.value = share;
  shareId.value = share.id;
  formState.value.name = share.id === 'add' ? '' : share.name;
  formState.value.url = share.url;
  formState.value.expiredFlag = !share.expiredFlag;
  formState.value.publicFlag = share.publicFlag;
  formState.value.targetId = share.targetId;
  formState.value.targetType = share.targetType.value;
  passd.value = share.passd || '';
  apiIds.value = share.apiIds || [];
  expiredDate.value = share.expiredDate;
  // 校验数据是否有修改需要用到 old是恢复旧数据 校验时用不到全部数据
  historyData.value.name = share.id === 'add' ? '' : share.name;
  historyData.value.id = share.id;
  historyData.value.url = share.url;
  historyData.value.expiredFlag = !share.expiredFlag;
  if (!share.expiredFlag) {
    historyData.value.expiredDate = share.expiredDate;
  }
  historyData.value.targetId = share.targetId;
  historyData.value.targetType = share.targetType.value;
  historyData.value.publicFlag = share.publicFlag;
  if (!share.publicFlag) {
    historyData.value.passd = share.passd;
  }
  if (share.apiIds?.length) {
    historyData.value.apiIds = share.apiIds;
  }

  if (props.source === 'edit') {
    shareList.value[0].url = share.url;
  }

  const timer = setTimeout(() => {
    if (shareListRef.value) {
      shareListRef.value.scrollTop = 42 * index;
    }
    clearTimeout(timer);
  }, 500);
};

// 取消编辑 并恢复数据
const cancelEdit = (share) => {
  if (props.source === 'all') {
    if (shareList.value.length === 1) {
      if (shareList.value[0].isEdit) {
        emit('update:visible', false);
      }
      return;
    }
    share.isEdit = false;
    share = oldShare.value;
    return;
  }

  emit('update:visible', false);
};

const publicFlagOptions = [{
  label: t('公开') + t('(所有人可见)'),
  value: true
}, {
  label: t('私有') + t('(密码查看)'),
  value: false
}];

const expirationTimeOptions = [{
  label: t('永久有效'),
  value: true
}, {
  label: t('设置过期时间'),
  value: false
}];

const passd = ref('');
const publicFlagChange = e => {
  if (!e.target.value) {
    passd.value = randomString();
    realPassd.value = passd.value;
    seePassword.value = true;
  }
};

const resetPassword = () => {
  passd.value = randomString();
  passdErr.value = false;
};

const dateUnitOptions = ref<{ label: string; value: string; }[]>([]);

const loadUnit = async () => {
  const excludeUnit = ['Millisecond', 'Second'];
  const [error, data] = await enumLoader.load('ShortTimeUnit');
  if (error || !data) {
    return;
  }
  dateUnitOptions.value = data.filter(unit => !excludeUnit.includes(unit.value)).map(item => ({ label: item.message, value: item.value }));
};

const apiIndeterminate = ref<boolean>(false);
const apiCheckAll = ref<boolean>(false);
const selectALLApi = (e) => {
  if (e.target.checked) {
    apiIds.value = allAipIds.value;
    apiCheckAll.value = true;
  } else {
    apiCheckAll.value = false;
    apiIds.value = [];
  }
  apiIndeterminate.value = false;
};

const apiChecked = (checkedValue:string[]) => {
  apiIds.value = checkedValue;
  if (checkedValue.length) {
    const isEuqal = utils.deepCompare(allAipIds.value, checkedValue);
    if (isEuqal) {
      apiIndeterminate.value = false;
      apiCheckAll.value = true;
      return;
    }

    apiIndeterminate.value = true;
    apiCheckAll.value = true;
    return;
  }

  apiIndeterminate.value = false;
  apiCheckAll.value = false;
};

// 保存分享
const handleOk = async (share) => {
  // 校验分享名称
  nameErr.value = !formState.value.name;
  if (nameErr.value) {
    return;
  }

  let params:Record<string, any> = {
    ...formState.value,
    expiredFlag: !formState.value.expiredFlag
  };
  // 选择私有 参数提交密码
  if (!formState.value.publicFlag) {
    // 校验密码
    passdErr.value = !passd.value;
    if (passdErr.value) {
      return;
    }
    params = {
      ...params,
      passd: passd.value
    };
  }
  // 选择失效时间 参数提交失效时间（expiredDuration）
  if (!formState.value.expiredFlag) {
    // 校验时间
    const isExpired = isWithin5Minutes(expiredDate.value);

    if (isExpired) {
      dateErr.value = true;
      notification.warning('过期时间小于24小时，请重新选择日期');
      return;
    }

    params = {
      ...params,
      expiredDate: expiredDate.value
    };
  }
  if (props.type !== 'API') {
    if (!apiList.value.length) {
      notification.warning('没有可分享的接口');
      return;
    }

    // 如果勾选了分享接口 参数提交接口id
    if (!apiIds.value.length) {
      notification.warning('请选择接口');
      return;
    }

    params = {
      ...params,
      apiIds: apiIds.value
    };
  }

  // 如果是不是添加的数据 参数提交历史id 请求发送pantch请求
  if (share.id !== 'add') {
    params = {
      ...params,
      id: shareId.value
    };
    return patchShare(params, share);
  }

  // 添加分享 发送post请求
  share.isLoading = true;
  const [error, res = { data: [] }] = await apis.addShare(params);
  share.isLoading = false;
  if (error) {
    return;
  }

  share.name = formState.value.name;
  share.url = res.data.url;
  share.passd = res.data.passd;
  share.publicFlag = formState.value.publicFlag;
  emit('ok', { url: res.data.url, passd: params.passd, type: 'add' });
  copy(share);
  emit('update:visible', false);
};

const historyData = ref<{
  publicFlag: boolean;
  url: string;
  targetId: string;
  targetType: TargetType;
  expiredFlag: boolean;
  historyData?:string;
  passd?: string;
  id: '',
  name: '',
  apiIds?: string[];
}>({
  publicFlag: false,
  url: '',
  targetId: '',
  targetType: 'SERVICE',
  expiredFlag: false,
  id: '',
  name: '',
  apiIds: []
});

const patchShare = async (params, share) => {
  if (utils.deepCompare(historyData.value, params)) {
    copy(share);
    return;
  }
  share.isLoading = true;
  const [error] = await apis.patchShared(params);
  share.isLoading = false;
  if (error) {
    return;
  }
  emit('ok', { url: params.url, passd: params.passd, type: 'edit' });
  copy(params);
  emit('update:visible', false);
};

const shareId = ref(props.sharedId);

const nameErr = ref(false);
const nameChange = (event:ChangeEvent) => {
  const value = event.target.value;
  nameErr.value = !value;
};

const passdErr = ref(false);
const passdChange = (event:ChangeEvent) => {
  const value = event.target.value;
  passdErr.value = !value;
};

const dateErr = ref(false);
const dateChange = (value: string) => {
  if (!value) {
    dateErr.value = true;
    return;
  }

  const isExpired = isWithin5Minutes(value);

  if (isExpired) {
    dateErr.value = true;
    notification.warning('过期时间小于30分钟，请重新选择日期');
  } else {
    dateErr.value = false;
  }
};

const handleCancel = () => {
  emit('update:visible', false);
};

// 删除分享
const delShare = async (id:string) => {
  const [error] = await apis.delShare(id);
  if (error) {
    return;
  }
  notification.success('删除分享记录成功');
  shareList.value = shareList.value.filter(item => item.id !== id);
};

const clickCopyIcon = (item:ShareObj) => {
  copy(item, true);
};

// 复制名称、密码和链接?
const copy = (item:ShareObj, isCopy?:boolean) => {
  let message;
  if (!item.publicFlag) {
    message = `名称: ${item.name}\n链接: ${item.url}\n密码: ${item.passd || ''}`;
  } else {
    message = `名称: ${item.name}\n链接: ${item.url}`;
  }
  clipboard.toClipboard(message).then(() => {
    notification.success(isCopy ? '复制成功' : '分享成功');
  });
};

const queryApisByName = debounce(duration.search, (event:ChangeEvent, share:ShareObj) => {
  const value = event.target.value as string;
  // 如果没有条件 展示所有的接口
  if (!value) {
    share.apiList = allApiList.value;
    allAipIds.value = allApiList.value?.map(item => item.id) || [];
  }

  apiList.value = [];
  for (let i = 0; i < allApiList.value.length; i++) {
    const apiItem = allApiList.value[i];
    if (apiItem.summary.includes(value)) {
      apiList.value.push({ id: apiItem.id, summary: apiItem.summary, method: apiItem.method });
    }
  }
});

const seePassword = ref(true);
const realPassd = ref('');

const initData = () => {
  // 默认展开新家分享数据
  shareList.value = [{
    id: props.sharedId || 'add',
    name: '添加分享',
    targetType: {
      value: props.type,
      message: ''
    },
    targetId: props.id,
    apiIds: [],
    allFlag: false,
    url: '',
    expiredFlag: false,
    publicFlag: true,
    passd: undefined,
    createdBy: '',
    createdByName: '',
    createdDate: '',
    validFlag: false,
    isEdit: true,
    apiList: []
  }];

  expiredDate.value = dayjs().add(24.1, 'hour').format('YYYY-MM-DD HH:mm:ss');
  allAipIds.value = [];
  apiIds.value = [];
  apiList.value = [];
  allApiList.value = [];
  formState.value = {
    publicFlag: true,
    url: '',
    name: '',
    targetId: '',
    targetType: 'API',
    expiredFlag: true
  };
  editShare.value = undefined;
  oldShare.value = undefined;
  passd.value = '';
  apiIndeterminate.value = false;
  apiCheckAll.value = false;
  historyData.value = {
    publicFlag: false,
    url: '',
    targetId: '',
    targetType: 'SERVICE',
    expiredFlag: false,
    id: '',
    name: '',
    apiIds: []
  };
  shareId.value = props.sharedId;
  nameErr.value = false;
  passdErr.value = false;
  dateErr.value = false;
  seePassword.value = true;
  realPassd.value = '';
};

onMounted(async () => {
  await loadUnit();
  watch(() => props.visible, async (newValue) => {
    if (!newValue) {
      return;
    }
    initData();
    await loadApiList();
    if (props.source === 'all') {
      await loadSharedList();
    }

    if (props.source === 'edit') {
      handleEdit(props.share as ShareObj, 0);
    } else {
      getDefaultShareUrl();
    }

    formState.value.targetType = props.type;
    formState.value.targetId = props.id;
  }, {
    immediate: true
  });
});

const getBgColorByApiMethod = (apiMethods:string) => {
  switch (apiMethods) {
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
  }
};

const disabledDate = current => {
  return current && current < dayjs().subtract(1, 'day').endOf('day');
};

// 校验提交的时间是否是当前时间24小时之后的时间（主要用来处理用户点开时间弹框或者选择了时间一直不提交的情况）
function isWithin5Minutes (timeStr:string) {
  const currentTime = dayjs();
  const targetTime = dayjs(timeStr);
  const diffInMinutes = targetTime.diff(currentTime, 'minute');
  return diffInMinutes < 30;
}
</script>
<template>
  <Modal
    title="分享"
    :visible="props.visible"
    :reverse="true"
    :width="600"
    :footer="null"
    @cancel="handleCancel">
    <Hints text="通过分享能够授权其他人在指定时间内查看接口文档和调试接口。" />
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
                v-model:value="formState.name"
                :maxlength="100"
                :error="nameErr"
                placeholder="分享名称"
                @change="nameChange" />
            </template>
            <template v-else>
              <template v-if="item.id === 'add'">
                <div class="flex-1 mr-2 truncate cursor-pointer text-text-link hover:text-text-link-hover" @click="handleEdit(item,index)">{{ item.name }}</div>
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
                <Tooltip title="复制链接" placement="top">
                  <IconCopy
                    v-if="item.id !== 'add'"
                    class="ml-3.5"
                    @click="clickCopyIcon(item)" />
                </Tooltip>
                <Tooltip title="编辑" placement="top">
                  <Icon
                    icon="icon-shuxie"
                    class="cursor-pointer ml-2"
                    @click="handleEdit(item,index)" />
                </Tooltip>
                <Tooltip title="删除" placement="top">
                  <Icon
                    v-if="item.id !== 'add'"
                    icon="icon-qingchu"
                    class="cursor-pointer ml-2 text-3.5"
                    @click="delShare(item.id)" />
                </Tooltip>
              </template>
            </div>
          </div>
        </div>
        <div
          :class="item.isEdit ? 'open-info' : 'stop-info'"
          class="transition-height duration-500 overflow-hidden px-1">
          <template v-if="item.isEdit">
            <div class="text-text-sub-content flex-none mr-2 mt-2.5">链接<Colon /></div>
            <div class="break-all cursor-pointer text-text-link hover:text-text-link-hover text-3 px-2">{{ item.url }}</div>
            <div class="text-text-sub-content flex-none mr-2 mt-2.5">权限<Colon /></div>
            <div class="flex items-center h-7 -mt-0.5 px-2">
              <RadioGroup
                v-model:value="formState.publicFlag"
                size="small"
                class="w-1/2 flex"
                @change="publicFlagChange">
                <Radio
                  v-for="option,oIndex in publicFlagOptions"
                  :key="oIndex"
                  :value="option.value"
                  class="w-1/2 whitespace-nowrap">
                  {{ option.label }}
                </Radio>
              </RadioGroup>
              <template v-if="!formState.publicFlag">
                <Input
                  v-model:value="passd"
                  :error="passdErr"
                  :maxlength="20"
                  type="password"
                  dataType="mixin-en"
                  size="small"
                  placeholder="密码"
                  class="flex-1"
                  @change="passdChange">
                  <template #addonAfter>
                    <a class="text-3 text-text-sub-content" @click="resetPassword"><Icon icon="icon-shuaxin" class="mr-1 -mt-0.5" />重置</a>
                  </template>
                </Input>
              </template>
            </div>
            <div class="text-text-sub-content flex-none mr-2 mt-2.5">有效期<Colon /></div>
            <div class="flex items-center h-7 -mt-0.5  px-2">
              <RadioGroup
                v-model:value="formState.expiredFlag"
                size="small"
                class="w-1/2 flex">
                <Radio
                  v-for="option,oIndex in expirationTimeOptions"
                  :key="oIndex"
                  :value="option.value"
                  class="w-1/2 whitespace-nowrap">
                  {{ option.label }}
                </Radio>
              </RadioGroup>
              <template v-if="!formState.expiredFlag">
                <DatePicker
                  v-model:value="expiredDate"
                  :allowClear="false"
                  :showNow="false"
                  :disabledDate="disabledDate"
                  :showTime="{hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') }"
                  :error="dateErr"
                  internal
                  size="small"
                  type="date"
                  class="flex-grow"
                  @change="dateChange" />
              </template>
            </div>
            <template v-if="props.type !== 'API'">
              <div class="text-text-sub-content flex-none mr-2 mt-2.5">
                分享接口<Colon />
              </div>
              <div class=" border border-border-divider p-2 rounded" :class="allApiList.length > 0 ? 'h-73' : 'h-40'">
                <Input
                  v-if="allApiList.length > 0"
                  placeholder="查询接口名称"
                  size="small"
                  class="mb-2"
                  allowClear
                  @change="(event)=>queryApisByName(event,item)">
                  <template #suffix>
                    <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
                  </template>
                </Input>
                <template v-if="apiList.length">
                  <div class="overflow-hidden hover:overflow-y-auto -mr-2 pr-2.5 -mt-1.5" style="scrollbar-gutter: stable;max-height: 216px;">
                    <CheckboxGroup
                      v-model:value="apiIds"
                      class="text-3 w-full"
                      @change="apiChecked">
                      <div
                        v-for="api in item.apiList"
                        :key="api.id"
                        class="flex items-center space-x-2 mt-1.75 h-5 leading-5">
                        <Checkbox :value="api.id" class="-mt-0.25" />
                        <div class="w-13 whitespace-nowrap" :class="getBgColorByApiMethod(api.method)">{{ api.method }}</div>
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
                      v-model:checked="apiCheckAll"
                      class="checkbox-border-black"
                      :indeterminate="apiIndeterminate"
                      @change="selectALLApi">
                      全选
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
                @click="handleOk(item)">
                保存并复制链接及密码
              </Button>
              <Button
                type="link"
                size="small"
                class="-mx-2"
                @click="cancelEdit(item)">
                取消
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
