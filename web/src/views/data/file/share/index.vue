<script setup lang="ts">
import { computed, ref } from 'vue';
import { Icon, Input, Modal, notification, ShortDuration } from '@xcan-angus/vue-ui';
import type { TreeProps } from 'ant-design-vue';
import { Button, Form, FormItem, RadioGroup, Textarea, Tree } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { useShareModal } from './composables/useShareModal';

const { t } = useI18n();

interface Props {
  visible: boolean;
  id: string;
  spaceId: string;
  spaceName: string;
  defaultIds?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: '',
  defaultIds: () => ([])
});

// Define emits
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

// Use the share modal composable
const {
  expandedKeys,
  checkedKeys,
  treeData,
  isLoading,
  state,
  form,
  okButtonText,
  durationSelectProps,
  durationInputProps,
  handleChecked,
  loadNodeData,
  saveShare,
  close,
  handlePermissionChange,
  copyToClipboard
} = useShareModal();

// Initialize the composable
useShareModal().init(props, emits);

/**
 * Cancel share operation
 */
const cancel = () => {
  close();
};
</script>
<template>
  <Modal
    :title="t('fileSpace.share.title')"
    :visible="visible"
    :reverse="true"
    :okButtonProps="{loading: isLoading}"
    :okText="okButtonText"
    @ok="saveShare"
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
      :loadData="loadNodeData"
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
            @change="handlePermissionChange">
          </RadioGroup>
        </div>
        <Input
          v-if="!form.public0"
          v-model:value="form.password"
          dataType="mixin-en"
          :maxlength="40"
          size="small"
          :placeholder="t('common.password')"
          class="w-50 rounded mt-1"
          :class="{'!border-status-error': !form.password}" />
      </FormItem>
      <FormItem v-if="form.url" :label="t('fileSpace.share.form.viewLink')">
        <Input
          v-model:value="form.url"
          size="small"
          :disabled="true" />
        <div class="mt-3">
          <Button size="small" @click="copyToClipboard"><Icon icon="icon-fuzhi" class="mr-1.5" />{{ t('fileSpace.share.buttons.copyLinkAndPassword') }}</Button>
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
      <FormItem :label="t('common.remark')">
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
