<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Popover } from 'ant-design-vue';
import { Icon, Hints, Grid } from '@xcan-angus/vue-ui';
import beautify from 'js-beautify';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Format XML string with indentation
 * Uses js-beautify to create readable XML output
 * 
 * @param data - Raw XML string
 * @returns Prettified XML string
 */
const prettyXml = (data: string): string => {
  return beautify.html(data, {
    unformatted: ['code', 'pre', 'em', 'strong', 'span'],  // Don't format these tags
    indent_inner_html: true,                                // Indent inner HTML elements
    indent_char: ' ',                                       // Use spaces for indentation
    indent_size: 2                                          // 2 spaces per indent level
  });
};

/**
 * Match item examples for different expression types
 * Each example includes sample data, expression, result, and explanation
 */
const matchItemList = [
  // Regular expression matching example
  {
    key: 'regexp',
    name: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.regexpMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.expression')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchItem')
      }
    ]],
    data: {
      data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
      expression: '(1\\d{10})',  // Pattern to match 11-digit phone numbers starting with 1
      result: '["1888888888813999999999","18888888888","13999999999"]',  // Array of matches
      item: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.regexpItemDescription')
    }
  },
  
  // JSON path matching example
  {
    key: 'jsonpath',
    name: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.jsonpathMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.expression')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchItem')
      }
    ]],
    data: {
      data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
      expression: '$.store.book[*]',  // JSON path to select all books
      result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
      item: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.jsonpathItemDescription')
    }
  },
  
  // XPath matching example
  {
    key: 'xpath',
    name: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.xpathMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.expression')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchItem')
      }
    ]],
    data: {
      // Sample XML with person data
      data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
                '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
      expression: '/persons/person[age >= 30]',  // XPath to select persons with age >= 30
      result: '["30codingbasketballAngus1", "32codingAngus2"]',  // Array of matched results
      item: t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.xpathItemDescription')
    }
  }
];
</script>

<template>
  <!-- Help popover triggered by info icon click -->
  <Popover
    destroyTooltipOnHide
    placement="left"
    :trigger="['click']">
    <!-- Trigger icon: info/help icon -->
    <div class="flex-shrink-0 flex items-center cursor-pointer w-4 h-7">
      <Icon 
        icon="icon-jieshaoshuoming" 
        class="text-4 text-theme-text-hover" />
    </div>
    
    <!-- Popover content: help documentation -->
    <template #content>
      <div 
        style="max-height: 60vh; overflow: auto;" 
        class="w-196 leading-4.5 space-y-5 text-3 text-theme-content">
        <!-- Section 1: Expected value usage -->
        <div>
          <Hints 
            :text="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.title')" 
            class="mb-2 !font-semibold !text-theme-content" />
          <div class="mb-3">{{ t('common.description') }}</div>
          
          <!-- Expected value scenarios -->
          <ol class="pl-4 space-y-3">
            <!-- Scenario 1: Empty expected value -->
            <li style="list-style-type: circle;" class="space-y-2">
              <div class="font-semibold">
                {{ t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.emptyExpected') }}
              </div>
              <div class="text-theme-sub-content">
                {{ t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.emptyExpectedDescription') }}
              </div>
            </li>
            
            <!-- Scenario 2: Non-empty expected value -->
            <li style="list-style-type: circle;" class="space-y-2">
              <div class="font-semibold">
                {{ t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.nonEmptyExpected') }}
              </div>
              <div class="text-theme-sub-content">
                {{ t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.nonEmptyExpectedDescription') }}
              </div>
            </li>
          </ol>
        </div>

        <!-- Section 2: Match item examples -->
        <div>
          <Hints 
            :text="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchItemTitle')" 
            class="mb-2 !font-semibold !text-theme-content" />
          <div class="mb-3">
            {{ t('httpPlugin.uiConfig.httpConfigs.assertionForm.expectedPopover.matchItemDescription') }}
          </div>
          
          <!-- Match item examples (regex, JSON path, XPath) -->
          <ol class="pl-4 space-y-3">
            <li
              v-for="item in matchItemList"
              :key="item.key"
              style="list-style-type: circle;"
              class="space-y-2">
              <!-- Example type name -->
              <div class="font-semibold">{{ item.name }}</div>
              
              <!-- Example data grid -->
              <Grid
                labelStyle="color: var(--content-text-sub-content);"
                valueStyle="color: var(--content-text-sub-content);"
                labelSpacing="4px"
                :marginBottom="8"
                :columns="item.columns"
                :dataSource="item.data">
                <!-- Format data field as code block -->
                <template #data="{ text }">
                  <pre><code>{{ text }}</code></pre>
                </template>
                
                <!-- Format expression field as code block -->
                <template #expression="{ text }">
                  <pre><code>{{ text }}</code></pre>
                </template>
                
                <!-- Format result field as code block -->
                <template #result="{ text }">
                  <pre><code>{{ text }}</code></pre>
                </template>
              </Grid>
            </li>
          </ol>
        </div>
      </div>
    </template>
  </Popover>
</template>

<style scoped>
/**
 * Code block styling for example snippets
 * - Font size: 13px for readability
 * - Line height: 18px for comfortable reading
 * - Word wrapping enabled to prevent horizontal overflow
 * - Break long words to fit container
 * - Preserve whitespace and line breaks
 */
pre code {
  font-size: 13px;
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
