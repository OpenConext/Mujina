$(function() {
	// show the correct step
	$('#' + $('input#step').val()).collapse('show');

	// oauth1 and oauth2 require different input fields
	$('#version').change(function() {
		switch ($('#version').val()) {
		case '1.0a':
			$('#oauth10a').show();
			$('#oauth20').hide();
			$('#accessTokenRequestOptionsOauth2').hide();
			break;
		case '2.0':
			$('#oauth10a').hide();
			$('#oauth20').show();
			$('#accessTokenRequestOptionsOauth2').show();
			break;
		}
	});

	// oauth1 two-legged does not require request, accessTokens and
	// authentication
	$('#twoLegged').change(function() {
		$('#oauth10aInput').toggle(!this.checked);
	});

	// oauth2 implicit-grant only involves one request/ response and no secret
	$('#implicitGrant').click(function() {
		$('#oauth20Input').toggle(!this.checked);
		$('#secretInput').toggle(!this.checked);
	});

	// we are in step 3 of implicit grant
	if ($('#parseAnchorForAccesstoken').val() == 'true') {
		value = window.location.hash.replace("#", "");
		$('#parseAnchorForAccesstoken').val('');
		$.get('parseAnchor.shtml?' + value, function(data) {
			$('#responseInfo').html(data);
			$.each(data.split("&"), function(i, value) {
				param = value.split("=");
				if (param[0] == 'access_token') {
					$('#accessTokenValue').html(param[1]);
				}
			});
		});
	}
});
