package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 互动问题的回答或评论
 */
@Schema(description = "互动问题的回答或评论")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "interaction_reply")
public class InteractionReply implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 互动问题的回答id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "互动问题的回答id")
        private Long id;
        /**
         * 互动问题问题id
         */
        @TableField(value = "question_id")
        @Schema(description = "互动问题问题id")
        private Long questionId;
        /**
         * 回复的上级回答id
         */
        @TableField(value = "answer_id")
        @Schema(description = "回复的上级回答id")
        private Long answerId;
        /**
         * 回答者id
         */
        @TableField(value = "user_id")
        @Schema(description = "回答者id")
        private Long userId;
        /**
         * 回答内容
         */
        @TableField(value = "content")
        @Schema(description = "回答内容")
        private String content;
        /**
         * 回复的目标用户id
         */
        @TableField(value = "target_user_id")
        @Schema(description = "回复的目标用户id")
        private Long targetUserId;
        /**
         * 回复的目标回复id
         */
        @TableField(value = "target_reply_id")
        @Schema(description = "回复的目标回复id")
        private Long targetReplyId;
        /**
         * 评论数量
         */
        @TableField(value = "reply_times")
        @Schema(description = "评论数量")
        private Integer replyTimes;
        /**
         * 点赞数量
         */
        @TableField(value = "liked_times")
        @Schema(description = "点赞数量")
        private Integer likedTimes;
        /**
         * 是否被隐藏，默认false
         */
        @TableField(value = "hidden")
        @Schema(description = "是否被隐藏，默认false")
        private Boolean hidden;
        /**
         * 是否匿名，默认false
         */
        @TableField(value = "anonymity")
        @Schema(description = "是否匿名，默认false")
        private Boolean anonymity;
        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
        /**
         * 更新时间
         */
        @TableField(value = "update_time")
        @Schema(description = "更新时间")
        private LocalDateTime updateTime;
    }