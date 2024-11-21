package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 学习记录表
 */
@ApiModel(description = "学习记录表")
@Data
@AllArgsConstructor
@NoArgsConstructor
// 设置为false，表示不调用父类的equals和hashcode方法
@EqualsAndHashCode(callSuper = false)
// 设置为true，生成的setter方法返回this，方便链式调用
@Accessors(chain = true)
@TableName(value = "learning_record")
public class LearningRecord
    {
        /**
         * 学习记录的id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @ApiModelProperty(value = "学习记录的id")
        private Long id;
        
        /**
         * 对应课表的id
         */
        @ApiModelProperty(value = "对应课表的id")
        private Long lessonId;
        
        /**
         * 对应小节的id
         */
        @ApiModelProperty(value = "对应小节的id")
        private Long sectionId;
        
        /**
         * 用户id
         */
        @ApiModelProperty(value = "用户id")
        private Long userId;
        
        /**
         * 视频的当前观看时间点，单位秒
         */
        @ApiModelProperty(value = "视频的当前观看时间点，单位秒")
        private Integer moment;
        
        /**
         * 是否完成学习，默认false
         */
        @ApiModelProperty(value = "是否完成学习，默认false")
        private Boolean finished;
        
        /**
         * 第一次观看时间
         */
        @ApiModelProperty(value = "第一次观看时间")
        private LocalDateTime createTime;
        
        /**
         * 完成学习的时间
         */
        @ApiModelProperty(value = "完成学习的时间")
        private LocalDateTime finishTime;
        
        /**
         * 更新时间（最近一次观看时间）
         */
        @ApiModelProperty(value = "更新时间（最近一次观看时间）")
        private LocalDateTime updateTime;
    }