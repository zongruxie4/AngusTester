<script setup lang="ts">
import { nextTick, ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Collapse, CollapsePanel, RadioGroup, Radio, Switch } from 'ant-design-vue';
import Draggable from 'vuedraggable';
import { debounce } from 'throttle-debounce';
import { Icon, NoData, Input, Colon, Arrow, IconRequired, notification, Tooltip } from '@xcan-angus/vue-ui';
import { utils, duration } from '@xcan-angus/infra';

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

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const argumentsMap = ref<{[key:string]:{ id: string; name: string; value: string; }[]}>({});

const emptyNameSet = ref<Set<string>>(new Set());
const repeatNameIdSet = ref<Set<string>>(new Set());
const serverErrorSet = ref<Set<string>>(new Set());
const portErrorSet = ref<Set<string>>(new Set());

const argumentRepeatNameIdSet = ref<Set<string>>(new Set());

const insertData = () => {
  const hasEnabled = Object.values(dataMap.value).find(item => item.enabled === true);
  const id = utils.uuid();
  dataMap.value[id] = {
    id,
    target: 'LDAP',
    name: '',
    description: '',
    enabled: !hasEnabled,
    beforeName: '',
    arguments: null,
    deleteEntry: '',
    entryDn: '',
    searchBase: '',
    searchFilter: '',
    server: {
      password: '',
      port: '',
      rootDn: '',
      server: '',
      username: ''
    },
    testType: 'ADD',
    userDefined: false
  };

  argumentsMap.value[id] = [{ id: utils.uuid(), name: '', value: '' }];
  idList.value.push(id);

  if (!hasEnabled) {
    activeKeys.value.push(id);
  }

  scrollToBottom();
};

const checkedRepeatName = (id?:string) => {
  if (id) {
    emptyNameSet.value.delete(id);
  }

  repeatNameIdSet.value.clear();
  const uniqueNames = new Set();
  const repeatNameSet = new Set();
  const map = dataMap.value;
  for (const key in map) {
    const name = map[key].name;
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNameSet.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }

  for (const key in map) {
    if (repeatNameSet.has(map[key].name)) {
      repeatNameIdSet.value.add(key);
    }
  }
};

const nameChange = debounce(duration.search, checkedRepeatName);

const openChange = (id: string, _open: boolean) => {
  if (_open) {
    activeKeys.value.push(id);
    return;
  }

  activeKeys.value = activeKeys.value.filter(item => item !== id);
};

// 只能启用一个
const enabledChange = (id: string, enabled: boolean) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    dataMap.value[ids[i]].enabled = false;
  }

  dataMap.value[id].enabled = enabled;
};

const toClone = (targetId: string) => {
  const ids = idList.value;
  const cloneId = utils.uuid();
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const cloneData: PipelineConfig = JSON.parse(JSON.stringify(dataMap.value[targetId]));
      dataMap.value[cloneId] = cloneData;
      dataMap.value[cloneId].name = cloneData.name ? (cloneData.name + ' copy') : '';
      dataMap.value[cloneId].enabled = false;
      idList.value.splice(i + 1, 0, cloneId);

      if (argumentsMap.value[targetId]) {
        argumentsMap.value[cloneId] = JSON.parse(JSON.stringify(argumentsMap.value[targetId]));
      }

      break;
    }
  }

  // 判断克隆的名称是否已经存在
  if (dataMap.value[cloneId].name) {
    checkedRepeatName();
  }
};

const toDelete = (targetId: string) => {
  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (id === targetId) {
      const argumentIds = argumentsMap.value[id]?.map(item => item.id) || [];
      activeKeys.value = activeKeys.value.filter(item => item !== id);

      idList.value.splice(i, 1);
      delete dataMap.value[id];
      delete argumentsMap.value[id];

      emptyNameSet.value.delete(id);
      repeatNameIdSet.value.delete(id);
      serverErrorSet.value.delete(id);
      portErrorSet.value.delete(id);

      // 删除重复的ids
      for (let i = 0, len = argumentIds.length; i < len; i++) {
        argumentRepeatNameIdSet.value.delete(argumentIds[i]);
      }

      break;
    }
  }

  checkedRepeatName(targetId);
};

const serverChange = (id: string) => {
  serverErrorSet.value.delete(id);
};

