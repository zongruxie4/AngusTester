<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { duration } from '@xcan-angus/tools';
import { AsyncComponent, Icon, IconRefresh, Input, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { modules } from '@/api/tester';

import { travelTreeData } from './utils';

type TagItem = {
  id: string;
  name: string;
  showName?: string;
  showTitle?: string;
}

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  disabled: boolean;
  projectName: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  disabled: false
});

const CreateModal = defineAsyncComponent(() => import('./add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('./move.vue'));

// const MAX_LENGTH = 20;

const pageNo = ref(1);
// const pageSize = ref(200);
const inputValue = ref<string>();
const nameInputRef = ref();

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);

const dataList = ref<TagItem[]>([]);
const total = ref(0);

const editId = ref<string>();
const pid = ref<string>();
const modalVisible = ref(false);
const moveVsible = ref(false);

const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  inputValue.value = event.target.value;
  pageNo.value = 1;
  loadData();
});

const toCreate = () => {
  modalVisible.value = true;
};

const createOk = () => {
  // dataList.value.unshift(data);
  // total.value += 1;
  loadData();
};

const refresh = () => {
  pageNo.value = 1;
  loadData();
};

const getParams = () => {
  const params: {
    // pageNo: number;
    // pageSize: number;
    projectId: string;
    filters?: { key: 'name'; op: 'MATCH_END'; value: string }[],
  } = {
    // pageNo: pageNo.value,
    // pageSize: pageSize.value,
    projectId: props.projectId
  };

  if (inputValue.value) {
    params.filters = [{ key: 'name', op: 'MATCH_END', value: inputValue.value }];
  }

  return params;
};

// const format = (data: { id: string; name: string }) => {
//   let showTitle = '';
//   let showName = data.name;
//   if (data.name?.length > MAX_LENGTH) {
//     showTitle = data.name;
//     showName = showName.slice(0, MAX_LENGTH) + '...';
//   }

//   return {
//     ...data,
//     showTitle,
//     showName
//   };
// };

const loadData = async (remainder = 0, _params?:{}) => {
  loading.value = true;
  pid.value = undefined;
  let params = getParams();
  if (_params) {
    params = { ...params, ..._params };
  }

  const [error, res] = await modules.getModuleTree(params);
  loading.value = false;
  loaded.value = true;

  if (params.filters?.length) {
    searchedFlag.value = true;
  } else {
    searchedFlag.value = false;
  }

  if (error) {
    return;
  }

  const data = res?.data;
  // if (!data) {
  //   return;
  // }

  const list = ((data || []) as {id:string;name:string}[]).slice(remainder);
  dataList.value = travelTreeData(list);
  // const pureList = list.map((item) => {
  //   return item;
  // });

  // if (params.pageNo > 1) {
  //   dataList.value.push(...pureList);
  // } else {
  //   dataList.value = pureList;
  // }

  // total.value = +data.total;
};

const toEdit = (data: TagItem) => {
  if (props.disabled) {
    return;
  }
  editId.value = data.id;
  // nextTick(() => {
  //   nameInputRef.value.focus();
  // });
};

const cancelEdit = () => {
  editId.value = undefined;
};

const pressEnter = async (id: string, event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  loading.value = true;
  const [error] = await modules.updateModule([{ id, name: value }]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('修改成功');
  editId.value = undefined;
  loadData();
};

const hadleblur = (id: string, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editId.value === id) {
      pressEnter(id, event);
    }
  }, 300);
};

