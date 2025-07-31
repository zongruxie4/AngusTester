<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch, watchEffect } from 'vue';
import type { UploadFile } from 'ant-design-vue';
import { Button, Checkbox, Upload } from 'ant-design-vue';
import { Icon, Input, notification, Select } from '@xcan-angus/vue-ui';
import { gzip, ungzip, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { RequestBodyFormItem } from '../PropsType';

export interface Props {
  value: RequestBodyFormItem[];
  maxFileSize?: number;
  hasFileType?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  maxFileSize: 0,
  hasFileType: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', index: number, data: RequestBodyFormItem): void;
  (e: 'add', data: RequestBodyFormItem): void;
  (e: 'del', index: number): void;
  (e: 'errorNumChange', value: number): void;
}>();

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: RequestBodyFormItem }>({});
const checkedSet = ref(new Set<string>());
const nameErrorSet = ref(new Set<string>());

const fileMap = ref<{ [key: string]: {
   value: string|UploadFile;
   fileName: string;
   format: 'binary';
   contentEncoding: 'gzip_base64';
   contentType:string;
   size:number;
   uid:string;
}[] }>({});
const showSelectFileMap = ref<{ [key: string]: boolean }>({});
const multipleMap = ref<{ [key: string]: boolean }>({});
const urlMap = ref<{ [key: string]: { [key: string]: {name:string;url:string} } }>({});

const customRequest = async ({ file }: { file: UploadFile }, id: string, index:number) => {
  if (totalSize.value + file.size > +props.maxFileSize) {
    notification.warning(`总上传文件大小不能超过${utils.formatBytes(props.maxFileSize)}`);
    return;
  }

  const { name, size, uid, type } = file;
  const data:{
   value: string|UploadFile;
   fileName: string;
   format: 'binary';
   contentEncoding: 'gzip_base64';
   contentType: string;
   size:number;
   uid:string;
  } = {
    format: 'binary',
    contentEncoding: 'gzip_base64',
    fileName: name,
    value: file,
    contentType: type,
    size,
    uid
  };
  if (fileMap.value[id]?.length) {
    fileMap.value[id].push(data);
  } else {
    fileMap.value[id] = [data];
  }

  if (urlMap.value[id]) {
    urlMap.value[id][file.uid] = { name, url: undefined };
  } else {
    urlMap.value[id] = { [file.uid]: { name, url: undefined } };
  }

  showSelectFileMap.value[id] = multipleMap.value[id] || !fileMap.value[id]?.length;

  // @TODO gzip延迟导致克隆元素时丢失该数据，转换之前先触发change，转换之后再触发覆盖就数据
  setTimeout(async () => {
    for (let i = 0, len = fileMap.value[id].length; i < len; i++) {
      const _file = fileMap.value[id][i].value;
      if (_file instanceof File) {
        fileMap.value[id][i].value = await gzip(_file);
        urlMap.value[id][file.uid].url = window.URL.createObjectURL(_file);
      }
    }

    fileChangeEmit(id, index);
  }, 16.67);
};

const fileChangeEmit = (id:string, index:number) => {
  const rowData = JSON.parse(JSON.stringify(dataMap.value[id]));
  if (!multipleMap.value[id]) {
    if (fileMap.value[id].length) {
      const { value, contentType, fileName } = fileMap.value[id][0];
      rowData.value = value as string;
      rowData.contentType = contentType;
      rowData.fileName = fileName;
      rowData.type = 'string';
      rowData.format = 'binary';
    } else {
      rowData.value = null;
    }
  } else {
    if (fileMap.value[id].length) {
      const fileContents = fileMap.value[id].map(item => ({
        value: item.value,
        fileName: item.fileName,
        contentType: item.contentType,
        contentEncoding: item.contentEncoding,
        type: 'array',
        format: 'binary'
      }));
      rowData.value = JSON.stringify(fileContents);
      rowData.type = 'array';
      rowData.format = 'binary';
    } else {
      rowData.value = null;
    }
  }

  changeEmit(index, rowData);
};

const removeFile = (pid:string, pindex:number, uid:string, index:number) => {
  fileMap.value[pid].splice(index, 1);
  delete urlMap.value[pid][uid];
  showSelectFileMap.value[pid] = true;
  fileChangeEmit(pid, pindex);
};

const checkboxChange = (event: { target: { checked: boolean } }, index: number, id: string) => {
  dataMap.value[id].enabled = event.target.checked;
  changeEmit(index, dataMap.value[id]);
};

