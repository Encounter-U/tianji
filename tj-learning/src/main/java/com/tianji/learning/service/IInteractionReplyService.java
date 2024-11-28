package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;

public interface IInteractionReplyService extends IService<InteractionReply>
    {
        
        /**
         * 新增回答或评论
         *
         * @param replyDTO 回复 DTO
         */
        void addReply(ReplyDTO replyDTO);
        
        /**
         * 页面回复
         *
         * @param query 查询
         * @return {@link PageDTO }<{@link ReplyVO }>
         */
        PageDTO<ReplyVO> pageReply(ReplyPageQuery query, boolean isAdmin);
        
        /**
         * 根据显示或隐藏回复
         *
         * @param id     ReplyId
         * @param hidden 是否隐藏
         */
        void hiddenReply(Long id, Boolean hidden);
    }
