package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;

import javax.validation.Valid;

public interface IInteractionQuestionService extends IService<InteractionQuestion>
    {
        
        /**
         * 保存问题
         *
         * @param questionFormDTO 问题表 DTO
         */
        void saveQuestion(@Valid QuestionFormDTO questionFormDTO);
        
        /**
         * 更新问题
         *
         * @param questionFormDTO 问题表 DTO
         * @param id              id
         */
        void updateQuestion(QuestionFormDTO questionFormDTO, Long id);
        
        /**
         * 分页查询问题
         *
         * @param questionPageQuery 问题页面查询
         * @return {@link PageDTO }<{@link QuestionVO }>
         */
        PageDTO<QuestionVO> pageQuestion(QuestionPageQuery questionPageQuery);
        
        /**
         * 根据id获取问题详情
         *
         * @param id 问题id
         * @return {@link QuestionVO } 问题详情
         */
        QuestionVO getQuestionById(Long id);
        
        /**
         * 删除由id
         *
         * @param id id
         */
        void deleteById(Long id);
        
        /**
         * 管理员分页查询Question
         *
         * @param adminPageQuery 管理员分页查询条件
         * @return {@link PageDTO }<{@link QuestionAdminVO }> 问题列表
         */
        PageDTO<QuestionAdminVO> adminPage(QuestionAdminPageQuery adminPageQuery);
        
        /**
         * 管理端隐藏或显示问题
         *
         * @param id     问题id
         * @param hidden 是否隐藏
         */
        void hidden(Long id, Boolean hidden);
        
        /**
         * 管理获取由id
         *
         * @param id 问题id
         * @return {@link QuestionAdminVO }
         */
        QuestionAdminVO adminGetById(Long id);
    }
