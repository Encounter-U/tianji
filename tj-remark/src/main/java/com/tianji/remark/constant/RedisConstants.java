package com.tianji.remark.constant;

/**
 * @author Encounter
 * @date 2024/11/29 17:16<br/>
 */
public interface RedisConstants
    {
        //给业务点赞的用户集合的KEY前缀，后缀是业务id
        String LIKE_BIZ_KEY_PREFIX = "likes:set:biz:";
        //业务点赞数统计的KEY前缀，后缀是业务类型
        String LIKES_TIMES_KEY_PREFIX = "likes:times:type:";
    }
