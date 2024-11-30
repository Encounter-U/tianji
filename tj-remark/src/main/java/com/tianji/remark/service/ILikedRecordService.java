package com.tianji.remark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.po.LikedRecord;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * @author Encounter
 * @date 2024/11/29 12:15 <br>
 */
public interface ILikedRecordService extends IService<LikedRecord>
    {
        
        /**
         * 添加 Like Record
         *
         * @param likeRecordFormDTO LIKE 记录表 DTO
         */
        void addLikeRecord(@Valid LikeRecordFormDTO likeRecordFormDTO);
        
        /**
         * 赞过列表
         *
         * @param bizIds 业务 ID
         * @return {@link Set }<{@link Long }>
         */
        Set<Long> likedList(List<Long> bizIds);
        
        /**
         * 查询点赞数并发送消息
         *
         * @param bizType    业务类型
         * @param maxBizSize 最大业务规模
         */
        void readLikedTimesAndSendMessage(String bizType, int maxBizSize);
    }
