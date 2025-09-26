<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, Hints, Icon, IconRequired, Input, notification, Spin } from '@xcan-angus/vue-ui';
import { Button, Textarea, Tooltip, TypographyParagraph } from 'ant-design-vue';
import { services } from '@/api/tester';
import { regexpUtils, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { SaveParams, TagObj } from './PropsType';

interface Props {
  visible: boolean;
  disabled: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  disabled: true,
  id: ''
});

const { t } = useI18n();

const emit = defineEmits<{(e: 'update:visible', value:boolean): void}>();

// 空数据
const newData:TagObj = {
  id: utils.uuid('api'),
  name: '',
  description: '',
  externalDocs: {
    url: '',
    ellipsis: false,
    showEllipsis: false,
    urlErr: {
      emptyUrl: false,
      errUrl: false
    }
  },
  isEdit: true,
  isExpand: true,
  isAdd: true,
  delLoading: false,
  saveLoading: false,
  nameErr: false,
  ellipsis: false,
  showEllipsis: false
};

const tagList = ref<TagObj[]>([]);
const oldtagList = ref<TagObj[]>([]);
const loading = ref(false);
// 初始化配置
const getProjectSchematagList = async () => {
  loading.value = true;
  const [error, { data }] = await services.getServicesSchemaTag(props.id);
  loading.value = false;
  if (error) {
    return;
  }
  // // 如果没有历史数据 默认展示一条空数据
  if (!data?.length) {
    tagList.value = [JSON.parse(JSON.stringify(newData))];
    // 记录正在编辑的数据 编辑逻辑需要
    currEditData.value = tagList.value[0];
    return;
  }

  tagList.value = data.map(item => ({
    ...item,
    id: utils.uuid('api'),
    isEdit: false,
    isAdd: false,
    isExpand: false,
    delLoading: false,
    saveloading: false,
    nameErr: false,
    ellipsis: false,
    showEllipsis: false,
    externalDocs: {
      ...item.externalDocs,
      ellipsis: false,
      showEllipsis: false,
      urlErr: {
        emptyUrl: false,
        errUrl: false
      }
    }
  }));
  // 记录历史数据
  oldtagList.value = JSON.parse(JSON.stringify(tagList.value));
  // 启用添加
  addBtnDisabled.value = false;
};

// 添加按钮禁用状态
const addBtnDisabled = ref(false);

// 添加新配置
const addSyncInfo = () => {
  const hasEditData = tagList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    const checkRes = getCheckDataResult(hasEditData[0]);
    if (checkRes) {
      return;
    }
    if (getChenkUpdateRes()) {
      return;
    }
  }

  if (tagList.value[0]?.isAdd) {
    tagList.value[0] = { ...JSON.parse(JSON.stringify(newData)), id: tagList.value[0].id };
    return;
  }
  // 列表开始位置添加一条新数据
  tagList.value.unshift(JSON.parse(JSON.stringify(newData)));
  // 记录正在编辑的数据(编辑逻辑需要)
  currEditData.value = tagList.value[0];
  // 追加后禁用添加按钮
  addBtnDisabled.value = true;
  setEditFalseExceptId(tagList.value, currEditData.value.id);
};

