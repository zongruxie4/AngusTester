<script setup lang="ts">
import { defineAsyncComponent, ref, onMounted, watch } from 'vue';
import { Icon, NoData } from '@xcan-angus/vue-ui';
import Draggable from 'vuedraggable';

import { utils } from '@xcan-angus/infra';
import { TargetKey, PipelineConfig } from '../PropsType';

type DragDataConfig = {
  id: string;
  target: TargetKey;
  children?: {
    id: string;
    target: TargetKey;
  }[];
}

export interface Props {
  value: PipelineConfig[];
  loaded: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => [],
  loaded: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'delete', delList: PipelineConfig[], data: DragDataConfig[]): void;
  (e: 'errorNumChange', value: number): void;
  (e: 'renderChange', value: boolean): void;
}>();

const HTTPConfigs = defineAsyncComponent(() => import('../HTTPConfigs/index.vue'));
const WaitingTime = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/WaitingTime/index.vue'));
const Rendezvous = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/Rendezvous/index.vue'));
const TransStart = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/TransStart/index.vue'));
const TransEnd = defineAsyncComponent(() => import('@/plugins/test/components/UIConfigComp/TransEnd/index.vue'));
const Throughput = defineAsyncComponent(() => import('../Throughput/index.vue'));

const rendered = ref(false);

const refsMap = ref<{ [key: string]: any }>({});
const dataList = ref<DragDataConfig[]>([]);
const dataMap = ref<{ [key: string]: PipelineConfig }>({});
const errorNumMap = ref<{ [key: string]: number }>({});
const repeatNames = ref<string[]>([]);

const renderedNum = ref(0);
const totalPipelines = ref(0);

const dragAdd = (event: { oldIndex: number; item: { id: string; type: TargetKey } }, targetId: string, targetList: { id: string; target: TargetKey; }[]) => {
  const { id, type: target } = event.item;
  // 禁止事务嵌套
  if (target !== 'TRANS_START') {
    dragUpdate(targetId, targetList);
    return;
  }

  // 恢复被移动的数据
  const sourceData = targetList.find(item => item.id === id);
  dataList.value.splice(event.oldIndex, 0, sourceData);
  // 删除被添加到targetList的数据
  const index = targetList.findIndex(item => item.id === id);
  targetList.splice(index, 1);
};

const dragUpdate = (id: string, list: { id: string; target: TargetKey; }[]) => {
  const len = list.length;
  const index = list.findIndex(item => item.target === 'TRANS_END');
  // 没有结束事务或者结束事务在最后位置，不涉及到数据改变
  if (index === -1 || index === (len - 1)) {
    return;
  }

  // 结束事务在中间，自动把结束事务后的数据删除，并自动添加到外部数据集
  const sliceData = list.splice(index + 1);
  // 找到父级的下标
  const pindex = dataList.value.findIndex(item => item.id === id);
  // 添加到父级之后的位置
  dataList.value.splice(pindex + 1, 0, ...sliceData);
};

const actionClick = (value: 'delete' | 'clone', id: string) => {
  if (value === 'clone') {
    toClone(id);
    return;
  }

  if (value === 'delete') {
    toDelete(id);
  }
};

const toClone = (targetId: string) => {
  const list = dataList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id, target, children } = list[i];
    if (id === targetId) {
      const pid = utils.uuid();
      const sourceData: PipelineConfig = refsMap.value[targetId].getData();
      dataMap.value[pid] = sourceData;
      dataMap.value[pid].name = sourceData.name ? (sourceData.name + ' copy') : '';
      const newData: DragDataConfig = { id: pid, target };
      // 克隆事务，需要把子集也一起克隆
      if (children?.length) {
        newData.children = [];
        for (let j = 0, _len = children.length; j < _len; j++) {
          const cid = utils.uuid();
          const _sourceData: PipelineConfig = refsMap.value[children[j].id].getData();
          dataMap.value[cid] = _sourceData;
          dataMap.value[cid].name = _sourceData.name ? (_sourceData.name + ' copy') : '';
          newData.children.push({ id: cid, target: children[j].target });
        }
      }
      dataList.value.splice(i + 1, 0, newData);
      return;
    }

    if (children?.length) {
      for (let j = 0, _len = children.length; j < _len; j++) {
        const data = children[j];
        if (data.id === targetId) {
          const newId = utils.uuid();
          const sourceData: PipelineConfig = refsMap.value[targetId].getData();
          dataMap.value[newId] = sourceData;
          dataMap.value[newId].name = sourceData.name ? (sourceData.name + ' copy') : '';
          children.splice(j + 1, 0, { ...data, id: newId });
          return;
        }
      }
    }
  }
};

