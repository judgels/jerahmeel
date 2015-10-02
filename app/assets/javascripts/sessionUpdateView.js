require(["jquery"], function( __tes__ ) {
    $(document).ready(function() {
        $.ajax({
            url: sessionUpdateViewUrl,
            type: 'POST',
            data: {
                courseSessionId: courseSessionId
            },
            contentType: 'application/x-www-form-urlencoded',
        });
    });
});