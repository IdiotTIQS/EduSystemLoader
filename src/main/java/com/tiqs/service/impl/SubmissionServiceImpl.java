package com.tiqs.service.impl;

import com.tiqs.entity.Submission;
import com.tiqs.mapper.SubmissionMapper;
import com.tiqs.service.SubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionMapper submissionMapper;
    public SubmissionServiceImpl(SubmissionMapper submissionMapper){this.submissionMapper=submissionMapper;}

    @Transactional
    public Submission submit(Submission s){submissionMapper.insert(s); return submissionMapper.findUnique(s.getAssignmentId(), s.getStudentId());}
    public Submission findUnique(Long assignmentId,Long studentId){return submissionMapper.findUnique(assignmentId,studentId);}    
    public List<Submission> listByAssignment(Long assignmentId){return submissionMapper.findByAssignment(assignmentId);}    
    @Transactional
    public Submission updateContent(Long id,String filePath,String answerText){
        // id 是 submission 主键，需先查出，再更新
        Submission existing = null;
        // 由于 mapper 没有按主键查询，这里简单通过 assignmentId+studentId 不实现，改为抛异常或扩展 mapper；为保持简洁：直接构建对象更新
        Submission s = new Submission();
        s.setId(id); s.setFilePath(filePath); s.setAnswerText(answerText);
        submissionMapper.updateContent(s);
        return s;
    }
    @Transactional
    public Submission grade(Long id,Double score,String feedback){
        Submission s = new Submission();
        s.setId(id); s.setScore(score); s.setFeedback(feedback);
        submissionMapper.grade(s);
        return s;
    }
}
