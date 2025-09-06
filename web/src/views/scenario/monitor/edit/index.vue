<script lang="ts" setup>
import { onMounted, watch, defineAsyncComponent, inject, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Select, Icon, Spin, Hints, IconRequired } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Textarea, Tabs, TabPane, RadioGroup, Radio } from 'ant-design-vue';
import { AuthObjectType, TESTER, GM } from '@xcan-angus/infra';

// Import composables
import { useFormData } from './composables/useFormData';
import { useMonitorActions } from './composables/useMonitorActions';
import { useServerConfig } from './composables/useServerConfig';
import { useNotificationConfig } from './composables/useNotificationConfig';
import { useFormValidation } from './composables/useFormValidation';

// Import types
import type { MonitorEditProps } from './types';

const { t } = useI18n();

// Component props
const props = defineProps<MonitorEditProps>();

// Async component
const CreatedDate = defineAsyncComponent(() => import('@/views/scenario/monitor/edit/CreatedDate.vue'));

// Initialize composables
const {
  formState,
  timeSetting,
  noticeSetting,
  setFormData
} = useFormData();

const {
  loading,
  loadMonitorDetail,
  updateMonitor,
  createMonitor,
  refreshMonitorList,
  closeTabPane
} = useMonitorActions();

// Inject tab pane functions
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

const {
  serverSetting: serverConfig,
  isHttpPlugin,
  loadServerSetting,
  loadScenarioPlugin,
  hasVariables,
  changeVariableDefaultValue,
  handleScenarioChange
} = useServerConfig();

const {
  orgs: notificationOrgs,
  initializeOrgs,
  validateOrgs,
  handleOrgTypeChange,
  handleOrgSelectionChange,
  getNotificationSettingWithOrgs
} = useNotificationConfig();

const {
  formRef,
  createdDateRef,
  activeTabKey,
  buildMonitorParams,
  validateAndSubmit
} = useFormValidation();

// Check if form is in edit mode
const isEditMode = computed(() => {
  return !!props.data?.id;
});

// Computed property for organization selection values (for Select component)
const orgSelectionValues = computed({
  get: () => noticeSetting.value.orgs.map(org => org.id),
  set: (_values: string[]) => {
    // This will be handled by the change event
  }
});

// Initialize organization list with user info
initializeOrgs(props.userInfo);

/**
 * Load monitor data and populate form
 * @param id - Monitor ID
 */
const loadData = async (id: string) => {
  const data = await loadMonitorDetail(id);
  if (!data) {
    return;
  }

  setFormData(data);

  if (data.scenarioId) {
    await loadScenarioPlugin(data.scenarioId);
  }

  // Update tab pane title
  if (data.name) {
    updateTabPane({ name: data.name, _id: id });
  }
};

/**
 * Handle form submission
 */
const handleSubmit = async () => {
  const params = buildMonitorParams(
    formState.value,
    timeSetting.value,
    getNotificationSettingWithOrgs(noticeSetting.value),
    serverConfig.value,
    props.projectId,
    props.data?.id
  );

  const success = isEditMode.value
    ? await updateMonitor(params)
    : await createMonitor(params);

  if (success) {
    refreshMonitorList();
    closeTabPane(props._id);
  }
};

/**
 * Handle form cancellation
 */
const handleCancel = () => {
  closeTabPane(props._id);
};

/**
 * Handle scenario selection change
 * @param value - Selected scenario ID
 * @param option - Scenario option data
 */
const handleSceneSelectionChange = (value: any, option: any) => {
  if (typeof value === 'string') {
    handleScenarioChange(value, option);
  }
};

/**
 * Handle organization type change
 */
const handleOrgTypeChangeWrapper = () => {
  handleOrgTypeChange(noticeSetting.value);
};

/**
 * Handle organization selection change
 * @param value - Selected organization values
 * @param option - Organization option data
 */