const toDelete = (targetId: string) => {
  const list = dataList.value;
  const delList: PipelineConfig[] = [];
  for (let i = 0, len = list.length; i < len; i++) {
    const { id, children } = list[i];
    if (id === targetId) {
      if (children?.length) {
        for (let j = 0, _len = children.length; j < _len; j++) {
          const cid = children[j].id;
          if (dataMap.value[cid].target !== 'TRANS_END') {
            const name = refsMap.value[cid]?.getName();
            repeatNames.value = repeatNames.value.filter(item => item !== name);
          }
          delList.push(dataMap.value[cid]);
          delete dataMap.value[cid];
          delete refsMap.value[cid];
        }
      }

      const name = refsMap.value[id]?.getName();
      repeatNames.value = repeatNames.value.filter(item => item !== name);
      delList.push(dataMap.value[id]);
      delete dataMap.value[id];
      delete refsMap.value[id];
      dataList.value.splice(i, 1);
      break;
    }

    if (children?.length) {
      for (let j = 0, _len = children.length; j < _len; j++) {
        const cid = children[j].id;
        if (cid === targetId) {
          const name = refsMap.value[cid]?.getName();
          repeatNames.value = repeatNames.value.filter(item => item !== name);
          delList.push(dataMap.value[cid]);
          delete dataMap.value[cid];
          delete refsMap.value[cid];
          children.splice(j, 1);
          break;
        }
      }
    }
  }

  emit('delete', delList, dataList.value);

  setTimeout(() => {
    for (const key in refsMap.value) {
      if (!refsMap.value[key]) {
        delete refsMap.value[key];
      }
    }
  }, 16.66);
};

const nameChange = () => {
  const uniqueNames = new Set();
  repeatNames.value = [];
  const refs = Object.values(refsMap.value);
  for (let i = 0, len = refs.length; i < len; i++) {
    if (typeof refs[i]?.getName === 'function') {
      const name = refs[i].getName();
      if (name) {
        if (uniqueNames.has(name)) {
          repeatNames.value.push(name);
        } else {
          uniqueNames.add(name);
        }
      }
    }
  }
};

const enabledChange = (enabled: boolean, targetId: string) => {
  const list = dataList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id } = list[i];
    if (id === targetId) {
      dataMap.value[id].enabled = enabled;
    } else if (enabled) {
      dataMap.value[id].enabled = false;
    }
  }
};

const change = (data: PipelineConfig, id: string) => {
  dataMap.value[id] = data;
};

const reset = () => {
  refsMap.value = {};
  dataList.value = [];
  dataMap.value = {};
  errorNumMap.value = {};
  repeatNames.value = [];
  renderedNum.value = 0;
  totalPipelines.value = 0;
};

