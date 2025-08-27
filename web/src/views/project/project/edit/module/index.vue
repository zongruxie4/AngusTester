<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { duration } from '@xcan-angus/infra';
import { AsyncComponent, Icon, IconRefresh, Input, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { modules } from '@/api/tester';

import { travelTreeData } from './utils';

const { t } = useI18n();

type TagItem = {
  id: string;
  name: string;
  key?: string;
  showName?: string;
  showTitle?: string;
  children?: TagItem[];
  level?: number;
  index?: number;
  ids?: string[];
  isLast?: boolean;
  pid?: string;
  sequence?: string;
  childLevels?: number;
  hasEditPermission?: boolean;
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

const inputChange = debounce(duration.search, (event: any) => {
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

const loadData = async (remainder = 0, _params?: Record<string, any>) => {
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
  notification.success(t('tips.modifySuccess'));
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
    content: t('project.projectEdit.module.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      loading.value = true;
      const [error] = await modules.deleteModule(params);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('project.projectEdit.module.deleteSuccess'));
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

const moveUp = async (record: any) => {
  const { index, ids, id } = record;
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
  notification.success(t('tips.moveSuccess'));
  loadData();
};

const activeModule = ref();
const moveLevel = (record) => {
  activeModule.value = record;
  moveVsible.value = true;
};

const onMenuClick = (menu, record) => {
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
  <div class="w-full h-full leading-5 text-xs overflow-auto">
    <!-- 模块说明区域 -->
    <div class="space-y-4">
      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
          <span class="text-xs font-semibold text-gray-600">{{ t('project.projectEdit.module.about') }}</span>
        </div>
        <div class="text-xs text-gray-700 ml-3">{{ t('project.projectEdit.module.aboutDescription') }}</div>
      </div>

      <div class="space-y-4">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-purple-500 to-purple-600 rounded-full"></div>
          <span class="text-xs font-semibold text-gray-600">{{ t('project.projectEdit.module.addedModules') }}</span>
        </div>

        <Spin
          :spinning="loading"
          class="flex flex-col min-h-96">
          <template v-if="loaded">
            <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center py-12">
              <img src="../../../../../assets/images/nodata.png" class="w-32 h-32 mb-4">
              <div v-if="!props.disabled" class="flex items-center text-gray-500 text-xs">
                <span>{{ t('project.projectEdit.module.noModules') }}</span>
                <Button
                  type="link"
                  size="small"
                  class="text-xs py-0 px-1 text-blue-600"
                  @click="toCreate">
                  {{ t('project.projectEdit.module.addModule') }}
                </Button>
              </div>
              <div v-else class="text-gray-500 text-xs">{{ t('project.projectEdit.module.noModulesDescription') }}</div>
            </div>

            <template v-else>
              <div class="flex items-center justify-between mt-4 mb-4">
                <div class="flex items-center">
                  <Input
                    v-model:value="inputValue"
                    :placeholder="t('project.projectEdit.module.moduleNamePlaceholder')"
                    class="w-64 mr-3"
                    trimAll
                    :allowClear="true"
                    :maxlength="50"
                    @change="inputChange">
                    <template #suffix>
                      <Icon class="text-xs cursor-pointer text-gray-400" icon="icon-sousuo" />
                    </template>
                  </Input>
                </div>

                <div class="flex items-center space-x-3">
                  <Button
                    v-if="!props.disabled"
                    type="primary"
                    size="small"
                    class="flex items-center space-x-1 text-xs"
                    @click="toCreate">
                    <Icon icon="icon-jia" class="text-xs" />
                    <span>{{ t('project.projectEdit.module.addModule') }}</span>
                  </Button>

                  <IconRefresh @click="refresh">
                    <template #default>
                      <div class="flex items-center cursor-pointer text-gray-600 space-x-1 hover:text-gray-800 text-xs">
                        <Icon icon="icon-shuaxin" class="text-xs" />
                        <span>{{ t('project.projectEdit.module.refresh') }}</span>
                      </div>
                    </template>
                  </IconRefresh>
                </div>
              </div>

              <NoData v-if="dataList.length === 0" class="flex-1 py-12" />

              <div v-else class="mt-2">
                <Tree
                  :treeData="dataList"
                  blockNode
                  defaultExpandAll
                  :fieldNames="{
                    children: 'children',
                    title: 'name',
                    key: 'id'
                  }">
                  <template #title="{name, id, index, level, isLast, pid, ids, childLevels, hasEditPermission}">
                    <div v-if="editId === id" class="flex items-center">
                      <Input
                        ref="nameInputRef"
                        :placeholder="t('project.projectEdit.module.moduleNamePlaceholder')"
                        class="flex-1 mr-2 bg-white text-xs"
                        trim
                        :value="name"
                        :allowClear="false"
                        :maxlength="50"
                        @blur="hadleblur(id, $event)"
                        @pressEnter="pressEnter(id, $event)" />
                      <Button
                        type="link"
                        size="small"
                        class="px-0 py-0 mr-1 text-xs"
                        @click="cancelEdit">
                        {{ t('project.projectEdit.module.cancel') }}
                      </Button>
                    </div>
                    <div v-else class="flex items-center space-x-2 tree-title group">
                      <Icon icon="icon-mokuai" class="text-xs text-gray-500" />
                      <span class="flex-1 truncate min-w-0 text-xs text-gray-700" :title="name">{{ name }}</span>
                      <Dropdown :trigger="['click']">
                        <Button
                          v-if="!props.disabled && hasEditPermission"
                          class="hidden group-hover:inline-block"
                          type="text"
                          size="small">
                          <Icon icon="icon-more" class="text-xs text-gray-400" />
                        </Button>
                        <template #overlay>
                          <Menu class="text-xs" @click="onMenuClick($event, {name, id, index, ids, level, childLevels, pid})">
                            <MenuItem v-if="level < 4" key="add">
                              <Icon icon="icon-jia" class="text-xs" />
                              {{ t('project.projectEdit.module.newSubModule') }}
                            </MenuItem>
                            <MenuItem v-if="index > 0 || +pid > 0" key="up">
                              <Icon icon="icon-shangyi" class="text-xs" />
                              {{ index < 1 ? t('project.projectEdit.module.moveUp') : t('project.projectEdit.module.moveUpOne') }}
                            </MenuItem>
                            <MenuItem v-if="!isLast" key="down">
                              <Icon icon="icon-xiayi" class="text-xs" />
                              {{ t('project.projectEdit.module.moveDown') }}
                            </MenuItem>
                            <MenuItem key="move">
                              <Icon icon="icon-yidong" class="text-xs" />
                              {{ t('project.projectEdit.module.move') }}
                            </MenuItem>
                            <MenuItem key="edit">
                              <Icon icon="icon-bianji" class="text-xs" />
                              {{ t('actions.edit') }}
                            </MenuItem>
                            <MenuItem key="del">
                              <Icon icon="icon-qingchu" class="text-xs" />
                              {{ t('actions.delete') }}
                            </MenuItem>
                          </Menu>
                        </template>
                      </Dropdown>
                    </div>
                  </template>
                </Tree>
              </div>
            </template>
          </template>
        </Spin>
      </div>
    </div>

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

:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 32px;
  padding-left: 0;
  line-height: 18px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: #f8fafc;
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: #f8fafc;
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 30px;
  margin-top: 1px;
  line-height: 30px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 32px;
  margin: 0;
  padding: 0;
  line-height: 32px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 32px;
  line-height: 32px;
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
