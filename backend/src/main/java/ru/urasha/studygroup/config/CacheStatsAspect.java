package ru.urasha.studygroup.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheStatsAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheStatsAspect.class);

    private final EntityManagerFactory entityManagerFactory;
    private final CacheLoggingProperties properties;

    @Around("execution(* ru.urasha.studygroup.services..*(..))")
    public Object logL2CacheStats(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isStatsLoggingEnabled()) {
            return joinPoint.proceed();
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        long hitsBefore = statistics.getSecondLevelCacheHitCount();
        long missesBefore = statistics.getSecondLevelCacheMissCount();

        Object result = joinPoint.proceed();

        long hitsAfter = statistics.getSecondLevelCacheHitCount();
        long missesAfter = statistics.getSecondLevelCacheMissCount();

        long deltaHits = hitsAfter - hitsBefore;
        long deltaMisses = missesAfter - missesBefore;
        long totalDelta = deltaHits + deltaMisses;

        if (totalDelta >= properties.getStatsLogThreshold()) {
            log.info("L2 cache stats for {}: hits +{}, misses +{}, total hits={}, total misses={}",
                    joinPoint.getSignature().toShortString(),
                    deltaHits,
                    deltaMisses,
                    hitsAfter,
                    missesAfter);
        }

        return result;
    }
}