const renderChange = () => {
  renderedNum.value += 1;
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }

    totalPipelines.value = newValue.length;
    const list = JSON.parse(JSON.stringify(newValue));
    for (let i = 0, len = list.length; i < len; i++) {
      const data = list[i];
      const { id, target } = data;
      const temp: DragDataConfig = { id, target };
      if (target === 'TRANS_START') {
        temp.children = [];
        dataList.value.push(temp);
      } else if (dataList.value[dataList.value.length - 1]?.target === 'TRANS_START') {
        const _children = dataList.value[dataList.value.length - 1].children;
        if (_children.length === 0) {
          _children.push(temp);
        } else {
          if (_children[_children.length - 1].target === 'TRANS_END') {
            dataList.value.push(temp);
          } else {
            _children.push(temp);
          }
        }
      } else {
        dataList.value.push(temp);
      }

      // 没有设置enabled字段，默认值为true
      if (data.enabled !== false) {
        data.enabled = true;
      }

      if (data.target === 'HTTP') {
        if (data.request) {
          let url = data.request.url;
          if (url) {
            // 以第一个 / 来拆分
            const symbol = `[${utils.uuid()}]`;
            url = url.replace(/([^/]+)\/([^/]+)/, '$1' + symbol + '/$2');
            const [serverUrl = null, endpoint = null] = url.split(symbol);
            data.request.server = {
              url: serverUrl,
              description: null,
              variables: null
            };

            // 解析server url中的变量
            // eslint-disable-next-line no-useless-escape
            const matches = serverUrl.match(/\{[^\{\}]+\}/g);
            if (matches?.length) {
              data.request.server.variables = {};
              for (let i = 0, len = matches.length; i < len; i++) {
                // eslint-disable-next-line no-useless-escape
                const key = matches[i].replace(/[\{\}]/g, '');
                data.request.server.variables[key] = {
                  allowableValues: null,
                  defaultValue: null,
                  description: null
                };
              }
            }

            data.request.endpoint = endpoint;
          }

          if (!data.request.server) {
            data.request.server = {
              url: null,
              description: null,
              variables: null
            };
          } else {
            if (!data.request.server?.variables) {
              // 解析server url中的变量
            // eslint-disable-next-line no-useless-escape
              const matches = data.request.server.url?.match(/\{[^\{\}]+\}/g);
              if (matches?.length) {
                data.request.server.variables = {};
                for (let i = 0, len = matches.length; i < len; i++) {
                // eslint-disable-next-line no-useless-escape
                  const key = matches[i].replace(/[\{\}]/g, '');
                  data.request.server.variables[key] = {
                    allowableValues: null,
                    defaultValue: null,
                    description: null
                  };
                }
              }
            }
          }

          delete data.request.url;
        }
      }

      dataMap.value[id] = data;
    }
  }, { immediate: true });

  watch(() => renderedNum.value, (newValue) => {
    if (newValue === totalPipelines.value) {
      emit('renderChange', true);
      rendered.value = true;
    }
  }, { immediate: true });
});

const add = (data: PipelineConfig) => {
  const listItem: { id: string; target: TargetKey; children?: { id: string; target: TargetKey }[] } = { id: data.id, target: data.target };
  dataMap.value[data.id] = data;
  // 插入的是开始事务或者列表为空，直接追加到编排列表中
  if (listItem.target === 'TRANS_START' || !dataList.value.length) {
    if (listItem.target === 'TRANS_START') {
      listItem.children = [];
    }
    dataList.value.push(listItem);
    return;
  }

  const target = data.target;
  // 插入结束事务，倒序插入到最后一个没有结束的事务
  if (target === 'TRANS_END') {
    const list = dataList.value;
    for (let i = list.length; i--;) {
      const item = list[i];
      if (item.target === 'TRANS_START') {
        if (!item.children?.length) {
          item.children.push(data);
          return;
        } else if (item.children[item.children.length - 1].target !== 'TRANS_END') {
          item.children.push(data);
          return;
        }
      }
    }

    return;
  }

  const prevItem = dataList.value[dataList.value.length - 1];
  const prevTarget = prevItem.target;
  // 如果前一个是开始事务及这个事务没有结束，则把这个编排元素插入到事务的子集中
  if (prevTarget === 'TRANS_START') {
    const children = prevItem.children || [];
    // 子集为空或者子集中最后一个元素不是结束事务，直接追加到子集列表中
    if (!children?.length || children[children.length - 1].target !== 'TRANS_END') {
      children.push(listItem);
      return;
    }
  }

  // 前一个不是开始事务或者该事务已经结束，追加到编排列表中，直接追加到编排列表中
  dataList.value.push(listItem);
};

