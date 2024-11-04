package ojosama.talkak.recommendation.repository;

import java.util.List;
import java.util.Set;
import ojosama.talkak.common.util.HashConverter;
import ojosama.talkak.common.RedisRepository;
import ojosama.talkak.recommendation.domain.Scores;
import ojosama.talkak.recommendation.innerkey.ScoresSetKey;
import ojosama.talkak.recommendation.key.DefaultScoresKey;
import ojosama.talkak.recommendation.key.ScoresKey;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Repository
public class ScoresRepository {

    private final RedisRepository redisRepository;
    private final HashConverter hashConverter;

    public ScoresRepository(RedisRepository redisRepository, HashConverter hashConverter) {
        this.redisRepository = redisRepository;
        this.hashConverter = hashConverter;
    }

    public List<Scores> findDefaultTopRank(Long categoryId, long count) {
        String key = DefaultScoresKey.SCORES.generateKey(categoryId);
        return getScoresList(categoryId, count, key);
    }

    public List<Scores> findTopRankScores(Long memberId, Long categoryId, long count) {
        String key = ScoresKey.SCORES.generateKey(categoryId, memberId);
        return getScoresList(categoryId, count, key);
    }

    private List<Scores> getScoresList(Long categoryId, long count, String key) {
        Set<TypedTuple<Object>> tuple = redisRepository.getSortedSetOps(key, count);

        return tuple.stream()
            .filter(t -> t.getValue() != null && t.getScore() != null)
            .map(t -> Scores.of(categoryId,
                ScoresSetKey.SCORE.getVideoId(t.getValue().toString()),
                t.getScore().floatValue()
            ))
            .toList();
    }

}
