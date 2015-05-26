require(["jquery"], function( __tes__ ) {
    $.ajax({
        url: sandalphonLessonTOTPURL,
        type: 'POST',
        data: body,
        contentType: 'text/plain',
        success: function (data) {
            $(".lesson_statement").html(data);
        }
    });
});