const portChange = (id: string) => {
  portErrorSet.value.delete(id);
};

const testTypeChange = (id: string) => {
  dataMap.value[id].entryDn = '';
  dataMap.value[id].deleteEntry = '';
  dataMap.value[id].searchBase = '';
  dataMap.value[id].searchFilter = '';

  const ids = argumentsMap.value[id]?.map(item => item.id) || [];
  argumentsMap.value[id] = [{ id: utils.uuid(), name: '', value: '' }];

  // 删除重复的ids
  for (let i = 0, len = ids.length; i < len; i++) {
    argumentRepeatNameIdSet.value.delete(ids[i]);
  }
};

const customTestChange = (id:string) => {
  const ids = argumentsMap.value[id]?.map(item => item.id) || [];
  argumentsMap.value[id] = [{ id: utils.uuid(), name: '', value: '' }];

  // 删除重复的ids
  for (let i = 0, len = ids.length; i < len; i++) {
    argumentRepeatNameIdSet.value.delete(ids[i]);
  }
};

const deleteArgumentRepeatNameIdSet = (id:string) => {
  const argumentIds = argumentsMap.value[id]?.map(item => item.id) || [];
  for (let i = 0, len = argumentIds.length; i < len; i++) {
    argumentRepeatNameIdSet.value.delete(argumentIds[i]);
  }
};

const checkedArgumentRepeatName = (id:string) => {
  deleteArgumentRepeatNameIdSet(id);

  const uniqueNames = new Set();
  const repeatNameSet = new Set();
  const list = argumentsMap.value[id] || [];
  for (let i = 0, len = list.length; i < len; i++) {
    const name = list[i].name;
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNameSet.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }

  for (let i = 0, len = list.length; i < len; i++) {
    if (repeatNameSet.has(list[i].name)) {
      argumentRepeatNameIdSet.value.add(list[i].id);
    }
  }
};

const argumentNameChange = debounce(duration.search, checkedArgumentRepeatName);

const cloneArgument = (id:string, index:number) => {
  const item = argumentsMap.value[id] || [];
  const { name, value } = item[index];
  const copyName = name ? (name.slice(0, 400) + '_copy') : '';
  const data = { name: copyName, value, id: utils.uuid() };
  argumentsMap.value[id]?.splice(index + 1, 0, data);

  const prevIds = argumentsMap.value[id]?.slice(0, index + 1)?.map(ele => ele.id) || [];
  const argumentIds = argumentsMap.value[id]?.map(item => item.id) || [];
  for (let i = 0, len = argumentIds.length; i < len; i++) {
    const _id = argumentIds[i];
    if (!prevIds.includes(_id)) {
      argumentRepeatNameIdSet.value.delete(_id);
    }
  }

  const uniqueNames = new Set();
  const repeatNameSet = new Set();
  const list = argumentsMap.value[id] || [];
  for (let i = 0, len = list.length; i < len; i++) {
    const name = list[i].name;
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNameSet.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }

  for (let i = index + 1, len = list.length; i < len; i++) {
    const { name: _name, id: _id } = list[i];
    if (repeatNameSet.has(_name)) {
      setTimeout(() => {
        nextTick(() => {
          argumentRepeatNameIdSet.value.add(_id);
        });
      }, 16.66);
    }
  }
};

const addArgument = (id:string, index:number) => {
  if (argumentsMap.value[id]?.length >= 50) {
    notification.info(t('ldapPlugin.uiConfig.maxAttributeParams'));
    return;
  }

  argumentsMap.value[id]?.splice(index + 1, 0, { id: utils.uuid(), name: '', value: '' });
};

