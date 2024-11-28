package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Encounter
 * @date 2024/11/28 18:18<br/>
 */
@RestController
@RequestMapping("/admin/replies")
@RequiredArgsConstructor
@Tag(name = "管理端评论相关接口")
public class InteractionReplyAdminController
    {
        private final IInteractionReplyService replyService;
        
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
                return replyService.pageReply(query, true);
            }
        
        /**
         * 隐藏回复
         *
         * @param id     id
         * @param hidden 隐藏
         */
        @PutMapping("/{id}/hidden/{hidden}")
        @Operation(summary = "隐藏或显示评论")
        public void hiddenReply(@PathVariable Long id, @PathVariable Boolean hidden)
            {
                replyService.hiddenReply(id, hidden);
            }
    }
