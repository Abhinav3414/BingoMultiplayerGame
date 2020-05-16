var animateButton = function(e) {

	e.preventDefault;
	// reset animation
	e.target.classList.remove('animate');
	e.target.classList.add('animate');
	setTimeout(function() {
		e.target.classList.remove('animate');
	}, 700);
};

var bubblyButtons = document.getElementsByClassName("bubbly-button");

for (var i = 0; i < bubblyButtons.length; i++) {
	bubblyButtons[i].addEventListener('click', animateButton, false);
}


$('#blogLink').click(function(e) {
	e.preventDefault(); // will stop the link href to call the blog page
	setTimeout(function() {
		window.location.href = "gamesetup";
	}, 1200); // will call the function after 2 secs.
});

$('#blogLink2').click(function(e) {
	e.preventDefault(); // will stop the link href to call the blog page
	setTimeout(function() {
		window.location.href = "callNextRandomNumber";
	}, 700); // will call the function after 2 secs.
});
