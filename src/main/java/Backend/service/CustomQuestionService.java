package Backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Backend.entity.CustomQuestion;
import Backend.repository.CustomQuestionRepository;

@Service
public class CustomQuestionService {
    
    @Autowired
    private CustomQuestionRepository customQuestionRepository;
    
    public List<CustomQuestion> getAllActiveQuestions() {
        return customQuestionRepository.findAllActive();
    }
    
    public List<CustomQuestion> getQuestionsByCategory(CustomQuestion.QuestionCategory category) {
        return customQuestionRepository.findByCategoryAndActive(category);
    }
    
    public CustomQuestion createQuestion(String question, CustomQuestion.QuestionCategory category) {
        CustomQuestion customQuestion = new CustomQuestion();
        customQuestion.setQuestion(question);
        customQuestion.setCategory(category);
        customQuestion.setActive(true);
        
        return customQuestionRepository.save(customQuestion);
    }
}