const nameChange = debounce(duration.delay, (event: { target: { value: string } }, index: number, id: string): void => {
  nameErrorSet.value.delete(id);
  const value = event.target.value?.trim();
  const data = dataMap.value[id];
  if (value) {
    if (!checkedSet.value.has(id)) {
      data.enabled = true;
      checkedSet.value.add(id);
    }

    if (index === idList.value.length - 1) {
      const newId = utils.uuid();
      idList.value.push(newId);
      dataMap.value[newId] = { id: newId, type: 'string', name: '' };
      emit('add', JSON.parse(JSON.stringify(dataMap.value[newId])));
    }
  }

  data.name = value;
  changeEmit(index, data);
});

// 数据类型变更
const typeChange = (type: RequestBodyFormItem['type'], index: number, id: string) => {
  let format: RequestBodyFormItem['format'] = 'string';
  if (type === 'file' || type === 'file(array)') {
    format = 'binary';
    if (type === 'file(array)') {
      type = 'array';
      multipleMap.value[id] = true;
    } else if (type === 'file') {
      type = 'string';
      multipleMap.value[id] = false;
    }

    showSelectFileMap.value[id] = true;
  }

  // 切换类型清空value
  delete fileMap.value[id];
  delete urlMap.value[id];
  dataMap.value[id].value = undefined;
  const data = { ...dataMap.value[id], type, format };
  changeEmit(index, data);
};

const valueChange = debounce(duration.delay, (event: { target: { value: string } }, index: number, id: string): void => {
  dataMap.value[id].value = event.target.value;
  changeEmit(index, dataMap.value[id]);
});

const deleteHandler = (index: number, id: string): void => {
  const len = idList.value.length - 1;
  if (len === index) {
    dataMap.value[id].name = '';
    dataMap.value[id].value = '';
    dataMap.value[id].type = 'string';
    changeEmit(index, dataMap.value[id]);
  } else {
    idList.value.splice(index, 1);
    delete dataMap.value[id];
    emit('del', index);
  }

  checkedSet.value.delete(id);
  nameErrorSet.value.delete(id);
  delete fileMap.value[id];
  delete urlMap.value[id];
  delete showSelectFileMap.value[id];
  delete multipleMap.value[id];
};

const changeEmit = (index: number, data: RequestBodyFormItem): void => {
  emit('change', index, JSON.parse(JSON.stringify(data)));
};

const reset = () => {
  idList.value = [];
  dataMap.value = {};
  checkedSet.value.clear();
  nameErrorSet.value.clear();
  fileMap.value = {};
  showSelectFileMap.value = {};
  multipleMap.value = {};
  urlMap.value = {};
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    const fileOriginData: { [key: string]: RequestBodyFormItem } = {};
    let hasEmptyRow = false;
    if (newValue?.length) {
      for (let i = 0, len = newValue.length; i < len; i++) {
        const id = utils.uuid();
        const item = newValue[i];
        let { type } = item;
        if (item.format === 'binary') {
          if (type === 'array') {
            type = 'file(array)';
          } else if (type === 'string') {
            type = 'file';
          }
        }

        checkedSet.value.add(id);
        idList.value.push(id);
        dataMap.value[id] = {
          ...item,
          type,
          id
        };

        if (item.format === 'binary') {
          fileOriginData[id] = dataMap.value[id];
        }
      }

      hasEmptyRow = !newValue[newValue.length - 1].name;
    }

    if (!hasEmptyRow) {
      const id = utils.uuid();
      dataMap.value[id] = { id: utils.uuid(), type: 'string', name: '' };
      idList.value.push(id);
      emit('add', JSON.parse(JSON.stringify(dataMap.value[id])));
    }

    nextTick(async () => {
      for (const key in fileOriginData) {
        const data = fileOriginData[key];
        fileMap.value[key] = [];
        let originDataList: RequestBodyFormItem[] = [];
        if (data.type === 'file(array)') {
          originDataList = JSON.parse(data.value || '[]');
        } else {
          originDataList = data ? [data] : [];
        }

        for (let i = 0, len = originDataList.length; i < len; i++) {
          const { value, fileName, contentType } = originDataList[i];
          if (value) {
            const buffer = ungzip(value);
            const file = new File([buffer], fileName, { type: contentType });
            const uid = utils.uuid();
            fileMap.value[key].push({
              value,
              fileName,
              contentType,
              contentEncoding: 'gzip_base64',
              format: 'binary',
              size: file.size,
              uid: uid
            });
            if (urlMap.value[key]) {
              urlMap.value[key][uid] = { name: fileName, url: window.URL.createObjectURL(file) };
            } else {
              urlMap.value[key] = { [uid]: { name: fileName, url: window.URL.createObjectURL(file) } };
            }
          }
        }

        multipleMap.value[key] = data.type === 'file(array)';
        showSelectFileMap.value[key] = multipleMap.value[key] || !fileMap.value[key]?.length;
      }
    });
  }, { immediate: true });

  watchEffect(() => {
    const size = nameErrorSet.value.size;
    emit('errorNumChange', size);
  });
});

