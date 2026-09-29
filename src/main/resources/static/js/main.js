$(document).ready(function () {

    // ============================================
    // 1. XỬ LÝ ĐĂNG NHẬP (chỉ chạy trên trang login)
    // ============================================
    if (window.location.pathname === '/login' || window.location.pathname === '/') {
        $('#btnLogin').click(function () {
            var email = $('#email').val().trim();
            var password = $('#password').val().trim();

            if (email === '' || password === '') {
                $('#message').text('Vui lòng nhập đầy đủ email và password!');
                return;
            }

            $.ajax({
                type: 'POST',
                url: '/auth/login',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    email: email,
                    password: password
                }),
                success: function (data) {
                    localStorage.token = data.token;
                    window.location.href = '/user/profile';
                },
                error: function (e) {
                    console.error('Lỗi đăng nhập:', e);
                    $('#message').text('Đăng nhập thất bại! Kiểm tra lại email/password.');
                }
            });
        });
    }

    // ============================================
    // 2. HIỂN THỊ USER TRÊN TRANG PROFILE
    // ============================================
    if (window.location.pathname === '/user/profile') {
        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            beforeSend: function (xhr) {
                if (localStorage.token) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
                }
            },
            success: function (data) {
                console.log('User data:', data);
                $('#profile').html(data.fullName);
                if (data.images) {
                    document.getElementById('images').src = data.images;
                } else {
                    document.getElementById('images').src = '/images/default.jpg';
                }
            },
            error: function (e) {
                console.error('Lỗi lấy user:', e);
                alert('Bạn chưa đăng nhập hoặc token đã hết hạn!');
                window.location.href = '/login';
            }
        });
    }
});