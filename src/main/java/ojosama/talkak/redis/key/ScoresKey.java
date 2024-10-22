package ojosama.talkak.redis.key;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ScoresKey {
    SCORES("scores:%s");

    private final String key;

    public String generateKey(Long memberId) {
        return String.format(key, memberId);
    }

    public Long getMemberId() {
        return Long.parseLong(key.split(":")[1]);
    }
}
