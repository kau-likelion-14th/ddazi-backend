package likelion14th.lte.user.dto.response;

import likelion14th.lte.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
// 1. 모든 필드를 매개변수로 받는 생성자를 자동으로 만들어줍니다.
public class UserProfileResponse {
    private String userName;
    private String profileImageUrl;
    private String introduction;

    // 2. [매우 중요] 정적(static) 팩토리 메서드입니다.
    // DB에서 가져온 원본 Entity(User)를 집어넣으면, 화면에 뿌려줄 DTO로 변환해서 반환해 줍니다.
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUsername() + "#" + user.getUserTag(), // DB엔 따로 저장된 이름과 태그를 여기서 하나로 예쁘게 합칩니다!
                user.getProfileImage(),
                user.getIntroduction()
        );
    }
}
