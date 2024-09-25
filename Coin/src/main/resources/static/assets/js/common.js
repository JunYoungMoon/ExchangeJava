function showloading() {
    console.log('Loading...');
}

function hideloading() {
    console.log('Loading finished.');
}

function ajaxRequest(url, method, data, callback) {
    let xhr = new XMLHttpRequest();

    xhr.onloadstart = function () {
        showloading();
    };

    xhr.open(method, url, true);

    xhr.setRequestHeader('Content-Type', 'application/json');
    if (data) {
        if (data.token) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + data.token);
        }
        if (data.csrfToken) {
            xhr.setRequestHeader('X-XSRF-TOKEN', data.csrfToken);
        }
    }

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            hideloading();
            if (xhr.status === 200) {
                callback(xhr.responseText);
            } else {
                switch (xhr.status) {
                    case 400:
                        console.error('Bad Request:', xhr.responseText);
                        // Add your handling code for 400 Bad Request here
                        break;
                    case 401:
                        console.error('Unauthorized:', xhr.responseText);
                        // Add your handling code for 401 Unauthorized here
                        break;
                    case 403:
                        console.error('Forbidden:', xhr.responseText);
                        // Add your handling code for 403 Forbidden here
                        break;
                    default:
                        console.error('Request failed with status:', xhr.status);
                    // Add handling code for other status codes here
                }
            }
        }
    };

    xhr.send(data);
}

// 페이지 로딩 시 실행
window.addEventListener('load', function () {
    ajaxRequest('/getcsrf', 'GET', null, function (response) {
        let tokenInfo = JSON.parse(response);
        console.log('csrf token stored in Cookies:', tokenInfo.token);
    });

    checkAuth('accessToken');
});

function checkAuth(tokenType) {
    let token;
    if (tokenType === 'accessToken') {
        token = getCookie('accessToken');
    } else if (tokenType === 'refreshToken') {
        token = getCookie('refreshToken');
    } else {
        console.error('Invalid tokenType:', tokenType);
        return;
    }

    const csrfToken = getCookie('XSRF-TOKEN');

    ajaxRequest('/check-auth', 'POST', {
        token: token,
        csrfToken: csrfToken
    }, function (res) {
        if (tokenType === 'accessToken' && res === 'Refresh token required') {
            checkAuth('refreshToken');
            return false;
        }

        let headerElement = document.getElementById("header");

        if (tokenType === 'refreshToken') {
            let token = JSON.parse(res);

            setSessionCookie("accessToken", token.accessToken);
            setSessionCookie("refreshToken", token.refreshToken);

            headerElement.innerHTML = '<div>로그인 상태 입니다.</div>';
        }else{
            headerElement.innerHTML = res;
        }
    });
}

// Set a session cookie
function setSessionCookie(name, value) {
    document.cookie = name + "=" + value + ";path=/";
}

// Function to retrieve a cookie by name
function getCookie(name) {
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        if (cookie.startsWith(name + '=')) {
            return cookie.substring(name.length + 1);
        }
    }
    return null;
}