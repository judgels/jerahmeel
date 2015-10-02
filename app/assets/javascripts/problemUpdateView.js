require(["jquery"], function( __tes__ ) {
    $(document).ready(function() {
        $.ajax({
            url: problemUpdateViewUrl,
            type: 'POST',
            data: {
                sessionProblemId: sessionProblemId
            },
            contentType: 'application/x-www-form-urlencoded',
        });
    });
});