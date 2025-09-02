import { useI18n } from 'vue-i18n';
import beautify from 'js-beautify';

/**
 * Composable for managing match item popover logic
 * Provides data and formatting functions for match item examples
 */
export function useMatchItemPopover () {
  const { t } = useI18n();

  /**
   * Format XML data for better readability
   * @param data - XML string to format
   * @returns Formatted XML string
   */
  const prettyXml = (data: string) => {
    return beautify.html(data, {
      unformatted: ['code', 'pre', 'em', 'strong', 'span'],
      indent_inner_html: true,
      indent_char: ' ',
      indent_size: 2
    });
  };

  /**
   * Match item examples data
   * Contains examples for different extraction methods
   */
  const matchItemList = [
    {
      key: 'regexp',
      name: t('dataVariable.detail.matchItemPopover.regexp.name'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('dataVariable.detail.matchItemPopover.regexp.columns.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataVariable.detail.matchItemPopover.regexp.columns.regexp')
        },
        {
          dataIndex: 'result',
          label: t('dataVariable.detail.matchItemPopover.regexp.columns.group')
        },
        {
          dataIndex: 'item',
          label: t('dataVariable.detail.matchItemPopover.regexp.item')
        }
      ]],
      data: {
        data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
        expression: '(1\\d{10})',
        result: '["18888888888","13999999999"]',
        item: '不指定位置默认取合并结果合并匹配值："1888888888813999999999"，指定位置0取值："18888888888"，指定位置1取值："13999999999"。'
      }
    },
    {
      key: 'jsonpath',
      name: t('dataVariable.detail.matchItemPopover.jsonpath.name'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('dataVariable.detail.matchItemPopover.jsonpath.columns.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataVariable.detail.matchItemPopover.jsonpath.columns.jsonpath')
        },
        {
          dataIndex: 'result',
          label: t('dataVariable.detail.matchItemPopover.jsonpath.columns.result')
        },
        {
          dataIndex: 'item',
          label: t('dataVariable.detail.matchItemPopover.jsonpath.item')
        }
      ]],
      data: {
        data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
        expression: '$.store.book[*]',
        result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
        item: '不指定位置默认取合并结果：[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]，指定位置0取值：{"title":"Sayings of the Century","price":100}，指定位置1取值：{"title":"confucianism","price":200}。'
      }
    },
    {
      key: 'xpath',
      name: t('dataVariable.detail.matchItemPopover.xpath.name'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('dataVariable.detail.matchItemPopover.xpath.columns.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataVariable.detail.matchItemPopover.xpath.columns.xpath')
        },
        {
          dataIndex: 'result',
          label: t('dataVariable.detail.matchItemPopover.xpath.columns.result')
        },
        {
          dataIndex: 'item',
          label: t('dataVariable.detail.matchItemPopover.xpath.item')
        }
      ]],
      data: {
        data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
          '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
        expression: '/persons/person[age >= 30]',
        result: '["30codingbasketballAngus1", "32codingAngus2"]',
        item: '不指定位置默认取合并结果："30codingbasketballAngus132codingAngus2"，指定位置0取值："30codingbasketballAngus1"，指定位置1取值："30codingbasketballAngus2"。'
      }
    }
  ];

  return {
    matchItemList
  };
}
