package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Encounter
 * @date 2024/11/27 15:18<br/>
 */
@RestController
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
@Tag(name = "互动提问管理端")
public class InteractionQuestionAdminController
    {
        private final IInteractionQuestionService questionService;
        
        /**
         * 分页查询问题
         *
         * @param adminPageQuery 管理员页面查询
         * @return {@link PageDTO }<{@link QuestionAdminVO }> 问题列表
         */
        @GetMapping("/page")
        @Operation(summary = "管理端分页查询互动问题")
        public PageDTO<QuestionAdminVO> page(QuestionAdminPageQuery adminPageQuery)
            {
                return questionService.adminPage(adminPageQuery);
            }
        
        /**
         * 管理端隐藏或显示问题
         *
         * @param id     问题id
         * @param hidden 隐藏
         */
        @PutMapping("/{id}/hidden/{hidden}")
        @Operation(summary = "管理端隐藏或显示问题")
        public void hidden(@PathVariable Long id, @PathVariable Boolean hidden)
            {
                questionService.hidden(id, hidden);
            }
        
        /**
         * 获取由id
         *
         * @param id 问题id
         * @return {@link QuestionAdminVO }
         */
        @GetMapping("/{id}")
        @Operation(summary = "管理端获取问题详情")
        public QuestionAdminVO getById(@PathVariable Long id)
            {
                return questionService.adminGetById(id);
            }
    }
