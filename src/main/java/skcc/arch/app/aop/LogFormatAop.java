package skcc.arch.app.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import skcc.arch.app.util.LogFormatUtil;

@Aspect
@Component
@Slf4j
public class LogFormatAop {

    // Controller 계층 Pointcut
    @Pointcut("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerLayer() {
    }

    // Service 계층 Pointcut
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {
    }

    // Repository 계층 Pointcut
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryLayer() {
    }

    // 모든 Pointcut 묶기
    @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer()")
    public void allLayers() {
    }

    /**
     * 로그 메시지 앞에 Depth 를 추가하고 START/END 로그를 출력한다
     */
    @Around("allLayers()")
    public Object manageDepth(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean isFirst = LogFormatUtil.isEmpty();
        String signature = getSignature(joinPoint);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // START 로그 출력
        updateLogDepth(isFirst, signature);
        logMethodStart(className, methodName, joinPoint.getArgs());
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            // 성공 END 로그 출력
            long executionTime = System.currentTimeMillis() - startTime;
            logMethodEnd(className, methodName, executionTime, true, null);
            return result;
        } catch (Exception e) {
            // 예외 END 로그 출력
            long executionTime = System.currentTimeMillis() - startTime;
            logMethodEnd(className, methodName, executionTime, false, e);
            throw e;
        } finally {
            restoreLogDepth(isFirst);
        }
    }

    private String getSignature(ProceedingJoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();
    }

    private void updateLogDepth(boolean isFirst, String signature) {
        if (isFirst) {
            LogFormatUtil.initializeDepth(signature);
        } else {
            LogFormatUtil.incrementDepth(signature);
        }
    }

    private void restoreLogDepth(boolean isFirst) {
        if (isFirst) {
            LogFormatUtil.clearDepth();
        } else {
            LogFormatUtil.decrementDepth();
        }
    }
    
    /**
     * 메서드 시작 로그를 출력합니다
     */
    private void logMethodStart(String className, String methodName, Object[] args) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("START - ").append(className).append(".").append(methodName).append("()");
        
        if (args != null && args.length > 0) {
            logMessage.append(" with parameters: ");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) logMessage.append(", ");
                logMessage.append(getParameterString(args[i]));
            }
        }
        
        log.info(logMessage.toString());
    }
    
    /**
     * 메서드 종료 로그를 출력합니다
     */
    private void logMethodEnd(String className, String methodName, long executionTime, boolean isSuccess, Exception exception) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("END - ").append(className).append(".").append(methodName).append("()");
        logMessage.append(" [").append(executionTime).append("ms]");
        
        if (isSuccess) {
            logMessage.append(" - SUCCESS");
        } else {
            logMessage.append(" - FAILED");
            if (exception != null) {
                logMessage.append(" (").append(exception.getClass().getSimpleName())
                         .append(": ").append(exception.getMessage()).append(")");
            }
        }
        
        if (isSuccess) {
            log.info(logMessage.toString());
        } else {
            log.error(logMessage.toString());
        }
    }
    
    /**
     * 파라미터를 로그 출력용 문자열로 변환합니다
     */
    private String getParameterString(Object param) {
        if (param == null) {
            return "null";
        }
        
        String className = param.getClass().getSimpleName();
        
        // 민감한 정보는 마스킹
        if (className.toLowerCase().contains("password") || 
            (param instanceof String && ((String) param).toLowerCase().contains("password"))) {
            return "[MASKED]";
        }
        
        // 토큰도 마스킹
        if (param instanceof String && ((String) param).startsWith("eyJ")) {
            return "[TOKEN]";
        }
        
        // 기본 타입은 값 출력
        if (param instanceof String || param instanceof Number || param instanceof Boolean) {
            return param.toString();
        }
        
        // 객체는 클래스명만 출력
        return className + "@" + Integer.toHexString(param.hashCode());
    }
}