// 删除配置
const hanldeDelete = async (tag:TagObj) => {
  if (tag.delLoading) {
    return;
  }
  // 如果是添加数据 直接删除
  if (tag.isAdd) {
    // 判断列表是否剩余一条数据 剩余一条数据禁止删除
    if (tagList.value.length === 1) {
      return;
    }
    tagList.value = tagList.value.filter(item => item.id !== tag.id);
    return;
  }

  loading.value = true;
  const [error] = await services.delServicesSchemaTag(props.id, [tag.name]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.deleteSuccess'));
  // 如果删除成功
  tagList.value = tagList.value.filter(item => item.id !== tag.id);
  oldtagList.value = oldtagList.value.filter(item => item.id !== tag.id);

  // 如果列表没有数据 删除后添加一条添加的数据
  if (tagList.value.length === 0) {
    tagList.value.unshift(JSON.parse(JSON.stringify(newData)));
  }
};

// 保存
const handleSave = (tag:TagObj) => {
  // 校验有没有空项
  const checkRes = getCheckDataResult(tag);
  if (checkRes) {
    return;
  }

  // 如果是添加数据 判断url有没有重复
  if (tag.isAdd) {
    const len = tagList.value.filter(item => item.name === tag.name)?.length;
    if (len >= 2) {
      notification.warning(t('service.tag.messages.nameExists'));
      tag.nameErr = true;
      return;
    }
  } else {
    // 如果是旧数据 判断数据有没有修改
    if (!chenkUpdate(tag)) {
      tag.isEdit = false;
      tag.isExpand = lastIsExpandState.value;
      return;
    }
  }
  saveNewData(tag);
};

const saveNewData = async (tag:TagObj) => {
  // 如果是添加数据 判断url有没有重复
  if (tag.isAdd) {
    const len = tagList.value.filter(item => item.name === tag.name)?.length;
    if (len >= 2) {
      notification.warning(t('service.tag.messages.nameExists'));
      tag.nameErr = true;
      return;
    }
  } else {
    // 如果是旧数据 判断数据有没有修改
    if (!chenkUpdate(tag)) {
      tag.isEdit = false;
      tag.isExpand = lastIsExpandState.value;
      return;
    }
  }

  let params:SaveParams = {
    name: tag.name
  };
  if (tag.description) {
    params.description = tag.description;
  }
  if (tag.externalDocs.url) {
    params = {
      ...params,
      externalDocs: {
        url: tag.externalDocs.url
      }
    };
    if (tag.externalDocs.description) {
      params = {
        ...params,
        externalDocs: {
          url: tag.externalDocs.url,
          description: tag.externalDocs.description
        }
      };
    }
  }

  loading.value = true;
  const [error] = await services.addServicesSchemaTag(props.id, params);
  loading.value = false;
  if (error) {
    return false;
  }
  oldtagList.value = JSON.parse(JSON.stringify(tagList.value));
  tag.isEdit = false;
  tag.isExpand = lastIsExpandState.value;
  addBtnDisabled.value = false;
  currEditData.value = undefined;
  notification.success(t('actions.tips.saveSuccess'));
  if (tag.isAdd) {
    tag.isAdd = false;
  }
};

// 检查提交的数据有没有空项 有空项返回true 否则返回false
const getCheckDataResult = (_data:TagObj):boolean => {
  let hasEmpty = false;
  if (!_data.name) {
    _data.nameErr = true;
    hasEmpty = true;
  }

  if (_data.externalDocs.description) {
    if (!_data.externalDocs.url) {
      _data.externalDocs.urlErr.emptyUrl = true;
      hasEmpty = true;
    }
  }
  return hasEmpty;
};

onMounted(() => {
  getProjectSchematagList();
});

// 记录正在编辑的数据 同时只有一个编辑
const currEditData = ref<TagObj>();

// 展开收起 开启关闭编辑
const handleExpand = (tag:TagObj) => {
  const hasEditData = tagList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      tagList.value = tagList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = getCheckDataResult(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (getChenkUpdateRes()) {
        return;
      }
    }
  }

  tag.isExpand = !tag.isExpand;
  if (!tag.isExpand) {
    tag.isEdit = false;
  }
  setEditFalseExceptId(tagList.value, tag.id);
  addBtnDisabled.value = false;
};

// 提起公共代码 校验数据未保存
const getChenkUpdateRes = () => {
  if (currEditData.value) {
    const hasUpdate = chenkUpdate(tagList.value.filter(item => item.id === currEditData.value?.id)[0]);
    if (hasUpdate) {
      notification.warning(t('service.tag.messages.dataNotSaved'));
      return true;
    }
  }
  return false;
};

