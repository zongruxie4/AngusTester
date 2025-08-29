<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { Icon, Input, Modal, notification, ShortDuration } from '@xcan-angus/vue-ui';
import type { TreeProps } from 'ant-design-vue';
import { Button, Form, FormItem, RadioGroup, Textarea, Tree } from 'ant-design-vue';
import { AppOrServiceRoute, DomainManager, enumUtils, ShortTimeUnit, toClipboard, utils } from '@xcan-angus/infra';
import { space } from '@/api/storage';
import store from '@/store';
import { useI18n } from 'vue-i18n';

import { randomString } from '@/utils/utils';

const { t } = useI18n();

interface Props {
  visible:boolean,
  id:string,
  spaceId: string,
  spaceName: string,
  defaultIds?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: '',
  bizType: undefined,
  defaultIds: () => ([])
});

const expandedKeys = ref<string[]>([]);
const checkedKeys = ref<{checked: string[]}>({ checked: props.defaultIds });
const treeData = ref<TreeProps['treeData']>([{ name: props.spaceName, id: props.spaceId, key: props.spaceId, type: 'SPACE', disabled: true }]);

const resetDefaultData = () => {
  checkedKeys.value = { checked: props.defaultIds };
  treeData.value = [{ name: props.spaceName, id: props.spaceId, key: props.spaceId, type: 'SPACE', disabled: true }];
};

const checkedMap = {};
const handleChecked = (_checkedKeys, { checked, node }) => {
  if (checked) {
    checkedMap[node.id] = node.idLines;
  } else {
    delete checkedMap[node.id];
  }
};

const onLoadData:TreeProps['loadData'] = treeNode => {
  const spaceId = props.spaceId;
  const parentDirectoryId = treeNode.type === 'DIRECTORY' ? treeNode.id : '-1';
  return space.getFileList({ spaceId, parentDirectoryId, pageSize: store.state.maxPageSize, pageNo: 1 })
    .then(([error, res]) => {
      if (error) {
        return;
      }
      if (treeNode.dataRef?.children) {
        return;
      }

      if (treeNode.dataRef) {
        const idLines = +parentDirectoryId === -1 ? [] : treeNode.idLines.concat([parentDirectoryId]);
        treeNode.dataRef.children = (res.data.list || []).map(data => ({ ...data, idLines: [...idLines, data.id], spaceId: spaceId, type: data.type.value, isLeaf: data.type.value === 'FILE' }));
      }
      treeData.value = [...treeData.value];
    });
};

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e:'update:visible', value:boolean):void,
  (e:'ok'):void
}>();

const isLoading = ref(false);

const state = reactive({
  enums: [{
    label: t('fileSpace.share.permissionOptions.public'),
    value: true
  }, {
    label: t('fileSpace.share.permissionOptions.private'),
    value: false
  }],
  unitEnum: [],
  expiredEnums: [{
    label: t('fileSpace.share.validityOptions.setValidity'),
    value: true
  }, {
    label: t('fileSpace.share.validityOptions.permanent'),
    value: false
  }]
});

const form = reactive({
  expiredDuration: '15d',
  password: undefined,
  public0: true,
  url: '',
  expiredFlag: true,
  remark: undefined,
  id: undefined
});

const resetForm = () => {
  form.expiredDuration = '15d';
  form.expiredFlag = true;
  form.password = undefined;
  form.public0 = true;
  form.url = '';
  form.remark = undefined;
  form.id = undefined;
};

const loadUnit = () => {
  if (state.unitEnum.length) {
    return;
  }
  const excludeUnit = [ShortTimeUnit.Millisecond, ShortTimeUnit.Second];
  const data = enumUtils.enumToMessages(ShortTimeUnit);
  state.unitEnum = data.filter(unit => !excludeUnit.includes(unit.value));
};

let initParams = {};

// 分享记录信息
const loadShareInfo = async () => {
  const [error, res = { data: {} }] = await space.getShareDetail(props.id);
  if (error) {
    return;
  }
  const { expiredDuration, password, public0, url, remark, id, objectIds, expiredFlag } = res.data;
  form.expiredDuration = expiredDuration;
  form.password = password;
  form.public0 = public0;
  form.url = url;
  form.expiredFlag = expiredFlag;
  form.remark = remark;
  form.id = id;
  checkedKeys.value.checked.push(...objectIds);
  initParams = {
    ...form,
    objectIds: objectIds
  };
};

// 获取分享的url
const loadShareUrl = async () => {
  const host = DomainManager.getInstance().getAppDomain(AppOrServiceRoute.tester);
  const route = '/share/file';
  return `${host}${route}`;
};

const getCheckedIds = () => {
  const ids = [...checkedKeys.value.checked].filter(id => id !== props.spaceId);
  Object.keys(checkedMap).forEach(key => {
    ids.push(...checkedMap[key]);
  });
  return Array.from(new Set(ids));
};