const handleOrgSelectionChangeWrapper = (value: any, option: any) => {
  if (Array.isArray(value) && Array.isArray(option)) {
    handleOrgSelectionChange(value, option);
  }
};

// Watch for data changes
onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id || id === oldValue?.id) {
      return;
    }
    await loadData(id);
  }, { immediate: true });

  watch(() => formState.value.scenarioId, (newValue) => {
    loadServerSetting(newValue);
  });
});
</script>
<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <!-- Action buttons -->
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="validateAndSubmit(handleSubmit, noticeSetting, notificationOrgs)">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('scenarioMonitor.edit.save') }}</span>
      </Button>
      <Button
        size="small"
        class="flex items-center space-x-1"
        @click="handleCancel">
        <span>{{ t('scenarioMonitor.edit.cancel') }}</span>
      </Button>
    </div>

    <!-- Main form -->
    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '75px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <!-- Scenario selection -->
      <FormItem
        required
        name="scenarioId"
        :label="t('scenarioMonitor.edit.monitorScene')">
        <Select
          v-model:value="formState.scenarioId"
          :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{label: 'name', value: 'id'}"
          :disabled="!!props?.data?.id"
          :placeholder="t('scenarioMonitor.edit.selectScene')"
          @change="handleSceneSelectionChange" />
      </FormItem>

      <!-- Monitor name -->
      <FormItem
        required
        :label="t('scenarioMonitor.edit.monitorName')"
        name="name"
        class="flex-1 min-w-0">
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          :placeholder="t('scenarioMonitor.edit.monitorNamePlaceholder')" />
      </FormItem>

      <!-- Description -->
      <FormItem
        :label="t('scenarioMonitor.edit.description')"
        name="description"
        class="flex-1 min-w-0">
        <Textarea
          v-model:value="formState.description"
          :maxlength="200"
          :placeholder="t('scenarioMonitor.edit.descriptionPlaceholder')" />
      </FormItem>

      <!-- Tab panels -->
      <Tabs v-model:activeKey="activeTabKey" size="small">
        <!-- Time configuration tab -->
        <TabPane key="time">
          <template #tab>
            <div><IconRequired />{{ t('scenarioMonitor.edit.monitorTime') }}</div>
          </template>
          <CreatedDate
            ref="createdDateRef"
            :createTimeSetting="timeSetting as any" />
        </TabPane>

        <!-- Server configuration tab (only for HTTP plugins) -->
        <TabPane
          v-if="isHttpPlugin"
          key="server"
          :tab="t('scenarioMonitor.edit.serverConfig')">
          <div class="w-100 space-y-3">
            <div
              v-for="(serverObj, idx) in serverConfig"
              :key="idx"
              class="border rounded p-2">
              <div class="font-bold text-text-title flex items-center">
                <Icon icon="icon-fuwuqi" class="mr-1" />{{ serverObj.url }}
              </div>
              <div class="my-3">{{ serverObj.description || t('scenarioMonitor.edit.noDescription') }}</div>

              <!-- Variables section -->
              <ul v-if="hasVariables(serverObj.variables)" class="list-disc space-y-1 pl-4">
                <li v-for="(variableValue, key) in (serverObj.variables || {})" :key="key">
                  <div
                    class="text-3 text-text-title rounded-sm leading-5 truncate cursor-pointer inline font-bold"
                    :title="String(key)"
                    style="max-width: 400px;">
                    {{ key }}
                  </div>
                  <div class="space-y-1">
                    <div
                      v-for="enumValue in variableValue.enum"
                      :key="enumValue"
                      class="flex items-center justify-between">
                      <div
                        class="truncate cursor-pointer"
                        style="max-width: 400px;"
                        :title="String(enumValue)">
                        {{ enumValue }}
                      </div>
                      <div class="inline-flex items-center space-x-1">
                        <span v-show="variableValue.default === enumValue">{{ t('scenarioMonitor.edit.default') }}</span>
                        <Radio
                          size="small"
                          :checked="variableValue.default === enumValue"
                          class="-mt-1.5"
                          @click="changeVariableDefaultValue(idx, String(key), String(enumValue))" />
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
              <div v-else>
                {{ t('scenarioMonitor.edit.noVariables') }}
              </div>
            </div>
          </div>
        </TabPane>

        <!-- Notification configuration tab -->
        <TabPane
          key="notice"
          forceRender>
          <template #tab>
            <div><IconRequired />{{ t('scenarioMonitor.edit.notificationConfig') }}</div>
          </template>

          <!-- Notification enable/disable -->
          <RadioGroup
            v-model:value="noticeSetting.enabled"
            :options="[
              {value: false, label: t('scenarioMonitor.edit.notificationOptions.noNotify')},
              { value: true, label: t('scenarioMonitor.edit.notificationOptions.notify') }]">
          </RadioGroup>
          <Hints :text="t('scenarioMonitor.edit.notificationHint')" class="mt-3" />

          <!-- Notification recipients configuration -->
          <template v-if="noticeSetting.enabled">
            <div class="flex space-x-3 mt-3">
              <span>{{ t('scenarioMonitor.edit.recipient') }}</span>

              <!-- Organization type selection -->
              <RadioGroup
                v-model:value="noticeSetting.orgType"
                @change="handleOrgTypeChangeWrapper">
                <Radio :value="AuthObjectType.USER">{{ t('scenarioMonitor.edit.recipientOptions.user') }}</Radio>
                <Radio :value="AuthObjectType.DEPT">{{ t('scenarioMonitor.edit.recipientOptions.dept') }}</Radio>
                <Radio :value="AuthObjectType.GROUP">{{ t('scenarioMonitor.edit.recipientOptions.group') }}</Radio>
              </RadioGroup>

              <!-- Organization selection -->
              <FormItem
                name="orgs"
                class="w-50"
                :rules="[{validator: validateOrgs}]">
                <!-- User selection -->
                <Select
                  v-if="noticeSetting.orgType === AuthObjectType.USER"
                  v-model:value="orgSelectionValues"
                  :showSearch="true"
                  defaultActiveFirstOption
                  :lazy="false"
                  mode="multiple"
                  allowClear
                  class="w-50"
                  :placeholder="t('scenarioMonitor.edit.selectUser')"
                  :action="`${GM}/user?fullTextSearch=true`"
                  :fieldNames="{ label: 'fullName', value: 'id' }"
                  @change="handleOrgSelectionChangeWrapper">
                </Select>

                <!-- Department selection -->
                <Select
                  v-if="noticeSetting.orgType === AuthObjectType.DEPT"
                  v-model:value="orgSelectionValues"
                  defaultActiveFirstOption
                  :placeholder="t('scenarioMonitor.edit.selectDept')"
                  class="w-50"
                  mode="multiple"
                  allowClear
                  :lazy="false"
                  :showSearch="true"
                  :action="`${GM}/dept?fullTextSearch=true`"
                  :fieldNames="{ label: 'name', value: 'id' }"
                  @change="handleOrgSelectionChangeWrapper">
                </Select>

                <!-- Group selection -->
                <Select
                  v-if="noticeSetting.orgType === AuthObjectType.GROUP"
                  v-model:value="orgSelectionValues"
                  defaultActiveFirstOption
                  :placeholder="t('scenarioMonitor.edit.selectGroup')"
                  class="w-50"
                  mode="multiple"
                  allowClear
                  :lazy="false"
                  :showSearch="true"
                  :action="`${GM}/group?fullTextSearch=true`"
                  :fieldNames="{ label: 'name', value: 'id' }"
                  @change="handleOrgSelectionChangeWrapper">
                </Select>
              </FormItem>
            </div>
          </template>
        </TabPane>
      </Tabs>
    </Form>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
