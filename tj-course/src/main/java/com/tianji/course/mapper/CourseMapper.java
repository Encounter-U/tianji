package com.tianji.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.api.dto.IdAndNumDTO;
import com.tianji.course.domain.po.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 草稿课程 Mapper 接口
 * </p>
 *
 * @author wusongsong
 * @since 2022-07-22
 */
public interface CourseMapper extends BaseMapper<Course> {
    @Select("select count(1) from course where name = #{name}")
    int countSameName(@Param("name") String name);

    @Update("update course set status=#{po.status}, cover_url=#{po.coverUrl},purchase_end_time=" +
            "#{po.purchaseEndTime},media_duration=#{po.mediaDuration} where id=#{po.id}")
    int updateVariableById(@Param("po") Course course);

    /**
     * 批量查询老师所负责的课程数量
     * @param teacherIds
     * @return
     */
    @Select("<script>SELECT ct.teacher_id as id,count(*) as num " +
            " from course c LEFT JOIN course_teacher ct on c.id=ct.course_id " +
            "where c.status!=1 and c.deleted=0 and ct.teacher_id in (<foreach collection='teacherIds' " +
            "item='teacherId' separator=','>#{teacherId}</foreach>)" +
            " GROUP BY ct.teacher_id</script>")
    List<IdAndNumDTO> countCourseNumOfTeacher(@Param("teacherIds")List<Long> teacherIds);
}
