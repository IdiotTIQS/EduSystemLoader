package com.tiqs.service;

import com.tiqs.entity.Submission;
import java.util.List;

public interface SubmissionService {
    Submission submit(Submission s);
    Submission updateContent(Long id,String filePath,String answerText);
    Submission grade(Long id,Double score,String feedback);
    Submission findUnique(Long assignmentId,Long studentId);
    List<Submission> listByAssignment(Long assignmentId);
}
