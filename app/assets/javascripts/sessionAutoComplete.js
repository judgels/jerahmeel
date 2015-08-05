require(["jquery", "jquery-ui"], function( __tes__ ) {
    $(".session_autocomplete").autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: sessionAutoCompleteUrl,
                type: 'GET',
                data: {
                    term: request.term
                },
                dataType: "jsonp",
                success: function( data ) {
                    response( data );
                }
            });
        },
        minLength: 2
    });
});