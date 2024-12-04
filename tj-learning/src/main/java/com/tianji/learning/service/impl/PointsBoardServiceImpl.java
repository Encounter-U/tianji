package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardItemVO;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.mapper.PointsBoardMapper;
import com.tianji.learning.service.IPointsBoardService;
import com.tianji.learning.utils.TableInfoContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.learning.constants.LearningConstants.POINTS_BOARD_TABLE_PREFIX;

/**
 * @author Encounter
 * @date 2024/12/02 15:29 <br>
 */
@Service
@RequiredArgsConstructor
public class PointsBoardServiceImpl extends ServiceImpl<PointsBoardMapper, PointsBoard> implements IPointsBoardService
    {
        private final StringRedisTemplate redisTemplate;
        private final UserClient userClient;
        
        /**
         * 查询排行榜
         *
         * @param query 查询条件
         * @return {@link PointsBoardVO }
         */
        @Override
        public PointsBoardVO queryPointsBoard(PointsBoardQuery query)
            {
                //校验数据
                boolean isCurrentSeason = query.getSeason() == null || query.getSeason() == 0;
                
                //获取redis的key
                LocalDate now = LocalDate.now();
                String key = RedisConstants.POINTS_RECORD_KEY_PREFIX + now.format(DateUtils.POINTS_BOARD_SUFFIX_FORMATTER);
                
                //查询我的积分
                PointsBoard myBoard = isCurrentSeason ?
                        queryMyCurrentBoard(key) :
                        queryMyHistoryBoard(query.getSeason());
                
                //获取排行榜
                List<PointsBoard> pointsBoards = isCurrentSeason ?
                        queryCurrentSeasonPointsBoard(key, query.getPageNo(), query.getPageSize()) :
                        queryHistorySeasonPointsBoard(query);
                
                //封装数据
                PointsBoardVO vo = new PointsBoardVO();
                if (myBoard != null)
                    {
                        vo.setRank(myBoard.getRank());
                        vo.setPoints(myBoard.getPoints());
                    }
                if (CollUtils.isEmpty(pointsBoards))
                    {
                        return vo;
                    }
                
                //查询用户信息
                Set<Long> userIds = pointsBoards.stream().map(PointsBoard::getUserId).collect(Collectors.toSet());
                List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
                Map<Long, String> userMap = new HashMap<>(userDTOS.size());
                if (CollUtils.isNotEmpty(userDTOS))
                    {
                        userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
                    }
                
                //封装用户信息
                List<PointsBoardItemVO> items = new ArrayList<>(pointsBoards.size());
                for (PointsBoard pointsBoard : pointsBoards)
                    {
                        PointsBoardItemVO item = new PointsBoardItemVO();
                        item.setName(userMap.get(pointsBoard.getUserId()));
                        item.setPoints(pointsBoard.getPoints());
                        item.setRank(pointsBoard.getRank());
                        items.add(item);
                    }
                vo.setBoardList(items);
                return vo;
            }
        
        /**
         * 按赛季创建积分板表
         *
         * @param seasonId 赛季id
         */
        @Override
        public void createPointsBoardTableBySeason(Integer seasonId)
            {
                baseMapper.createPointsBoardTable(POINTS_BOARD_TABLE_PREFIX + seasonId);
            }
        
        /**
         * 查询当前赛季积分板
         *
         * @param key      Redis key
         * @param pageNo   页码
         * @param pageSize 每页查询数量
         * @return {@link List }<{@link PointsBoard }>
         */
        @Override
        public List<PointsBoard> queryCurrentSeasonPointsBoard(
                String key, Integer pageNo, Integer pageSize)
            {
                //从哪一条开始
                int start = (pageNo - 1) * pageSize;
                //查询数据
                Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(key, start, start + pageSize - 1);
                
                //验证数据
                if (CollUtils.isEmpty(tuples))
                    {
                        return CollUtils.emptyList();
                    }
                
                //封装数据
                int rank = start + 1;
                List<PointsBoard> pointsBoards = new ArrayList<>(tuples.size());
                for (ZSetOperations.TypedTuple<String> tuple : tuples)
                    {
                        String userId = tuple.getValue();
                        Double points = tuple.getScore();
                        if (userId == null || points == null)
                            {
                                continue;
                            }
                        PointsBoard board = new PointsBoard();
                        board.setUserId(Long.parseLong(userId));
                        board.setPoints(points.intValue());
                        board.setRank(rank++);
                        pointsBoards.add(board);
                    }
                return pointsBoards;
            }
        
        /**
         * 查询历史 季节积分板
         *
         * @param query 查询条件
         * @return {@link List }<{@link PointsBoard }>
         */
        private List<PointsBoard> queryHistorySeasonPointsBoard(PointsBoardQuery query)
            {
                //TODO Encounter 2024/12/03 23:02 查询历史赛季积分板
                //要查询的赛季
                Long season = query.getSeason();
                //计算表名
                TableInfoContext.setInfo(POINTS_BOARD_TABLE_PREFIX + season);
                //查询历史赛季积分榜
                List<PointsBoard> list = page(query.toMpPage()).getRecords();
                if (CollUtils.isEmpty(list))
                    {
                        return CollUtils.emptyList();
                    }
                list.forEach(p -> p.setRank(p.getId().intValue()));
                return list;
            }
        
        /**
         * 查询我历史赛季积分
         *
         * @param season 季节
         * @return {@link PointsBoard }
         */
        private PointsBoard queryMyHistoryBoard(Long season)
            {
                //获取当前用户信息
                Long userId = UserContext.getUser();
                //计算表名
                TableInfoContext.setInfo(POINTS_BOARD_TABLE_PREFIX + season);
                //查询当前用户积分
                PointsBoard one = lambdaQuery()
                        .eq(PointsBoard::getUserId, userId)
                        .one();
                if (one == null)
                    {
                        return null;
                    }
                one.setRank(one.getId().intValue());
                return one;
            }
        
        /**
         * 查询我当前赛季积分
         *
         * @param key redis key
         * @return {@link List }<{@link PointsBoard }>
         */
        private PointsBoard queryMyCurrentBoard(String key)
            {
                //绑定key
                BoundZSetOperations<String, String> ops = redisTemplate.boundZSetOps(key);
                //获取当前用户信息
                Long userId = UserContext.getUser();
                //查询当前用户积分
                Double score = ops.score(userId.toString());
                //查询当前用户排名
                Long rank = ops.reverseRank(userId.toString());
                
                //封装数据
                PointsBoard myBoard = new PointsBoard();
                myBoard.setUserId(userId);
                myBoard.setPoints(score == null ? 0 : score.intValue());
                myBoard.setRank(rank == null ? 0 : rank.intValue() + 1);
                return myBoard;
            }
    }
