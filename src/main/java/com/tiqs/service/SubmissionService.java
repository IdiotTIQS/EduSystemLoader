/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description SubmissionService
 */
package com.tiqs.service;

import com.tiqs.entity.Submission;

import java.util.List;

public interface SubmissionService {
    Submission submit(Submission s);

    Submission updateContent(Long id, String filePath, String answerText, String originalFileName);

    Submission grade(Long id, Double score, String feedback);

    Submission findUnique(Long assignmentId, Long studentId);

    List<Submission> listByAssignment(Long assignmentId);
}