const getData = (): PipelineConfig[] => {
  const result: PipelineConfig[] = [];
  const list = dataList.value;
  let beforeName = '';
  const _refsMap = refsMap.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id, children } = list[i];
    let transactionName: string = null;
    if (typeof _refsMap[id]?.getData === 'function') {
      const tempData = _refsMap[id].getData();
      if (tempData) {
        result.push({ ...tempData, beforeName });
        beforeName = tempData.name;
        transactionName = tempData.name;
      }
    }

    if (children?.length) {
      for (let j = 0, _len = children.length; j < _len; j++) {
        const _id = children[j].id;
        if (typeof _refsMap[_id]?.getData === 'function') {
          const _tempData = _refsMap[_id].getData();
          if (_tempData) {
            result.push({ ..._tempData, beforeName, transactionName });
            beforeName = _tempData.name;
          }
        }
      }
    }
  }

  return JSON.parse(JSON.stringify(result));
};

const isValid = (): boolean => {
  const uniqueNames = new Set();
  repeatNames.value = [];
  const refs = Object.values(refsMap.value);
  for (let i = 0, len = refs.length; i < len; i++) {
    if (typeof refs[i]?.getName === 'function') {
      const name = refs[i].getName();
      if (name) {
        if (uniqueNames.has(name)) {
          repeatNames.value.push(name);
        } else {
          uniqueNames.add(name);
        }
      }
    }
  }

  let errorNum = 0;
  const list = dataList.value;
  for (let i = 0, len = list.length; i < len; i++) {
    const { id, children } = list[i];
    if (typeof refsMap.value[id]?.isValid === 'function') {
      if (!refsMap.value[id].isValid()) {
        errorNum++;
      }
    }

    if (typeof refsMap.value[id]?.validateRepeatName === 'function') {
      if (!refsMap.value[id].validateRepeatName(repeatNames.value)) {
        errorNum++;
      }
    }

    if (children?.length) {
      for (let j = 0, _len = children.length; j < _len; j++) {
        const _id = children[j].id;
        if (typeof refsMap.value[_id]?.isValid === 'function') {
          if (!refsMap.value[_id].isValid()) {
            errorNum++;
          }
        }

        if (typeof refsMap.value[_id]?.validateRepeatName === 'function') {
          if (!refsMap.value[_id].validateRepeatName(repeatNames.value)) {
            errorNum++;
          }
        }
      }
    }
  }

  return !errorNum;
};

defineExpose({
  add,
  getData,
  isValid
});
</script>