const deleteArgument = (id:string, index:number) => {
  if (argumentsMap.value[id]?.length === 1) {
    argumentsMap.value[id][index].name = '';
    argumentsMap.value[id][index].value = '';
  } else {
    argumentsMap.value[id]?.splice(index, 1);
  }

  deleteArgumentRepeatNameIdSet(id);

  setTimeout(() => {
    nextTick(() => {
      checkedArgumentRepeatName(id);
    });
  }, 16.66);
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
  idList.value = [];
  dataMap.value = {};
  argumentsMap.value = {};

  emptyNameSet.value.clear();
  repeatNameIdSet.value.clear();
  serverErrorSet.value.clear();
  portErrorSet.value.clear();
  argumentRepeatNameIdSet.value.clear();
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();

    if (!newValue?.length) {
      return;
    }

    const list = JSON.parse(JSON.stringify(newValue)) as PipelineConfig[];
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id, arguments: _arguments, server, enabled } = data;
      dataMap.value[id] = data;

      if (!server) {
        dataMap.value[id].server = {
          password: '',
          port: '',
          rootDn: '',
          server: '',
          username: ''
        };
      }

      if (enabled === true) {
        activeKeys.value.push(id);
      }

      if (data.userDefined === true && ['ADD', 'MODIFY'].includes(data.testType)) {
        if (_arguments && Object.keys(_arguments).length) {
          argumentsMap.value[id] = Object.entries(_arguments).map(([name, value]) => {
            return {
              id: utils.uuid(),
              name,
              value
            };
          });
        } else {
          argumentsMap.value[id] = [{
            id: utils.uuid(),
            name: '',
            value: ''
          }];
        }
      }

      idList.value.push(id);
    }
  }, { immediate: true });

  emit('renderChange', true);
  rendered.value = true;
});

const nameErrorSet = computed(() => {
  return new Set<string>([...emptyNameSet.value, ...repeatNameIdSet.value]);
});

const isValid = (): boolean => {
  emptyNameSet.value.clear();
  serverErrorSet.value.clear();
  portErrorSet.value.clear();

  const ids = idList.value;
  const map = dataMap.value;
  const set = repeatNameIdSet.value;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, server, userDefined, testType } = map[id];
    if (!name) {
      emptyNameSet.value.add(id);
      errorNum++;
    } else {
      if (set.has(id)) {
        errorNum++;
      }
    }

    if (server) {
      const { server: _server, port } = server;
      if (!_server) {
        serverErrorSet.value.add(id);
        errorNum++;
      }

      if (!port) {
        portErrorSet.value.add(id);
        errorNum++;
      }
    } else {
      serverErrorSet.value.add(id);
      portErrorSet.value.add(id);
      errorNum++;
    }

    if (userDefined === true && ['ADD', 'MODIFY'].includes(testType) && argumentsMap.value[id]?.length) {
      const argumentList = argumentsMap.value[id] || [];
      for (let j = 0, _len = argumentList.length; j < _len; j++) {
        const { id: _id } = argumentList[j];
        if (argumentRepeatNameIdSet.value.has(_id)) {
          errorNum++;
        }
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
    const data = JSON.parse(JSON.stringify(map[id])) as PipelineConfig;
    if (data.userDefined === true && ['ADD', 'MODIFY'].includes(data.testType) && argumentsMap.value[id]?.length) {
      data.arguments = argumentsMap.value[id].reduce((prev, { name, value }) => {
        if (name) {
          prev[name] = value;
        }

        return prev;
      }, {} as {[key:string]:string});
    }

    result.push({ ...data, beforeName });
    beforeName = data.name;
  }

  return result;
};

defineExpose({
  isValid,
  getData
});
</script>

