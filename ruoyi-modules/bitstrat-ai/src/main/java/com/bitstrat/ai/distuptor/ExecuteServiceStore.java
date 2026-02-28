package com.bitstrat.ai.distuptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteServiceStore {
    public final static ExecutorService DisruptorExecutorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
    public ExecuteServiceStore() {

    }
}
