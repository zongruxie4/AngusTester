import uitlAssert from '@/components/ApiAssert/utils/assert';

import uitlExpression from '@/components/ApiAssert/utils/expression';
import extract from '@/components/ApiAssert/utils/extract';
import uitlJsonpath from '@/components/ApiAssert/utils/jsonpath';
import uitlProxy from '@/components/ApiAssert/utils/proxy';
import uitlRegexp from '@/components/ApiAssert/utils/regexp';
import uitlXpath from '@/components/ApiAssert/utils/xpath';

export const assertUtil = {
  assert: uitlAssert,
  uitlExpression,
  extract,
  jsonpath: uitlJsonpath,
  proxy: uitlProxy,
  regexp: uitlRegexp,
  xpath: uitlXpath
};

export default {
  assert: uitlAssert,
  uitlExpression,
  extract,
  jsonpath: uitlJsonpath,
  proxy: uitlProxy,
  regexp: uitlRegexp,
  xpath: uitlXpath
};
