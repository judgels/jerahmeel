require(["jquery"], function( __tes__ ) {
    $(document).ready(function() {
        $.ajax({
            url: lessonUpdateViewUrl,
            type: 'POST',
            data: {
                sessionLessonId: sessionLessonId
            },
            contentType: 'application/x-www-form-urlencoded',
        });
    });
});