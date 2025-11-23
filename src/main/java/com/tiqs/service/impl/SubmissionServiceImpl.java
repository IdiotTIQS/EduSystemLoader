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
    public Submission submit(Submission s){submissionMapper.insert(s); return s;}
    public Submission findUnique(Long assignmentId,Long studentId){return submissionMapper.findUnique(assignmentId,studentId);}    
    public List<Submission> listByAssignment(Long assignmentId){return submissionMapper.findByAssignment(assignmentId);}    
    @Transactional
    public Submission updateContent(Long id,String filePath,String answerText){Submission s = submissionMapper.findUnique(id,null); return null;}
    @Transactional
    public Submission grade(Long id,Double score,String feedback){Submission s = submissionMapper.findUnique(id,null); return null;}
}
