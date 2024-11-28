package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 互动问题的回答或评论(tj_learning.interaction_reply)表控制层
 *
 * @author Encounter
 * @date 2024-11-26
 */
@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
@Tag(name = "评论相关接口")
public class InteractionReplyController
    {
        private final IInteractionReplyService replyService;
        
        /**
         * 新增回答或评论
         *
         * @param replyDTO 回复 DTO
         */
        @PostMapping
        @Operation(summary = "新增回答或评论")
        public void addReply(@RequestBody ReplyDTO replyDTO)
            {
                replyService.addReply(replyDTO);
            }
        
        /**
         * 分页查询回答或评论
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link ReplyVO }>
         */
        @GetMapping("/page")
        @Operation(summary = "分页查询回答或评论")
        public PageDTO<ReplyVO> pageReply(ReplyPageQuery query)
            {
                return replyService.pageReply(query, false);
            }
    }
