package com.bitstrat.utils;

import java.util.ArrayList;
import java.util.List;

public class RatioOperationBuilder {

    /**
     * 计算每次需要处理的数量，支持 double 类型的比例
     *
     * @param ratio 每次标准处理比例（如 12.5）
     * @return 每次实际要处理的数量列表（最后一项可能大于 ratio，以补齐总量）
     */
    public static List<Double> calculateOperations(double ratio) {
        if (ratio <= 0 || ratio > 100) {
            throw new IllegalArgumentException("比例必须在 1 到 100 之间");
        }

        double total = 100.0;
        List<Double> result = new ArrayList<>();

        double sum = 0.0;

        // 累加直到下一次就会超过 100
        while (sum + ratio < total) {
            result.add(ratio);
            sum += ratio;
        }

        // 最后一次补齐剩下的部分（可能比 ratio 小，也可能恰好等于）
        double last = total - sum;
        if (last > 0 && last == ratio) {
            result.add(last);
        }

        return result;
    }

    public static void main(String[] args) {
        double ratio = 15;
        List<Double> ops = calculateOperations(ratio);

        System.out.println("操作次数：" + ops.size());
        double totalSum = 0;
        for (int i = 0; i < ops.size(); i++) {
            System.out.printf("第 %d 次处理：%.4f%%\n", i + 1, ops.get(i));
            totalSum += ops.get(i);
        }
        System.out.printf("总和：%.4f%%\n", totalSum);
    }
}
