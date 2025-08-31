<script setup lang="ts">
import { Button, CheckboxGroup, Popover, Progress, TabPane, Tabs, Tag } from 'ant-design-vue';
import { watch } from 'vue';
import { Grid, Hints, Icon, Input, Tooltip } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { formItems, nodeStatus, nodeUseProgresses, viewItem } from './constant';
import { getStrokeColor, installConfigColumns } from '../constant';
import { NodeItemsProps, NodeItemsEmits } from './types';

// Import composables
import { useNodeData } from './composables/useNodeData';
import { useNodeOperations } from './composables/useNodeOperations';
import { useNodeMonitoring } from './composables/useNodeMonitoring';
import { useNodeNameEdit } from './composables/useNodeNameEdit';
import { useNodeUtils } from './composables/useNodeUtils';

const { t } = useI18n();

/**
 * Component props definition
 */
type Props = NodeItemsProps

const props = withDefaults(defineProps<Props>(), {
  nodeList: () => [],
  roleOptions: () => [],
  autoRefresh: false,
  isAdmin: false
});

/**
 * Component emits definition
 */
const emits = defineEmits<NodeItemsEmits>();

// Initialize composables
const {
  nodeList,
  nodeParams,
  validated,
  showPortTip,
  testBtnDisable,
  addNode,
  cancelEdit,
  changeEditable,
  saveNode,
  deleteNode,
  getEditTip,
  getDeleteTip
} = useNodeData(props, emits);

const {
  loadingStates,
  showTested,
  testSuccess,
  testFailContent,
  showInstallCtrlAccessTokenMap,
  getOnlineInstallTip,
  installAgent,
  foldInstallAgent,
  showInstallStep,
  restartAgent,
  toggleNodeEnabled,
  testConnection,
  toggleShowCtrlAccessToken,
  resetConnectionTest
} = useNodeOperations(emits);

const {
  startMonitoringInterval
} = useNodeMonitoring(props, nodeList);

const {
  editNameInputRef,
  editNameValue,
  editNodeName,
  handleNameBlur,
  isEditingName
} = useNodeNameEdit();

const {
  tenantInfo,
  copyToClipboard,
  canEditNode,
  canDeleteNode,
  canToggleNodeEnabled,
  canRestartAgent,
  getDisabledButtonTooltip
} = useNodeUtils(props);

/**
 * Enhanced add function that resets connection test state
 */
const add = () => {
  addNode();
  resetConnectionTest();
};

/**
 * Enhanced cancel function that resets connection test state
 */
const cancel = (state: any) => {
  cancelEdit(state);
  resetConnectionTest();
};

/**
 * Enhanced changeEditable function that resets connection test state
 */
const changeEditableEnhanced = (state: any) => {
  changeEditable(state);
  resetConnectionTest();
};

/**
 * Enhanced save function that resets connection test state
 */
const saveNodeEnhanced = async () => {
  await saveNode();
  resetConnectionTest();
};

/**
 * Enhanced delete function
 */
const delNode = (state: any) => {
  deleteNode(state);
};

/**
 * Enhanced enable function
 */
const enable = async (state: any) => {
  await toggleNodeEnabled(state);
};

/**
 * Enhanced test function
 */
const test = async () => {
  await testConnection(nodeParams);
};

/**
 * Enhanced edit name function
 */
const editName = (name: string, id: string) => {
  editNodeName(name, id);
};

/**
 * Enhanced node name blur function
 */
const nodeNameBlur = async (name: string, id: string) => {
  await handleNameBlur(name, id);

  // Update local node list after successful name change
  nodeList.value.forEach(node => {
    if (node.id === id) {
      node.name = editNameValue.value;
    }
  });
};

/**
 * Enhanced copy content function
 */
const copyContent = (text: string) => {
  copyToClipboard(text);
};

// Watch for changes in props.nodeList and start monitoring
watch(() => props.nodeList, () => {
  startMonitoringInterval();
}, {
  deep: true,
  immediate: true
});

// Watch for changes in autoRefresh setting
watch(() => props.autoRefresh, (newValue) => {
  if (newValue) {
    startMonitoringInterval();
  }
});

