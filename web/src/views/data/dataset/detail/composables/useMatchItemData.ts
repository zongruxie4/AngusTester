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
      name: t('dataCommon.matchItemPopover.regexp.title'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('common.data')
        },
        {
          dataIndex: 'expression',
          label: t('common.expression')
        },
        {
          dataIndex: 'result',
          label: t('common.result')
        },
        {
          dataIndex: 'item',
          label: t('common.item')
        }
      ]],
      data: {
        data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
        expression: '(1\\d{10})',
        result: '["18888888888","13999999999"]',
        item: t('dataCommon.matchItemPopover.regexp.item')
      }
    },
    {
      key: 'jsonpath',
      name: t('dataCommon.matchItemPopover.jsonpath.title'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('common.data')
        },
        {
          dataIndex: 'expression',
          label: t('common.expression')
        },
        {
          dataIndex: 'result',
          label: t('common.result')
        },
        {
          dataIndex: 'item',
          label: t('common.item')
        }
      ]],
      data: {
        data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
        expression: '$.store.book[*]',
        result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
        item: t('dataCommon.matchItemPopover.jsonpath.item')
      }
    },
    {
      key: 'xpath',
      name: t('dataCommon.matchItemPopover.xpath.title'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('common.data')
        },
        {
          dataIndex: 'expression',
          label: t('common.expression')
        },
        {
          dataIndex: 'result',
          label: t('common.result')
        },
        {
          dataIndex: 'item',
          label: t('common.item')
        }
      ]],
      data: {
        data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
          '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
        expression: '/persons/person[age >= 30]',
        result: '["30codingbasketballAngus1", "32codingAngus2"]',
        item: t('dataCommon.matchItemPopover.xpath.item')
      }
    }
  ];

  return {
    matchItemList
  };
}
