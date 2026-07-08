package likelion14th.lte.user.service;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 1. "나는 핵심 비즈니스 로직을 처리하는 서비스 계층이다!"라고 스프링에게 알립니다.
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
// 2. [의존성 주입(DI)] final이 붙은 변수의 생성자를 자동으로 만들어줍니다. Controller에서 본 것과 똑같죠?
public class UserProfileService {

    private final UserRepository userRepository;
    // 3. Service는 DB에 접근해야 하므로 Repository를 부릅니다. 스프링이 알아서 객체를 조립(주입)해 줍니다.

    // ==========================================
    // [1] 회원 정보 조회 로직
    // ==========================================
    @Transactional(readOnly = true)
    // 4. [트랜잭션 최적화] 데이터를 읽기만(SELECT) 할 때는 'readOnly = true'를 붙여줍니다.
    // JPA가 "아, 수정은 안 할 거니까 더티 체킹(감시)은 안 해도 되겠네?"라고 판단해 메모리와 속도를 최적화합니다.
    // 또한 실수로 데이터를 바꿔버리는 것을 차단합니다.
    public UserProfileResponse getUserProfile(Long userId) {

        // 5. Repository에 "ID로 유저 좀 찾아와!"라고 명령합니다.
        User user = userRepository.findById(userId)
                // 6. [예외 처리] 자바의 Optional 기능입니다.
                // 만약 DB에 그 ID를 가진 회원이 없다면? 서버가 에러(NullPointerException)를 뿜고 죽지 않도록,
                // "유저를 찾을 수 없습니다(USER_NOT_FOUND)"라는 우리가 만든 깔끔한 커스텀 예외를 던지게 만듭니다.
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        // 7. DB에서 찾아온 순수한 자바 객체(Entity)를 화면에 뿌려줄 전용 상자(DTO)로 변환해서 반환합니다.
        return UserProfileResponse.from(user);
    }

    // ==========================================
    // [2] 회원 생성(저장) 로직
    // ==========================================
    @Transactional
    // 8. [트랜잭션 방어막] 데이터를 삽입(INSERT)하므로 무조건 붙여야 합니다.
    // 만약 아래 코드 실행 중 에러가 터지면, 지금까지 DB에 넣은 데이터를 모두 롤백(취소)하여 데이터를 안전하게 보호합니다.
    public UserProfileResponse createTestUser(CreateTestUserRequest request) {

        // 9. DTO -> Entity 변환 작업 (Controller에서 넘어온 DTO 바구니를 DB에 넣을 Entity로 만듭니다)
        User newUser = User.builder() // 10. 생성자 대신 '빌더(Builder) 패턴'을 사용했습니다.
                .username(request.getUsername()) // "username에는 이걸 넣고,"
                .introduction(request.getIntroduction()) // "소개글에는 이걸 넣어!"
                .userTag(request.getUserTag())
                .build(); // 명확하게 조립 완료.

        User savedUser;

        // 11. DB 저장 및 예외 처리
        try {
            // 12. 조립된 Entity를 DB에 저장하라고 Repository에 명령합니다. (JPA가 INSERT 쿼리 자동 실행)
            savedUser = userRepository.save(newUser);
        } catch (Exception e) {
            // 13. 만약 DB 저장 중 문제(예: 이미 존재하는 userTag 등)가 발생하면,
            // 쌩(raw) 데이터베이스 에러를 화면에 던지지 않고 "잘못된 요청입니다(BAD_REQUEST)"로 감싸서 예쁘게 던져줍니다.
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        // 14. 방금 DB에 무사히 저장된 회원 데이터(Entity)를 다시 화면용 DTO로 변환하여 Controller로 돌려보냅니다.
        return UserProfileResponse.from(savedUser);
    }
}