<template>
  <template v-if="props.loaded && !dataList.length">
    <NoData />
  </template>
  <template v-else>
    <Draggable
      :list="dataList"
      :animation="300"
      group="scenario"
      itemKey="id"
      tag="ul"
      handle=".drag-handle">
      <template #item="{ element: { id, target, children } }: { element: DragDataConfig }">
        <li
          :id="id"
          :key="id"
          :type="target"
          class="drag-item relative">
          <Icon
            :class="{ invisible: !rendered }"
            icon="icon-yidong"
            class="drag-handle absolute top-3.75 left-3 z-10 text-4 cursor-move text-theme-sub-content" />
          <template v-if="target === 'HTTP'">
            <HTTPConfigs
              :ref="el => refsMap[id] = el"
              :value="dataMap[id]"
              :repeatNames="repeatNames"
              @click="actionClick($event, id)"
              @nameChange="nameChange"
              @enabledChange="enabledChange($event, id)"
              @change="change($event, id)"
              @renderChange="renderChange" />
          </template>
          <template v-else-if="target === 'WAITING_TIME'">
            <WaitingTime
              :ref="el => refsMap[id] = el"
              :value="dataMap[id]"
              :repeatNames="repeatNames"
              @click="actionClick($event, id)"
              @nameChange="nameChange"
              @enabledChange="enabledChange($event, id)"
              @change="change($event, id)"
              @renderChange="renderChange" />
          </template>
          <template v-else-if="target === 'RENDEZVOUS'">
            <Rendezvous
              :ref="el => refsMap[id] = el"
              :value="dataMap[id]"
              :repeatNames="repeatNames"
              @click="actionClick($event, id)"
              @nameChange="nameChange"
              @enabledChange="enabledChange($event, id)"
              @change="change($event, id)"
              @renderChange="renderChange" />
          </template>
          <template v-else-if="target === 'THROUGHPUT'">
            <Throughput
              :ref="el => refsMap[id] = el"
              :value="dataMap[id]"
              :repeatNames="repeatNames"
              @click="actionClick($event, id)"
              @nameChange="nameChange"
              @enabledChange="enabledChange($event, id)"
              @change="change($event, id)"
              @renderChange="renderChange" />
          </template>
          <template v-else-if="target === 'TRANS_START'">
            <TransStart
              :ref="el => refsMap[id] = el"
              :value="dataMap[id]"
              :repeatNames="repeatNames"
              @click="actionClick($event, id)"
              @nameChange="nameChange"
              @enabledChange="enabledChange($event, id)"
              @change="change($event, id)"
              @renderChange="renderChange">
              <template #default>
                <Draggable
                  :list="children"
                  :animation="300"
                  group="scenario"
                  itemKey="id"
                  tag="ul"
                  class="child-drag-container space-y-3 pt-3"
                  handle=".drag-handle"
                  @add="dragAdd($event, id, children)"
                  @update="dragUpdate(id, children)">
                  <template #item="{ element: { id: _cid, target: _target } }">
                    <li
                      :id="_cid"
                      :key="_cid"
                      :pid="id"
                      :type="_target"
                      class="drag-item relative">
                      <Icon
                        v-if="_target !== 'TRANS_END'"
                        icon="icon-yidong"
                        class="drag-handle absolute top-3.75 left-8 z-10 text-4 cursor-move text-theme-sub-content" />
                      <template v-if="_target === 'HTTP'">
                        <HTTPConfigs
                          :ref="el => refsMap[_cid] = el"
                          :value="dataMap[_cid]"
                          :repeatNames="repeatNames"
                          :enabled="dataMap[id].enabled"
                          class="mx-5"
                          @click="actionClick($event, _cid)"
                          @nameChange="nameChange"
                          @enabledChange="enabledChange($event, _cid)"
                          @change="change($event, _cid)"
                          @renderChange="renderChange" />
                      </template>
                      <template v-else-if="_target === 'WAITING_TIME'">
                        <WaitingTime
                          :ref="el => refsMap[_cid] = el"
                          :value="dataMap[_cid]"
                          :repeatNames="repeatNames"
                          :enabled="dataMap[id].enabled"
                          class="mx-5"
                          @click="actionClick($event, _cid)"
                          @nameChange="nameChange"
                          @enabledChange="enabledChange($event, _cid)"
                          @change="change($event, _cid)"
                          @renderChange="renderChange" />
                      </template>
                      <template v-else-if="_target === 'RENDEZVOUS'">
                        <Rendezvous
                          :ref="el => refsMap[_cid] = el"
                          :value="dataMap[_cid]"
                          :repeatNames="repeatNames"
                          :enabled="dataMap[id].enabled"
                          class="mx-5"
                          @click="actionClick($event, _cid)"
                          @nameChange="nameChange"
                          @enabledChange="enabledChange($event, _cid)"
                          @change="change($event, _cid)"
                          @renderChange="renderChange" />
                      </template>
                      <template v-else-if="_target === 'THROUGHPUT'">
                        <Throughput
                          :ref="el => refsMap[_cid] = el"
                          :value="dataMap[_cid]"
                          :repeatNames="repeatNames"
                          :enabled="dataMap[id].enabled"
                          class="mx-5"
                          @click="actionClick($event, _cid)"
                          @nameChange="nameChange"
                          @enabledChange="enabledChange($event, _cid)"
                          @change="change($event, _cid)"
                          @renderChange="renderChange" />
                      </template>
                      <template v-else-if="_target === 'TRANS_END'">
                        <TransEnd
                          :ref="el => refsMap[_cid] = el"
                          :description="dataMap[_cid].description"
                          :name="dataMap[id].name"
                          @renderChange="renderChange" />
                      </template>
                    </li>
                  </template>
                </Draggable>
              </template>
            </TransStart>
          </template>
        </li>
      </template>
    </Draggable>
  </template>
</template>

<style scoped>
.child-drag-container .indent-3 {
  left: 12px;
}
</style>
