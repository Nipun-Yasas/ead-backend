package Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Backend.entity.CustomQuestion;

@Repository
public interface CustomQuestionRepository extends JpaRepository<CustomQuestion, Long> {
    
    @Query("SELECT cq FROM CustomQuestion cq WHERE cq.isActive = true ORDER BY cq.category, cq.question")
    List<CustomQuestion> findAllActive();
    
    @Query("SELECT cq FROM CustomQuestion cq WHERE cq.category = :category AND cq.isActive = true ORDER BY cq.question")
    List<CustomQuestion> findByCategoryAndActive(@Param("category") CustomQuestion.QuestionCategory category);
}