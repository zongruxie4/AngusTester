<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { duration } from '@xcan-angus/infra';
import { AsyncComponent, Hints, Icon, IconRefresh, Input, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { tag } from '@/api/tester';

type TagItem = {
  id: string;
  name: string;
  showName: string;
  showTitle: string;
  hasEditPermission: boolean;

}

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  disabled: false
});

const CreateModal = defineAsyncComponent(() => import('./add.vue'));

const MAX_LENGTH = 20;

const pageNo = ref(1);
const pageSize = ref(200);
const inputValue = ref<string>();

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);

const dataList = ref<TagItem[]>([]);
const total = ref(0);

const editId = ref<string>();
const modalVisible = ref(false);

const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  inputValue.value = event.target.value;
  pageNo.value = 1;
  loadData();
});

const toCreate = () => {
  modalVisible.value = true;
};

const createOk = (data: { id: string; name: string }) => {
  const _data = format(data);
  dataList.value.unshift(_data);
  total.value += 1;
};

const refresh = () => {
  pageNo.value = 1;
  loadData();
};

const getParams = () => {
  const params: {
    pageNo: number;
    pageSize: number;
    projectId: string;
    filters?: { key: 'name'; op: 'MATCH_END'; value: string }[],
  } = {
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    projectId: props.projectId
  };

  if (inputValue.value) {
    params.filters = [{ key: 'name', op: 'MATCH_END', value: inputValue.value }];
  }

  return params;
};

const format = (data: { id: string; name: string }) => {
  let showTitle = '';
  let showName = data.name;
  if (data.name?.length > MAX_LENGTH) {
    showTitle = data.name;
    showName = showName.slice(0, MAX_LENGTH) + '...';
  }

  return {
    ...data,
    showTitle,
    showName
  };
};

const loadData = async (remainder = 0, _params?:{pageNo?:number;pageSize?:number;}) => {
  loading.value = true;
  let params = getParams();
  if (_params) {
    params = { ...params, ..._params };
  }

  const [error, res] = await tag.getTagList(params);
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
  if (!data) {
    return;
  }

  const list = ((data.list || []) as {id:string;name:string}[]).slice(remainder);
  const pureList = list.map((item) => {
    return format(item);
  });

  if (params.pageNo > 1) {
    dataList.value.push(...pureList);
  } else {
    dataList.value = pureList;
  }

  total.value = +data.total;
};

const toEdit = (data: TagItem) => {
  if (props.disabled || !data.hasEditPermission) {
    return;
  }
  editId.value = data.id;
};

const cancelEdit = () => {
  editId.value = undefined;
};

const pressEnter = async (id: string, index: number, event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  if (value === dataList.value[index].showName) {
    editId.value = undefined;
    return;
  }

  loading.value = true;
  const [error] = await tag.updateTag([{ id, name: value }]);
  loading.value = false;
  if (error) {
    return;
  }

  dataList.value[index] = format({ id: dataList.value[index].id, name: value });
  notification.success('修改成功');
  editId.value = undefined;
};

const hadleblur = (id: string, index: number, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editId.value === id) {
      pressEnter(id, index, event);
    }
  }, 300);
};

// 删除弹框
const toDelete = (data: TagItem, index:number) => {
  modal.confirm({
    content: `确定删除标签【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await tag.deleteTag([id]);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('删除标签成功');
      // 删除列表中该条数据
      dataList.value.splice(index, 1);
      total.value -= 1;

      // 如果删除的不是所有数据的最后一条，则需要追加该下标的数据
      if (index < total.value) {
        const params = {
          pageNo: dataList.value.length + 1,
          pageSize: 1
        };
        loadData(0, params);
      }
    }
  });
};

const loadMore = () => {
  // 修复pageNo，因为添加、删除后前端没有刷新列表
  const remainder = dataList.value.length % pageSize.value;
  const current = Math.floor(dataList.value.length / pageSize.value);
  pageNo.value = current + 1;
  loadData(remainder);
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

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const showLoadMore = computed(() => {
  return dataList.value.length < total.value;
});
</script>
<template>
  <div class="w-full h-full leading-5 text-3 overflow-auto">
    <div class="mb-6">
      <div class="text-3.5 font-medium mb-2.5">关于数据标签</div>
      <div>数据标签可以方便对任务、功能用例等分类、提高检索和管理效率，促进团队高效协作。示例：边界测试、业务流程测试、错误处理、输入验证、输出验证、异常条件测试、安全性测试、兼容性测试等。</div>
    </div>

    <div class="flex space-x-2 items-center">
      <div class="text-3.5 font-medium">已添加的数据标签</div>
      <Hints
        v-if="!props.disabled"
        text="回车或鼠标失焦时保存添加标签，双击已存在标签可进入编辑状态。"
        class="flex-1" />
    </div>

    <Spin
      :spinning="loading"
      style="min-height: calc(100% - 108px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../../assets/images/nodata.png">
          <div v-if="!props.disabled" class="flex items-center text-theme-sub-content text-3 leading-7">
            <span>您尚未添加任何标签，立即</span>
            <Button
              type="link"
              size="small"
              class="text-3 py-0 px-1"
              @click="toCreate">
              添加标签
            </Button>
          </div>
          <div v-else="props.disabled">
            您尚未添加任何标签。
          </div>
        </div>
        <template v-else>
          <div class="flex items-center mt-3.5">
            <div class="flex items-center">
              <Input
                v-model:value="inputValue"
                placeholder="请输入标签名称"
                class="w-75 mr-2"
                trimAll
                :allowClear="true"
                :maxlength="50"
                @change="inputChange">
                <template #suffix>
                  <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
                </template>
              </Input>
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
                添加标签
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

          <div v-else class="flex items-center flex-wrap">
            <div
              v-for="(item, index) in dataList"
              :key="item.id"
              class="mr-3.5 mb-3.5">
              <div v-if="editId === item.id" class="flex items-center">
                <Input
                  placeholder="请输入标签名称"
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
                <Icon icon="icon-biaoqian1" class="mr-1 text-3.5" />
                <span :title="item.showTitle" class="truncate mr-2">{{ item.showName }}</span>
                <Icon
                  v-if="!props.disabled && item.hasEditPermission"
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
          </div>
        </template>
      </template>
    </Spin>

    <AsyncComponent :visible="modalVisible">
      <CreateModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        @ok="createOk" />
    </AsyncComponent>
  </div>
</template>
