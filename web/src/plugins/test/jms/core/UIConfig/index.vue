<script setup lang="ts">
import { nextTick, ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Button, Collapse, CollapsePanel, Switch, Tabs, TabPane, RadioGroup, Radio, Checkbox } from 'ant-design-vue';
import Draggable from 'vuedraggable';
import { debounce } from 'throttle-debounce';
import { Icon, NoData, Input, Tooltip, Colon, Arrow } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';
import SelectEnum from '@/components/enum/SelectEnum.vue';

import { PipelineConfig } from '../PropsType';

const { t } = useI18n();

export interface Props {
  value: PipelineConfig[];
  loaded: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loaded: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'errorNumChange', value: number): void;
  (e: 'renderChange', value: boolean): void;
}>();

const domId = utils.uuid();
const rendered = ref(false);

const activeKeys = ref<string[]>([]);
const emptyNameSet = ref<Set<string>>(new Set());

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const repeatNameSet = ref<Set<string>>(new Set());
const fileMap = ref<{ [key: string]: { url: string; file: File } }>({});

const tabActiveKeyMap = ref<{[key:string]:'mail'|'security'|'other'}>({});
const mailSetErrorNumMap = ref<Map<string, number>>(new Map());
const securitySetErrorNumMap = ref<Map<string, number>>(new Map());
const otherSetErrorNumMap = ref<Map<string, number>>(new Map());

const insertData = () => {
  const id = utils.uuid();
  dataMap.value[id] = {
    id,
    target: 'JMS',
    name: '',
    description: '',
    enabled: true,
    beforeName: '',
    download: false,
    localUrl: '',
    method: 'RETR',
    remoteUrl: '',
    server: {
      ip: '',
      port: ''
    },
    username: '',
    password: '',
    transmissionMode: 'binary'
  };
  idList.value.push(id);
  activeKeys.value.push(id);

  scrollToBottom();
};

const nameChange = debounce(duration.search, (id: string, event: { target: { value: string } }) => {
  const value = event.target.value;
  dataMap.value[id].name = value;

  repeatNameSet.value.clear();
  const uniqueNames = new Set();
  const map = dataMap.value;
  for (const key in map) {
    const name = map[key].name;
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNameSet.value.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }
});

const openChange = (id: string, _open: boolean) => {
  if (_open) {
    activeKeys.value.push(id);
    return;
  }

  activeKeys.value = activeKeys.value.filter(item => item !== id);
};

const enabledChange = (id: string, enabled: boolean) => {
  dataMap.value[id].enabled = enabled;
};

const actionClick = (id: string, type: 'delete' | 'clone') => {
  switch (type) {
    case 'clone':
      toClone(id);
      break;
    case 'delete':
      toDelete(id);
      break;
  }
};

const toClone = (targetId: string) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const cloneId = utils.uuid();
      const sourceData: PipelineConfig = JSON.parse(JSON.stringify(dataMap.value[targetId]));
      dataMap.value[cloneId] = sourceData;
      dataMap.value[cloneId].name = sourceData.name ? (sourceData.name + ' copy') : '';
      idList.value.splice(i + 1, 0, cloneId);
      if (activeKeys.value.includes(id)) {
        activeKeys.value.push(cloneId);
      }

      return;
    }
  }
};

const toDelete = (targetId: string) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const name = dataMap.value[targetId].name;
      repeatNameSet.value.delete(name);
      delete dataMap.value[targetId];
      idList.value.splice(i, 1);
      break;
    }
  }
};

const scrollToBottom = () => {
  const dom = document.getElementById(domId);
  if (!dom) {
    return;
  }

  nextTick(() => {
    setTimeout(() => {
      dom.scrollTop = dom.scrollHeight;
    }, 16.66);
  });
};

const reset = () => {
  activeKeys.value = [];
  emptyNameSet.value.clear();
  idList.value = [];
  dataMap.value = {};
  repeatNameSet.value.clear();
  fileMap.value = {};
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();

    if (!newValue?.length) {
      insertData();
      return;
    }

    const list = JSON.parse(JSON.stringify(newValue));
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id } = data;
      // 自动补全enabled，默认值为true
      if (data.enabled !== false) {
        data.enabled = true;
      }

      dataMap.value[id] = data;
      idList.value.push(id);
      activeKeys.value.push(id);
    }
  }, { immediate: true });

  emit('renderChange', true);
  rendered.value = true;
});