const durationSelectProps = {
  excludes: (value) => {
    return ['ms'].includes(value.message);
  },
  class: '!w-20'
};

const durationInputProps = {
  placeholder: t('fileSpace.share.placeholders.validityPeriod')
};

// 增加分享成功
const addProjectShare = async () => {
  if (!form.password && !form.public0) {
    return;
  }
  if (form.url) {
    patchShare();
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
  notification.success(t('fileSpace.share.messages.generateShareLink'));
  form.url = res.data.url;
  form.id = res.data.id;
  initParams = {
    ...form,
    objectIds: params.objectIds
  };
};

const patchShare = async () => {
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

const cancel = () => {
  close();
};

const close = () => {
  resetForm();
  emits('update:visible', false);
};

const resetPass = () => {
  if (!form.public0) {
    form.password = randomString();
  } else {
    form.password = undefined;
  }
};

// 复制密码和链接
const copy = () => {
  let message;
  if (form.public0) {
    message = t('fileSpace.share.messages.link', { url: form.url });
  } else {
    message = t('fileSpace.share.messages.linkAndPassword', { url: form.url, password: form.password || '' });
  }
  toClipboard(message).then(() => {
    notification.success(t('tips.copySuccess'));
  });
};

// 修改分享权限类型
const changeType = () => {
  resetPass();
};

const getOkText = computed(() => {
  return form.url ? t('actions.confirm') : t('fileSpace.share.buttons.generateLink');
});

watch(() => props.visible, newValue => {
  if (newValue) {
    resetPass();
    loadUnit();
    resetDefaultData();
    if (props.id) {
      loadShareInfo();
    }
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="t('fileSpace.share.title')"
    :visible="visible"
    :reverse="true"
    :okButtonProps="{loading: isLoading}"
    :okText="getOkText"
    @ok="addProjectShare"
    @cancel="cancel">
    <Tree
      v-if="props.visible"
      v-model:expandedKeys="expandedKeys"
      v-model:checkedKeys="checkedKeys"
      showIcon
      checkable
      checkStrictly
      class="space-tree"
      :selectable="false"
      :height="260"
      :fieldNames="{children:'children', title:'name', key:'id'}"
      :loadData="onLoadData"
      :treeData="treeData"
      @check="handleChecked">
      <template #icon="{type}">
        <template v-if="type==='SPACE'">
          <Icon icon="icon-kongjian" class="text-4" />
        </template>
        <template v-else-if="type==='DIRECTORY'">
          <Icon icon="icon-wenjianjia" class="text-4" />
        </template>
        <template v-else>
          <Icon icon="icon-wenjian" class="text-4" />
        </template>
      </template>
    </Tree>
    <Form layout="vertical" size="small">
      <FormItem :label="t('fileSpace.share.form.viewPermission')">
        <div class="">
          <RadioGroup
            v-model:value="form.public0"
            :disabled="!!form.url"
            :options="state.enums"
            name="radioGroup"
            size="small"
            @change="changeType">
          </RadioGroup>
        </div>
        <Input
          v-if="!form.public0"
          v-model:value="form.password"
          dataType="mixin-en"
          :maxlength="40"
          size="small"
          :placeholder="t('fileSpace.share.placeholders.password')"
          class="w-50 rounded mt-1"
          :class="{'!border-status-error': !form.password}" />
      </FormItem>
      <FormItem v-if="form.url" :label="t('fileSpace.share.form.viewLink')">
        <Input
          v-model:value="form.url"
          size="small"
          :disabled="true" />
        <div class="mt-3">
          <Button size="small" @click="copy"><Icon icon="icon-fuzhi" class="mr-1.5" />{{ t('fileSpace.share.buttons.copyLinkAndPassword') }}</Button>
        </div>
      </FormItem>
      <FormItem :label="t('fileSpace.share.form.validityPeriod')">
        <div class="flex w-full justify-between items-center">
          <RadioGroup
            v-model:value="form.expiredFlag"
            name="radioGroup"
            size="small"
            :inputProps="durationInputProps"
            :options="state.expiredEnums">
          </RadioGroup>
        </div>
        <ShortDuration
          v-show="form.expiredFlag"
          v-model:value="form.expiredDuration"
          class="!w-50 mt-1"
          :selectProps="durationSelectProps" />
      </FormItem>
      <FormItem :label="t('fileSpace.share.form.remark')">
        <Textarea
          v-model:value="form.remark"
          class="mb-3"
          showCount
          :maxlength="200" />
      </FormItem>
    </Form>
  </Modal>
</template>
<style>
.space-tree.ant-tree .ant-tree-treenode {
  max-width: 480px;
  white-space: nowrap;
}

.space-tree.ant-tree .ant-tree-treenode .ant-tree-node-content-wrapper {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
