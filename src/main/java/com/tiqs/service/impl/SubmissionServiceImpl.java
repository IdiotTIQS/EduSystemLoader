/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description SubmissionServiceImpl
 */
package com.tiqs.service.impl;

import com.tiqs.entity.Submission;
import com.tiqs.mapper.SubmissionMapper;
import com.tiqs.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionMapper submissionMapper;

    public SubmissionServiceImpl(SubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Transactional
    @Override
    public Submission submit(Submission s) {
        submissionMapper.insert(s);
        log.info("提交作业成功 assignmentId={} studentId={}", s.getAssignmentId(), s.getStudentId());
        return submissionMapper.findUnique(s.getAssignmentId(), s.getStudentId());
    }
    @Override
    public Submission findUnique(Long assignmentId, Long studentId) {
        return submissionMapper.findUnique(assignmentId, studentId);
    }
    @Override
    public List<Submission> listByAssignment(Long assignmentId) {
        return submissionMapper.findByAssignment(assignmentId);
    }

    @Transactional
    @Override
    public Submission updateContent(Long id, String filePath, String answerText, String originalFileName) {
        Submission s = new Submission();
        s.setId(id);
        s.setFilePath(filePath);
        s.setAnswerText(answerText);
        s.setOriginalFileName(originalFileName);
        submissionMapper.updateContent(s);
        log.info("更新提交内容成功 submissionId={}", id);
        return s;
    }

    @Transactional
    @Override
    public Submission grade(Long id, Double score, String feedback) {
        Submission s = new Submission();
        s.setId(id);
        s.setScore(score);
        s.setFeedback(feedback);
        submissionMapper.grade(s);
        log.info("评分成功 submissionId={} score={}", id, score);
        return s;
    }
}
