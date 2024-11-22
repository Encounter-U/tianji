package com.tianji.learning.utils;

import lombok.Data;

import java.time.Duration;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Encounter
 * @date 2024/11/22 13:42<br/>
 */
@Data
public class DelayTask<D> implements Delayed
    {
        private D data;
        private long deadlineNanos;
        
        public DelayTask(D data, Duration delayTime)
            {
                this.data = data;
                // 计算截止时间 = 当前时间 + 延迟时间
                this.deadlineNanos = System.nanoTime() + delayTime.toNanos();
            }
        
        /**
         * 距离截止时间的剩余时间
         *
         * @param unit 时间单位
         * @return long 剩余时间
         */
        @Override
        public long getDelay(TimeUnit unit)
            {
                return unit.convert(Math.max(0, deadlineNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
            }
        
        /**
         * 比较对象
         *
         * @param o 对象
         * @return int 比较结果 0：相等，1：大于，-1：小于
         */
        @Override
        public int compareTo(Delayed o)
            {
                // 比较剩余时间
                long l = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
                if (l > 0)
                    return 1;
                else if (l < 0)
                    return -1;
                return 0;
            }
    }