<template>
  <div class="py-5 h-full text-3 leading-5">
    <div class="flex items-center flex-nowrap whitespace-nowrap space-x-2 px-5">
      <Button
        type="default"
        size="small"
        @click="insertData">
        <div class="flex items-center">
          <Icon icon="icon-chajianpeizhi" class="mr-1" />
          <span>{{ t('ldapPlugin.uiConfig.insertLdapRequest') }}</span>
        </div>
      </Button>

      <div class="flex-1 flex items-center overflow-hidden" :title="t('ldapPlugin.uiConfig.supportMultipleLdap')">
        <Icon
          icon="icon-tishi1"
          class="flex-shrink-0 text-3.5 mr-0.5"
          style="color:#a6ceff;" />
        <span class="text-theme-sub-content truncate">{{ t('ldapPlugin.uiConfig.supportMultipleLdap') }}</span>
      </div>
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
                    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-chajianpeizhi" />
                    <div class="flex-1 flex items-center space-x-2 mr-5">
                      <Tooltip
                        :title="t('ldapPlugin.uiConfig.nameDuplicate')"
                        internal
                        placement="right"
                        destroyTooltipOnHide
                        :visible="!!repeatNameIdSet.has(id)">
                        <Input
                          v-model:value="dataMap[id].name"
                          :maxlength="400"
                          :error="!!nameErrorSet.has(id)"
                          :title="dataMap[id].name"
                          style="flex:1 1 40%;"
                          trim
                          :placeholder="t('common.placeholders.searchKeyword')"
                          @change="nameChange(id)" />
                      </Tooltip>
                      <Input
                        v-model:value="dataMap[id].description"
                        :maxlength="800"
                        :title="dataMap[id].description"
                        trim
                        style="flex:1 1 60%;"
                        :placeholder="t('ldapPlugin.uiConfig.descriptionPlaceholder')" />
                    </div>
                    <div class="flex items-center flex-shrink-0 space-x-3">
                      <Switch
                        :checked="dataMap[id].enabled"
                        size="small"
                        @change="enabledChange(id, $event)" />
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="克隆">
                        <Icon
                          icon="icon-fuzhi"
                          class="text-3.5"
                          @click="toClone(id)" />
                      </div>
                      <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="删除">
                        <Icon
                          icon="icon-qingchu"
                          class="text-3.5"
                          @click="toDelete(id)" />
                      </div>
                      <Arrow :open="activeKeys.includes(id)" @change="openChange(id, $event)" />
                    </div>
                  </div>
                </template>

                <div class="space-y-3.5">
                  <div class="flex items-center text-theme-title">
                    <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
                    <span>{{ t('ldapPlugin.uiConfig.serverConfig') }}</span>
                  </div>
                  <div class="space-y-3.5 ml-2.75">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5 flex items-center">
                        <IconRequired />
                        <span>{{ t('ldapPlugin.uiConfig.hostnameOrIP') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex items-center space-x-5 max-w-175">
                        <Input
                          v-model:value="dataMap[id].server.server"
                          :maxlength="4096"
                          :error="!!serverErrorSet.has(id)"
                          trimAll
                          style="flex: 1 1 75%;"
                          :placeholder="t('ldapPlugin.uiConfig.hostnameOrIPPlaceholder')"
                          @change="serverChange(id)" />

                        <div style="flex: 1 1 25%;" class="flex items-center">
                          <div class="flex-shrink-0 w-11.5 flex items-center">
                            <IconRequired />
                            <span>{{ t('common.port') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].server.port"
                            :maxlength="5"
                            :min="1"
                            :max="65535"
                            :error="!!portErrorSet.has(id)"
                            trimALl
                            dataType="integer"
                            style="min-width: 75px;max-width: 200px;"
                            :placeholder="t('ldapPlugin.uiConfig.portPlaceholder')"
                            @change="portChange(id)" />
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5 flex items-center">
                        <span>{{ t('common.username') }}</span>
                        <Colon />
                      </div>
                      <div class="flex-1 flex items-center space-x-5 max-w-175">
                        <Input
                          v-model:value="dataMap[id].server.username"
                          :maxlength="400"
                          trimAll
                          :placeholder="t('ldapPlugin.uiConfig.usernamePlaceholder')"
                          style="width:calc((100% - 58px)/2);" />
                        <div class="flex-1 flex items-center">
                          <div class="flex-shrink-0 w-9.5 flex items-center">
                            <span>{{ t('common.password') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].server.password"
                            :maxlength="4096"
                            trimAll
                            :placeholder="t('ldapPlugin.uiConfig.passwordPlaceholder')"
                            type="password" />
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5 flex items-center">
                        <span>{{ t('ldapPlugin.uiConfig.rootDn') }}</span>
                        <Colon />
                      </div>
                      <Input
                        v-model:value="dataMap[id].server.rootDn"
                        :maxlength="4096"
                        trimAll
                        :placeholder="t('ldapPlugin.uiConfig.rootDnPlaceholder')"
                        class="max-w-175" />
                    </div>
                  </div>
                </div>

                <div class="space-y-3.5 mt-5">
                  <div class="flex items-center text-theme-title">
                    <div class="w-1.25 h-3 rounded mr-1.5" style="background-color: #1e88e5;"></div>
                    <span>{{ t('ldapPlugin.uiConfig.testConfig') }}</span>
                  </div>
                  <div class="space-y-3 ml-2.75">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5">
                        <IconRequired />
                        <span>{{ t('ldapPlugin.uiConfig.testType') }}</span>
                        <Colon />
                      </div>
                      <RadioGroup
                        v-model:value="dataMap[id].testType"
                        :name="id + 'testType'"
                        @change="testTypeChange(id)">
                        <Radio value="ADD">{{ t('actions.add') }}</Radio>
                        <Radio value="DELETE">{{ t('actions.delete') }}</Radio>
                        <Radio value="SEARCH">{{ t('actions.search') }}</Radio>
                        <Radio value="MODIFY">{{ t('actions.modify') }}</Radio>
                      </RadioGroup>
                    </div>

                    <div class="flex items-center">
                      <div class="flex-shrink-0 w-20.5">
                        <span>{{ t('ldapPlugin.uiConfig.customTest') }}</span>
                        <Colon />
                      </div>
                      <Switch
                        v-model:checked="dataMap[id].userDefined"
                        size="small"
                        @change="customTestChange(id)" />
                    </div>

                    <template v-if="dataMap[id].userDefined === true">
                      <template v-if="dataMap[id].testType === 'ADD'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-20.5 flex items-center">
                            <span>{{ t('ldapPlugin.uiConfig.entryDn') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].entryDn"
                            :maxlength="4096"
                            trim
                            :placeholder="t('ldapPlugin.uiConfig.entryDnAddPlaceholder')"
                            class="max-w-175" />
                        </div>

                        <div class="flex items-start">
                          <div class="leading-7 flex-shrink-0 w-20.5">
                            <span>{{ t('ldapPlugin.uiConfig.attributeParams') }}</span>
                            <Colon />
                          </div>
                          <div class="flex-1 space-y-2 max-w-175">
                            <div
                              v-for="(item,index) in argumentsMap[id]"
                              :key="item.id"
                              class="flex items-center space-x-2.5">
                              <Tooltip
                                :title="t('ldapPlugin.uiConfig.nameDuplicate')"
                                internal
                                placement="right"
                                destroyTooltipOnHide
                                :visible="!!argumentRepeatNameIdSet.has(item.id)">
                                <Input
                                  v-model:value="item.name"
                                  :error="!!argumentRepeatNameIdSet.has(item.id)"
                                  :maxlength="400"
                                  trimAll
                                  style="flex: 1 1 50%;"
                                  :placeholder="t('ldapPlugin.uiConfig.paramNamePlaceholder')"
                                  @change="argumentNameChange(id)" />
                              </Tooltip>
                              <Input
                                v-model:value="item.value"
                                :maxlength="4096"
                                trim
                                style="flex: 1 1 50%;"
                                :placeholder="t('ldapPlugin.uiConfig.paramValuePlaceholder')" />
                              <div class="flex-shrink-0 flex items-center space-x-2">
                                <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="克隆">
                                  <Icon
                                    icon="icon-fuzhi"
                                    class="text-3.5"
                                    @click="cloneArgument(id, index)" />
                                </div>
                                <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="添加">
                                  <Icon
                                    icon="icon-jia"
                                    class="text-3.5"
                                    @click="addArgument(id, index)" />
                                </div>
                                <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="删除">
                                  <Icon
                                    icon="icon-jian"
                                    class="text-3.5"
                                    @click="deleteArgument(id, index)" />
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </template>

                      <template v-if="dataMap[id].testType === 'DELETE'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-20.5 flex items-center">
                            <span>{{ t('ldapPlugin.uiConfig.deleteEntry') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].deleteEntry"
                            :maxlength="4096"
                            trim
                            :placeholder="t('ldapPlugin.uiConfig.deleteEntryPlaceholder')"
                            class="max-w-175" />
                        </div>
                      </template>

                      <template v-if="dataMap[id].testType === 'SEARCH'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-20.5 flex items-center">
                            <span>{{ t('ldapPlugin.uiConfig.searchBase') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].searchBase"
                            :maxlength="4096"
                            trim
                            :placeholder="t('ldapPlugin.uiConfig.searchBasePlaceholder')"
                            class="max-w-175" />
                        </div>

                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-20.5 flex items-center">
                            <span>{{ t('ldapPlugin.uiConfig.searchFilter') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].searchFilter"
                            :maxlength="4096"
                            trim
                            :placeholder="t('ldapPlugin.uiConfig.searchFilterPlaceholder')"
                            class="max-w-175" />
                        </div>
                      </template>

                      <template v-if="dataMap[id].testType === 'MODIFY'">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 w-20.5 flex items-center">
                            <span>{{ t('ldapPlugin.uiConfig.entryDn') }}</span>
                            <Colon />
                          </div>
                          <Input
                            v-model:value="dataMap[id].entryDn"
                            :maxlength="4096"
                            trim
                            :placeholder="t('ldapPlugin.uiConfig.entryDnModifyPlaceholder')"
                            class="max-w-175" />
                        </div>

                        <div class="flex items-start">
                          <div class="leading-7 flex-shrink-0 w-20.5">
                            <span>{{ t('ldapPlugin.uiConfig.attributeParams') }}</span>
                            <Colon />
                          </div>
                          <div class="flex-1 space-y-2 max-w-175">
                            <div
                              v-for="(item,index) in argumentsMap[id]"
                              :key="item.id"
                              class="flex items-center space-x-2.5">
                              <Tooltip
                                :title="t('ldapPlugin.uiConfig.nameDuplicate')"
                                internal
                                placement="right"
                                destroyTooltipOnHide
                                :visible="!!argumentRepeatNameIdSet.has(item.id)">
                                <Input
                                  v-model:value="item.name"
                                  :error="!!argumentRepeatNameIdSet.has(item.id)"
                                  :maxlength="400"
                                  trimAll
                                  style="flex: 1 1 50%;"
                                  :placeholder="t('ldapPlugin.uiConfig.paramNamePlaceholder')"
                                  @change="argumentNameChange(id)" />
                              </Tooltip>
                              <Input
                                v-model:value="item.value"
                                :maxlength="4096"
                                trim
                                style="flex: 1 1 50%;"
                                :placeholder="t('ldapPlugin.uiConfig.paramValuePlaceholder')" />
                              <div class="flex-shrink-0 flex items-center space-x-2">
                                <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="克隆">
                                  <Icon
                                    icon="icon-fuzhi"
                                    class="text-3.5"
                                    @click="cloneArgument(id, index)" />
                                </div>
                                <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="添加">
                                  <Icon
                                    icon="icon-jia"
                                    class="text-3.5"
                                    @click="addArgument(id, index)" />
                                </div>
                                <div class="flex items-center cursor-pointer hover:text-text-link-hover" title="删除">
                                  <Icon
                                    icon="icon-jian"
                                    class="text-3.5"
                                    @click="deleteArgument(id, index)" />
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </template>
                    </template>

                    <div v-else class="flex items-center leading-4.5">
                      <div class="flex-shrink-0 w-20.5"></div>
                      <div class="flex-1 flex max-w-175 space-x-5 items-stretch">
                        <div class="flex-1 rounded border border-solid px-3.5 py-2 border-theme-text-box break-all">
                          <div class="flex items-center mb-2.5"><span>{{ t('ldapPlugin.uiConfig.defaultPersonEntryInfo') }}</span><Colon /></div>
                          <div class="space-y-1.5 text-theme-sub-content">
                            <div>objectClass：top,person,organizationalPerson,inetOrgPerson</div>
                            <div>givenname：User</div>
                            <div>sn：Test</div>
                            <div>cn：TestUser + {{ t('ldapPlugin.uiConfig.uniqueCounter') }}</div>
                            <div>uid：user</div>
                            <div>userpassword：Test</div>
                          </div>
                        </div>

                        <div class="flex-1 rounded border border-solid px-3.5 py-2 border-theme-text-box break-all">
                          <div class="flex items-center mb-2.5"><span>{{ t('ldapPlugin.uiConfig.defaultTestTypeLogic') }}</span><Colon /></div>
                          <div class="space-y-1.5 text-theme-sub-content">
                            <div>{{ t('actions.add') }}</div>
                            <div>{{ t('actions.modify') }}</div>
                            <div>{{ t('actions.search') }}</div>
                            <div>{{ t('actions.delete') }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
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
  padding: 14px 20px 20px;
  font-size: 12px;
}

:deep(.ant-radio):not(.ant-radio-checked) .ant-radio-inner,
.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled) {
  background-color: #fff;
}
</style>
