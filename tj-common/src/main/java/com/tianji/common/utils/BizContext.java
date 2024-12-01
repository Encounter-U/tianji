package com.tianji.common.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务类型
 *
 * @author Encounter
 * @date 2024/11/30 18:04<br/>
 */
public class BizContext
    {
        private static final ConcurrentHashMap<Long, String> BIZ_MAP = new ConcurrentHashMap<>();
        
        /**
         * 添加业务
         *
         * @param bizId   bizId
         * @param bizType 业务类型
         */
        public static void addBiz(Long bizId, String bizType)
            {
                BIZ_MAP.put(bizId, bizType);
            }
        
        /**
         * 获取业务
         *
         * @return 业务id
         */
        public static ConcurrentHashMap<Long, String> getBiz()
            {
                return BIZ_MAP;
            }
        
        /**
         * 清除所有业务
         */
        public static void clearAllBiz()
            {
                BIZ_MAP.clear();
            }
        
        /**
         * 根据业务id删除业务信息
         */
        public static void removeBiz(String bizId)
            {
                BIZ_MAP.remove(bizId);
            }
    }