// 删除弹框
const toDelete = (data: TagItem) => {
  modal.confirm({
    content: `确定删除模块【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      loading.value = true;
      const [error] = await modules.deleteModule(params);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('删除模块成功');
      // 删除列表中该条数据
      // dataList.value.splice(index, 1);
      // total.value -= 1;

      // 如果删除的不是所有数据的最后一条，则需要追加该下标的数据
      // if (index < total.value) {
      //   const params = {
      //     // pageNo: dataList.value.length + 1,
      //     // pageSize: 1
      //   };
      //   loadData(0, params);
      // }
      loadData();
    }
  });
};

const loadMore = () => {
  // 修复pageNo，因为添加、删除后前端没有刷新列表
  // const remainder = dataList.value.length % pageSize.value;
  // const current = Math.floor(dataList.value.length / pageSize.value);
  // pageNo.value = current + 1;
  // loadData(remainder);
};

const reset = () => {
  pageNo.value = 1;
  inputValue.value = undefined;

  loaded.value = false;
  loading.value = false;
  searchedFlag.value = false;

  dataList.value = [];
  total.value = 0;

  editId.value = undefined;
  modalVisible.value = false;
};

const moveUp = async (record) => {
  const { index, ids, pid, id } = record;
  let params = {};
  if (index === 0) {
    let targetParent;
    for (const linkId of ids) {
      if (linkId !== record.id) {
        if (targetParent) {
          targetParent = (targetParent.children || []).find(item => item.id === linkId);
        } else {
          targetParent = dataList.value.find(item => item.id === linkId);
        }
      }
    }
    params = {
      id,
      pid: targetParent.pid || '-1',
      sequence: (+targetParent.sequence) - 1
    };
  } else {
    let parentChildren;
    if (ids.length === 1) {
      parentChildren = dataList.value;
    } else {
      for (const linkId of ids) {
        if (linkId !== record.id) {
          if (parentChildren) {
            parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
          } else {
            parentChildren = dataList.value.find(item => item.id === linkId)?.children || [];
          }
        }
      }
    }
    params = {
      id,
      sequence: parentChildren[index - 1].sequence - 1
    };
  }

  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success('移动成功');
  loadData();
};

const moveDown = async (record) => {
  const { index, ids, id } = record;
  let parentChildren;
  if (ids.length === 1) {
    parentChildren = dataList.value;
  } else {
    for (const linkId of ids) {
      if (linkId !== record.id) {
        if (parentChildren) {
          parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
        } else {
          parentChildren = dataList.value.find(item => item.id === linkId)?.children || [];
        }
      }
    }
  }
  const params = {
    id,
    sequence: +parentChildren[index + 1].sequence + 1
  };
  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success('移动成功');
  loadData();
};

const activeModule = ref();
const moveLevel = (record) => {
  activeModule.value = record;
  moveVsible.value = true;
};

const onMenuClick = (menu, record) => {
  // debugger;
  if (menu.key === 'edit') {
    toEdit(record);
  } else if (menu.key === 'add') {
    pid.value = record.id;
    toCreate();
  } else if (menu.key === 'del') {
    toDelete(record);
  } else if (menu.key === 'up') {
    moveUp(record);
  } else if (menu.key === 'down') {
    moveDown(record);
  } else if (menu.key === 'move') {
    moveLevel(record);
  }
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

// const showLoadMore = computed(() => {
//   return dataList.value.length < total.value;
// });
</script>
<template>
  <div class="w-full h-full leading-5 text-3 overflow-auto">
    <div class="mb-6">
      <div class="text-3.5 font-medium mb-2.5">关于软件模块</div>
      <div>软件模块用于定义和组织软件系统中相对独立的功能集，通过模块可以划分任务和功能用例，从而提高可维护性。</div>
    </div>

    <div class="text-3.5 font-medium">已添加的软件模块</div>

    <Spin
      :spinning="loading"
      style="min-height: calc(100% - 108px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../../assets/images/nodata.png">
          <div v-if="!props.disabled" class="flex items-center text-theme-sub-content text-3 leading-7">
            <span>您尚未添加任何模块，立即</span>
            <Button
              type="link"
              size="small"
              class="text-3 py-0 px-1"
              @click="toCreate">
              添加模块
            </Button>
          </div>
          <div v-else class="text-theme-sub-content text-3 leading-7">您尚未添加任何模块。</div>
        </div>

        <template v-else>
          <div class="flex items-center mt-3.5">
            <div class="flex items-center">
              <Input
                v-model:value="inputValue"
                placeholder="请输入模块名称"
                class="w-75 mr-2"
                trimAll
                :allowClear="true"
                :maxlength="50"
                @change="inputChange">
                <template #suffix>
                  <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
                </template>
              </Input>

              <!-- <Hints v-if="!props.disabled" text="回车或鼠标失焦时保存添加模块，双击已存在模块可进入编辑状态" class="flex-1" /> -->
            </div>

            <div class="flex items-center space-x-3">
              <!-- <ButtonAuth
                code="CasesTagAdd"
                type="primary"
                icon="icon-jia"
                @click="toCreate" /> -->
              <Button
                v-if="!props.disabled"
                type="primary"
                size="small"
                class="flex space-x-1"
                @click="toCreate">
                <Icon icon="icon-jia" />
                添加模块
              </Button>

              <IconRefresh @click="refresh">
                <template #default>
                  <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                    <Icon icon="icon-shuaxin" />
                    <span class="ml-1">刷新</span>
                  </div>
                </template>
              </IconRefresh>
            </div>
          </div>

          <div class="w-full border-b border-solid border-theme-text-box my-3.5"></div>

          <NoData v-if="dataList.length === 0" class="flex-1" />

          <div v-else class="w-80">
            <Tree
              :treeData="dataList"
              blockNode
              defaultExpandAll
              :fieldNames="{
                children: 'children',
                title: 'name',
                key: 'id'
              }">
              <template #title="{key, title, name, id, index, level, isLast, pid, ids, sequence, childLevels, hasEditPermission}">
                <div v-if="editId === id" class="flex items-center">
                  <Input
                    ref="nameInputRef"
                    placeholder="请输入模块名称"
                    class="flex-1 mr-2 bg-white"
                    trim
                    :value="name"
                    :allowClear="false"
                    :maxlength="50"
                    @blur="hadleblur(id, $event)"
                    @pressEnter="pressEnter(id, $event)" />
                  <Button
                    type="link"
                    size="small"
                    class="px-0 py-0 mr-1"
                    @click="cancelEdit">
                    取消
                  </Button>
                </div>
                <div v-else class="flex items-center space-x-2 tree-title">
                  <Icon icon="icon-mokuai" />
                  <span class="flex-1 truncate min-w-0" :title="name">{{ name }}</span>
                  <Dropdown :trigger="['click']">
                    <Button
                      v-if="!props.disabled && hasEditPermission"
                      class="hidden gengduo"
                      type="text"
                      size="small">
                      <Icon icon="icon-more" class="text-3.5" />
                    </Button>
                    <template #overlay>
                      <Menu class="w-50" @click="onMenuClick($event, {name, id, index, ids, level, childLevels, pid})">
                        <MenuItem v-if="level < 4" key="add">
                          <Icon icon="icon-jia" />
                          新建子模块
                        </MenuItem>
                        <MenuItem v-if="index > 0 || +pid > 0" key="up">
                          <Icon icon="icon-shangyi" />
                          {{ index < 1 ? '移到上一层' : '上移' }}
                        </MenuItem>
                        <MenuItem v-if="!isLast" key="down">
                          <Icon icon="icon-xiayi" />
                          下移
                        </MenuItem>
                        <MenuItem key="move">
                          <Icon icon="icon-yidong" />
                          移动
                        </MenuItem>
                        <MenuItem key="edit">
                          <Icon icon="icon-bianji" />
                          编辑
                        </MenuItem>
                        <MenuItem key="del">
                          <Icon icon="icon-qingchu" />
                          删除
                        </MenuItem>
                      </Menu>
                    </template>
                  </Dropdown>
                </div>
              </template>
            </Tree>
          </div>

          <!-- <div v-else class="flex items-center flex-wrap">
            <div
              v-for="(item, index) in dataList"
              :key="item.id"
              class="mr-3.5 mb-3.5">
              <div v-if="editId === item.id" class="flex items-center">
                <Input
                  placeholder="请输入模块名称"
                  class="w-50 mr-2"
                  trim
                  :value="item.name"
                  :allowClear="false"
                  :maxlength="50"
                  @blur="hadleblur(item.id, index, $event)"
                  @pressEnter="pressEnter(item.id, index, $event)" />
                <Button
                  type="link"
                  size="small"
                  class="px-0 py-0"
                  @click="cancelEdit">
                  取消
                </Button>
              </div>

              <div
                v-else
                class="flex items-center border border-solid border-theme-text-box rounded px-2 py-0.75 cursor-pointer"
                @dblclick="toEdit(item)">
                <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                <span :title="item.name" class="truncate mr-2">{{ item.name }}</span>
                <Icon
                  v-if="!props.disabled"
                  class="text-theme-placeholder text-theme-text-hover"
                  icon="icon-shanchuguanbi"
                  @click="toDelete(item,index)" />
              </div>
            </div>

            <Button
              v-if="showLoadMore"
              size="small"
              type="link"
              class="px-0 py-0 h-5 leading-5 mb-3"
              @click="loadMore">
              加载更多
            </Button>
          </div> -->
        </template>
      </template>
    </Spin>

    <AsyncComponent :visible="modalVisible">
      <CreateModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        :pid="pid"
        @ok="createOk" />
    </AsyncComponent>
    <AsyncComponent :visible="moveVsible">
      <MoveModuleModal
        v-model:visible="moveVsible"
        :projectId="props.projectId"
        :projectName="props.projectName"
        :module="activeModule"
        @ok="createOk" />
    </AsyncComponent>
  </div>
</template>
<style scoped>

.tree-title:hover .gengduo {
  display: inline-block;
}

:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 36px;
  padding-left: 0;
  line-height: 20px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 34px;
  margin-top: 2px;
  line-height: 34px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 36px;
  margin: 0;
  padding: 0;
  line-height: 36px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 36px;
  line-height: 36px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 18px;
}

:deep(.ant-tree-node-content-wrapper.ant-tree-node-content-wrapper-normal) {
  @apply !flex-1 min-w-0;
}

</style>
