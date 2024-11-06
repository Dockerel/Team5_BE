package ojosama.talkak.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import ojosama.talkak.category.domain.Category;
import ojosama.talkak.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import ojosama.talkak.member.repository.MemberRepository;
import ojosama.talkak.video.domain.Video;
import ojosama.talkak.video.repository.VideoRepository;
import ojosama.talkak.category.repository.CategoryRepository;

@SpringBootTest
public class DummyDataTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Member 더미 데이터 확인")
    void testMemberDummyData() {
        List<Member> members = memberRepository.findAll();
        assertThat(members).hasSize(2);  // 예상되는 Member 데이터 수 확인
        assertThat(members.get(0).getUsername()).isNotNull();
    }

    @Test
    @DisplayName("Category 더미 데이터 확인")
    void testCategoryDummyData() {
        List<Category> categories = categoryRepository.findAll();
        assertThat(categories).isNotEmpty();
        assertThat(categories.get(0).getCategoryType()).isNotNull();
    }

    @Test
    @DisplayName("Video 더미 데이터 확인")
    void testVideoDummyData() {
        List<Video> videos = videoRepository.findAll();
        assertThat(videos).hasSize(50);  // 예상되는 Video 데이터 수 확인
        assertThat(videos.get(0).getTitle()).isNotEmpty();
        assertThat(videos.get(0).getCategoryId()).isNotNull();
    }
}
