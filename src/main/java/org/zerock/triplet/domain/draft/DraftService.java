package org.zerock.triplet.domain.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DraftService {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    @Value("${app.drafts.ttlDays}") private int ttlDays;

    private String key(long userId){return "trip:draft:" + userId;}
    private Duration ttl() {return Duration.ofDays(ttlDays);}

    public Optional<Draft> get(long userId){
        String v = redis.opsForValue().get(key(userId));
        if(v == null) return Optional.empty();
        try{ return Optional.of(om.readValue(v, Draft.class));}
        catch(Exception e){throw new RuntimeException(e);}
    }

    public Draft upsertMerge(long userId, Map<String, Object> patch){
        Draft base = get(userId).orElse(new Draft());
        Draft merged = JsonMergeUtil.merge(base, patch, Draft.class, om);
        merged.setUpdatedAt(System.currentTimeMillis());
        try{
            String json = om.writeValueAsString(merged);
            redis.opsForValue().set(key(userId), json, ttl());
            return merged;
        }catch(Exception e){throw new RuntimeException(e);}
    }

    public void delete(long userId){redis.delete(key(userId));}

}
