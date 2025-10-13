<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Divider, Form, FormItem, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { Card, Hints, Icon, IconCopy, Input, Select } from '@xcan-angus/vue-ui';
import { EditionType } from '@xcan-angus/infra';

import { useMockSetting } from './composables/useMockSetting';

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const { t } = useI18n();

// Use the mock setting composable
const {
  // State
  infoFormState,
  securityFormState,
  apisCors,
  setting,
  mockServiceInfo,
  loading,
  editionType,
  isOpenSurety,

  // Edit flags
  editServiceDomain,
  editName,
  editCredentials,
  editOrigin,
  editRequestHeaders,
  editRequestMethods,
  editExposeHeaders,
  editWorkThreadNum,
  editMaxContentLength,
  editWorkPushbackThreadNum,
  editMaxPushbackConnectTimeout,
  editMaxPushbackRequestTimeout,

  // Options
  corsCookieOptions,
  inOptions,

  // Methods
  apiSuretyChange,
  handleEdit,
  settingFlagChange,
  apisCorsEnabledFlagChange,
  saveSureTy,
  addApisSecurityItem,
  delApisSecurityItem,
  keNameValidator,
  hasEditAuth
} = useMockSetting(props.id);

// Form references
const infoFormRef = ref();
const settingFormRef = ref();
const apisCorsFormRef = ref();
const securityFormRef = ref();
</script>
<template>
  <Card>
    <template #title>
      <span class="text-3.5 font-semibold">{{ t('mock.detail.basicInfo.title') }}</span>
    </template>
    <Form
      ref="infoFormRef"
      :model="infoFormState"
      :labelCol="{ style: { width: '200px', fontWeight: 'bold' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>ID</span>
            <div class="w-4.5"></div>
          </div>
        </template>
        <Input :value="mockServiceInfo?.id" disabled />
      </FormItem>
      <FormItem
        :rules="{ required: true, message: t('mock.detail.validation.enterName'), trigger: 'change' }"
        name="name"
        class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('common.name') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.nameTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon
                v-if="mockServiceInfo"
                icon="icon-tishi1"
                class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="infoFormState.name"
          :maxlength="100"
          allowClear
          :disabled="!editName">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editName">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('name','cancel','infoForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('name','save','infoForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('name','open','infoForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem
        :rules="{ required: true, message: t('mock.detail.validation.enterDomain'), trigger: 'change' }"
        name="serviceDomainUrl"
        class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.domain') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.domainTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="infoFormState.serviceDomainUrl"
          allowClear
          :disabled="!editServiceDomain">
          <template v-if="editionType !== EditionType.CLOUD_SERVICE && hasEditAuth" #suffix>
            <template v-if="editServiceDomain">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('serviceDomainUrl','cancel','infoForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 cursor-pointer"
                @click="handleEdit('serviceDomainUrl','save','infoForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('serviceDomainUrl','open','infoForm')" />
            </template>
          </template>
        </Input>
        <IconCopy :copyText="infoFormState.serviceDomainUrl" class="absolute -right-5 top-2.25 text-3" />
      </FormItem>
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.port') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.portTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input :value="mockServiceInfo?.servicePort" disabled />
      </FormItem>
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.node') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.nodeTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input :value="`${mockServiceInfo?.nodeName} ( ${mockServiceInfo?.nodeIp} )`" disabled />
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3.5 flex-none font-semibold">{{ t('mock.detail.basicInfo.serviceConfig') }}</span>
        <Hints class="mt-2.75 ml-2" :text="t('mock.detail.basicInfo.serviceConfigTooltip')" />
      </div>
    </template>
    <Form
      ref="settingFormRef"
      :model="setting"
      :labelCol="{ style: { width: '200px', fontWeight: 'bold' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem name="useSsl" class="w-150 hidden">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('mock.detail.basicInfo.useSsl') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.useSslTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="setting.useSsl"
          @change="(value: boolean) => settingFlagChange('useSsl', value)" />
      </FormItem>
      <FormItem name="workThreadNum" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.workThreadNum') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.workThreadNumTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.workThreadNum"
          dataType="number"
          :min="1"
          :max="10000"
          :disabled="!editWorkThreadNum">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editWorkThreadNum">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('workThreadNum', 'cancel', 'settingForm')">{{t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('workThreadNum', 'save', 'settingForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('workThreadNum','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="logFileLevel" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.logLevel') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.logLevelTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <RadioGroup
          :value="setting.logFileLevel"
          class="mt-0.75"
          :disabled="!hasEditAuth"
          @change="(e: any) => settingFlagChange('logFileLevel', e.target.value)">
          <Radio value="NONE">
            None
            <Tooltip
              :title="t('mock.detail.basicInfo.logFileLevel.none')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="BASIC">
            Basic
            <Tooltip
              :title="t('mock.detail.basicInfo.logFileLevel.basic')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="HEADERS">
            Headers
            <Tooltip
              :title="t('mock.detail.basicInfo.logFileLevel.headers')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
          <Radio value="FULL">
            Full
            <Tooltip
              :title="t('mock.detail.basicInfo.logFileLevel.full')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '400px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer -mt-0.5" />
            </Tooltip>
          </Radio>
        </RadioGroup>
      </FormItem>
      <FormItem name="sendRequestLog" class="w-150">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('mock.detail.basicInfo.sendRequestLog') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.sendRequestLogTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="setting.sendRequestLog"
          @change="(value: boolean) => settingFlagChange('sendRequestLog', value)" />
      </FormItem>
      <FormItem name="maxContentLength" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.maxContentLength') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.maxContentLengthTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '600px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.maxContentLength"
          :disabled="!editMaxContentLength"
          :max="1000*1024*1024"
          dataType="number">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editMaxContentLength">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('maxContentLength', 'cancel', 'settingForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('maxContentLength', 'save', 'settingForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('maxContentLength','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="workPushbackThreadNum" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.pushbackThreadNum') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.pushbackThreadNumTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.workPushbackThreadNum"
          :disabled="!editWorkPushbackThreadNum"
          :max="10000"
          dataType="number">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editWorkPushbackThreadNum">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('workPushbackThreadNum', 'cancel', 'settingForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('workPushbackThreadNum', 'save', 'settingForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('workPushbackThreadNum','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="maxPushbackConnectTimeout" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.pushbackConnectTimeout') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.pushbackConnectTimeoutTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.maxPushbackConnectTimeout"
          :disabled="!editMaxPushbackConnectTimeout"
          :max="2147483647"
          dataType="number"
          includes="-">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editMaxPushbackConnectTimeout">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('maxPushbackConnectTimeout', 'cancel', 'settingForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('maxPushbackConnectTimeout', 'save', 'settingForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('maxPushbackConnectTimeout','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
      <FormItem name="maxPushbackRequestTimeout" class="w-150">
        <template #label>
          <div class="flex items-center">
            <span>{{ t('mock.detail.basicInfo.pushbackRequestTimeout') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.pushbackRequestTimeoutTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Input
          v-model:value="setting.maxPushbackRequestTimeout"
          :disabled="!editMaxPushbackRequestTimeout"
          :max="2147483647"
          dataType="number"
          includes="-">
          <template v-if="hasEditAuth" #suffix>
            <template v-if="editMaxPushbackRequestTimeout">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('maxPushbackRequestTimeout', 'cancel', 'settingForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('maxPushbackRequestTimeout', 'save', 'settingForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('maxPushbackRequestTimeout','open','settingForm')" />
            </template>
          </template>
        </Input>
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3.5 flex-none font-semibold">{{ t('mock.detail.basicInfo.security') }}</span>
        <Hints class="mt-2.75 ml-2" :text="t('mock.detail.basicInfo.securityHints')" />
      </div>
    </template>
    <Form
      ref="securityFormRef"
      :model="securityFormState"
      :labelCol="{ style: { width: '200px', fontWeight: 'bold' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem class="w-150">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('mock.detail.basicInfo.security') }}</span>
            <Tooltip
              :title="t('mock.detail.basicInfo.securityTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="isOpenSurety"
          @change="(value: boolean) => apiSuretyChange(value)" />
      </FormItem>
      <div v-if="isOpenSurety" class="flex flex-col items-end ml-10 w-150">
        <div
          v-for="(item,index) in securityFormState.apisSecurity"
          :key="index"
          style="width: 460px;"
          class="flex relative space-x-1 flex-1">
          <FormItem
            class="flex-1"
            :name="['apisSecurity', index, 'keyName']"
            :rules="{required: true, validator: keNameValidator}">
            <Input v-model:value="item.keyName" :placeholder="t('mock.detail.basicInfo.paramsName')" />
          </FormItem>
          <FormItem class="w-20">
            <Select
              v-model:value="item.in"
              :options="inOptions"
              size="small"
              :placeholder="t('mock.detail.basicInfo.in')" />
          </FormItem>
          <FormItem
            class="flex-1"
            :name="['apisSecurity', index, 'value']"
            :rules="{required: true, message: t('mock.detail.basicInfo.paramsValueRule')}">
            <Input v-model:value="item.value" :placeholder="t('mock.detail.basicInfo.paramsValue')" />
          </FormItem>
          <div
            class="flex items-center text-4 absolute top-1.5"
            :class="securityFormState.apisSecurity.length-1 === index && securityFormState.apisSecurity.length !== 10?'-right-11':'-right-7'">
            <Icon
              icon="icon-jianshao"
              class="cursor-pointer mr-1"
              @click="delApisSecurityItem(index)" />
            <template v-if="securityFormState.apisSecurity.length-1 === index && securityFormState.apisSecurity.length !== 10">
              <Icon
                icon="icon-tianjia"
                class="cursor-pointer"
                @click="addApisSecurityItem" />
            </template>
          </div>
        </div>
      </div>
      <FormItem v-if="isOpenSurety" label=" ">
        <Button
          size="small"
          :disabled="!hasEditAuth"
          @click="saveSureTy">
          {{ t('actions.save') }}
        </Button>
      </FormItem>
    </Form>
  </Card>
  <Card class="mt-2">
    <template #title>
      <div class="flex items-start">
        <span class="text-3.5 flex-none font-semibold">{{ t('mock.detail.cors.title') }}</span>
        <Hints class="mt-2.75 ml-2" :text="t('common.description')" />
      </div>
    </template>
    <Form
      ref="apisCorsFormRef"
      :model="apisCors"
      :labelCol="{ style: { width: '200px', fontWeight: 'bold' } }"
      :colon="false"
      size="small"
      layout="horizontal">
      <FormItem name="enabled" class="w-150">
        <template #label>
          <div class="flex items-center" @click.prevent>
            <span>{{ t('status.enabled') }}</span>
            <Tooltip
              :title="t('mock.detail.cors.enabledTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{'max-width': '1000px'}">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
            </Tooltip>
          </div>
        </template>
        <Switch
          size="small"
          class="w-8"
          :disabled="!hasEditAuth || loading"
          :checked="apisCors.enabled"
          @change="apisCorsEnabledFlagChange" />
      </FormItem>
      <div
        :class="apisCors.enabled ? 'open-api-cors' : 'stop-api-cors'"
        class="transition-height overflow-hidden -mt-1.5"
        style="transition-duration: 400ms;">
        <FormItem name="allowCorsOrigin" class="w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.detail.cors.allowOrigin') }}</span>
              <Tooltip
                :title="t('mock.detail.cors.allowOriginTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input
            v-model:value="apisCors.allowCorsOrigin"
            :disabled="!editOrigin">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editOrigin">
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowCorsOrigin', 'cancel', 'apisCorsForm')"> {{ t('actions.cancel') }}</a>
                <Divider type="vertical" />
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowCorsOrigin', 'save', 'apisCorsForm')"> {{ t('actions.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  @click="handleEdit('allowCorsOrigin','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
        <FormItem name="allowCorsCredentials" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.detail.cors.allowCredentials') }}</span>
              <Tooltip
                :title="t('mock.detail.cors.allowCredentialsTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Select
            v-model:value="apisCors.allowCorsCredentials"
            :class="editCredentials?'api-cors-allow-cors-credentials-edit':'api-cors-allow-cors-credentials'"
            :options="corsCookieOptions"
            :disabled="!editCredentials" />
          <div class="absolute right-2 top-0.75">
            <template v-if="editCredentials">
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('allowCorsCredentials', 'cancel', 'apisCorsForm')"> {{ t('actions.cancel') }}</a>
              <Divider type="vertical" />
              <a
                class="text-text-link text-3 leading-3"
                @click="handleEdit('allowCorsCredentials', 'save', 'apisCorsForm')"> {{ t('actions.confirm') }}</a>
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                @click="handleEdit('allowCorsCredentials','open','apisCorsForm')" />
            </template>
          </div>
        </FormItem>
        <FormItem name="allowCorsRequestMethods" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.detail.cors.allowRequestMethods') }}</span>
              <Tooltip
                :title="t('mock.detail.cors.allowRequestMethodsTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input
            v-model:value="apisCors.allowCorsRequestMethods"
            enumKey="HttpMethod"
            :disabled="!editRequestMethods"
            :class="editRequestMethods?'api-cors-allow-cors-request-methods-edit':'api-cors-allow-cors-request-methods'">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editRequestMethods">
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowCorsRequestMethods', 'cancel', 'apisCorsForm')"> {{ t('actions.cancel') }}</a>
                <Divider type="vertical" />
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowCorsRequestMethods', 'save', 'apisCorsForm')"> {{ t('actions.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  @click="handleEdit('allowCorsRequestMethods','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
        <FormItem name="allowCorsRequestHeaders" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.detail.cors.allowRequestHeaders') }}</span>
              <Tooltip
                :title="t('mock.detail.cors.allowRequestHeadersTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input v-model:value="apisCors.allowCorsRequestHeaders" :disabled="!editRequestHeaders">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editRequestHeaders">
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowCorsRequestHeaders', 'cancel', 'apisCorsForm')"> {{ t('actions.cancel') }}</a>
                <Divider type="vertical" />
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowCorsRequestHeaders', 'save', 'apisCorsForm')"> {{ t('actions.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  @click="handleEdit('allowCorsRequestHeaders','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
        <FormItem name="allowExposeHeaders" class="relative w-150">
          <template #label>
            <div class="flex items-center">
              <span>{{ t('mock.detail.cors.allowExposeHeaders') }}</span>
              <Tooltip
                :title="t('mock.detail.cors.allowExposeHeadersTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '1000px'}">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </div>
          </template>
          <Input v-model:value="apisCors.allowExposeHeaders" :disabled="!editExposeHeaders">
            <template v-if="hasEditAuth" #suffix>
              <template v-if="editExposeHeaders">
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowExposeHeaders', 'cancel', 'apisCorsForm')"> {{ t('actions.cancel') }}</a>
                <Divider type="vertical" />
                <a
                  class="text-text-link text-3 leading-3"
                  @click="handleEdit('allowExposeHeaders', 'save', 'apisCorsForm')"> {{ t('actions.confirm') }}</a>
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  @click="handleEdit('allowExposeHeaders','open','apisCorsForm')" />
              </template>
            </template>
          </Input>
        </FormItem>
      </div>
    </Form>
  </Card>
</template>
<style scoped>
:deep(.ant-form-item-label > label::after) {
  margin: 0 8px 0 4px;
}

:deep(.ant-divider-vertical) {
  top: 0;
  margin: 0 6px 0 4px;
}

.open-api-cors {
  height: 240px;
}

.stop-api-cors {
  height: 0;
}

.api-cors-allow-cors-credentials :deep(.ant-select-arrow) {
  right: 28px;
}

.api-cors-allow-cors-credentials :deep(.ant-select-selector) {
  padding-right: 32px;
}

.api-cors-allow-cors-credentials-edit :deep(.ant-select-arrow) {
  right: 72px;
}

.api-cors-allow-cors-credentials-edit :deep(.ant-select-selector) {
  padding-right: 86px;
}
</style>
