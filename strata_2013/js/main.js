
// From docs.scala-lang.org
function styleCode()
	{
		if (typeof disableStyleCode != "undefined")
		{
				return;
		}
		var a = false;
		$("pre code").parent().each(function()
		{
				if (!$(this).hasClass("prettyprint"))
				{
						$(this).addClass("prettyprint lang-scala linenums");
						a = true
				}
		});
		if (a) { prettyPrint() }
}


function codeTabs() {
	var counter = 0;
	var langImages = {
		"scala": "img/scala-sm.png",
		"python": "img/python-sm.gif"
	};
	$("div.codetabs").each(function() {
		$(this).addClass("tab-content");

		// Insert the tab bar
		var tabBar = $('<ul class="nav nav-tabs" data-tabs="tabs"></ul>');
		$(this).before(tabBar);

		// Add each code sample to the tab bar:
		var codeSamples = $(this).children("div");
		codeSamples.each(function() {
			$(this).addClass("tab-pane");
			var lang = $(this).data("lang");
			var capitalizedLang = lang.substr(0, 1).toUpperCase() + lang.substr(1);
			var id = "tab_" + lang + "_" + counter;
			$(this).attr("id", id);
			if (langImages[lang]) {
				var buttonLabel =
					"<img src='" +langImages[lang] + "' alt='" + capitalizedLang + "' />";
			} else {
				var buttonLabel = capitalizedLang;
			}
			tabBar.append(
				'<li>' +
				'<a data-toggle="tab" href="#' + id + '">' +
				buttonLabel +
				'</a></li>'
			);
		});

		codeSamples.first().addClass("active");
		tabBar.children("li").first().addClass("active");
		counter++;
	});
}


function viewSolution() {
	var counter = 0
	$("div.solution").each(function() {
		var id = "solution_" + counter

		$(this).addClass("accordion-inner");
		$(this).wrap('<div class="accordion" />')
		$(this).wrap('<div class="accordion-group" />')
		$(this).wrap('<div id="' + id + '" class="accordion-body collapse" />')
		$(this).parent().before(
			 '<div class="accordion-heading">' +
				 '<a class="accordion-toggle" data-toggle="collapse" href="#' + id + '">' +
					 '<i class="icon-ok-sign"> </i>' +
					 'View Solution' +
				 '</a>' +
			 '</div>'
		);

		counter++;
	});
}


$(document).ready(function() {
	codeTabs();
	viewSolution();
	$('#toc').toc({exclude: ''});
	styleCode();
});