const repeatNameIdSet = computed(() => {
  const set = new Set<string>();
  const ids = idList.value;
  const _repeatNameSet = repeatNameSet.value;
  const map = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (_repeatNameSet.has(map[id].name)) {
      set.add(id);
    }
  }

  return set;
});

const nameErrorSet = computed(() => {
  return new Set<string>([...emptyNameSet.value, ...repeatNameIdSet.value]);
});

const isValid = (): boolean => {
  emptyNameSet.value.clear();
  const ids = idList.value;
  const map = dataMap.value;
  const set = repeatNameIdSet.value;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name } = map[id];
    if (!name) {
      emptyNameSet.value.add(id);
      errorNum++;
    } else {
      if (set.has(id)) {
        emptyNameSet.value.add(id);
        errorNum++;
      }
    }
  }

  return !errorNum;
};

const getData = (): PipelineConfig[] => {
  const result: PipelineConfig[] = [];
  const map = dataMap.value;
  const ids = idList.value;
  let beforeName = '';
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const data = JSON.parse(JSON.stringify(map[id]));
    result.push({ ...data, beforeName });
    beforeName = data.name;
  }

  return result;
};

defineExpose({
  isValid,
  getData
});

const selectOptions = [
  { label: 'SMTP', value: 'SMTP' },
  { label: 'POP3', value: 'POP3' },
  { label: 'IMAP', value: 'IMAP' },
  { label: 'HTTP', value: 'HTTP' },
  { label: 'HTTPS', value: 'HTTPS' }
];
</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertData">
        <div class="flex items-center">
          <Icon icon="icon-httpcanshu" class="mr-1" />
          <span>{{ t('jmsPlugin.uiConfig.insertFTPRequest') }}</span>
        </div>
      </Button>
    </div>
    <template v-if="props.loaded">
      <Draggable
        v-if="!!idList.length"
        :id="domId"
        :list="idList"
        :animation="300"
        group="scenario"
        itemKey="id"
        tag="ul"
        handle=".drag-handle"
        style="height: calc(100% - 40px);"
        class="my-4 pl-5 pr-4 space-y-3 overflow-y-auto overflow-x-hidden scroll-smooth">
        <template #item="{ element: id }">
          <li
            :id="id"
            :key="id"
            class="drag-item relative">
            <Icon
              :class="{ invisible: !rendered }"
              icon="icon-yidong"
              class="drag-handle absolute top-3.75 left-3 z-10 text-4 cursor-move text-theme-sub-content" />
            <Collapse
              :activeKey="activeKeys"
              :class="{ 'opacity-70': !dataMap[id].enabled }"
              :bordered="false">
              <CollapsePanel
                :key="id"
                :showArrow="false"
                style="background-color: rgba(251, 251, 251, 100%);"
                collapsible="disabled">
                <template #header>
                  <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
                    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-httpcanshu" />
                    <div class="flex-1 flex items-center space-x-3 mr-5">
                      <Tooltip
                        :title="t('jmsPlugin.uiConfig.nameDuplicate')"
                        internal
                        placement="right"
                        destroyTooltipOnHide
                        :visible="repeatNameIdSet.has(id)">
                        <Input
                          :value="dataMap[id].name"
                          :maxlength="400"
                          :error="nameErrorSet.has(id)"
                          :title="dataMap[id].name"
                          style="flex:1 1 40%;"
                          trim
                          :placeholder="t('common.placeholders.searchKeyword')"
                          @change="nameChange(id, $event)" />
                      </Tooltip>
                      <Input
                        v-model:value="dataMap[id].description"
                        :maxlength="800"
                        :title="dataMap[id].description"
                        trim
                        style="flex:1 1 60%;"
                        :placeholder="t('jmsPlugin.uiConfig.descriptionPlaceholder')" />
                    </div>
                    <div class="flex items-center flex-shrink-0 space-x-3">
                      <Switch
                        :checked="dataMap[id].enabled"
                        size="small"
                        @change="enabledChange(id, $event)" />
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.clone')">
                        <Icon
                          icon="icon-fuzhi"
                          class="text-3.5"
                          @click="actionClick(id, 'clone')" />
                      </div>
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.delete')">
                        <Icon
                          icon="icon-qingchu"
                          class="text-3.5"
                          @click="actionClick(id, 'delete')" />
                      </div>
                      <Arrow :open="activeKeys.includes(id)" @change="openChange(id, $event)" />
                    </div>
                  </div>
                </template>

                <Tabs v-model:activeKey="tabActiveKeyMap[id]" size="small">
                  <TabPane key="mail">
                    <template #tab>
                      <Badge
                        class="count-Badge-container"
                        size="small"
                        :count="mailSetErrorNumMap.get(id)">
                        <div>{{ t('jmsPlugin.uiConfig.mailSettings') }}</div>
                      </Badge>
                    </template>
                    <div class="space-y-3">
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-21.5">
                          <span>{{ t('protocol.name') }}</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-2.5 max-w-175">
                          <SelectEnum
                            style="width: calc((100% - 10px)/2);"
                            :placeholder="t('jmsPlugin.uiConfig.protocolPlaceholder')"
                            :options="selectOptions" />
                        </div>
                      </div>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-21.5">
                          <span>{{ t('jmsPlugin.uiConfig.hostnameOrIP') }}</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-2.5 max-w-175">
                          <Input
                            v-model:value="dataMap[id].server.ip"
                            :maxlength="200"
                            trimAll
                            style="flex: 1 1 80%;"
                            :title="t('jmsPlugin.uiConfig.hostnameOrIP')"
                            :placeholder="t('jmsPlugin.uiConfig.hostnameOrIPPlaceholder')" />
                          <Input
                            v-model:value="dataMap[id].server.port"
                            :maxlength="8"
                            :min="0"
                            trimALl
                            style="flex: 1 1 20%;min-width: 75px;max-width: 200px;"
                            :title="t('protocol.port')"
                            dataType="number"
                            :placeholder="t('jmsPlugin.uiConfig.portPlaceholder')" />
                        </div>
                      </div>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-21.5">
                          <span>{{ t('jmsPlugin.uiConfig.usernamePassword') }}</span>
                          <Colon />
                        </div>
                        <div class="flex-1 flex items-center space-x-2.5 max-w-175">
                          <Input
                            v-model:value="dataMap[id].username"
                            :maxlength="200"
                            trimAll
                            :title="t('common.username')"
                            :placeholder="t('jmsPlugin.uiConfig.usernamePlaceholder')" />
                          <Input
                            v-model:value="dataMap[id].password"
                            :maxlength="200"
                            trimAll
                            :title="t('common.password')"
                            :placeholder="t('jmsPlugin.uiConfig.passwordPlaceholder')"
                            type="password" />
                        </div>
                      </div>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-21.5">
                          <span>{{ t('jmsPlugin.uiConfig.inboxFolder') }}</span>
                          <Colon />
                        </div>
                        <Input
                          v-model:value="dataMap[id].remoteUrl"
                          :maxlength="800"
                          :title="t('jmsPlugin.uiConfig.inboxFolder')"
                          :placeholder="t('jmsPlugin.uiConfig.inboxFolderPlaceholder')"
                          class="max-w-175" />
                      </div>
                    </div>
                  </TabPane>
                  <TabPane key="security">
                    <template #tab>
                      <Badge
                        class="count-Badge-container"
                        size="small"
                        :count="securitySetErrorNumMap.get(id)">
                        <div>{{ t('jmsPlugin.uiConfig.securitySettings') }}</div>
                      </Badge>
                    </template>
                    <div class="space-y-3">
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5">
                          <span>{{ t('jmsPlugin.uiConfig.securityFunction') }}</span>
                          <Colon />
                        </div>
                        <RadioGroup v-model:value="dataMap[id].transmissionMode" :name="id">
                          <Radio class="w-26" value="none">{{ t('jmsPlugin.uiConfig.notUse') }}</Radio>
                          <Radio class="w-29" value="SSL">SSL</Radio>
                          <Radio value="StartTLS">StartTLS</Radio>
                        </RadioGroup>
                      </div>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5">
                          <span>{{ t('jmsPlugin.uiConfig.trustAllCertificates') }}</span>
                          <Colon />
                        </div>
                        <RadioGroup v-model:value="dataMap[id].transmissionMode" :name="id">
                          <Radio class="w-26" value="SSL">{{ t('jmsPlugin.uiConfig.trustAllCertificates') }}</Radio>
                          <Radio class="w-29" value="StartTLS">{{ t('jmsPlugin.uiConfig.useLocalTrustStore') }}</Radio>
                          <Radio value="StartTLS">{{ t('jmsPlugin.uiConfig.executeStartTLS') }}</Radio>
                        </RadioGroup>
                      </div>
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-33.5">
                          <span>{{ t('jmsPlugin.uiConfig.overrideSystemSSLTLS') }}</span>
                          <Colon />
                        </div>
                        <Input
                          v-model:value="dataMap[id].username"
                          :maxlength="200"
                          trimAll
                          :title="t('common.username')"
                          :placeholder="t('jmsPlugin.uiConfig.usernamePlaceholder')"
                          class="flex-1 max-w-175" />
                      </div>
                    </div>
                  </TabPane>
                  <TabPane key="other">
                    <template #tab>
                      <Badge
                        class="count-Badge-container"
                        size="small"
                        :count="otherSetErrorNumMap.get(id)">
                        <div>{{ t('jmsPlugin.uiConfig.otherSettings') }}</div>
                      </Badge>
                    </template>
                    <div class="space-y-3">
                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-24.5">
                          <span>{{ t('jmsPlugin.uiConfig.other') }}</span>
                          <Colon />
                        </div>
                        <div class="flex items-center">
                          <Checkbox>{{ t('jmsPlugin.uiConfig.getHeadersOnly') }}</Checkbox>
                          <Checkbox>{{ t('jmsPlugin.uiConfig.deleteMessagesFromServer') }}</Checkbox>
                          <Checkbox>{{ t('jmsPlugin.uiConfig.useMIMEStorage') }}</Checkbox>
                        </div>
                      </div>

                      <div class="flex items-center">
                        <div class="flex-shrink-0 w-24.5">
                          <span>{{ t('jmsPlugin.uiConfig.numberOfMessagesToRetrieve') }}</span>
                          <Colon />
                        </div>

                        <div class="flex items-center flex-1 max-w-175 space-x-2">
                          <RadioGroup
                            v-model:value="dataMap[id].transmissionMode"
                            class="flex-shrink-0"
                            :name="id">
                            <Radio value="SSL">{{ t('common.all') }}</Radio>
                            <Radio value="StartTLS">{{ t('jmsPlugin.uiConfig.otherOption') }}</Radio>
                          </RadioGroup>

                          <Input
                            v-model:value="dataMap[id].username"
                            :maxlength="15"
                            :min="1"
                            trimAll
                            dataType="number"
                            :title="t('jmsPlugin.uiConfig.messageCount')"
                            :placeholder="t('jmsPlugin.uiConfig.messageCountPlaceholder')"
                            class="max-w-35" />
                        </div>
                      </div>
                    </div>
                  </TabPane>
                </Tabs>
              </CollapsePanel>
            </Collapse>
          </li>
        </template>
      </Draggable>

      <NoData v-else style="height: calc(100% - 40px);" />
    </template>
  </div>
</template>

<style scoped>
em {
  font-style: normal
}

.ant-collapse {
  line-height: 20px;
}

.ant-collapse-borderless> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
  line-height: 20px;
}

.ant-collapse :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 4px 14px 20px;
  font-size: 12px;
}

:deep(.ant-radio):not(.ant-radio-checked) .ant-radio-inner,
.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled),
.ant-select:not(.ant-select-disabled) :deep(.ant-select-selector),
:deep(.ant-checkbox-inner) {
  background-color: #fff;
}

.ant-tabs {
  line-height: 20px;
}

.count-Badge-container :deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}
</style>