// Expose methods for parent component
defineExpose({
  add,
  startInterval: startMonitoringInterval
});
</script>

<template>
  <div
    v-for="state in nodeList"
    :key="state.id"
    class="border rounded mb-4"
    :class="{'border-blue-text-active': state.editable}">
    <div class="py-2 px-4 font-semibold bg-gray-2 flex justify-between border-b text-3">
      <div class="flex items-center">
        <span v-if="!state.id">{{ t('node.buttons.addNode') }}</span>
        <template v-else>
          <template v-if="!isEditingName(state.id)">
            <RouterLink :to="`/node/detail/${state.id}`"><span class="text-3.5">{{ state.name }}</span></RouterLink>
            <Icon
              v-if="!state.free || tenantInfo?.id === '1'"
              icon="icon-shuxie"
              class="ml-1 hover:text-blue-1 cursor-pointer"
              @click="editName(state.name, state.id)" />
          </template>
          <template v-else>
            <Input
              ref="editNameInputRef"
              v-model:value="editNameValue"
              :placeholder="t('node.nodeItem.labels.inputLimit200')"
              trim
              :maxlength="200"
              @blur="nodeNameBlur(state.name, state.id)" />
          </template>
        </template>
        <Tooltip
          color="#fff"
          overlayClassName="agent-uninstall"
          placement="right">
          <template #title><div class="text-3">{{ t('node.nodeItem.labels.installAgentTip') }}</div></template>
          <p v-show="!state.installAgent && !!state.id" class="text-http-put ml-6 cursor-pointer"><Icon icon="icon-tishi1" class="text-3.5" /></p>
        </Tooltip>
      </div>
      <div v-show="!state.editable" class="flex">
        <Tag
          v-if="state.free"
          color="success"
          class="h-5 leading-5 mr-4">
          {{ t('node.nodeItem.labels.freeNode') }}
        </Tag>
        <div
          v-for="item in nodeStatus"
          :key="item.label"
          class="w-40">
          <label class="text-gray-text">{{ item.label }}</label>
          <span class="text-text-content node-status-icon pl-1" :class="item.status?.['' + !!state?.[item.valueKey]]">{{ item.valueName?.['' + !!state?.[item.valueKey]] }}</span>
        </div>
      </div>
    </div>
    <div class="px-4">
      <!-- 编辑状态 -->
      <div v-if="state.editable" class="text-3">
        <div class="flex flex-wrap justify-between text-text-content text-3 leading-3">
          <div
            v-for="item in formItems"
            :key="item.valueKey"
            class="mt-4 pb-2 relative"
            :style="{width: item.width }">
            <div class="mb-2.5 text-text-content font-medium">{{ item.label }} <span v-if="item.required" class="text-status-error">*</span></div>
            <Input
              v-model:value="nodeParams[item.valueKey]"
              class="input-sm rounded"
              :type="item.type || 'input'"
              :dataType="item.inputType"
              :placeholder="item.placeholder"
              :maxlength="item.maxlength" />
            <span
              v-if="item.required"
              class="text-status-error absolute left-0 -bottom-1.5"
              :class="[validated && !(nodeParams[item.valueKey]?.trim()) ? 'inline' : 'hidden']">{{ t('node.nodeItem.labels.required') }}</span>
            <span
              v-if="item.valueKey === 'sshPort'"
              class="text-status-error absolute left-0 -bottom-1.5"
              :class="[showPortTip ? 'inline' : 'hidden']">{{ t('node.nodeItem.labels.portRangeTip') }}</span>
          </div>
          <div class="mt-4 w-1/3">
            {{ t('node.nodeItem.labels.role') }} <span class="text-status-error">*</span>
            <Popover
              :title="t('node.nodeItem.tips.roleDescription')"
              placement="bottomLeft"
              class="text-3">
              <template #content>
                <div class="max-w-200 text-3">
                  <p>{{ t('node.nodeItem.labels.nodeTip') }}</p>
                  <p class="pl-3"><span>-{{ t('node.nodeItem.labels.managerNode') }}</span>{{ t('node.nodeItem.desc.managerNodeDesc') }}</p>
                  <p class="pl-3"><span>-{{ t('node.nodeItem.labels.controllerNode') }}</span>{{ t('node.nodeItem.desc.controllerNodeDesc') }}</p>
                  <p class="pl-3"><span>-{{ t('node.nodeItem.labels.execNode') }}</span>{{ t('node.nodeItem.desc.execNodeDesc') }}</p>
                  <p class="pl-3"><span>-{{ t('node.nodeItem.labels.mockNode') }}</span>{{ t('node.nodeItem.desc.mockNodeDesc') }}</p>
                  <p class="pl-3"><span>-{{ t('node.nodeItem.labels.appNode') }}</span>{{ t('node.nodeItem.desc.appNodeDesc') }}</p>
                </div>
              </template>
              <Icon icon="icon-shuoming" class="text-blue-light text-3.5 leading-3 ml-1.5" />
            </Popover>
            <div>
              <CheckboxGroup
                v-model:value="nodeParams.roles"
                class="mt-3 check-box-group"
                :options="props.roleOptions">
              </CheckboxGroup>
              <span
                class="text-status-error"
                :class="[validated && nodeParams.roles.length < 1 ? 'block' : 'hidden']">{{ t('node.nodeItem.labels.selectRole') }}</span>
            </div>
          </div>
        </div>

        <div class="pt-5 pb-4 flex items-center">
          <Button
            type="primary"
            class="node-normal-btn"
            @click="saveNodeEnhanced">
            {{ t('actions.save') }}
          </Button>
          <Button class="node-normal-btn" @click="cancel(state)">{{ t('node.nodeItem.buttons.cancel') }}</Button>
          <Button
            :disabled="testBtnDisable"
            class="node-normal-btn"
            @click="test">
            {{ t('node.nodeItem.buttons.testConnection') }}
          </Button>
          <p v-show="showTested && testSuccess" class="text-text-content font-medium">
            <Icon icon="icon-duihao" class="text-status-success mr-1.5 icon-text-3 leading-3" />
            {{ t('node.nodeItem.labels.connectionSuccess') }}
          </p>
          <p v-show="showTested && !testSuccess" class="text-text-content font-medium">
            <Icon icon="icon-chahao" class="text-status-error mr-1.5 icon-text-3 leading-3" />
            {{ t('node.nodeItem.labels.connectionFailed') }}, {{ testFailContent }}
          </p>
        </div>
      </div>
      <!-- 视图状态 -->
      <div v-else class="text-3">
        <div class="flex items-center min-h-28.5 justify-between">
          <div class="flex-1">
            <div class="py-2 flex-wrap leading-8 justify-between">
              <div
                v-for="item in viewItem"
                :key="item.valueKey"
                class="inline-flex items-center"
                :class="{'mr-7.25': !!state[item.valueKey]}">
                <label v-if="!!state[item.valueKey]" class="text-black-active mr-1">{{ item.label }}</label>
                <span v-if="item.type !== 'tag'" class="text-text-content">{{ state[item.valueKey] }}</span>
                <span
                  v-for="tag in state.roles"
                  v-else-if="item.valueKey === 'roles'"
                  :key="tag.value"
                  class="px-1.5 py-1 border text-3 leading-3 rounded-full border-gray-light-b mr-2 text-text-content">{{ tag.message }}
                </span>
                <div v-else-if="!!state.os" class="80">
                  <span
                    v-for="tag in state.os"
                    :key="tag"
                    class="px-1.5 py-1 border text-3 leading-3 rounded-full border-gray-light-b mr-2 text-text-content">{{ tag }}
                  </span>
                </div>
              </div>
            </div>
            <div class="pb-2 flex justify-between max-w-180">
              <!-- 权限控制：（1、只允许管理员修改 || 2、自己添加的节点允许修改）&& 3、不允许修改租户1共享的节点。 -->
              <Tooltip v-if="!canEditNode(state)" :title="getEditTip(state)">
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-shuxie" />
                  {{ t('actions.edit') }}
                </Button>
              </Tooltip>
              <Button
                v-else
                type="text"
                size="small"
                class="flex space-x-1"
                @click="changeEditableEnhanced(state)">
                <Icon icon="icon-shuxie" />
                {{ t('actions.edit') }}
              </Button>
              <Button
                type="text"
                :disabled="!canToggleNodeEnabled(state)"
                :showTextIndex="state.enabled?0:1"
                size="small"
                class="flex space-x-1"
                @click="enable(state)">
                <Icon :icon="state.enabled?'icon-jinyong':'icon-qiyong'" />
                {{ state.enabled ? t('node.nodeItem.buttons.disable') : t('node.nodeItem.buttons.enable') }}
              </Button>
              <Tooltip v-if="!canDeleteNode(state)" :title="getDeleteTip(state)">
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-qingchu" />
                  {{ t('actions.delete') }}
                </Button>
              </Tooltip>
              <Button
                v-else
                type="text"
                size="small"
                class="flex space-x-1"
                @click="delNode(state)">
                <Icon icon="icon-qingchu" />
                {{ t('actions.delete') }}
              </Button>
              <Tooltip v-if="state.free" :title="getOnlineInstallTip(state)">
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-anzhuangdaili" />
                  {{ t('node.nodeItem.buttons.onlineInstall') }}
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="state.installAgent"
                class="node-action-btn"
                :loading="loadingStates.installingMap[state.id]"
                @click="installAgent(state)">
                <Icon icon="icon-anzhuangdaili" class="mr-1" />
                <span> {{ t('node.nodeItem.buttons.onlineInstall') }} </span>
                <Hints
                  v-if="loadingStates.installingMap[state.id]"
                  class="ml-1"
                  :text="t('node.nodeItem.labels.estimatedTime')" />
              </Button>
              <Tooltip v-if="state.free" :title="getDisabledButtonTooltip(state, 'manualInstall')">
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-anzhuangdaili" />
                  {{ t('node.nodeItem.buttons.manualInstall') }}
                </Button>
              </Tooltip>
              <Button
                v-else
                type="text"
                size="small"
                class="flex space-x-1"
                @click="showInstallStep(state)">
                <Icon icon="icon-anzhuangdaili" />
                {{ t('node.nodeItem.buttons.manualInstall') }}
              </Button>
              <Button
                type="text"
                size="small"
                class="flex space-x-1"
                :loading="loadingStates.restartingMap[state.id]"
                :disabled="!canRestartAgent(state)"
                @click="restartAgent(state)">
                <Icon icon="icon-zhongxinkaishi" />
                {{ t('node.nodeItem.buttons.restartAgent') }}
              </Button>
            </div>
          </div>
          <div class="flex items-center justify-between text-right min-w-2/5">
            <div
              v-for="pro in nodeUseProgresses"
              :key="pro.valueKey"
              class="ml-5">
              <Progress
                type="circle"
                :percent="+state.monitorData[pro.valueKey] || 0"
                :width="72"
                :strokeColor="getStrokeColor(state.monitorData[pro.valueKey] || 0)">
                <template #format>
                  <div v-if="pro.valueKey === 'network-up'" class="text-3">
                    <div class="whitespace-nowrap">{{ state.monitorData.txBytesRate || 0 }}MB/s</div>
                  </div>
                  <div v-else-if="pro.valueKey === 'network-down'" class="text-3">
                    <div class="mt-1 whitespace-nowrap">{{ state.monitorData.rxBytesRate || 0 }}MB/s</div>
                  </div>
                  <div v-else class="text-black-active text-3">{{ state.monitorData[pro.valueKey] || 0 }}%</div>
                </template>
              </Progress>
              <div class="text-center mt-1">{{ pro.label }}</div>
            </div>
          </div>
        </div>
        <div v-show="state.showInstallAgent" class="border-t pt-4 pb-4 relative">
          <Tabs size="small">
            <TabPane key="linux" :tab="t('node.nodeItem.installSteps.linuxTitle')">
              <div class="text-3">{{ t('node.nodeItem.installSteps.description') }}</div>
              <div class="text-3">
                {{ t('node.nodeItem.installSteps.method1') }}<Icon
                  icon="icon-fuzhi"
                  class="cursor-pointer text-3.5 text-blue-1"
                  @click="copyContent(state.linuxOfflineInstallSteps?.onlineInstallCmd)" />
                <p class="install-step whitespace-pre-line">
                  {{ state.linuxOfflineInstallSteps?.onlineInstallCmd }}
                </p>
                {{ t('node.nodeItem.installSteps.method2') }}
                <p class="install-step whitespace-pre-line">
                  {{ t('node.nodeItem.installSteps.downloadScript') }}<a class="cursor-pointer" :href="state.linuxOfflineInstallSteps?.installScriptUrl">{{ state.linuxOfflineInstallSteps?.installScriptName }}</a> <br />
                  {{ t('node.nodeItem.installSteps.copyScript', { scriptName: state.linuxOfflineInstallSteps?.installScriptName }) }}<br />
                  {{ state.linuxOfflineInstallSteps?.runInstallCmd }}
                </p>
              </div>
            </TabPane>
            <TabPane key="config" :tab="t('node.nodeItem.installSteps.configTitle')">
              <Grid
                :dataSource="state.installConfig"
                :columns="installConfigColumns">
                <template #tenantId="{text}">
                  <div class="flex items-center">
                    <span>{{ text }}</span> <Button
                      type="link"
                      size="small"
                      @click="copyContent(text)">
                      <Icon icon="icon-fuzhi" class="ml-0.5" />
                    </Button>
                  </div>
                </template>
                <template #deviceId="{text}">
                  <div class="flex items-center">
                    <span>{{ text }}</span> <Button
                      type="link"
                      size="small"
                      @click="copyContent(text)">
                      <Icon icon="icon-fuzhi" class="ml-0.5" />
                    </Button>
                  </div>
                </template>
                <template #serverCtrlUrlPrefix="{text}">
                  <div class="flex items-center">
                    <span>{{ text }}</span> <Button
                      type="link"
                      size="small"
                      @click="copyContent(text)">
                      <Icon icon="icon-fuzhi" class="ml-0.5" />
                    </Button>
                  </div>
                </template>
                <template #ctrlAccessToken="{text}">
                  <div class="flex items-center">
                    <span>{{ showInstallCtrlAccessTokenMap[state.id] ? text : '******' }}</span>
                    <Button
                      size="small"
                      type="link"
                      class="leading-5 h-5"
                      @click="toggleShowCtrlAccessToken(state.id)">
                      <Icon class="text-4" :icon="showInstallCtrlAccessTokenMap[state.id] ? 'icon-biyan' : 'icon-zhengyan'" />
                    </Button>
                  </div>
                </template>
              </Grid>
            </TabPane>
          </Tabs>
          <Button
            class="absolute right-0 bottom-0 text-3"
            type="link"
            size="small"
            @click="foldInstallAgent(state)">
            {{ t('node.nodeItem.buttons.collapse') }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.node-action-btn {
  @apply rounded mr-2 text-text-content text-3 border-0  px-2 shadow-none flex items-center;
}

.node-action-btn :deep(span) {
  @apply ml-1;
}

.install-step {
  @apply px-3 py-1.5 my-2 leading-6;

  background-color: #f6f6f6;
}

.node-action-btn[disabled],
.node-action-btn:not([disabled]),
.node-action-btn:focus {
  @apply bg-transparent !important;
}

.node-action-btn[disabled] {
  @apply opacity-50;
}

.node-action-btn:not([disabled]):hover {
  @apply text-text-link;
}

.node-action-btn::after {
  @apply hidden;
}

.check-box-group :deep(.ant-checkbox-group-item span) {
  @apply text-gray-text text-3 leading-3 !important;
}

.check-box-group :deep(.ant-checkbox-inner) {
  @apply w-3 h-3;
}

.node-normal-btn {
  @apply border-0 rounded mr-3 text-3 leading-3 h-7;
}

.node-normal-btn:not(.ant-btn-primary) {
  @apply bg-gray-bg;
}

.node-status-icon::before {
  @apply w-1.5 h-1.5 rounded-full mr-1;

  content: "";
  display: inline-block;
}

.success.node-status-icon::before {
  @apply bg-status-success;
}

.fail.node-status-icon::before {
  @apply bg-status-error;
}

</style>
<style>
.ant-modal.ant-modal-confirm-confirm .ant-modal-confirm-body > .anticon > svg {
  font-size: 18px;
}

.agent-uninstall {
  max-width: inherit;
}
</style>
