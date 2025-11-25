package com.tiqs.mapper;

import com.tiqs.entity.Submission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SubmissionMapper {
    @Insert("INSERT INTO submissions(assignment_id, student_id, file_path, original_file_name, answer_text, submitted_at, status) VALUES(#{assignmentId},#{studentId},#{filePath},#{originalFileName},#{answerText},NOW(),'SUBMITTED')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission s);

    @Select("SELECT id,assignment_id AS assignmentId,student_id AS studentId,file_path AS filePath,original_file_name AS originalFileName,answer_text AS answerText,submitted_at AS submittedAt,score,feedback,graded_at AS gradedAt,status FROM submissions WHERE assignment_id=#{assignmentId} AND student_id=#{studentId}")
    Submission findUnique(@Param("assignmentId") Long assignmentId,@Param("studentId") Long studentId);

    @Select("SELECT id,assignment_id AS assignmentId,student_id AS studentId,file_path AS filePath,original_file_name AS originalFileName,answer_text AS answerText,submitted_at AS submittedAt,score,feedback,graded_at AS gradedAt,status FROM submissions WHERE assignment_id=#{assignmentId}")
    List<Submission> findByAssignment(Long assignmentId);

    @Update("UPDATE submissions SET file_path=#{filePath},original_file_name=#{originalFileName},answer_text=#{answerText},submitted_at=NOW() WHERE id=#{id}")
    int updateContent(Submission s);

    @Update("UPDATE submissions SET score=#{score},feedback=#{feedback},graded_at=NOW(),status='GRADED' WHERE id=#{id}")
    int grade(Submission s);
}
