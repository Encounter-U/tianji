package com.tianji.remark.controller;

import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.service.ILikedRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 点赞记录表(tj_remark.liked_record)表控制层
 *
 * @author Encounter
 * @date 2024/11/28 23:06 <br>
 */
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "点赞业务相关接口")
public class LikedRecordController
    {
        //服务对象
        private final ILikedRecordService ILikedRecordService;
        
        /**
         * 添加 Like Record
         *
         * @param likeRecordFormDTO 点赞记录表 DTO
         */
        @PostMapping
        @Operation(summary = "点赞或取消点赞")
        public void addLikeRecord(@Valid @RequestBody LikeRecordFormDTO likeRecordFormDTO)
            {
                ILikedRecordService.addLikeRecord(likeRecordFormDTO);
            }
        
        /**
         * 赞过列表
         *
         * @param bizIds 业务 ID
         * @return {@link Set }<{@link Long }>
         */
        @GetMapping("/list")
        @Operation(summary = "查询点赞状态")
        public Set<Long> likedList(@RequestParam List<Long> bizIds)
            {
                return ILikedRecordService.likedList(bizIds);
            }
    }
