package skcc.arch.biz.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skcc.arch.app.cache.CacheService;
import skcc.arch.app.cache.CaffeineCacheService;
import skcc.arch.app.cache.RedisCacheService;
import skcc.arch.biz.code.domain.Code;
import skcc.arch.biz.code.service.port.CodeRepositoryPort;
import skcc.arch.biz.common.constants.CacheGroup;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.menu.service.port.MenuRepositoryPort;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyCacheService {

    public static final String DELIMITER = ":";
    private final CacheService cacheService;
    private final CodeRepositoryPort codeRepositoryPort;
    private final MenuRepositoryPort menuRepositoryPort;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void loadCacheData() {

        // 메모리(로컬)
        if(cacheService instanceof CaffeineCacheService) {
            // 초기 적재할 캐시
            loadCodeCacheData();
            log.info("캐시 적재 완료");
        }
        // 레디스(서버)
        else if (cacheService instanceof RedisCacheService) {
            // 초기로딩이 필요한지?
            loadCodeCacheData();
        }
    }

    /*
        캐시명.캐시 KEY,값
        캐시명은 상수값을 사용한다
     */
    public void put(CacheGroup cacheGroup, String key, Object value) {
        try {
            cacheService.put(cacheGroup.name() + DELIMITER + key, value);
        } catch (Exception e) {
            log.error(" cache put error : {}", e.getMessage());
        }
    }

    public <T> T get(CacheGroup cacheGroup, String key, Class<T> clazz) {
        T t;
        try {
            t = cacheService.get(cacheGroup.name() + DELIMITER + key, clazz);
        } catch (Exception e) {
            log.error(" cache get error : {}", e.getMessage());
            t = null;
        }
        return t;
    }

    public void evict(CacheGroup cacheGroup, String key) {
        try {
            cacheService.evict(cacheGroup.name() + DELIMITER + key);
        } catch (Exception e) {
            log.error(" cache evict error : {}", e.getMessage());
        }
    }

    public void clearAll() {
        try {
            cacheService.clearAll();
        } catch (Exception e) {
            log.error(" cache clearAll error : {}", e.getMessage());
        }
    }

    public void clearByCacheGroup(CacheGroup cacheGroup) {

        try {
            cacheService.clearByCacheGroup(cacheGroup.name());
        } catch (Exception e) {
            log.error(" cache clearCacheName error : {}", e.getMessage());
        }
    }


    /**
     * 비즈니스 요건에 맞게 캐시 설계
     *
     * Sample - 코드 도메인의 최상위 부모만 캐시에 적재한다.
     *       Cache Name : code
     *       KEY: 부모의 코드값
     *       VALUE: Code 모델 (최하위 요소까지 포함)
     */
    private void loadCodeCacheData() {
        // 최상위 부모 조회
        List<Code> parent = codeRepositoryPort.findByParentCodeId(null);
        for (Code code : parent) {
            if(this.get(CacheGroup.CODE, code.getCode(), Code.class) == null)
            {
                // 최하위 까지 조회
                Code nodes = codeRepositoryPort.findAllLeafNodes(code.getId());
                this.put(CacheGroup.CODE, code.getCode(), nodes);
            }
        }

        // 최상위 메뉴 캐시 적재
        Map<Long, Menu> menuMap = menuRepositoryPort.loadCacheData();
        this.put(CacheGroup.MENU, "ROOT", menuMap);

    }

}