const lastIsExpandState = ref(false);
// 开启关闭编辑 同时修改展开收起
const handleEdit = (tag:TagObj) => {
  const hasEditData = tagList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      tagList.value = tagList.value.filter(item => item.id !== hasEditData[0].id);
      addBtnDisabled.value = false;
    } else {
      const checkRes = getCheckDataResult(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (getChenkUpdateRes()) {
        return;
      }
    }
  }

  lastIsExpandState.value = tag.isExpand;
  tag.isEdit = true;
  tag.isExpand = true;
  tag.showEllipsis = false;
  tag.ellipsis = false;
  tag.externalDocs.showEllipsis = false;
  tag.externalDocs.ellipsis = false;
  currEditData.value = tag;
  setEditFalseExceptId(tagList.value, tag.id);
  addBtnDisabled.value = false;
};

// 取消编辑
const cancelEdit = (tag:TagObj) => {
  // 如果取消的是添加的数据
  if (tag.isAdd) {
    // 如果列表仅有一条数据 且是添加的 禁止取消，保持展开并且编辑状态
    if (tagList.value.length === 1) {
      emit('update:visible', false);
      return;
    }
    // 如果列表有多条数据 取消后删除添加的数据 并启用添加按钮
    tagList.value = tagList.value.filter(item => item.id !== tag.id);
    addBtnDisabled.value = false;
    currEditData.value = undefined;
    return;
  }

  //  如果取消的是历史数据 判断数据有没有修改，然后收起详情并取消编辑状态
  const hasUpdate = chenkUpdate(tag);
  //  如果有修改,取消编辑先恢复数据
  if (hasUpdate) {
    const oldSync = oldtagList.value.find(item => item.id === tag.id);
    for (let i = 0; i < tagList.value.length; i++) {
      if (tag.id === tagList.value[i].id) {
        tagList.value[i] = oldSync ? JSON.parse(JSON.stringify(oldSync)) : tag;
        tagList.value[i].isEdit = false;
        tagList.value[i].isExpand = lastIsExpandState.value;
        break;
      }
    }
  }

  tag.isEdit = false;
  tag.isExpand = lastIsExpandState.value;
  currEditData.value = undefined;
  addBtnDisabled.value = false;
};

// 判断编辑的数据有无改变
const chenkUpdate = (newData:TagObj) => {
  const _oldDataList = oldtagList.value.filter(item => item?.id === newData?.id);
  if (!_oldDataList.length) {
    return false;
  }
  const _oldData = {
    name: _oldDataList[0].name,
    description: _oldDataList[0]?.description || '',
    externalDocs: {
      description: _oldDataList[0].externalDocs?.description || '',
      url: _oldDataList[0]?.externalDocs?.url || ''
    },
    id: _oldDataList[0].id
  };
  const _newData = {
    name: newData.name,
    description: newData?.description || '',
    externalDocs: {
      description: newData.externalDocs?.description || '',
      url: newData?.externalDocs?.url || ''
    },
    id: newData.id
  };
  return !utils.deepCompare(_oldData, _newData);
};

// 收起当前数据之外的数据并取消编辑
const setEditFalseExceptId = (arr:TagObj[], id:string):void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

const tagListRef = ref<HTMLElement | null>();

const tagNameChange = debounce(duration.search, (value:string, tag:TagObj) => {
  tag.nameErr = !value;
});

const externalDocsUrlChange = debounce(duration.search, (value:string, tag:TagObj) => {
  if (!value) {
    tag.externalDocs.urlErr.emptyUrl = true;
    return;
  }

  tag.externalDocs.urlErr.emptyUrl = false;
  if (regexpUtils.isUrl(value)) {
    tag.externalDocs.urlErr.errUrl = false;
    return;
  }
  tag.externalDocs.urlErr.errUrl = true;
});

const handleDesExpand = (event, tag:TagObj) => {
  event.stopPropagation();
  tag.ellipsis = !tag.ellipsis;
};

const tagDesEllipsis = (tag:TagObj) => {
  return {
    rows: 3,
    onEllipsis: (ellipsis) => {
      tag.showEllipsis = ellipsis;
    }
  };
};

const handleExternalDocsDesExpand = (event, tag:TagObj) => {
  event.stopPropagation();
  tag.externalDocs.ellipsis = !tag.externalDocs.ellipsis;
};

