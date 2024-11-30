package com.tianji.remark.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞记录表
 */
@Schema(description = "点赞记录表")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "liked_record")
public class LikedRecord implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 主键id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "主键id")
        private Long id;
        /**
         * 用户id
         */
        @TableField(value = "user_id")
        @Schema(description = "用户id")
        private Long userId;
        /**
         * 点赞的业务id
         */
        @TableField(value = "biz_id")
        @Schema(description = "点赞的业务id")
        private Long bizId;
        /**
         * 点赞的业务类型
         */
        @TableField(value = "biz_type")
        @Schema(description = "点赞的业务类型")
        private String bizType;
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