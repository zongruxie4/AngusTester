import { useI18n } from 'vue-i18n';
import beautify from 'js-beautify';

/**
 * Composable for managing match item data and formatting in dataset components
 * This composable handles the logic for displaying examples of different extraction methods
 * including regex, JSONPath, and XPath with proper formatting
 */
export function useMatchItemData () {
  const { t } = useI18n();

  /**
   * Format XML data for better readability
   * @param data - Raw XML string to format
   * @returns Formatted XML string with proper indentation
   */
  const prettyXml = (data: string): string => {
    return beautify.html(data, {
      unformatted: ['code', 'pre', 'em', 'strong', 'span'],
      indent_inner_html: true,
      indent_char: ' ',
      indent_size: 2
    });
  };

  /**
   * Collection of match item examples for different extraction methods
   * Each example includes data, expression, result, and item description
   */
  const matchItemList = [
    {
      key: 'regexp',
      name: t('dataset.detail.matchItemPopover.regexp.name'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('dataset.detail.matchItemPopover.regexp.columns.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataset.detail.matchItemPopover.regexp.columns.expression')
        },
        {
          dataIndex: 'result',
          label: t('dataset.detail.matchItemPopover.regexp.columns.result')
        },
        {
          dataIndex: 'item',
          label: t('dataset.detail.matchItemPopover.regexp.columns.item')
        }
      ]],
      data: {
        data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
        expression: '(1\\d{10})',
        result: '["18888888888","13999999999"]',
        item: t('dataset.detail.matchItemPopover.regexp.item')
      }
    },
    {
      key: 'jsonpath',
      name: t('dataset.detail.matchItemPopover.jsonpath.name'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('dataset.detail.matchItemPopover.jsonpath.columns.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataset.detail.matchItemPopover.jsonpath.columns.expression')
        },
        {
          dataIndex: 'result',
          label: t('dataset.detail.matchItemPopover.jsonpath.columns.result')
        },
        {
          dataIndex: 'item',
          label: t('dataset.detail.matchItemPopover.jsonpath.columns.item')
        }
      ]],
      data: {
        data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
        expression: '$.store.book[*]',
        result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
        item: t('dataset.detail.matchItemPopover.jsonpath.item')
      }
    },
    {
      key: 'xpath',
      name: t('dataset.detail.matchItemPopover.xpath.name'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('dataset.detail.matchItemPopover.xpath.columns.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataset.detail.matchItemPopover.xpath.columns.expression')
        },
        {
          dataIndex: 'result',
          label: t('dataset.detail.matchItemPopover.xpath.columns.result')
        },
        {
          dataIndex: 'item',
          label: t('dataset.detail.matchItemPopover.xpath.columns.item')
        }
      ]],
      data: {
        data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
          '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
        expression: '/persons/person[age >= 30]',
        result: '["30codingbasketballAngus1", "32codingAngus2"]',
        item: t('dataset.detail.matchItemPopover.xpath.item')
      }
    }
  ];

  return {
    matchItemList
  };
}
