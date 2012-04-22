$(function() {
	// show the correct step
	$('#' + $('input#step').val()).collapse('show');
	
	// oauth1 and oauth2 require different input fields
	$('#version').change(function() {
		switch ($('#version').val()) {
		case '1.0a':
			$('#oauth10a').show();
			$('#oauth20').hide();
			break;
		case '2.0':
			$('#oauth10a').hide();
			$('#oauth20').show();
			break;
		}
	});
	
	// oauth1 two-legged does not require request, accessTokens and authentication
	$('#twoLegged').change(function() {
		$('#oauth10aInput').toggle(!this.checked);
	});
	
	// oauth2 implicit-grant only involves one request/ response
	$('#implicitGrant').click(function() {
		$('#oauth20Input').toggle(!this.checked);
	});
});
