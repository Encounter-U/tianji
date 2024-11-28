package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 互动提问的问题表(tj_learning.interaction_question)表控制层
 *
 * @author Encounter
 * @date 2024-11-26
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "互动提问用户端")
public class InteractionQuestionController
    {
        private final IInteractionQuestionService questionService;
        
        /**
         * 保存问题 启用验证
         *
         * @param questionFormDTO 问题表 DTO
         */
        @PostMapping
        @Operation(summary = "新增问题")
        public void saveQuestion(@Valid @RequestBody QuestionFormDTO questionFormDTO)
            {
                questionService.saveQuestion(questionFormDTO);
            }
        
        /**
         * 更新问题 不启用验证
         *
         * @param questionFormDTO 问题表 DTO
         * @param id              id
         */
        @PutMapping("/{id}")
        @Operation(summary = "更新问题")
        public void updateQuestion(@RequestBody QuestionFormDTO questionFormDTO, @PathVariable Long id)
            {
                questionService.updateQuestion(questionFormDTO, id);
            }
        
        /**
         * 分页查询问题
         *
         * @param questionPageQuery 问题页面查询
         * @return {@link PageDTO }<{@link QuestionVO }>
         */
        @GetMapping("/page")
        @Operation(summary = "分页查询问题")
        public PageDTO<QuestionVO> pageQuestion(QuestionPageQuery questionPageQuery)
            {
                return questionService.pageQuestion(questionPageQuery);
            }
        
        /**
         * 根据id查询问题详情
         *
         * @param id 问题id
         * @return {@link QuestionVO } 问题详情
         */
        @GetMapping("/{id}")
        @Operation(summary = "根据id查询问题")
        public QuestionVO getQuestionById(@PathVariable Long id)
            {
                return questionService.getQuestionById(id);
            }
        
        /**
         * 删除由id
         *
         * @param id id
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "根据id删除问题")
        public void deleteById(@PathVariable Long id)
            {
                questionService.deleteById(id);
            }
    }