const isValid = (): boolean => {
  nameErrorSet.value.clear();
  const ids = idList.value;
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    if (!dataMap.value[id].name) {
      nameErrorSet.value.add(id);
    }
  }

  return !nameErrorSet.value.size;
};

defineExpose({ isValid });

const totalSize = computed(() => {
  return Object.values(fileMap.value).reduce((prev, cur) => {
    prev += cur.reduce((_prev, _cur) => {
      _prev += _cur.size;
      return _prev;
    }, 0);
    return prev;
  }, 0);
});

const selectOptions = computed(() => {
  return props.hasFileType
    ? [{
        label: 'string',
        value: 'string'
      },
      {
        label: 'boolean',
        value: 'boolean'
      },
      {
        label: 'integer',
        value: 'integer'
      },
      {
        label: 'number',
        value: 'number'
      },
      {
        label: 'file',
        value: 'file'
      },
      {
        label: 'file(array)',
        value: 'file(array)'
      }]
    : [{
        label: 'string',
        value: 'string'
      },
      {
        label: 'boolean',
        value: 'boolean'
      },
      {
        label: 'integer',
        value: 'integer'
      },
      {
        label: 'number',
        value: 'number'
      }];
});
</script>
<template>
  <div class="space-y-3 relative">
    <div
      v-for="(item, index) in idList"
      :key="item"
      :class="{ 'opacity-70': !dataMap[item].enabled }"
      class="flex flex-nowrap items-start whitespace-nowrap space-x-2">
      <Checkbox
        v-model:checked="dataMap[item].enabled"
        class="transform-gpu translate-y-0.5"
        @change="checkboxChange($event, index, item)" />
      <Input
        v-model:value="dataMap[item].name"
        placeholder="请输入参数名称"
        trim
        class="max-w-100 flex-1"
        :error="nameErrorSet.has(item)"
        @change="nameChange($event, index, item)" />
      <Select
        v-model:value="dataMap[item].type"
        class="w-25"
        placeholder="请选择参数类型"
        :options="selectOptions"
        @change="typeChange($event, index, item)" />
      <div
        v-if="['file(array)', 'file'].includes(dataMap[item].type)"
        class="flex items-start flex-wrap flex-1 min-h-7 rounded border border-solid px-1.75 min-w-50 bg-white border-theme-text-box">
        <Upload
          :showUploadList="false"
          :customRequest="($event) => customRequest($event, item, index)"
          :multiple="multipleMap[item]">
          <Button
            v-if="showSelectFileMap[item]"
            style="height: 24px;"
            size="small"
            class="mr-3 transform-gpu translate-y-0.25">
            <Icon icon="icon-xuanze" class="mr-1" />
            <span>选择文件</span>
          </Button>
        </Upload>
        <template v-if="!!fileMap[item]?.length">
          <div
            v-for="(_file,_index) in fileMap[item]"
            :key="_file.uid"
            class="flex items-center pt-0.5 leading-5.5 min-w-0 mx-1.5">
            <a
              class="inline-block truncate"
              :title="_file.fileName"
              :download="_file.fileName"
              :href="urlMap[item][_file.uid].url">{{ _file.fileName }}</a>
            <Icon
              icon="icon-shanchuguanbi"
              class="flex-shrink-0 ml-1.5 cursor-pointer text-theme-text-hover"
              @click="removeFile(item, index, _file.uid,_index)" />
          </div>
        </template>
      </div>
      <Input
        v-else
        v-model:value="dataMap[item].value"
        placeholder="请输入参数值，最多可输入4096个字符"
        trim
        class="flex-1"
        :maxlength="4096"
        @change="valueChange($event, index, item)" />
      <Button
        size="small"
        class="w-7 p-0"
        type="default"
        @click="deleteHandler(index, item)">
        <Icon icon="icon-shanchuguanbi" />
      </Button>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-upload-list.ant-upload-list-text) {
  display: flex;
}

:deep(.ant-upload-list.ant-upload-list-text) .ant-upload-list-text-container {
  margin-top: 1px !important;
  margin-bottom: 1px !important;
  transition: none !important;
}

:deep(.ant-upload-list.ant-upload-list-text) .ant-upload-list-item-name {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.ant-upload-list-item-card-actions-btn) {
  width: 14px;
  height: 14px;
  opacity: 1;
  background-color: transparent;
}

:deep(.ant-upload-list-item) .ant-upload-list-item-info {
  border-radius: 4px;
  background-color: #f5f5f5;
}

:deep(.ant-upload-list-item) {
  margin-top: 0 !important;
  margin-left: 4px
}
</style>
