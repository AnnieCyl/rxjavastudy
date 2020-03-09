package com.annie.study.rxjavastudy;

import org.junit.Test;

import static org.junit.Assert.*;

public class SettlementTest {

    private Settlement settlement = new Settlement();

    @Test
    public void settleAndPrinting() throws InterruptedException {
        settlement.settleAndPrinting()
                .test()
                .await();
    }
}