// JWT 토큰 인터셉터 - 모든 폼 제출과 AJAX 요청에 토큰 자동 추가
(function() {
    console.log('JWT Interceptor: 전역 JWT 인터셉터 활성화됨');
    
    function getStoredToken() {
        const token = localStorage.getItem('authToken');
        console.log('JWT Interceptor: 토큰 조회 결과:', token ? `토큰 있음 (길이: ${token.length})` : '토큰 없음');
        return token;
    }
    
    function redirectToLogin() {
        alert('인증이 만료되었습니다. 다시 로그인해주세요.');
        localStorage.removeItem('authToken');
        window.location.href = '/auth/login';
    }
    
    // 1. 폼 제출과 페이지 이동에 토큰 자동 추가 (통합 버전)
    document.addEventListener('submit', function(event) {
        console.log('JWT Interceptor: 폼 제출 감지됨', event.target);
        
        const form = event.target;
        const token = getStoredToken();
        
        if (!token) {
            console.warn('JWT Interceptor: 토큰이 없음, 폼 제출 차단');
            event.preventDefault();
            redirectToLogin();
            return;
        }
        
        // 기존 토큰 필드 제거
        const existingTokenField = form.querySelector('input[name="authToken"]');
        if (existingTokenField) {
            existingTokenField.remove();
        }
        
        // 새 토큰 히든 필드 추가
        const tokenField = document.createElement('input');
        tokenField.type = 'hidden';
        tokenField.name = 'authToken';
        tokenField.value = token;
        form.appendChild(tokenField);
        
        // 폼 액션 URL에도 토큰 추가 (GET 메서드의 경우)
        if (form.method.toLowerCase() === 'get' && form.action && form.action.indexOf('authToken=') === -1) {
            const separator = form.action.indexOf('?') === -1 ? '?' : '&';
            form.action += separator + 'authToken=' + encodeURIComponent(token);
            console.log('JWT Interceptor: GET 폼 액션에 토큰 추가:', form.action);
        }
        
        console.log('JWT Interceptor: 폼에 토큰 추가 완료');
    });
    
    // 2. Fetch API 인터셉터
    const originalFetch = window.fetch;
    window.fetch = function(url, options = {}) {
        console.log('JWT Interceptor: Fetch 요청 감지:', url);
        
        const token = getStoredToken();
        if (token) {
            options.headers = options.headers || {};
            options.headers['Authorization'] = 'Bearer ' + token;
            console.log('JWT Interceptor: Fetch에 Authorization 헤더 추가');
        }
        
        return originalFetch.call(this, url, options)
            .then(response => {
                if (response.status === 401) {
                    console.error('JWT Interceptor: 401 Unauthorized 응답');
                    setTimeout(() => redirectToLogin(), 100);
                }
                return response;
            });
    };
    
    // 3. XMLHttpRequest 인터셉터 (DELETE 요청 포함 강화)
    const originalXHROpen = XMLHttpRequest.prototype.open;
    const originalXHRSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
        this._method = method;
        this._url = url;
        console.log('JWT Interceptor: XHR OPEN 감지:', method, url);
        return originalXHROpen.call(this, method, url, async, user, password);
    };
    
    XMLHttpRequest.prototype.send = function(body) {
        const token = getStoredToken();
        if (token) {
            this.setRequestHeader('Authorization', 'Bearer ' + token);
            console.log('JWT Interceptor: XHR에 Authorization 헤더 추가:', this._method, this._url);
        } else {
            console.warn('JWT Interceptor: XHR 요청에 토큰 없음:', this._method, this._url);
        }
        return originalXHRSend.call(this, body);
    };
    
    // 4. 페이지 이동 시 토큰 자동 추가 강화
    document.addEventListener('click', function(event) {
        const target = event.target;
        
        // 링크나 버튼 클릭 시
        if (target.tagName === 'A' && target.href && !target.href.includes('#') && !target.target) {
            const token = getStoredToken();
            if (token && target.href.indexOf('authToken=') === -1) {
                const separator = target.href.indexOf('?') === -1 ? '?' : '&';
                target.href += separator + 'authToken=' + encodeURIComponent(token);
                console.log('JWT Interceptor: 링크에 토큰 추가:', target.href);
            }
        }
    });
    
    // 5. window.location 변경 인터셉트
    const originalLocation = window.location;
    Object.defineProperty(window, 'location', {
        get: function() {
            return originalLocation;
        },
        set: function(url) {
            const token = getStoredToken();
            if (token && typeof url === 'string' && url.indexOf('authToken=') === -1) {
                const separator = url.indexOf('?') === -1 ? '?' : '&';
                url += separator + 'authToken=' + encodeURIComponent(token);
                console.log('JWT Interceptor: window.location에 토큰 추가:', url);
            }
            originalLocation.href = url;
        }
    });
    
    // 6. 폼 액션 URL에도 토큰 추가
    // 이 부분은 위의 통합 버전에서 처리되므로 제거
    
    // 7. 모든 navigation 함수들 오버라이드
    const originalPushState = history.pushState;
    const originalReplaceState = history.replaceState;
    
    history.pushState = function(state, title, url) {
        const token = getStoredToken();
        if (token && url && typeof url === 'string' && url.indexOf('authToken=') === -1) {
            const separator = url.indexOf('?') === -1 ? '?' : '&';
            url += separator + 'authToken=' + encodeURIComponent(token);
            console.log('JWT Interceptor: pushState에 토큰 추가:', url);
        }
        return originalPushState.call(this, state, title, url);
    };
    
    history.replaceState = function(state, title, url) {
        const token = getStoredToken();
        if (token && url && typeof url === 'string' && url.indexOf('authToken=') === -1) {
            const separator = url.indexOf('?') === -1 ? '?' : '&';
            url += separator + 'authToken=' + encodeURIComponent(token);
            console.log('JWT Interceptor: replaceState에 토큰 추가:', url);
        }
        return originalReplaceState.call(this, state, title, url);
    };
    
    // 8. 페이지 로드 시 토큰 상태 체크
    document.addEventListener('DOMContentLoaded', function() {
        console.log('=== JWT Interceptor 디버깅 정보 ===');
        console.log('페이지 URL:', window.location.href);
        console.log('현재 시간:', new Date().toLocaleString());
        
        const token = getStoredToken();
        if (token) {
            console.log('JWT Interceptor: 토큰 확인됨, 인터셉터 준비 완료');
            console.log('토큰 정보:', {
                길이: token.length,
                시작: token.substring(0, 20) + '...',
                localStorage키: 'authToken'
            });
            
            // 현재 URL에 토큰이 없으면 자동으로 추가
            const currentUrl = window.location.href;
            if (currentUrl.indexOf('authToken=') === -1 && 
                !currentUrl.includes('/auth/login') && 
                !currentUrl.includes('/login') &&
                !currentUrl.includes('/favicon.ico') &&
                !currentUrl.includes('/js/') &&
                !currentUrl.includes('/css/')) {
                
                console.log('JWT Interceptor: 현재 URL에 토큰이 없음, 토큰 추가 후 페이지 새로고침');
                const separator = currentUrl.indexOf('?') === -1 ? '?' : '&';
                const newUrl = currentUrl + separator + 'authToken=' + encodeURIComponent(token);
                
                // 즉시 새로운 URL로 리다이렉트 (무한 루프 방지를 위해 sessionStorage 체크)
                const redirectKey = 'jwt_redirected_' + window.location.pathname;
                if (!sessionStorage.getItem(redirectKey)) {
                    sessionStorage.setItem(redirectKey, 'true');
                    window.location.href = newUrl;
                    return; // 나머지 코드 실행 중지
                } else {
                    // 이미 리다이렉트했으면 sessionStorage 클리어
                    sessionStorage.removeItem(redirectKey);
                }
            }
            
            // 토큰 상태 표시
            const statusDiv = document.createElement('div');
            statusDiv.id = 'jwt-status-indicator';
            statusDiv.style.cssText = `
                position: fixed;
                top: 10px;
                right: 10px;
                background-color: #28a745;
                color: white;
                padding: 8px 12px;
                border-radius: 4px;
                font-size: 12px;
                z-index: 10000;
                box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                pointer-events: none;
                cursor: default;
            `;
            statusDiv.textContent = '🔐 JWT 인증 활성';
            statusDiv.title = '토큰 길이: ' + token.length + ' 문자';
            document.body.appendChild(statusDiv);
            
            // 토큰 테스트 버튼 추가 (개발용)
            const testDiv = document.createElement('div');
            testDiv.style.cssText = `
                position: fixed;
                top: 50px;
                right: 10px;
                background-color: #007bff;
                color: white;
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 10px;
                z-index: 10000;
                cursor: pointer;
                box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            `;
            testDiv.textContent = '🧪 토큰 테스트';
            testDiv.onclick = function() {
                console.log('=== JWT 토큰 테스트 ===');
                console.log('저장된 토큰:', localStorage.getItem('authToken'));
                console.log('토큰 길이:', token.length);
                console.log('현재 URL:', window.location.href);
                console.log('URL 파라미터:', new URLSearchParams(window.location.search).toString());
                alert('토큰 상태: 활성\n길이: ' + token.length + ' 문자\n콘솔에서 자세한 정보 확인\nURL 토큰: ' + (window.location.href.includes('authToken=') ? '포함됨' : '없음'));
            };
            document.body.appendChild(testDiv);
            
            setTimeout(() => {
                statusDiv.style.display = 'none';
                testDiv.style.display = 'none';
            }, 5000);
        } else {
            console.warn('JWT Interceptor: 토큰이 없음, 로그인 필요');
            console.log('localStorage 상태:', {
                authToken: localStorage.getItem('authToken'),
                전체키목록: Object.keys(localStorage)
            });
            
            // 로그인 페이지가 아닌 경우에만 리다이렉트
            if (!window.location.pathname.includes('/auth/login') && 
                !window.location.pathname.includes('/login')) {
                
                // 경고 표시
                const warningDiv = document.createElement('div');
                warningDiv.style.cssText = `
                    position: fixed;
                    top: 10px;
                    right: 10px;
                    background-color: #dc3545;
                    color: white;
                    padding: 8px 12px;
                    border-radius: 4px;
                    font-size: 12px;
                    z-index: 10000;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                `;
                warningDiv.textContent = '⚠️ JWT 토큰 없음';
                document.body.appendChild(warningDiv);
                
                setTimeout(() => {
                    redirectToLogin();
                }, 2000);
            }
        }
        
        console.log('=== JWT Interceptor 준비 완료 ===');
    });
    
    // 9. 페이지 이탈 시 토큰 만료 체크
    window.addEventListener('beforeunload', function() {
        const token = getStoredToken();
        if (!token) {
            console.warn('JWT Interceptor: 페이지 이탈 시 토큰 없음 감지');
        }
    });
})();

// 전역에서 사용할 수 있는 유틸리티 함수들
window.JWTUtils = {
    getToken: function() {
        return localStorage.getItem('authToken');
    },
    
    setToken: function(token) {
        if (token) {
            localStorage.setItem('authToken', token);
            console.log('JWT Interceptor: 토큰 저장됨');
        }
    },
    
    removeToken: function() {
        localStorage.removeItem('authToken');
        console.log('JWT Interceptor: 토큰 제거됨');
    },
    
    isAuthenticated: function() {
        const token = this.getToken();
        return token && token.length > 0;
    },
    
    redirectToLogin: function() {
        this.removeToken();
        window.location.href = '/auth/login';
    }
}; 