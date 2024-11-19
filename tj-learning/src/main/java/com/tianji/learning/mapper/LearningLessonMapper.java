package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.LearningLesson;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LearningLessonMapper extends BaseMapper<LearningLesson>
    {
        /**
         * delete by primary key
         *
         * @param id primaryKey
         * @return deleteCount
         */
        int deleteByPrimaryKey(Long id);
        
        /**
         * insert record to table
         *
         * @param record the record
         * @return insert count
         */
        int insert(LearningLesson record);
        
        /**
         * insert record to table selective
         *
         * @param record the record
         * @return insert count
         */
        int insertSelective(LearningLesson record);
        
        /**
         * select by primary key
         *
         * @param id primary key
         * @return object by primary key
         */
        LearningLesson selectByPrimaryKey(Long id);
        
        /**
         * update record selective
         *
         * @param record the updated record
         * @return update count
         */
        int updateByPrimaryKeySelective(LearningLesson record);
        
        /**
         * update record
         *
         * @param record the updated record
         * @return update count
         */
        int updateByPrimaryKey(LearningLesson record);
    }