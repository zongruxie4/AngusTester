export interface CountObj{
  pendingReviewNum: number | string;
  failedReviewNum: number | string;
  passedReviewNum: number | string;
  totalReviewNum: number | string;
  totalTestNum: number | string;
  passedTestNum: number | string;
  notPassedTestNum: number | string;
  ignoredTestNum: number | string;
  blockedTestNum: number | string;
}

export interface PieSetting {
  key: string;
  value: string;
  type: { message: string; value: string; }[]
}

export interface PieData {
  key: string;
  title: string;
  total: number;
  color: string [];
  legend:{value:string, message:string}[];
  data: { name: string, value: number, codes?:number }[];
}
