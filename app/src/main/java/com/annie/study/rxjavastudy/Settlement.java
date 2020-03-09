package com.annie.study.rxjavastudy;

import io.reactivex.Completable;

public class Settlement {
    private String settlementInfo;

    // 结算接口，完成后修改结算结果 settlementInfo。
    private Completable settle() {
        return Completable.fromAction(() -> settlementInfo = "统计信息");
    }

    // 结算打印接口，根据结算结果进行打印
    private Completable print(String settlementInfo) {
        return Completable.fromAction(() -> {
            System.out.println(settlementInfo);
        });
    }

    // 批上送接口
    private Completable upload() {
        return Completable.complete();
    }

    public String getSettlementInfo() {
        return settlementInfo;
    }

    // 对外暴露的接口，结算并且打印。
    public Completable settleAndPrinting() {
        // 联机结算
        // 批上送
        return settle().andThen(upload())
                // 将 Completable 转化为 Single，值为 getSettlementInfo
                .toSingle(()->getSettlementInfo())
                // 打印结算结果
                .flatMapCompletable(settlementInfo -> print(settlementInfo));
    }
}
