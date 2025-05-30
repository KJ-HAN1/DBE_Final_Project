// 임시로 사용
package com.ttasum.memorial.service.forbiddenWord;

import com.ttasum.memorial.annotation.blameText.CheckBlameText;
import com.ttasum.memorial.domain.Board;
import com.ttasum.memorial.domain.entity.DonationStory.DonationStory;
import com.ttasum.memorial.domain.entity.Story;
import com.ttasum.memorial.domain.entity.blameText.BlameTextLetter;
import com.ttasum.memorial.dto.ApiResponse;
import com.ttasum.memorial.dto.forbiddenWord.ReviewRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TestReviewService {

    @CheckBlameText  // 이 메서드가 AOP의 Pointcut 대상임을 나타냄
    public ResponseEntity<?> saveReview(ReviewRequestDto request){
        // 이 메서드 자체가 JoinPoint이며, 실행 전후에 Advice가 적용됨
        return ResponseEntity.ok(ApiResponse.ok(HttpStatus.OK.value(), "등록이 성공적으로 완료되었습니다."));
    }

    @CheckBlameText  // 이 메서드가 AOP의 Pointcut 대상임을 나타냄
    public void saveReviewFromBlameTable(Story story, Board board){
        // 이 메서드 자체가 JoinPoint이며, 실행 전후에 Advice가 적용됨
    }
}
