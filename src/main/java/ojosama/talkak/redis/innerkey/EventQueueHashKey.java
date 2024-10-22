package ojosama.talkak.redis.innerkey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventQueueHashKey {
    MEMBER_ID("user_id"),
    CATEGORY_ID("category_id"),
    LAST_UPDATED_AT("last_updated_at");

    private final String key;
}
