export interface CountObj{
  pendingReviewNum: number | string;
  failedReviewNum: number | string;
  passedReviewNum: number | string;
  totalReviewNum: number | string;
  totalTestNum: number | string;
  passedTestNum: number | string;
  notPassedTestNum: number | string;
  canceledTestNum: number | string;
  blockedTestNum: number | string;
}