const tagExternalDocsDesEllipsis = (tag:TagObj) => {
  return {
    rows: 3,
    onEllipsis: (ellipsis) => {
      tag.externalDocs.showEllipsis = ellipsis;
    }
  };
};
</script>
<template>
  <Spin class="h-full flex flex-col" :spinning="loading">
    <Hints :text="t('service.tag.hints.main')" />
    <Hints :text="t('service.tag.hints.sub')" class="mt-2 hints-text" />
    <div class="mt-2">
      <Button
        size="small"
        type="primary"
        class="flex items-center"
        :disabled="props.disabled || tagList.length > 1999 || addBtnDisabled"
        @click="addSyncInfo">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('common.add') }}
      </Button>
    </div>
    <div
      ref="tagListRef"
      style="min-height: 400px;scrollbar-gutter: stable;"
      class="flex-1 overflow-y-auto flex flex-col space-y-2 pr-1.5 -mr-3.5 mb-5 text-3 text-text-content">
      <div
        v-for="tag in tagList"
        :key="tag.id"
        class="border border-border-divider p-2 rounded mt-2">
        <div v-if="!tag.isAdd && !tag.isEdit">
          <div class="flex items-start justify-between -mt-1 leading-5">
            <div class="mr-2 mt-1.5 truncate cursor-pointer flex items-center" @click="handleExpand(tag)">
              <Icon icon="icon-biaoqian" class="mr-1 flex-none" />
              <div class="truncate flex-1">
                <Tooltip
                  :overlayStyle="{zIndex: 9999}"
                  :title="tag.name"
                  placement="topLeft">
                  {{ tag.name }}
                </Tooltip>
              </div>
            </div>
            <div class="flex flex-none items-center mt-1.75 leading-5">
              <Tooltip :title="t('common.edit')" placement="top">
                <template v-if="props.disabled">
                  <Icon
                    icon="icon-shuxie"
                    class="mr-1 cursor-not-allowed text-text-disabled" />
                </template>
                <template v-else>
                  <Icon
                    icon="icon-shuxie"
                    class="mr-1"
                    :class="tag.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                    @click="handleEdit(tag)" />
                </template>
              </Tooltip>
              <Tooltip :title="t('actions.delete')" placement="top">
                <template v-if="props.disabled">
                  <Icon
                    icon="icon-qingchu"
                    class="mr-1 cursor-not-allowed text-text-disabled text-3.5" />
                </template>
                <template v-else>
                  <Icon
                    icon="icon-qingchu"
                    class="mr-1 text-3.5"
                    :class="tag.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                    @click="hanldeDelete(tag)" />
                </template>
              </Tooltip>
              <Arrow
                :open="tag.isExpand"
                @click="handleExpand(tag)" />
            </div>
          </div>
        </div>
        <div
          :class="tag.isExpand ? 'open-info' : 'stop-info'"
          class="transition-height duration-500 overflow-hidden leading-3 text-3">
          <template v-if="tag.isExpand">
            <div v-if="tag.isEdit" class="flex-1 text-3">
              <div><IconRequired />{{ t('common.name') }}</div>
              <Input
                v-model:value="tag.name"
                :placeholder="t('common.placeholders.searchKeyword')"
                size="small"
                class="mt-2 mb-5"
                :maxlength="200"
                :error="tag.nameErr"
                :disabled="!tag.isAdd"
                @change="(event)=>tagNameChange(event.target.value,tag)" />
            </div>
            <template v-if="tag.isEdit">
              <div class="pl-1.75">{{ t('common.description') }}</div>
              <Textarea
                v-model:value="tag.description"
                size="small"
                class="mt-2 mb-5"
                :placeholder="t('service.tag.form.descriptionPlaceholder')"
                :rows="3"
                :maxlength="400"
                :disabled="!tag.isEdit" />
            </template>
            <template v-else>
              <template v-if="tag.description">
                <TypographyParagraph
                  class="break-all whitespace-break-spaces leading-5 mt-1"
                  :ellipsis="tag.ellipsis ? false : tagDesEllipsis(tag)"
                  :content="tag.description"
                  :copyable="tag.showEllipsis?{ tooltip: false }:false">
                  <template v-if="tag.showEllipsis" #copyableIcon>
                    <a @click="(e)=>handleDesExpand(e,tag)">{{ tag.ellipsis ? t('actions.collapse') : t('actions.expand') }}</a>
                  </template>
                </TypographyParagraph>
              </template>
            </template>
            <div v-if="tag.isExpand && !tag.isEdit && tag.externalDocs.url" class="border-t border-dashed border-border-divider mt-2"></div>
            <div v-if="tag.isEdit" class="flex-1">
              <div class="pl-1.75">{{ t('service.tag.form.externalDocsUrl') }}</div>
              <Input
                v-model:value="tag.externalDocs.url"
                :placeholder="t('service.tag.form.externalDocsUrlPlaceholder')"
                size="small"
                class="mt-2"
                :maxlength="400"
                :error="tag.externalDocs.urlErr.emptyUrl || tag.externalDocs.urlErr.errUrl"
                @change="(event)=>externalDocsUrlChange(event.target.value,tag)" />
              <div v-if="tag.isEdit" class="text-rule text-3 h-5">
                <template v-if="tag.externalDocs.urlErr.emptyUrl">{{ t('service.tag.validation.enterUrl') }}</template>
                <template v-if="!tag.externalDocs.urlErr.emptyUrl && tag.externalDocs.urlErr.errUrl">
                  {{ t('service.tag.validation.enterCorrectUrl') }}
                </template>
              </div>
            </div>
            <template v-else>
              <div v-if="tag.externalDocs.url" class="mt-2 mb-1 leading-5">
                <Icon icon="icon-lianjie2" class="text-3 mr-1 -mt-0.5" />
                {{ t('service.tag.form.externalDocsLink') }}
              </div>
              <a
                :href="tag.externalDocs.url"
                target="_blank"
                class="break-all whitespace-pre-wrap cursor-pointer text-text-link leading-5">
                {{ tag.externalDocs.url }}
              </a>
            </template>
            <template v-if="tag.isEdit">
              <div class="pl-1.75">{{ t('service.tag.form.externalDocsDescription') }}</div>
              <Textarea
                v-model:value="tag.externalDocs.description"
                size="small"
                class="mt-2"
                :placeholder="t('service.tag.form.externalDocsDescriptionPlaceholder')"
                :rows="3"
                :maxlength="400" />
            </template>
            <template v-else>
              <template v-if="tag.externalDocs?.description">
                <TypographyParagraph
                  class="break-all whitespace-break-spaces leading-5 mt-1"
                  :ellipsis="tagExternalDocsDesEllipsis(tag)"
                  :content="tag.externalDocs.description"
                  :copyable="tag.externalDocs.showEllipsis ? { tooltip: false }:false">
                  <template v-if="tag.externalDocs.showEllipsis" #copyableIcon>
                    <a @click="(e)=>handleExternalDocsDesExpand(e,tag)">
                      {{ tag.externalDocs.ellipsis ? t('actions.collapse') : t('actions.expand') }}
                    </a>
                  </template>
                </TypographyParagraph>
              </template>
            </template>
            <div class="flex justify-end" :class="tag.isEdit?'mt-3':''">
              <template v-if="tag.isEdit">
                <Button
                  type="link"
                  size="small"
                  class="mr-2 px-0"
                  :disabled="tagList.length === 1 && tag.isAdd"
                  @click="cancelEdit(tag)">
                  {{ t('common.cancel') }}
                </Button>
                <Button
                  size="small"
                  type="link"
                  class="px-0"
                  :disabled="props.disabled || tag.saveLoading || !tagList.length"
                  @click="handleSave(tag)">
                  {{ t('actions.save') }}
                </Button>
              </template>
            </div>
          </template>
        </div>
      </div>
    </div>
  </Spin>
</template>
<style scoped>
.open-info {
  height: auto;
}

.stop-info {
  height: 0;
}

:deep(.hints-text svg) {
  visibility: hidden;
}

:deep(.v-note-wrapper .v-note-read-model) {
  width: 264px;
  padding: 8px;
}
</style>
